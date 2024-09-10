package com.cleverhouse.spendless.budget.repositories

import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.repositories.BudgetRepository.BudgetFilters
import com.cleverhouse.spendless.budget.repositories.tables.BudgetTable
import slick.jdbc.PostgresProfile.api.*
import com.cleverhouse.spendless.utils.db.{FilteringRepository, IdRepository}
import slick.ast.BaseTypedType
import slick.lifted.{Query, TableQuery}

import java.util.UUID
import scala.concurrent.ExecutionContext

class BudgetRepositoryImpl(implicit e: ExecutionContext)
  extends IdRepository[Budget, UUID, BudgetTable]
    with FilteringRepository[Budget, BudgetTable, BudgetFilters]
    with BudgetRepository {

  override protected def getId: Budget => UUID = _.id

  override protected def idBaseTypedType: BaseTypedType[UUID] = implicitly[BaseTypedType[UUID]]

  override protected def ec: ExecutionContext = e

  override protected def prepareFilterQuery(tableQuery: Query[BudgetTable, Budget, Seq], filters: BudgetFilters): Query[BudgetTable, Budget, Seq] =
    table
      .filterOpt(filters.id)(_.id === _)
      .filterOpt(filters.ids)(_.id inSet _)
      .filterOpt(filters.name)(_.name === _)
      .sortBy(_.createdAt)

  override protected val table: TableQuery[BudgetTable] = TableQuery[BudgetTable]

}
