package com.cleverhouse.spendless.budget.services

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.repositories.BudgetRepository
import com.cleverhouse.spendless.budget.repositories.BudgetRepository.BudgetFilters
import com.cleverhouse.spendless.budget.services.BudgetFindService.BudgetFindResult
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.log.Logging
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import pl.iterators.sealedmonad.syntax.*

import java.util.UUID

class BudgetFindService(
  transactor: PostgresIOTransactor,
  budgetRepository: BudgetRepository
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[BudgetFindResult](ioRuntime)
    with Logging {

  def find(budgetId: UUID): IO[BudgetFindResult] =
    (for {
      budget <- fetchBudget(budgetId)
    } yield BudgetFindResult.Ok(budget)).run

  private def fetchBudget(budgetId: UUID): StepIO[Budget] =
    transactor
      .execute(budgetRepository.find(BudgetFilters(id = Some(budgetId))))
      .valueOr(BudgetFindResult.BudgetDoNotExist)
}

object BudgetFindService {
  enum BudgetFindResult:
    case Ok(budget: Budget)
    case BudgetDoNotExist
}
