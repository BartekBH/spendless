package com.cleverhouse.spendless.utils

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import org.reactivestreams.Publisher
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import slick.dbio._

import scala.concurrent.Future

class FakeTransactor(implicit runtime: IORuntime) extends PostgresIOTransactor(null)(runtime) {
  override def failed(t: => Throwable): DBIO[Nothing]                                     = DBIO.failed(t)
  override def from[T](g: => IO[T]): DBIO[T]                                              = DBIO.from(g.unsafeToFuture())
  override def execute[T](f: => DBIO[T]): IO[T]                                           = IO.fromFuture(IO(fakeDB(f)))
  override def executeTransactionally[T](f: => DBIO[T]): IO[T]                            = IO.fromFuture(IO(fakeDB(f)))
  private def fakeDB[R](action: DBIOAction[R, NoStream, Effect.All]): Future[R] =
    action match {
      case SuccessAction(v)           => Future.successful(v)
      case FailureAction(t)           => Future.failed(t)
      case FutureAction(f)            => f
      case FlatMapAction(base, f, ec) => fakeDB(base).flatMap(v => fakeDB(f(v)))(ec)
      case action @ AndThenAction(_) =>
        println(s"!!!!!!!!!! FakeTransactor: AndThenAction $action")
        ???
      case action @ SequenceAction(_) =>
        println(s"!!!!!!!!!! FakeTransactor: SequenceAction $action")
        ???
      case action @ CleanUpAction(_, _, _, _) =>
        println(s"!!!!!!!!!! FakeTransactor: CleanUpAction $action")
        ???
      case action @ FailedAction(_) =>
        println(s"!!!!!!!!!! FakeTransactor: FailedAction $action")
        ???
      case action @ AsTryAction(_) =>
        println(s"!!!!!!!!!! FakeTransactor: AsTryAction $action")
        ???
      case action @ NamedAction(_, _) =>
        println(s"!!!!!!!!!! FakeTransactor: NamedAction $action")
        ???
      case action =>
        println(s"!!!!!!!!!! FakeTransactor: Unknown action $action")
        ???
    }
}
