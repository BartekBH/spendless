package com.cleverhouse.spendless.auth.domain

import com.cleverhouse.spendless.user.domain.User

import java.util.UUID

case class AuthContext(id: UUID, email: String)

object AuthContext {
  def apply(user: User): AuthContext = AuthContext(user.id, user.email)
}
