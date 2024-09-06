package com.cleverhouse.spendless.user.domain

import java.time.Instant
import java.util.UUID

case class User(
  id: UUID,
  email: String,
  password: String,
  createdAt: Instant,
  modifiedAt: Instant)
