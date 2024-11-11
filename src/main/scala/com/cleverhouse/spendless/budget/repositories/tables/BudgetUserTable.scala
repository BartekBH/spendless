package com.cleverhouse.spendless.budget.repositories.tables

import com.cleverhouse.spendless.budget.domain.BudgetDomain.BudgetId
import com.cleverhouse.spendless.budget.domain.BudgetUser
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import com.cleverhouse.spendless.utils.db.IdTable
import slick.jdbc.PostgresProfile.api.*
import slick.lifted.ProvenShape

import java.util.UUID

class BudgetUserTable(tag: Tag) extends Table[BudgetUser](tag, "budget_user") {
  import BudgetTable._
  import com.cleverhouse.spendless.user.repositories.tables.UserTable._

  def budgetId = column[BudgetId]("budget_id")
  def userId = column[UserId]("user_id")

  def * = (budgetId, userId) <> (BudgetUser.apply.tupled, BudgetUser.unapply)
}