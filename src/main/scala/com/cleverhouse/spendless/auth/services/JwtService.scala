package com.cleverhouse.spendless.auth.services

import com.typesafe.config.{Config => TsConfig}
import io.circe._
import pdi.jwt.exceptions.JwtExpirationException
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import com.cleverhouse.spendless.auth.services.JwtService.DecodingResult

import java.time.ZonedDateTime
import scala.util.{Failure, Success}

class JwtService(config: JwtService.Config) {
  private val algorithm = JwtAlgorithm.HS256

  def decode(jwt: String): DecodingResult =
    Jwt.decode(jwt, config.secret, Seq(algorithm)) match {
      case Success(payload) =>
        parser.parse(payload.content) match {
          case Right(jsObject) => DecodingResult.Decoded(jsObject)
          case Left(t)         => DecodingResult.ParsingFailure(t)
        }
      case Failure(_: JwtExpirationException) => DecodingResult.Expired
      case Failure(t)                         => DecodingResult.InvalidToken(t)
    }

  def encode(
    payload: Json,
    issuer: Option[String] = None,
    subject: Option[String] = None,
    audience: Option[Set[String]] = None,
    expiresAt: Option[ZonedDateTime] = None,
    notBefore: Option[ZonedDateTime] = None,
    issuedAt: Option[ZonedDateTime] = None,
    jti: Option[String] = None
  ): String =
    Jwt.encode(
      JwtClaim(
        payload.toString(),
        issuer = issuer,
        subject = subject,
        audience = audience,
        expiration = expiresAt.map(_.toEpochSecond),
        notBefore = notBefore.map(_.toEpochSecond),
        issuedAt = issuedAt.map(_.toEpochSecond),
        jwtId = jti
      ),
      config.secret,
      algorithm
    )
}

object JwtService {
  sealed trait DecodingResult

  case class Config(secret: String)

  object Config {
    def apply(tsConfig: TsConfig): Config =
      Config(secret = tsConfig.getString("jwt.secret"))
  }

  object DecodingResult {
    case class Decoded(payload: Json) extends DecodingResult

    case class ParsingFailure(t: io.circe.ParsingFailure) extends DecodingResult

    case class InvalidToken(t: Throwable) extends DecodingResult

    case object Expired extends DecodingResult
  }

}
