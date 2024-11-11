package com.cleverhouse.spendless.auth.repositories.tables

import com.cleverhouse.spendless.auth.domain.AuthDomain.PasswordHash
import com.cleverhouse.spendless.auth.domain.UserPassword
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import com.cleverhouse.spendless.utils.db.IdTable
import slick.jdbc.PostgresProfile.api.*
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

class UserPasswordTable(tag: Tag) extends Table[UserPassword](tag, "user_password") with IdTable[UserPassword, UserId] {
  import UserPasswordTable._
  import com.cleverhouse.spendless.user.repositories.tables.UserTable._

  override def id: Rep[UserId] = userId

  def userId: Rep[UserId] = column[UserId]("user_id")
  def password: Rep[PasswordHash] = column[PasswordHash]("password")

  def * = (userId, password) <> (UserPassword.apply.tupled, UserPassword.unapply)
}

object UserPasswordTable {
  implicit val passwordHashColumnType: BaseColumnType[PasswordHash] = MappedColumnType.base[PasswordHash, String](_.unwrap, PasswordHash.apply)

}
