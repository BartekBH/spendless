package com.cleverhouse.spendless.utils

import org.scalatest.wordspec.AnyWordSpec
import pl.iterators.kebs.scalacheck.KebsScalacheckGenerators
import slick.jdbc.PostgresProfile.api.*

import java.util.Properties
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

trait PgRepositorySpec extends AnyWordSpec with KebsScalacheckGenerators {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def withDatabase[T](a: => DBIO[T]): T = Await.result(db.run(a), Duration.Inf)

  private val url      = sys.env.getOrElse("TEST_DB_URL", "jdbc:postgresql://localhost:5432/spendless")
  private val user     = sys.env.getOrElse("TEST_DB_USER", "postgres")
  private val password = sys.env.getOrElse("TEST_DB_PASSWORD", "postgres")

  private lazy val db = Database.forURL(
    url = url,
    user = user,
    password = password,
    driver = "org.postgresql.Driver",
    executor = AsyncExecutor(getClass.getCanonicalName, minThreads = 1, maxThreads = 1, queueSize = 20, maxConnections = 1),
    prop = new Properties() {}
  )

}
