package com.cleverhouse.spendless.user.repositories.tables

import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain._
import com.cleverhouse.spendless.utils.db.IdTable
import slick.jdbc.PostgresProfile.api.*
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

class UserTable(tag: Tag) extends Table[User](tag, "user") with IdTable[User, UserId] {
  import UserTable._

  def id = column[UserId]("id")
  def email = column[UserEmail]("email")
  def name = column[UserName]("name")
  def createdAt = column[UserCreatedAt]("created_at")
  def modifiedAt = column[UserModifiedAt]("modified_at")

  def * = (id, email, name, createdAt, modifiedAt) <> (User.apply.tupled, User.unapply)
}

object UserTable {
  implicit val idColumnType: BaseColumnType[UserId]                 = MappedColumnType.base[UserId, UUID](_.unwrap, UserId.apply)
  implicit val emailColumnType: BaseColumnType[UserEmail]           = MappedColumnType.base[UserEmail, String](_.unwrap, UserEmail.apply)
  implicit val nameColumnType: BaseColumnType[UserName]             = MappedColumnType.base[UserName, String](_.unwrap, UserName.apply)
  implicit val createdAtColumnType: BaseColumnType[UserCreatedAt]   = MappedColumnType.base[UserCreatedAt, Instant](_.unwrap, UserCreatedAt.apply)
  implicit val modifiedAtColumnType: BaseColumnType[UserModifiedAt] = MappedColumnType.base[UserModifiedAt, Instant](_.unwrap, UserModifiedAt.apply)
}



