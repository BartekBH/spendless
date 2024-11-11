package com.cleverhouse.spendless.auth.domain

import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain._

case class AuthContext(id: UserId, email: UserEmail)

object AuthContext {
  def apply(user: User): AuthContext = AuthContext(user.id, user.email)
}
