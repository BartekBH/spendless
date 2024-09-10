package com.cleverhouse.spendless.budget.services

import cats.effect.{Clock, IO}
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.repositories.BudgetRepository
import com.cleverhouse.spendless.budget.services.BudgetCreateService._
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.log.Logging
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import org.typelevel.log4cats.Logger
import pl.iterators.sealedmonad.syntax.*

import java.time.Instant
import java.util.UUID

class BudgetCreateService(
  transactor: PostgresIOTransactor,
  budgetRepository: BudgetRepository
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[BudgetCreateResult](ioRuntime)
    with Logging {

  def create(request: BudgetCreateRequest): IO[BudgetCreateResult] =
    (for {
      _        <- Logger[IO].info(s"Creating budget ${request.name}").seal
      toInsert <- createBudget(request)
      budget   <- insert(toInsert)
      _        <- Logger[IO].info(s"Creating budget result $budget").seal
    } yield BudgetCreateResult.Ok(budget)).run

  private def createBudget(request: BudgetCreateRequest): StepIO[Budget] =
    Clock[IO].realTime.map { nowMillis =>
      val now = Instant.ofEpochMilli(nowMillis.toMillis)
      Budget(
        id = UUID.randomUUID,
        name = request.name,
        createdAt = now,
        modifiedAt = now
      )
    }.seal

  private def insert(budget: Budget): StepIO[Budget] =
    transactor.execute(budgetRepository.insert(budget)).seal

}

object BudgetCreateService {
  final case class BudgetCreateRequest(name: String)

  enum BudgetCreateResult:
    case Ok(budget: Budget)
}