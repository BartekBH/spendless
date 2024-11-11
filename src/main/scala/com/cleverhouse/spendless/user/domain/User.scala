package com.cleverhouse.spendless.user.domain

import com.cleverhouse.spendless.user.domain.UserDomain._

case class User(
  id: UserId,
  email: UserEmail,
  name: UserName,
  createdAt: UserCreatedAt,
  modifiedAt: UserModifiedAt)
