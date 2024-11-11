package com.cleverhouse.spendless.budget.services

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.domain.BudgetDomain.BudgetId
import com.cleverhouse.spendless.budget.repositories.{BudgetRepository, BudgetUserRepository}
import com.cleverhouse.spendless.budget.repositories.BudgetRepository.BudgetFilters
import com.cleverhouse.spendless.budget.repositories.BudgetUserRepository.BudgetUserFilters
import com.cleverhouse.spendless.budget.services.BudgetListService.BudgetListResult
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.log.Logging
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import pl.iterators.sealedmonad.syntax.*

class BudgetListService(
  transactor: PostgresIOTransactor,
  budgetRepository: BudgetRepository,
  budgetUserRepository: BudgetUserRepository
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[BudgetListResult](ioRuntime)
    with Logging {

  def list(authContext: AuthContext): IO[BudgetListResult] =
    (for {
      createdIds  <- prepareCreatedBudgets(authContext)
      assignedIds <- prepareAssignedBudgets(authContext)
      budgets     <- fetchBudgets(createdIds, assignedIds)
    } yield BudgetListResult.Ok(budgets)).run

  private def prepareCreatedBudgets(authContext: AuthContext): StepIO[Seq[BudgetId]] =
    transactor.execute {
      budgetRepository.list(BudgetFilters(createdBy = Some(authContext.id)))
    }
      .map(_.map(_.id))
      .seal

  private def prepareAssignedBudgets(authContext: AuthContext): StepIO[Seq[BudgetId]] =
    transactor.execute {
      budgetUserRepository.list(BudgetUserFilters(userId = Some(authContext.id)))
    }
      .map(_.map(_.budgetId))
      .seal
    
  private def fetchBudgets(createdIds: Seq[BudgetId], assignedIds: Seq[BudgetId]): StepIO[Seq[Budget]] =
    transactor.execute(budgetRepository.list(BudgetFilters(ids = Some(createdIds ++ assignedIds)))).seal
}

object BudgetListService {
  enum BudgetListResult:
    case Ok(budgets: Seq[Budget])
}