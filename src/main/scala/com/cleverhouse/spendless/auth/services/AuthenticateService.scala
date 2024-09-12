package com.cleverhouse.spendless.auth.services

import cats.effect.IO
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.auth.services.AuthenticateService.AuthenticateTokenResult

trait AuthenticateService {
  def authenticateToken(jwt: String): IO[AuthenticateTokenResult]
}

object AuthenticateService {
  sealed trait AuthenticateTokenResult
  object AuthenticateTokenResult {
    case class Ok(authContext: AuthContext) extends AuthenticateTokenResult
    case object JwtExpired                  extends AuthenticateTokenResult
    case object InvalidJwt                  extends AuthenticateTokenResult
  }
}
