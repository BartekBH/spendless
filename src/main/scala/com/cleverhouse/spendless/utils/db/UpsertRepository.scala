package com.cleverhouse.spendless.utils.db

import slick.jdbc.PostgresProfile.api.*

trait UpsertRepository[T, TableType <: Table[T] with PrimaryKeyTable[T]] {

  def upsert(obj: T): DBIO[T] = table.insertOrUpdate(obj).andThen(DBIO.successful(obj))

  protected val table: TableQuery[TableType]
}
