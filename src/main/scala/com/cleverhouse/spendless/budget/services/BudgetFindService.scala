package com.cleverhouse.spendless.budget.services

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.domain.BudgetDomain.BudgetId
import com.cleverhouse.spendless.budget.repositories.{BudgetRepository, BudgetUserRepository}
import com.cleverhouse.spendless.budget.repositories.BudgetRepository.BudgetFilters
import com.cleverhouse.spendless.budget.repositories.BudgetUserRepository.BudgetUserFilters
import com.cleverhouse.spendless.budget.services.BudgetFindService.BudgetFindResult
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.log.Logging
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import pl.iterators.sealedmonad.Sealed
import pl.iterators.sealedmonad.syntax.*

class BudgetFindService(
  transactor: PostgresIOTransactor,
  budgetRepository: BudgetRepository,
  budgetUserRepository: BudgetUserRepository
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[BudgetFindResult](ioRuntime)
    with Logging {
  

  def find(authContext: AuthContext, budgetId: BudgetId): IO[BudgetFindResult] =
    (for {
      budget <- fetchBudget(budgetId)
      _      <- checkPermission(authContext, budget)
    } yield BudgetFindResult.Ok(budget)).run

  private def checkPermission(authContext: AuthContext, budget: Budget): StepIO[Unit] =
    if (authContext.id == budget.createdBy) Sealed.liftF(())
    else
      transactor
        .execute {
          budgetUserRepository
            .find(BudgetUserFilters(budgetId = Some(budget.id), userId = Some(authContext.id)))
        }
        .valueOr(BudgetFindResult.OperationNotPermitted)
        .map(_ => ())

  private def fetchBudget(budgetId: BudgetId): StepIO[Budget] =
    transactor
      .execute(budgetRepository.find(BudgetFilters(id = Some(budgetId))))
      .valueOr(BudgetFindResult.BudgetDoNotExist)
}

object BudgetFindService {
  enum BudgetFindResult:
    case Ok(budget: Budget)
    case BudgetDoNotExist
    case OperationNotPermitted
}
