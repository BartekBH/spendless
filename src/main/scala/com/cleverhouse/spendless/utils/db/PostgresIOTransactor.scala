package com.cleverhouse.spendless.utils.db

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import cats.~>
import slick.jdbc.PostgresProfile.api.*

class PostgresIOTransactor(db: Database)(implicit runtime: IORuntime) extends Transactor[DBIO, IO] {
  override def failed(t: => Throwable): DBIO[Nothing] = DBIO.failed(t)
  override def from[T](g: => IO[T]): DBIO[T]          = DBIO.from(g.unsafeToFuture())
  override def execute[T](f: => DBIO[T]): IO[T]                = IO.fromFuture(IO(db.run(f)))
  override def executeTransactionally[T](f: => DBIO[T]): IO[T] = IO.fromFuture(IO(db.run(f.transactionally)))
}

trait Transactor[F[_], G[_]] extends (F ~> G) {
  def failed(t: => Throwable): F[Nothing]
  def from[T](g: => G[T]): F[T]
  def execute[T](f: => F[T]): G[T]
  def executeTransactionally[T](f: => F[T]): G[T]
  override def apply[A](fa: F[A]): G[A] = executeTransactionally(fa)
}
