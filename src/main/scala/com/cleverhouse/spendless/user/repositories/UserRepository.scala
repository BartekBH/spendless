package com.cleverhouse.spendless.user.repositories

import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.repositories.tables.UserTable
import com.cleverhouse.spendless.utils.db.FilterParamType
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api.*

import java.util.UUID

trait UserRepository {
  import UserRepository._

  def insert(user: User): DBIO[User]
  def find(filter: UserFilters): DBIO[Option[User]]
  def list(filter: UserFilters): DBIO[Seq[User]]
  def update(user: User): DBIO[Option[User]]
  def purgeById(userId: UUID): DBIO[Int]
}

object UserRepository {
  case class UserFilters(
    id: Option[UUID] = None,
    ids: Option[Seq[UUID]] = None,
    email: Option[String] = None)
      extends FilterParamType
}
