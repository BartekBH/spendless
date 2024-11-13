package com.cleverhouse.spendless.utils

import cats.effect.IO
import cats.effect.testing.specs2.CatsEffect
import cats.effect.unsafe.IORuntime
import pl.iterators.kebs.scalacheck.KebsScalacheckGenerators
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import org.scalamock.scalatest.MockFactory
import org.scalatest.wordspec.AnyWordSpec
import org.specs2.execute.ResultLike
import slick.dbio.DBIO

import scala.concurrent.{ExecutionContext, Future}
import scala.language.experimental.macros


trait ServiceSpec extends AnyWordSpec with CatsEffect with KebsScalacheckGenerators with MockFactory {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit val ioRuntime: IORuntime = cats.effect.unsafe.implicits.global

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
  }

}