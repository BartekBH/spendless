package com.cleverhouse.spendless.budget.services

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.domain.BudgetDomain.BudgetId
import com.cleverhouse.spendless.budget.repositories.{BudgetRepository, BudgetUserRepository}
import com.cleverhouse.spendless.budget.repositories.BudgetRepository.BudgetFilters
import com.cleverhouse.spendless.budget.repositories.BudgetUserRepository.BudgetUserFilters
import com.cleverhouse.spendless.budget.services.BudgetDeleteService.BudgetDeleteResult
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.log.Logging
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import org.typelevel.log4cats.Logger
import pl.iterators.sealedmonad.Sealed
import pl.iterators.sealedmonad.syntax.*

class BudgetDeleteService(
  transactor: PostgresIOTransactor,
  budgetRepository: BudgetRepository,
  budgetUserRepository: BudgetUserRepository
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[BudgetDeleteResult](ioRuntime)
    with Logging {

  def delete(authContext: AuthContext, budgetId: BudgetId): IO[BudgetDeleteResult] =
    (for {
      budget <- findBudget(budgetId)
      _      <- checkPermission(authContext, budget)
      _      <- deleteBudget(budget)
      _      <- Logger[IO].info(s"Deleting budget ${budget.id}").seal
    } yield BudgetDeleteResult.Ok).run

  private def findBudget(budgetId: BudgetId): StepIO[Budget] =
    transactor
      .execute(budgetRepository.find(BudgetFilters(id = Some(budgetId))))
      .valueOr(BudgetDeleteResult.BudgetDoNotExist)

  private def checkPermission(authContext: AuthContext, budget: Budget): StepIO[Boolean] =
    IO.pure(authContext.id == budget.createdBy).ensure(identity, BudgetDeleteResult.OperationNotPermitted)

  private def deleteBudget(budget: Budget): StepIO[Unit] =
    (transactor.execute(budgetRepository.purgeById(budget.id)) *>
      transactor.execute(budgetUserRepository.deleteByBudget(budget)))
        .seal
}

object BudgetDeleteService {
  enum BudgetDeleteResult:
    case Ok
    case BudgetDoNotExist
    case OperationNotPermitted
}
