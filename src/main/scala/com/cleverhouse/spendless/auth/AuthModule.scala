package com.cleverhouse.spendless.auth

import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.auth.repositories.{UserPasswordRepository, UserPasswordRepositoryImpl}
import com.cleverhouse.spendless.auth.services.*
import com.cleverhouse.spendless.auth.routers.AuthRouter
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.softwaremill.macwire.wire
import com.typesafe.config.Config
import org.apache.pekko.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

class AuthModule(
  val transactor: PostgresIOTransactor,
  val config: Config,
  val userRepository: UserRepository,
  implicit val executor: ExecutionContext,
  implicit val runtime: IORuntime) {

  private lazy val jwtServiceConfig: JwtService.Config = JwtService.Config(config)
  private lazy val jwtService: JwtService              = wire[JwtService]
  lazy val authService: AuthenticateService = wire[JwtAuthenticateService]
  protected lazy val userPasswordRepository: UserPasswordRepository = wire[UserPasswordRepositoryImpl]
  private lazy val passwordService: PasswordService = wire[PasswordService]
  private lazy val passwordSetServiceConfig: PasswordSetService.Config = PasswordSetService.Config(config)
  lazy val passwordSetService: PasswordSetService                      = wire[PasswordSetService]
  private lazy val loginByPasswordServiceConfig: LoginByPasswordService.Config = LoginByPasswordService.Config(config)
  protected lazy val loginByPasswordService: LoginByPasswordService            = wire[LoginByPasswordService]

  lazy val authRouter: AuthRouter = wire[AuthRouter]

  def route: Route = authRouter.route
  def route(authContext: AuthContext): Route = authRouter.route(authContext: AuthContext)

}
