package com.cleverhouse.spendless.utils.db

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import cats.~>
import org.reactivestreams.Publisher
import slick.jdbc.PostgresProfile.api.*
import slick.jdbc.TransactionIsolation

import scala.util.Try

class PostgresIOTransactor(db: Database)(implicit runtime: IORuntime) extends Transactor[DBIO, IO] {
  override def failed(t: => Throwable): DBIO[Nothing] = DBIO.failed(t)
  override def from[T](g: => IO[T]): DBIO[T]          = DBIO.from(g.unsafeToFuture())
  override def stream[T](f: => DBIO[T]): Either[StreamingNotSupported.type, Publisher[T]] =
    Try(db.stream(f.asInstanceOf[DBIOAction[T, Streaming[T], Effect.All]])).toEither.left
      .map(_ => StreamingNotSupported)
  override def execute[T](f: => DBIO[T]): IO[T]                = IO.fromFuture(IO(db.run(f)))
  override def executePinned[T](f: => DBIO[T]): IO[T]          = IO.fromFuture(IO(db.run(f.withPinnedSession)))
  override def executeTransactionally[T](f: => DBIO[T]): IO[T] = IO.fromFuture(IO(db.run(f.transactionally)))
  override def executeTransactionally[T](level: IsolationLevel)(f: => DBIO[T]): IO[T] =
    IO.fromFuture(IO(db.run(f.transactionally.withTransactionIsolation(isolationLevelToTransactionIsolation(level)))))

  private def isolationLevelToTransactionIsolation(isolationLevel: IsolationLevel): TransactionIsolation =
    isolationLevel match {
      case IsolationLevel.ReadCommitted   => TransactionIsolation.ReadCommitted
      case IsolationLevel.ReadUncommitted => TransactionIsolation.ReadUncommitted
      case IsolationLevel.RepeatableRead  => TransactionIsolation.RepeatableRead
      case IsolationLevel.Serializable    => TransactionIsolation.Serializable
    }
}

sealed trait IsolationLevel
object IsolationLevel {
  case object ReadUncommitted extends IsolationLevel
  case object ReadCommitted   extends IsolationLevel
  case object RepeatableRead  extends IsolationLevel
  case object Serializable    extends IsolationLevel
}

case object StreamingNotSupported

trait Transactor[F[_], G[_]] extends (F ~> G) {
  def failed(t: => Throwable): F[Nothing]
  def from[T](g: => G[T]): F[T]

  def stream[T](f: => F[T]): Either[StreamingNotSupported.type, Publisher[T]]

  def execute[T](f: => F[T]): G[T]
  def executePinned[T](f: => F[T]): G[T]
  def executeTransactionally[T](f: => F[T]): G[T]
  def executeTransactionally[T](level: IsolationLevel)(f: => F[T]): G[T]

  override def apply[A](fa: F[A]): G[A] = executeTransactionally(fa)
}
