package com.cleverhouse.spendless.budget.services

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.repositories.BudgetRepository
import com.cleverhouse.spendless.budget.repositories.BudgetRepository.BudgetFilters
import com.cleverhouse.spendless.budget.services.BudgetDeleteService.BudgetDeleteResult
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.log.Logging
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import org.typelevel.log4cats.Logger
import pl.iterators.sealedmonad.syntax.*

import java.util.UUID

class BudgetDeleteService(
  transactor: PostgresIOTransactor,
  budgetRepository: BudgetRepository
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[BudgetDeleteResult](ioRuntime)
    with Logging {

  def delete(budgetId: UUID): IO[BudgetDeleteResult] =
    (for {
      budget <- findBudget(budgetId)
      _      <- deleteBudget(budget.id)
      _      <- Logger[IO].info(s"Deleting budget ${budget.id}").seal
    } yield BudgetDeleteResult.Ok).run

  private def findBudget(budgetId: UUID): StepIO[Budget] =
    transactor
      .execute(budgetRepository.find(BudgetFilters(id = Some(budgetId))))
      .valueOr(BudgetDeleteResult.BudgetDoNotExist)

  private def deleteBudget(budgetId: UUID): StepIO[Int] =
    transactor.execute(budgetRepository.purgeById(budgetId)).seal
}

object BudgetDeleteService {
  enum BudgetDeleteResult:
    case Ok
    case BudgetDoNotExist
}
