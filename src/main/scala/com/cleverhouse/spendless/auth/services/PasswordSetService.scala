package com.cleverhouse.spendless.auth.services

import cats.Monad
import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.typesafe.config.{Config => TsConfig}
import com.cleverhouse.spendless.auth.domain.{AuthContext, UserPassword}
import com.cleverhouse.spendless.auth.repositories.UserPasswordRepository
import com.cleverhouse.spendless.auth.services.PasswordSetService.{PasswordSetRequest, PasswordSetResponse}
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import pl.iterators.sealedmonad.syntax._
import slick.dbio.DBIO

class PasswordSetService(
  transactor: PostgresIOTransactor,
  config: PasswordSetService.Config,
  userPasswordRepository: UserPasswordRepository,
  passwordService: PasswordService
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[PasswordSetResponse](ioRuntime) {

  def passwordSet(authContext: AuthContext, request: PasswordSetRequest): IO[PasswordSetResponse] =
    transactor.execute {
      (for {
        _ <- checkPassword(request)
        userPassword = preparePassword(authContext, request)
        _ <- insertPassword(userPassword)
      } yield PasswordSetResponse.Ok).run
    }

  private def checkPassword(request: PasswordSetRequest): StepDBIO[Boolean] =
    Monad[DBIO]
      .pure(request.newPassword.length >= config.minimumPasswordLength)
      .ensure(identity, PasswordSetResponse.InvalidPassword)

  private def preparePassword(authContext: AuthContext, request: PasswordSetRequest): UserPassword = {
    val hashed = passwordService.encrypt(request.newPassword)
    UserPassword(authContext.id, hashed)
  }

  private def insertPassword(userPassword: UserPassword): StepDBIO[UserPassword] =
    userPasswordRepository
      .upsert(userPassword)
      .seal

}

object PasswordSetService {
  sealed trait PasswordSetResponse

  case class Config(minimumPasswordLength: Int)

  final case class PasswordSetRequest(newPassword: String)

  final case class LoginResponseData(token: String)

  object Config {
    def apply(tsConfig: TsConfig): Config =
      Config(minimumPasswordLength = tsConfig.getInt("user.minimumPasswordLength"))
  }

  object PasswordSetResponse {
    final case object Ok              extends PasswordSetResponse
    final case object InvalidPassword extends PasswordSetResponse

  }

}
