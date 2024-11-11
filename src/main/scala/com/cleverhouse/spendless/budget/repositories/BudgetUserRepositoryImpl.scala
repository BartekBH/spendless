package com.cleverhouse.spendless.budget.repositories

import com.cleverhouse.spendless.budget.domain.BudgetDomain.BudgetId
import com.cleverhouse.spendless.budget.domain.{Budget, BudgetUser}
import com.cleverhouse.spendless.budget.repositories.BudgetUserRepository.BudgetUserFilters
import com.cleverhouse.spendless.budget.repositories.tables.{BudgetTable, BudgetUserTable}
import slick.jdbc.PostgresProfile.api.*
import com.cleverhouse.spendless.utils.db.{FilteringRepository, IdRepository}
import slick.ast.BaseTypedType
import slick.jdbc.PostgresProfile
import slick.lifted.{Query, TableQuery}

import java.util.UUID
import scala.concurrent.ExecutionContext

class BudgetUserRepositoryImpl(implicit e: ExecutionContext)
  extends FilteringRepository[BudgetUser, BudgetUserTable, BudgetUserFilters]
    with BudgetUserRepository {
  import BudgetTable._
  import com.cleverhouse.spendless.user.repositories.tables.UserTable._

  override def insert(budgetUser: BudgetUser): DBIO[BudgetUser] = (table += budgetUser).map(_ => budgetUser)

  override def delete(budgetUser: BudgetUser): DBIO[Unit] =
    table
      .filter(t => t.userId === budgetUser.userId && t.budgetId === budgetUser.budgetId)
      .delete
      .map(_ => ())
  
  override def deleteByBudget(budget: Budget): DBIO[Unit] =
    table
      .filter(t => t.budgetId === budget.id)
      .delete
      .map(_ => ())

  override protected def prepareFilterQuery(tableQuery: Query[BudgetUserTable, BudgetUser, Seq], filters: BudgetUserFilters): Query[BudgetUserTable, BudgetUser, Seq] =
    table
      .filterOpt(filters.budgetId)(_.budgetId === _)
      .filterOpt(filters.userId)(_.userId === _)

  override protected val table: TableQuery[BudgetUserTable] = TableQuery[BudgetUserTable]
}
