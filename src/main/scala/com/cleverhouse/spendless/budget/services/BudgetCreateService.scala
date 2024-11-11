package com.cleverhouse.spendless.budget.services

import cats.effect.{Clock, IO}
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.domain.BudgetDomain.*
import com.cleverhouse.spendless.budget.repositories.BudgetRepository
import com.cleverhouse.spendless.budget.services.BudgetCreateService.*
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
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

  def create(authContext: AuthContext, request: BudgetCreateRequest): IO[BudgetCreateResult] =
    (for {
      _        <- Logger[IO].info(s"Creating budget ${request.name}").seal
      toInsert <- createBudget(authContext.id, request)
      budget   <- insert(toInsert)
      _        <- Logger[IO].info(s"Creating budget result $budget").seal
    } yield BudgetCreateResult.Ok(budget)).run

  private def createBudget(userId: UserId, request: BudgetCreateRequest): StepIO[Budget] =
    Clock[IO].realTime.map { nowMillis =>
      val now = Instant.ofEpochMilli(nowMillis.toMillis)
      Budget(
        id = BudgetId(UUID.randomUUID),
        name = BudgetName(request.name),
        createdAt = BudgetCreatedAt(now),
        createdBy = userId,
        modifiedAt = BudgetModifiedAt(now),
        modifiedBy = userId
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