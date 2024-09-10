package com.cleverhouse.spendless.budget.services

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.repositories.BudgetRepository
import com.cleverhouse.spendless.budget.repositories.BudgetRepository.BudgetFilters
import com.cleverhouse.spendless.budget.services.BudgetListService.BudgetListResult
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.log.Logging
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import pl.iterators.sealedmonad.syntax.*

class BudgetListService(
  transactor: PostgresIOTransactor,
  budgetRepository: BudgetRepository
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[BudgetListResult](ioRuntime)
    with Logging {

  def list(): IO[BudgetListResult] =
    (for {
      budgets <- fetchBudgets()
    } yield BudgetListResult.Ok(budgets)).run

  private def fetchBudgets(): StepIO[Seq[Budget]] =
    transactor.execute(budgetRepository.list(BudgetFilters())).seal
}

object BudgetListService {
  enum BudgetListResult:
    case Ok(budgets: Seq[Budget])
}