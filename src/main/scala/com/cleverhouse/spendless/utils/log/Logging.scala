package com.cleverhouse.spendless.utils.log

import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import cats.effect.Sync

trait Logging {
  implicit def unsafeLogger[F[_] : Sync]: SelfAwareStructuredLogger[F] = Slf4jLogger.getLogger[F]
}