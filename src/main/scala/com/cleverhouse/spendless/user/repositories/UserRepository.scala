package com.cleverhouse.spendless.user.repositories

import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.{UserEmail, UserId}
import com.cleverhouse.spendless.utils.db.FilterParamType
import slick.jdbc.PostgresProfile.api.*

import java.util.UUID

trait UserRepository {
  import UserRepository._

  def insert(user: User): DBIO[User]
  def find(filter: UserFilters): DBIO[Option[User]]
  def list(filter: UserFilters): DBIO[Seq[User]]
  def update(user: User): DBIO[Option[User]]
  def purgeById(userId: UserId): DBIO[Int]
}

object UserRepository {
  case class UserFilters(
    id: Option[UserId] = None,
    ids: Option[Seq[UserId]] = None,
    email: Option[UserEmail] = None)
      extends FilterParamType
}
