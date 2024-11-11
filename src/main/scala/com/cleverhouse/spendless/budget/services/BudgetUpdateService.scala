package com.cleverhouse.spendless.budget.services

import cats.effect.{Clock, IO}
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.domain.BudgetDomain.*
import com.cleverhouse.spendless.budget.repositories.BudgetRepository
import com.cleverhouse.spendless.budget.repositories.BudgetRepository.BudgetFilters
import com.cleverhouse.spendless.budget.services.BudgetUpdateService.{BudgetUpdateRequest, BudgetUpdateResult}
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.log.Logging
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import org.typelevel.log4cats.Logger
import pl.iterators.sealedmonad.syntax.*

class BudgetUpdateService(
  transactor: PostgresIOTransactor,
  budgetRepository: BudgetRepository
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[BudgetUpdateResult](ioRuntime)
    with Logging {
  
  def update(authContext: AuthContext, budgetId: BudgetId, request: BudgetUpdateRequest): IO[BudgetUpdateResult] =
    (for {
      budget         <- findBudget(budgetId)
      _              <- checkPermission(authContext, budget)
      updatedBudget  <- updatedBudget(budget, request)
      result         <- updateBudget(updatedBudget)
      _              <- Logger[IO].info(s"Updating budget $budgetId").seal
    } yield BudgetUpdateResult.Ok(result)).run

  private def findBudget(budgetId: BudgetId): StepIO[Budget] =
    transactor
      .execute(budgetRepository.find(BudgetFilters(id = Some(budgetId))))
      .valueOr(BudgetUpdateResult.BudgetDoNotExist)

  private def checkPermission(authContext: AuthContext, budget: Budget): StepIO[Boolean] =
    IO.pure(authContext.id == budget.createdBy).ensure(identity, BudgetUpdateResult.OperationNotPermitted)

  private def updatedBudget(budget: Budget, request: BudgetUpdateRequest): StepIO[Budget] =
    Clock[IO].realTimeInstant.seal.map { now =>
      budget.copy(
        name = request.name.getOrElse(budget.name),
        modifiedAt = BudgetModifiedAt(now))
    }

  private def updateBudget(budget: Budget): StepIO[Budget] =
    transactor.execute(budgetRepository.update(budget)).valueOr(BudgetUpdateResult.BudgetDoNotExist)

}

object BudgetUpdateService {
  final case class BudgetUpdateRequest(name: Option[BudgetName])

  enum BudgetUpdateResult:
    case Ok(budget: Budget)
    case BudgetDoNotExist
    case OperationNotPermitted
}
