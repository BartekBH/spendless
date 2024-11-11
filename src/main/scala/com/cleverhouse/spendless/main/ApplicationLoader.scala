package com.cleverhouse.spendless.main

import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.AuthModule
import com.cleverhouse.spendless.budget.BudgetModule
import com.cleverhouse.spendless.user.UserModule
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.http.ApplicationRouteProvider
import com.typesafe.config.Config
import org.apache.pekko.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

class ApplicationLoader(
  val config: Config,
  implicit val executor: ExecutionContext,
  implicit val runtime: IORuntime,
  val transactor: PostgresIOTransactor) 
    extends ApplicationRouteProvider
    with AuthModule 
    with BudgetModule
    with UserModule {

  lazy val routes: Route =
    authenticateOrRejectWithChallenge(userAuthenticator) { auth =>
      route(auth) ~ route
    }

}
