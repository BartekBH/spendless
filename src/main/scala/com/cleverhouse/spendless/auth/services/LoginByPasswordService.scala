package com.cleverhouse.spendless.auth.services

import cats.Monad
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.typesafe.config.{Config => TsConfig}
import io.circe.syntax._
import com.cleverhouse.spendless.auth.domain.{AuthContext, UserPassword}
import com.cleverhouse.spendless.auth.repositories.UserPasswordRepository
import com.cleverhouse.spendless.auth.services.LoginByPasswordService.{LoginRequest, LoginResponse, LoginResponseData}
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.json.JsonProtocol
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import pl.iterators.sealedmonad.syntax._
import slick.dbio.DBIO

import java.time.ZonedDateTime
import scala.compat.java8.DurationConverters._
import scala.concurrent.duration.FiniteDuration

class LoginByPasswordService(
  transactor: PostgresIOTransactor,
  config: LoginByPasswordService.Config,
  userRepository: UserRepository,
  userPasswordRepository: UserPasswordRepository,
  passwordService: PasswordService,
  jwtService: JwtService
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[LoginResponse](ioRuntime)
    with JsonProtocol {

  def login(loginRequest: LoginRequest): IO[LoginResponse] =
    transactor.execute {
      (for {
        user     <- findUser(loginRequest.email)
        password <- findPassword(user)
        _        <- checkPassword(password, loginRequest)
        jwt = issueJwt(user)
      } yield LoginResponse.Ok(LoginResponseData(jwt))).run
    }

  private def findUser(email: String): StepDBIO[User] =
    userRepository
      .find(UserFilters(email = Some(email)))
      .valueOr(LoginResponse.UserOrPasswordNotFound)

  private def findPassword(user: User): StepDBIO[UserPassword] =
    userPasswordRepository
      .find(user.id)
      .valueOr(LoginResponse.UserOrPasswordNotFound)

  private def checkPassword(userPassword: UserPassword, loginRequest: LoginRequest): StepDBIO[Boolean] =
    Monad[DBIO]
      .pure(passwordService.check(loginRequest.password, userPassword.password))
      .ensure(identity, LoginResponse.InvalidCredentials)

  private def issueJwt(user: User): String = {
    val authContext = AuthContext(user)
    jwtService.encode(authContext.asJson, expiresAt = Some(ZonedDateTime.now().plus(config.validFor.toJava)))
  }

}

object LoginByPasswordService {
  sealed trait LoginResponse

  case class Config(validFor: FiniteDuration)

  final case class LoginRequest(email: String, password: String)

  final case class LoginResponseData(token: String)

  object Config {
    def apply(tsConfig: TsConfig): Config =
      Config(validFor = tsConfig.getDuration("jwt.valid-for").toScala)
  }

  object LoginResponse {
    final case class Ok(data: LoginResponseData) extends LoginResponse
    final case object UserOrPasswordNotFound     extends LoginResponse
    final case object InvalidCredentials         extends LoginResponse

  }

}