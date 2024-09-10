package com.cleverhouse.spendless.budget.repositories.tables

import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.utils.db.IdTable
import slick.jdbc.PostgresProfile.api.*
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

class BudgetTable(tag: Tag) extends Table[Budget](tag, "budget") with IdTable[Budget, UUID] {

  def id = column[UUID]("id")
  def name = column[String]("name")
  def createdAt = column[Instant]("created_at")
  def modifiedAt = column[Instant]("modified_at")

  def * = (id, name, createdAt, modifiedAt) <> (Budget.apply.tupled, Budget.unapply)
}
