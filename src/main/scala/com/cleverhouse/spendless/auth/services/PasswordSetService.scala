package com.cleverhouse.spendless.auth.services

import cats.Monad
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthDomain._
import com.typesafe.config.Config as TsConfig
import com.cleverhouse.spendless.auth.domain.{AuthContext, UserPassword}
import com.cleverhouse.spendless.auth.repositories.UserPasswordRepository
import com.cleverhouse.spendless.auth.services.PasswordSetService.{PasswordSetRequest, PasswordSetResponse}
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import pl.iterators.sealedmonad.syntax.*

class PasswordSetService(
  transactor: PostgresIOTransactor,
  config: PasswordSetService.Config,
  userPasswordRepository: UserPasswordRepository,
  passwordService: PasswordService
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[PasswordSetResponse](ioRuntime) {

  def passwordSet(authContext: AuthContext, request: PasswordSetRequest): IO[PasswordSetResponse] =
    (for {
      _ <- checkPassword(request)
      userPassword = preparePassword(authContext, request)
      _ <- insertPassword(userPassword)
    } yield PasswordSetResponse.Ok).run


  private def checkPassword(request: PasswordSetRequest): StepIO[Boolean] =
    Monad[IO]
      .pure(request.newPassword.unwrap.length >= config.minimumPasswordLength)
      .ensure(identity, PasswordSetResponse.InvalidPassword)

  private def preparePassword(authContext: AuthContext, request: PasswordSetRequest): UserPassword = {
    val hashed = passwordService.encrypt(request.newPassword)
    UserPassword(authContext.id, password = hashed)
  }

  private def insertPassword(userPassword: UserPassword): StepIO[UserPassword] =
    transactor.execute(userPasswordRepository.upsert(userPassword)).seal

}

object PasswordSetService {
  case class Config(minimumPasswordLength: Int)

  final case class PasswordSetRequest(newPassword: PasswordPlain)

  final case class LoginResponseData(token: Jwt)

  object Config {
    def apply(tsConfig: TsConfig): Config =
      Config(minimumPasswordLength = tsConfig.getInt("user.minimumPasswordLength"))
  }

  enum PasswordSetResponse:
    case Ok
    case InvalidPassword
  
}
