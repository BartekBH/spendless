package com.cleverhouse.spendless.main

import cats.effect.unsafe.IORuntime
import cats.effect.{IO, IOApp, Resource}
import com.cleverhouse.spendless.budget.BudgetModule
import com.cleverhouse.spendless.user.UserModule
import com.cleverhouse.spendless.auth.AuthModule
import com.typesafe.config.ConfigFactory
import com.cleverhouse.spendless.utils.ce.*
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import slick.jdbc.PostgresProfile.api.*
import org.apache.pekko.http.scaladsl.server.Directives.concat

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.*

object Main extends IOApp.Simple {
  override def run: IO[Unit] = (for {
    config <- Resource.eval(IO.delay(ConfigFactory.load()))
    actorSystem <- startActorSystemUntyped(
      "spendless-actor-system",
      config = None,
      useIOExecutionContext = true,
      timeoutAwaitCatsEffect = 10.seconds,
      timeoutAwaitAkkaTermination = 10.seconds
    )
    dbCtx <- Resource.make(IO.delay {
      val db = Database.forConfig("db", config)
      (db, new PostgresIOTransactor(db)(runtime))
    }) (dbCtx => IO.blocking(dbCtx._1.close()))
  } yield (config, actorSystem, dbCtx._2)).use { case (config, actorSystem, dbCtx) =>
    implicit val ec: ExecutionContext = runtime.compute
    implicit val system: ActorSystem = actorSystem
    implicit val rt: IORuntime = runtime

    val userModule: UserModule = UserModule(dbCtx, ec, rt)
    val authModule: AuthModule = AuthModule(dbCtx, config, userModule.userRepository, ec, rt)
    val budgetModule: BudgetModule = BudgetModule(dbCtx, ec, rt)

    val routes = concat(
      userModule.route,
      authModule.route,
      budgetModule.route
    )

    val host = config.getString("http.host")
    val port = config.getInt("http.port")

    IO.fromFuture {
      IO(
        Http()
          .newServerAt(host, port)
          .bindFlow(routes)
          .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))
      ) <* IO(println(s"Server started at http://$host:$port"))
    } *> IO.never
  }
}
