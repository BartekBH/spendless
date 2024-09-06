package com.cleverhouse.spendless.utils.http

import org.apache.pekko.http.scaladsl.marshalling.ToResponseMarshallable
import org.apache.pekko.http.scaladsl.server.Directives.{complete => pekkoComplete}
import org.apache.pekko.http.scaladsl.server.StandardRoute
import cats.effect.IO
import cats.effect.unsafe.IORuntime

trait CompleteDirectives {
  def complete[T](io: => IO[T])(map: T => ToResponseMarshallable)(implicit runtime: IORuntime): StandardRoute =
    pekkoComplete(io.map(map).unsafeToFuture())
}
object CompleteDirectives extends CompleteDirectives

