package com.cleverhouse.spendless.auth.repositories

import com.cleverhouse.spendless.auth.domain.UserPassword
import slick.dbio.DBIO

import java.util.UUID

trait UserPasswordRepository {
  def upsert(userPassword: UserPassword): DBIO[UserPassword]
  def find(userId: UUID): DBIO[Option[UserPassword]]
}
