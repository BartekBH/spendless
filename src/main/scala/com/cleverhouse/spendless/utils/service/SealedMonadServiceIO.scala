package com.cleverhouse.spendless.utils.service

import cats.effect.unsafe.IORuntime
import cats.effect.{IO, Sync}
import pl.iterators.sealedmonad.Sealed
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

abstract class SealedMonadServiceIO[ADT](val ioRuntime: IORuntime) {
  protected type Step[T] = Sealed[IO, T, ADT]
}

abstract class SealedMonadServiceIODBIO[ADT](val ioRuntime: IORuntime) {
  protected type StepDBIO[T] = Sealed[DBIO, T, ADT]
  protected type StepIO[T]   = Sealed[IO, T, ADT]

  implicit val ec: ExecutionContext = ioRuntime.compute
  implicit val dbioSync: Sync[DBIO] = implicitly[Sync[DBIO]]
}
