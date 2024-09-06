package com.cleverhouse.spendless.user.repositories.tables

import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.utils.db.IdTable
import slick.jdbc.PostgresProfile.api.*
import slick.lifted.ProvenShape

import java.time.Instant
import java.util.UUID

class UserTable(tag: Tag) extends Table[User](tag, "users") with IdTable[User, UUID] {

  def id = column[UUID]("id")
  def email = column[String]("email")
  def password = column[String]("password")
  def createdAt = column[Instant]("created_at")
  def modifiedAt = column[Instant]("modified_at")

  def * = (id, email, password, createdAt, modifiedAt) <> (User.apply.tupled, User.unapply)
}



