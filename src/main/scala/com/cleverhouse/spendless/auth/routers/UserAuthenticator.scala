package com.cleverhouse.spendless.auth.routers

import org.apache.pekko.http.scaladsl.model.headers.{HttpChallenge, HttpCredentials}
import org.apache.pekko.http.scaladsl.server.directives.SecurityDirectives.AuthenticationResult
import org.apache.pekko.http.scaladsl.server.directives.{AuthenticationResult, SecurityDirectives}
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.auth.services.JwtAuthenticateService
import com.cleverhouse.spendless.auth.services.JwtAuthenticateService.JwtAuthenticateTokenResult

import scala.concurrent.Future

trait UserAuthenticator extends (Option[HttpCredentials] => Future[AuthenticationResult[AuthContext]])

class AuthServiceUserAuthenticator(authService: JwtAuthenticateService)(implicit runtime: IORuntime) extends UserAuthenticator {
  override def apply(credentialsOpt: Option[HttpCredentials]): Future[AuthenticationResult[AuthContext]] =
    credentialsOpt match {
      case Some(credentials) if credentials.scheme() == "Bearer" =>
        authService
          .authenticateToken(credentials.token())
          .map {
            case JwtAuthenticateTokenResult.Ok(auth) => AuthenticationResult.success(auth)
            case _                                => Challenge
          }
          .recover {
            case t => Challenge
          }
          .unsafeToFuture()
      case _ => ChallengeFuture
    }

  private val Challenge: SecurityDirectives.AuthenticationResult[Nothing] =
    AuthenticationResult.failWithChallenge(HttpChallenge(scheme = "Bearer", realm = "spendless"))

  private val ChallengeFuture: Future[SecurityDirectives.AuthenticationResult[Nothing]] =
    Future.successful(Challenge)
}