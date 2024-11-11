package com.cleverhouse.spendless.auth.services

import cats.effect.IO
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.auth.services.JwtService.DecodingResult
import com.cleverhouse.spendless.utils.json.JsonProtocol
import pdi.jwt.Jwt

import scala.util.Try

class JwtAuthenticateService(jwtService: JwtService) extends JsonProtocol {
  import JwtAuthenticateService._
  
  def authenticateToken(jwt: String): IO[JwtAuthenticateService.JwtAuthenticateTokenResult] =
    IO.pure {
      Try {
        jwtService.decode(jwt) match {
          case DecodingResult.Decoded(decoded) =>
            decoded.as[AuthContext] match {
              case Right(context) => JwtAuthenticateTokenResult.Ok(context)
              case Left(_)        => JwtAuthenticateTokenResult.InvalidJwt
            }
          case DecodingResult.Expired           => JwtAuthenticateTokenResult.JwtExpired
          case DecodingResult.InvalidToken(_)   => JwtAuthenticateTokenResult.InvalidJwt
          case DecodingResult.ParsingFailure(_) => JwtAuthenticateTokenResult.InvalidJwt
        }
      }.toOption.getOrElse(JwtAuthenticateTokenResult.InvalidJwt)
    }
}

object JwtAuthenticateService {
  enum JwtAuthenticateTokenResult:
    case Ok(authContext: AuthContext)
    case JwtExpired
    case InvalidJwt
}
