package com.cleverhouse.spendless.budget.repositories

import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.utils.db.FilterParamType
import slick.jdbc.PostgresProfile.api.*

import java.util.UUID

trait BudgetRepository {
  import BudgetRepository._

  def insert(user: Budget): DBIO[Budget]
  def find(filter: BudgetFilters): DBIO[Option[Budget]]
  def list(filter: BudgetFilters): DBIO[Seq[Budget]]
  def update(user: Budget): DBIO[Option[Budget]]
  def purgeById(userId: UUID): DBIO[Int]
}

object BudgetRepository {
  case class BudgetFilters(
    id: Option[UUID] = None,
    ids: Option[Seq[UUID]] = None,
    name: Option[String] = None)
      extends FilterParamType
}
