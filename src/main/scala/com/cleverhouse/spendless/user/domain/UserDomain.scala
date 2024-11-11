package com.cleverhouse.spendless.user.domain

import pl.iterators.kebs.opaque.*

import java.time.Instant
import java.util.UUID

object UserDomain extends Domain 

trait Domain {
  opaque type UserId = UUID
  object UserId extends Opaque[UserId, UUID]

  opaque type UserEmail = String
  object UserEmail extends Opaque[UserEmail, String]

  opaque type UserName = String
  object UserName extends Opaque[UserName, String]
  
  opaque type UserCreatedAt = Instant
  object UserCreatedAt extends Opaque[UserCreatedAt, Instant]

  opaque type UserModifiedAt = Instant
  object UserModifiedAt extends Opaque[UserModifiedAt, Instant]
}
