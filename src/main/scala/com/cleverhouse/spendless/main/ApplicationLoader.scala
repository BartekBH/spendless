package com.cleverhouse.spendless.main

import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.AuthModule
import com.cleverhouse.spendless.budget.BudgetModule
import com.cleverhouse.spendless.user.UserModule
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.http.{ApplicationRouteProvider, Handlers}
import com.typesafe.config.Config
import org.apache.pekko.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

class ApplicationLoader(
  val config: Config,
  implicit val executor: ExecutionContext,
  implicit val runtime: IORuntime,
  val transactor: PostgresIOTransactor) 
    extends ApplicationRouteProvider 
      with Handlers
      with AuthModule 
      with BudgetModule
      with UserModule {

  private lazy val apiRoutes: Route =
      authenticateOrRejectWithChallenge(userAuthenticator) { auth =>
        route(auth) ~ route
      }

  private lazy val handleErrors =
    handleExceptions(exceptionHandler)

  lazy val routes: Route =
    handleErrors(apiRoutes)

}
