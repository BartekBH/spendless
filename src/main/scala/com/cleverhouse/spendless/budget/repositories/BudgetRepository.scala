package com.cleverhouse.spendless.budget.repositories

import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.domain.BudgetDomain.*
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import com.cleverhouse.spendless.utils.db.FilterParamType
import slick.jdbc.PostgresProfile.api.*

import java.util.UUID

trait BudgetRepository {
  import BudgetRepository._

  def insert(user: Budget): DBIO[Budget]
  def find(filter: BudgetFilters): DBIO[Option[Budget]]
  def list(filter: BudgetFilters): DBIO[Seq[Budget]]
  def update(user: Budget): DBIO[Option[Budget]]
  def purgeById(budgetId: BudgetId): DBIO[Int]
}

object BudgetRepository {
  case class BudgetFilters(
    id: Option[BudgetId] = None,
    ids: Option[Seq[BudgetId]] = None,
    name: Option[BudgetName] = None,
    createdBy: Option[UserId] = None)
      extends FilterParamType
}
