package com.cleverhouse.spendless.budget

import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.budget.repositories.{BudgetRepository, BudgetRepositoryImpl, BudgetUserRepository, BudgetUserRepositoryImpl}
import com.cleverhouse.spendless.budget.services.*
import com.cleverhouse.spendless.budget.routers.BudgetRouter
import com.cleverhouse.spendless.user.repositories.{UserRepository, UserRepositoryImpl}
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.http.{AuthRouteProvider, RouteProvider}
import com.softwaremill.macwire.wire
import org.apache.pekko.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

trait BudgetModule extends AuthRouteProvider with RouteProvider {
  implicit val executor: ExecutionContext
  implicit val runtime: IORuntime
  val transactor: PostgresIOTransactor

  lazy val userRepository: UserRepository

  lazy val budgetRepository: BudgetRepository = wire[BudgetRepositoryImpl]
  lazy val budgetUserRepository: BudgetUserRepository = wire[BudgetUserRepositoryImpl]

  lazy val budgetCreateService: BudgetCreateService = wire[BudgetCreateService]
  lazy val budgetDeleteService: BudgetDeleteService = wire[BudgetDeleteService]
  lazy val budgetFindService: BudgetFindService     = wire[BudgetFindService]
  lazy val budgetListService: BudgetListService     = wire[BudgetListService]
  lazy val budgetUpdateService: BudgetUpdateService = wire[BudgetUpdateService]
  lazy val budgetAssignService: BudgetAssignService = wire[BudgetAssignService]
  lazy val budgetDeassignService: BudgetDeassignService = wire[BudgetDeassignService]

  lazy val budgetRouter: BudgetRouter = wire[BudgetRouter]

  abstract override def route(authContext: AuthContext): Route = super.route(authContext) ~ budgetRouter.routes(authContext)

}