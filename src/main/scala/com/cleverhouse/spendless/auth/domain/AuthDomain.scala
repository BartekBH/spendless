package com.cleverhouse.spendless.auth.domain

import pl.iterators.kebs.opaque.*

object AuthDomain extends Domain

trait Domain {
  opaque type PasswordPlain = String
  object PasswordPlain extends Opaque[PasswordPlain, String]

  opaque type PasswordHash = String
  object PasswordHash extends Opaque[PasswordHash, String]

//  opaque type Jwt = String
//  object Jwt extends Opaque[Jwt, String]

/*
  problem with generating opaque type in tests - to change in future
 */
  case class Jwt(value: String)
}
