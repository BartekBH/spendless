package com.cleverhouse.spendless.auth

import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.auth.repositories.{UserPasswordRepository, UserPasswordRepositoryImpl}
import com.cleverhouse.spendless.auth.services.*
import com.cleverhouse.spendless.auth.routers.{AuthRouter, AuthServiceUserAuthenticator, UserAuthenticator}
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.http.{AuthRouteProvider, RouteProvider}
import com.softwaremill.macwire.wire
import com.typesafe.config.Config
import org.apache.pekko.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

trait AuthModule extends AuthRouteProvider with RouteProvider {
  implicit val executor: ExecutionContext
  implicit val runtime: IORuntime
  
  val transactor: PostgresIOTransactor
  val config: Config
  lazy val userRepository: UserRepository

  private lazy val jwtServiceConfig: JwtService.Config                         = JwtService.Config(config)
  private lazy val jwtService: JwtService                                      = wire[JwtService]
  lazy val authService: JwtAuthenticateService                                 = wire[JwtAuthenticateService]
  lazy val userAuthenticator: UserAuthenticator                                = wire[AuthServiceUserAuthenticator]
  protected lazy val userPasswordRepository: UserPasswordRepository            = wire[UserPasswordRepositoryImpl]
  private lazy val passwordService: PasswordService                            = wire[PasswordService]
  private lazy val passwordSetServiceConfig: PasswordSetService.Config         = PasswordSetService.Config(config)
  lazy val passwordSetService: PasswordSetService                              = wire[PasswordSetService]
  private lazy val loginByPasswordServiceConfig: LoginByPasswordService.Config = LoginByPasswordService.Config(config)
  protected lazy val loginByPasswordService: LoginByPasswordService            = wire[LoginByPasswordService]

  lazy val authRouter: AuthRouter = wire[AuthRouter]

  abstract override def route: Route = super.route ~ authRouter.routes
  abstract override def route(authContext: AuthContext): Route = super.route ~ authRouter.routes(authContext: AuthContext)

}
