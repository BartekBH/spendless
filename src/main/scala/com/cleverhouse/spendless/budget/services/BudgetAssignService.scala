package com.cleverhouse.spendless.budget.services

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.budget.domain.{Budget, BudgetUser}
import com.cleverhouse.spendless.budget.domain.BudgetDomain.BudgetId
import com.cleverhouse.spendless.budget.repositories.{BudgetRepository, BudgetUserRepository}
import com.cleverhouse.spendless.budget.repositories.BudgetRepository.BudgetFilters
import com.cleverhouse.spendless.budget.repositories.BudgetUserRepository.BudgetUserFilters
import com.cleverhouse.spendless.budget.services.BudgetAssignService.{BudgetAssignRequest, BudgetAssignResult}
import com.cleverhouse.spendless.budget.services.BudgetFindService.BudgetFindResult
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.{UserEmail, UserId}
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.log.Logging
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import org.typelevel.log4cats.Logger
import pl.iterators.sealedmonad.Sealed
import pl.iterators.sealedmonad.syntax.*

class BudgetAssignService(
  transactor: PostgresIOTransactor,
  budgetRepository: BudgetRepository,
  budgetUserRepository: BudgetUserRepository,
  userRepository: UserRepository
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[BudgetAssignResult](ioRuntime)
    with Logging {

  def assign(authContext: AuthContext, budgetId: BudgetId, request: BudgetAssignRequest): IO[BudgetAssignResult] =
    (for {
      _      <- Logger[IO].info(s"BudgetAssignService: assigning user: ${request.email} to budget: $budgetId").seal
      budget <- findBudget(budgetId)
      _      <- checkPermission(authContext, budget)
      user   <- findUser(request)
      _      <- checkBudgetUserExists(budget.id, user.id)
      -      <- assignUser(budget.id, user.id)
      _      <- Logger[IO].info(s"BudgetAssignService: successfully assigned user: ${request.email} to budget: $budgetId").seal
    } yield BudgetAssignResult.Assigned).run

  private def findBudget(budgetId: BudgetId): StepIO[Budget] =
    transactor
      .execute(budgetRepository.find(BudgetFilters(id = Some(budgetId))))
      .valueOr(BudgetAssignResult.BudgetDoNotExist)

  private def checkPermission(authContext: AuthContext, budget: Budget): StepIO[Boolean] =
    IO.pure(authContext.id == budget.createdBy).ensure(identity, BudgetAssignResult.OperationNotPermitted)

  private def findUser(request: BudgetAssignRequest): StepIO[User] =
    transactor
      .execute(userRepository.find(UserFilters(email = Some(request.email))))
      .valueOr(BudgetAssignResult.UserNotFound)
    
  private def checkBudgetUserExists(budgetId: BudgetId, userId: UserId): StepIO[Unit] =
    transactor
      .execute(budgetUserRepository.find(BudgetUserFilters(budgetId = Some(budgetId), userId = Some(userId))))
      .ensureF(
        _.isEmpty,
        Logger[IO].error(s"BudgetAssignService: user $userId is already assigned to budget $budgetId").as(BudgetAssignResult.UserAlreadyAssigned)
      )
      .map(_ => ())
    
  private def assignUser(budgetId: BudgetId, userId: UserId): StepIO[Unit] =
    transactor
      .execute(budgetUserRepository.insert(BudgetUser(budgetId, userId)))
      .map(_ => ())
      .seal
  
}

object BudgetAssignService {
  final case class BudgetAssignRequest(email: UserEmail)

  enum BudgetAssignResult:
    case Assigned
    case BudgetDoNotExist
    case OperationNotPermitted
    case UserNotFound
    case UserAlreadyAssigned
}