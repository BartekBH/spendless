package com.cleverhouse.spendless.budget.services

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.budget.domain.{Budget, BudgetUser}
import com.cleverhouse.spendless.budget.domain.BudgetDomain.BudgetId
import com.cleverhouse.spendless.budget.repositories.{BudgetRepository, BudgetUserRepository}
import com.cleverhouse.spendless.budget.repositories.BudgetRepository.BudgetFilters
import com.cleverhouse.spendless.budget.repositories.BudgetUserRepository.BudgetUserFilters
import com.cleverhouse.spendless.budget.services.BudgetDeassignService.{BudgetDeassignRequest, BudgetDeassignResult}
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

class BudgetDeassignService(
                           transactor: PostgresIOTransactor,
                           budgetRepository: BudgetRepository,
                           budgetUserRepository: BudgetUserRepository,
                           userRepository: UserRepository
                         )(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[BudgetDeassignResult](ioRuntime)
    with Logging {

  def assign(authContext: AuthContext, budgetId: BudgetId, request: BudgetDeassignRequest): IO[BudgetDeassignResult] =
    (for {
      _      <- Logger[IO].info(s"BudgetDeassignService: deassigning user: ${request.email} from budget: $budgetId").seal
      budget <- findBudget(budgetId)
      _      <- checkPermission(authContext, budget)
      user   <- findUser(request)
      _      <- checkBudgetUserExists(budget.id, user.id)
      -      <- deassign(budget.id, user.id)
      _      <- Logger[IO].info(s"BudgetDeassignService: successfully deassigned user: ${request.email} from budget: $budgetId").seal
    } yield BudgetDeassignResult.Deassigned).run

  private def findBudget(budgetId: BudgetId): StepIO[Budget] =
    transactor
      .execute(budgetRepository.find(BudgetFilters(id = Some(budgetId))))
      .valueOr(BudgetDeassignResult.BudgetDoNotExist)

  private def checkPermission(authContext: AuthContext, budget: Budget): StepIO[Boolean] =
    IO.pure(authContext.id == budget.createdBy).ensure(identity, BudgetDeassignResult.OperationNotPermitted)

  private def findUser(request: BudgetDeassignRequest): StepIO[User] =
    transactor
      .execute(userRepository.find(UserFilters(email = Some(request.email))))
      .valueOr(BudgetDeassignResult.UserNotFound)

  private def checkBudgetUserExists(budgetId: BudgetId, userId: UserId): StepIO[Unit] =
    transactor
      .execute(budgetUserRepository.find(BudgetUserFilters(budgetId = Some(budgetId), userId = Some(userId))))
      .ensureF(
        _.isEmpty,
        Logger[IO].error(s"BudgetDessignService: user $userId is not assigned to budget $budgetId").as(BudgetDeassignResult.UserNotAssigned)
      )
      .map(_ => ())

  private def deassign(budgetId: BudgetId, userId: UserId): StepIO[Unit] =
    transactor
      .execute(budgetUserRepository.delete(BudgetUser(budgetId, userId)))
      .seal

}

object BudgetDeassignService {
  final case class BudgetDeassignRequest(email: UserEmail)

  enum BudgetDeassignResult:
    case Deassigned
    case BudgetDoNotExist
    case OperationNotPermitted
    case UserNotFound
    case UserNotAssigned
}