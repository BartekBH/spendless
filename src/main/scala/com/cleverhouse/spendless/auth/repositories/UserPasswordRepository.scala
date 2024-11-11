package com.cleverhouse.spendless.auth.repositories

import com.cleverhouse.spendless.auth.domain.UserPassword
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import slick.dbio.DBIO

import java.util.UUID

trait UserPasswordRepository {
  def upsert(userPassword: UserPassword): DBIO[UserPassword]
  def find(userId: UserId): DBIO[Option[UserPassword]]
}
