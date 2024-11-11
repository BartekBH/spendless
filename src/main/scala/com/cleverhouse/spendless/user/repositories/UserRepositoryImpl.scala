package com.cleverhouse.spendless.user.repositories

import cats.implicits.catsSyntaxEq
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.user.repositories.tables.UserTable
import slick.jdbc.PostgresProfile.api.*
import com.cleverhouse.spendless.utils.db.{FilteringRepository, IdRepository}
import slick.ast.BaseTypedType
import slick.lifted.{Query, TableQuery}

import scala.concurrent.ExecutionContext

class UserRepositoryImpl(implicit e: ExecutionContext) 
  extends IdRepository[User, UserId, UserTable]
  with FilteringRepository[User, UserTable, UserFilters]
  with UserRepository {
  import tables.UserTable._
  
  override protected def getId: User => UserId = _.id

  override protected def idBaseTypedType: BaseTypedType[UserId] = implicitly[BaseTypedType[UserId]]

  override protected def ec: ExecutionContext = e

  override protected def prepareFilterQuery(tableQuery: Query[UserTable, User, Seq], filters: UserFilters): Query[UserTable, User, Seq] =
    table
      .filterOpt(filters.id)(_.id === _)
      .filterOpt(filters.ids)(_.id inSet _)
      .filterOpt(filters.email)(_.email === _)
      .sortBy(_.createdAt)

  override protected val table: TableQuery[UserTable] = TableQuery[UserTable]

}
