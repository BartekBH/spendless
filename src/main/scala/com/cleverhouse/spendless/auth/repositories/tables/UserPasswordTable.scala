package com.cleverhouse.spendless.auth.repositories.tables

import com.cleverhouse.spendless.auth.domain.UserPassword
import com.cleverhouse.spendless.utils.db.IdTable
import slick.jdbc.PostgresProfile.api.*
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

class UserPasswordTable(tag: Tag) extends Table[UserPassword](tag, "user_password") with IdTable[UserPassword, UUID] {

  override def id: Rep[UUID] = userId

  def userId: Rep[UUID] = column[UUID]("user_id")
  def password: Rep[String] = column[String]("password")

  def * = (userId, password) <> (UserPassword.apply.tupled, UserPassword.unapply)
}
