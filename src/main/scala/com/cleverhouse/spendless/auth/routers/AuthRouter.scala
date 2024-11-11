package com.cleverhouse.spendless.auth.routers

import org.apache.pekko.http.scaladsl.model.StatusCodes.*
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.server.Directives.*
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.auth.services.LoginByPasswordService.{LoginRequest, LoginResponse}
import com.cleverhouse.spendless.auth.services.PasswordSetService.{PasswordSetRequest, PasswordSetResponse}
import com.cleverhouse.spendless.auth.services.*
import com.cleverhouse.spendless.utils.json.JsonProtocol
import com.cleverhouse.spendless.utils.http.CompleteDirectives.complete

class AuthRouter(loginByPasswordService: LoginByPasswordService, passwordSetService: PasswordSetService)(implicit runtime: IORuntime) 
  extends JsonProtocol {
  import AuthRouter._

  def routes: Route =
    pathPrefix(authPath) {
      (post & path(loginPath) & entity(as[LoginRequest]) & pathEndOrSingleSlash) { request =>
        complete(loginByPasswordService.login(request)) {
          case LoginResponse.Ok(data)               => OK -> data
          case LoginResponse.InvalidCredentials     => Unauthorized
          case LoginResponse.UserOrPasswordNotFound => Unauthorized
        }
      }
    }

  def routes(authContext: AuthContext): Route =
    pathPrefix(authPath) {
      (patch & path(passwordPath) & entity(as[PasswordSetRequest]) & pathEndOrSingleSlash) { request =>
        complete(passwordSetService.passwordSet(authContext, request)) {
          case PasswordSetResponse.Ok              => OK
          case PasswordSetResponse.InvalidPassword => BadRequest
        }
      }
    }
}

object AuthRouter {
  val authPath          = "auth"
  val loginPath         = "login"
  val passwordPath      = "password"
}
