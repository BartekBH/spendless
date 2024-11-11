package com.cleverhouse.spendless.budget.repositories

import com.cleverhouse.spendless.budget.domain.BudgetDomain.BudgetId
import com.cleverhouse.spendless.budget.domain.{Budget, BudgetUser}
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import com.cleverhouse.spendless.utils.db.FilterParamType
import slick.jdbc.PostgresProfile.api.*

trait BudgetUserRepository {
  import BudgetUserRepository._

  def insert(user: BudgetUser): DBIO[BudgetUser]
  def find(filter: BudgetUserFilters): DBIO[Option[BudgetUser]]
  def list(filter: BudgetUserFilters): DBIO[Seq[BudgetUser]]
  def delete(budgetUser: BudgetUser): DBIO[Unit]
  def deleteByBudget(budget: Budget): DBIO[Unit]
}

object BudgetUserRepository {
  case class BudgetUserFilters(
    budgetId: Option[BudgetId] = None,
    userId: Option[UserId] = None)
      extends FilterParamType
}
