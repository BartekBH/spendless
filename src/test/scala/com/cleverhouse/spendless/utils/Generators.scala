package com.cleverhouse.spendless.utils

import org.scalacheck.{Arbitrary, Gen}
import pl.iterators.kebs.scalacheck.{KebsArbitraryPredefs, KebsScalacheckGenerators}

import java.time.Instant

trait Generators extends KebsScalacheckGenerators with KebsArbitraryPredefs {

  implicit override val arbInstant: Arbitrary[Instant] =
    Arbitrary {
      Gen.chooseNum(Int.MinValue, Int.MaxValue).map { value =>
        Instant.ofEpochMilli(System.currentTimeMillis()).plusMillis(value.toLong)
      }
    }

}
