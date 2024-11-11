package com.cleverhouse.spendless.budget.repositories.tables

import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.domain.BudgetDomain._
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import com.cleverhouse.spendless.utils.db.IdTable
import slick.jdbc.PostgresProfile.api.*
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

class BudgetTable(tag: Tag) extends Table[Budget](tag, "budget") with IdTable[Budget, BudgetId] {
  import BudgetTable._
  import com.cleverhouse.spendless.user.repositories.tables.UserTable._
  
  def id = column[BudgetId]("id")
  def name = column[BudgetName]("name")
  def createdAt = column[BudgetCreatedAt]("created_at")
  def createdBy = column[UserId]("created_by")
  def modifiedAt = column[BudgetModifiedAt]("modified_at")
  def modifiedBy = column[UserId]("modified_by")

  def * = (id, name, createdAt, createdBy, modifiedAt, modifiedBy) <> (Budget.apply.tupled, Budget.unapply)
}

object BudgetTable {
  implicit val idColumnType: BaseColumnType[BudgetId]                 = MappedColumnType.base[BudgetId, UUID](_.unwrap, BudgetId.apply)
  implicit val nameColumnType: BaseColumnType[BudgetName]             = MappedColumnType.base[BudgetName, String](_.unwrap, BudgetName.apply)
  implicit val createdAtColumnType: BaseColumnType[BudgetCreatedAt]   = MappedColumnType.base[BudgetCreatedAt, Instant](_.unwrap, BudgetCreatedAt.apply)
  implicit val modifiedAtColumnType: BaseColumnType[BudgetModifiedAt] = MappedColumnType.base[BudgetModifiedAt, Instant](_.unwrap, BudgetModifiedAt.apply)
}
