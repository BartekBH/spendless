package com.cleverhouse.spendless.user.repositories

import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.user.repositories.tables.UserTable
import slick.jdbc.PostgresProfile.api.*
import com.cleverhouse.spendless.utils.db.{FilteringRepository, IdRepository}
import slick.ast.BaseTypedType
import slick.lifted.{Query, TableQuery}

import java.util.UUID
import scala.concurrent.ExecutionContext

class UserRepositoryImpl(implicit e: ExecutionContext) 
  extends IdRepository[User, UUID, UserTable]
  with FilteringRepository[User, UserTable, UserFilters]
  with UserRepository {

  override protected def getId: User => UUID = _.id

  override protected def idBaseTypedType: BaseTypedType[UUID] = implicitly[BaseTypedType[UUID]]

  override protected def ec: ExecutionContext = e

  override protected def prepareFilterQuery(tableQuery: Query[UserTable, User, Seq], filters: UserFilters): Query[UserTable, User, Seq] =
    table
      .filterOpt(filters.id)(_.id === _)
      .filterOpt(filters.ids)(_.id inSet _)
      .filterOpt(filters.email)(_.email.toLowerCase === _.toLowerCase)
      .sortBy(_.createdAt)

  override protected val table: TableQuery[UserTable] = TableQuery[UserTable]

}
