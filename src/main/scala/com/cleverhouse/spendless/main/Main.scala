package com.cleverhouse.spendless.main

import cats.effect.{IO, IOApp, Resource}
import com.typesafe.config.ConfigFactory
import com.cleverhouse.spendless.utils.ce.*
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import slick.jdbc.PostgresProfile.api.*

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
    loader = new ApplicationLoader(config, runtime.compute, runtime, dbCtx._2)
  } yield (actorSystem, runtime.compute, loader)).use { case (actorSystem, executor, loader) =>
    implicit val ec: ExecutionContext = executor
    implicit val system: ActorSystem = actorSystem

    val host = loader.config.getString("http.host")
    val port = loader.config.getInt("http.port")

    IO.fromFuture {
      IO(
        Http()
          .newServerAt(host, port)
          .bindFlow(loader.routes)
          .map(_.addToCoordinatedShutdown(hardTerminationDeadline = 10.seconds))
      ) <* IO(println(s"Server started at http://$host:$port"))
    } *> IO.never
  }
}
