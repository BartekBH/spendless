package com.cleverhouse.spendless.utils.db

import slick.lifted.{PrimaryKey, Rep}
import slick.jdbc.PostgresProfile.api.*

trait IdTable[T, Id] extends PrimaryKeyTable[T] { this: Table[T] =>
  def id: Rep[Id]

  override val primaryKey: PrimaryKey = primaryKey(id)
}
