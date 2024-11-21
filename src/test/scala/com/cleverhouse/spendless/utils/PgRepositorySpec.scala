package com.cleverhouse.spendless.utils

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pl.iterators.kebs.scalacheck.KebsScalacheckGenerators
import slick.jdbc.PostgresProfile.api.*

import java.util.Properties
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

trait PgRepositorySpec extends AnyWordSpec with KebsScalacheckGenerators with Matchers {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def withDatabase[T](a: => DBIO[T]): T =
    Await.result(
      db.run {
        a.map[T](r => throw AbortTx(r)).transactionally.asTry.map[T] {
          case Failure(AbortTx(value)) => value.asInstanceOf[T]

  private val url      = sys.env.getOrElse("TEST_DB_URL", "jdbc:postgresql://localhost:5432/spendless-test")
  private val user     = sys.env.getOrElse("TEST_DB_USER", "postgres-test")
  private val password = sys.env.getOrElse("TEST_DB_PASSWORD", "postgres-test")

  private lazy val db = Database.forURL(
    url = url,
    user = user,
    password = password,
    driver = "org.postgresql.Driver",
    executor = AsyncExecutor(getClass.getCanonicalName, minThreads = 1, maxThreads = 1, queueSize = 20, maxConnections = 1),
    prop = new Properties() {}
  )

}
