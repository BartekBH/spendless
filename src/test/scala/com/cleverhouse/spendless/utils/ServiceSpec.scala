package com.cleverhouse.spendless.utils

import cats.effect.IO
import cats.effect.testing.specs2.CatsEffect
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import pl.iterators.kebs.scalacheck.{Generator, KebsScalacheckGenerators}
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import slick.dbio.DBIO

import scala.concurrent.{ExecutionContext, Future}
import scala.language.experimental.macros


trait ServiceSpec extends AnyWordSpec with CatsEffect with KebsScalacheckGenerators with MockFactory with Matchers {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val ioRuntime: IORuntime = cats.effect.unsafe.implicits.global

  lazy val authGenerator: Generator[AuthContext] = allGenerators[AuthContext].normal

  trait BaseScope {

    implicit class AsDBIO[T](t: T) {
      def asDBIO: DBIO[T] = slick.dbio.DBIO.successful(t)
    }
    implicit class AsFuture[T](t: T) {
      def asFuture: Future[T] = Future.successful(t)
    }
    implicit class AsIO[T](t: T) {
      def asIO: IO[T] = cats.effect.IO.pure(t)
    }

    val transactor: PostgresIOTransactor = new FakeTransactor()

    val authUser: AuthContext  = authGenerator.generate
  }

}