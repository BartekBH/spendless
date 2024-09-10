package com.cleverhouse.spendless.budget

import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.budget.repositories.{BudgetRepository, BudgetRepositoryImpl}
import com.cleverhouse.spendless.budget.services.*
import com.cleverhouse.spendless.budget.routers.BudgetRouter
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.softwaremill.macwire.wire
import org.apache.pekko.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

class BudgetModule(
  val transactor: PostgresIOTransactor,
  implicit val executor: ExecutionContext,
  implicit val runtime: IORuntime) {

  lazy val budgetRepository: BudgetRepository = wire[BudgetRepositoryImpl]

  lazy val budgetCreateService: BudgetCreateService = wire[BudgetCreateService]
  lazy val budgetDeleteService: BudgetDeleteService = wire[BudgetDeleteService]
  lazy val budgetFindService: BudgetFindService = wire[BudgetFindService]
  lazy val budgetListService: BudgetListService = wire[BudgetListService]
  lazy val budgetUpdateService: BudgetUpdateService = wire[BudgetUpdateService]

  lazy val budgetRouter: BudgetRouter = wire[BudgetRouter]

  def route: Route = budgetRouter.route

}