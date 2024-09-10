package com.cleverhouse.spendless.budget.services

import cats.effect.{Clock, IO}
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.repositories.BudgetRepository
import com.cleverhouse.spendless.budget.repositories.BudgetRepository.BudgetFilters
import com.cleverhouse.spendless.budget.services.BudgetUpdateService.{BudgetUpdateRequest, BudgetUpdateResult}
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.log.Logging
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import org.typelevel.log4cats.Logger
import pl.iterators.sealedmonad.syntax.*

import java.util.UUID

class BudgetUpdateService(
  transactor: PostgresIOTransactor,
  budgetRepository: BudgetRepository
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[BudgetUpdateResult](ioRuntime)
    with Logging {

  def update(budgetId: UUID, request: BudgetUpdateRequest): IO[BudgetUpdateResult] =
    (for {
      budget              <- findBudget(budgetId)
      updatedBudget       <- updatedBudget(budget, request)
      result            <- updateBudget(updatedBudget)
      _                 <- Logger[IO].info(s"Updating budget $budgetId").seal
    } yield BudgetUpdateResult.Ok(result)).run

  private def findBudget(budgetId: UUID): StepIO[Budget] =
    transactor
      .execute(budgetRepository.find(BudgetFilters(id = Some(budgetId))))
      .valueOr(BudgetUpdateResult.BudgetDoNotExist)

  private def updatedBudget(budget: Budget, request: BudgetUpdateRequest): StepIO[Budget] =
    Clock[IO].realTimeInstant.seal.map { now =>
      budget.copy(
        name = request.name.getOrElse(budget.name),
        modifiedAt = now)
    }

  private def updateBudget(budget: Budget): StepIO[Budget] =
    transactor.execute(budgetRepository.update(budget)).valueOr(BudgetUpdateResult.BudgetDoNotExist)

}

object BudgetUpdateService {
  final case class BudgetUpdateRequest(name: Option[String])

  enum BudgetUpdateResult:
    case Ok(budget: Budget)
    case BudgetDoNotExist
}
