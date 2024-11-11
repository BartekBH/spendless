package com.cleverhouse.spendless.auth.services

import cats.Monad
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthDomain._
import com.typesafe.config.Config as TsConfig
import io.circe.syntax.*
import com.cleverhouse.spendless.auth.domain.{AuthContext, UserPassword}
import com.cleverhouse.spendless.auth.repositories.UserPasswordRepository
import com.cleverhouse.spendless.auth.services.LoginByPasswordService.{LoginRequest, LoginResponse, LoginResponseData}
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.UserEmail
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.json.JsonProtocol
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import pl.iterators.sealedmonad.syntax.*

import java.time.ZonedDateTime
import scala.compat.java8.DurationConverters.*
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
      (for {
        user     <- findUser(loginRequest.email)
        password <- findPassword(user)
        _        <- checkPassword(password, loginRequest)
        jwt = issueJwt(user)
      } yield LoginResponse.Ok(LoginResponseData(jwt))).run

  private def findUser(email: UserEmail): StepIO[User] =
    transactor.execute(userRepository.find(UserFilters(email = Some(email))))
      .valueOr(LoginResponse.UserOrPasswordNotFound)

  private def findPassword(user: User): StepIO[UserPassword] =
    transactor.execute(userPasswordRepository.find(user.id))
      .valueOr(LoginResponse.UserOrPasswordNotFound)

  private def checkPassword(userPassword: UserPassword, loginRequest: LoginRequest): StepIO[Boolean] =
    Monad[IO]
      .pure(passwordService.check(loginRequest.password, userPassword.password))
      .ensure(identity, LoginResponse.InvalidCredentials)

  private def issueJwt(user: User): Jwt = {
    val authContext = AuthContext(user)
    Jwt(jwtService.encode(authContext.asJson, expiresAt = Some(ZonedDateTime.now().plus(config.validFor.toJava))))
  }

}

object LoginByPasswordService {
  case class Config(validFor: FiniteDuration)

  final case class LoginRequest(email: UserEmail, password: PasswordPlain)

  final case class LoginResponseData(token: Jwt)

  object Config {
    def apply(tsConfig: TsConfig): Config =
      Config(validFor = tsConfig.getDuration("jwt.valid-for").toScala)
  }

  enum LoginResponse:
    case Ok(data: LoginResponseData)
    case UserOrPasswordNotFound
    case InvalidCredentials
  
}