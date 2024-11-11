package com.cleverhouse.spendless.utils.db

import slick.lifted.{PrimaryKey, Rep}
import slick.jdbc.PostgresProfile.api.*

trait PrimaryKeyTable[T] { this: Table[T] =>
  def primaryKey(columns: Rep[?]*): PrimaryKey = PrimaryKey("primary_key", columns.map(_.toNode).toIndexedSeq)
  val primaryKey: PrimaryKey
}
