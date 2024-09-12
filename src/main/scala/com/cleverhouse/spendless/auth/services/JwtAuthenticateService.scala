package com.cleverhouse.spendless.auth.services

import cats.effect.IO
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.auth.services.AuthenticateService.AuthenticateTokenResult
import com.cleverhouse.spendless.auth.services.JwtService.DecodingResult
import com.cleverhouse.spendless.utils.json.JsonProtocol

import scala.util.Try

class JwtAuthenticateService(jwtService: JwtService) extends AuthenticateService with JsonProtocol {

  override def authenticateToken(jwt: String): IO[AuthenticateService.AuthenticateTokenResult] =
    IO.pure {
      Try {
        jwtService.decode(jwt) match {
          case DecodingResult.Decoded(decoded) =>
            decoded.as[AuthContext] match {
              case Right(context) => AuthenticateTokenResult.Ok(context)
              case Left(_)        => AuthenticateTokenResult.InvalidJwt
            }
          case DecodingResult.Expired           => AuthenticateTokenResult.JwtExpired
          case DecodingResult.InvalidToken(_)   => AuthenticateTokenResult.InvalidJwt
          case DecodingResult.ParsingFailure(_) => AuthenticateTokenResult.InvalidJwt
        }
      }.toOption.getOrElse(AuthenticateTokenResult.InvalidJwt)
    }
}
