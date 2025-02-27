package com.cleverhouse.spendless.user.services

import cats.effect.{Clock, IO}
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.auth.domain.AuthDomain.PasswordPlain
import com.cleverhouse.spendless.auth.services.PasswordSetService
import com.cleverhouse.spendless.auth.services.PasswordSetService.{PasswordSetRequest, PasswordSetResponse}
import com.cleverhouse.spendless.auth.services.PasswordSetService.PasswordSetResponse.{InvalidPassword, Ok}
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.*
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.user.services.UserCreateService.{UserCreateRequest, UserCreateResult}
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.log.Logging
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import org.typelevel.log4cats.Logger
import pl.iterators.sealedmonad.syntax.*

import java.time.Instant
import java.util.UUID

class UserCreateService(
  transactor: PostgresIOTransactor,
  userRepository: UserRepository,
  passwordSetService: PasswordSetService
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[UserCreateResult](ioRuntime)
  with Logging {

  def create(request: UserCreateRequest): IO[UserCreateResult] =
    (for {
      _        <- Logger[IO].info(s"Creating user ${request.email}").seal
      _        <- findUserByEmail(request)
      toInsert <- createUser(request)
      _        <- setPassword(toInsert, request)
      user     <- insert(toInsert)
      _        <- Logger[IO].info(s"Creating user result $user").seal
    } yield UserCreateResult.Ok(user)).run

  private def findUserByEmail(request: UserCreateRequest): StepIO[Option[User]] =
    transactor
      .execute(userRepository.find(UserFilters(email = Some(request.email))))
      .ensure(_.isEmpty, UserCreateResult.EmailAlreadyExist)

  private def createUser(request: UserCreateRequest): StepIO[User] =
    Clock[IO].realTime.map { nowMillis =>
      val now = Instant.ofEpochMilli(nowMillis.toMillis)
      User(
        id = UserId(UUID.randomUUID),
        email = request.email,
        name = request.name,
        createdAt = UserCreatedAt(now),
        modifiedAt = UserModifiedAt(now)
      )
    }.seal

  private def setPassword(user: User, request: UserCreateRequest): StepIO[Unit] =
    passwordSetService.passwordSet(AuthContext(user), PasswordSetRequest(request.password)).seal.attempt {
      case PasswordSetResponse.InvalidPassword => Left(UserCreateResult.InvalidPassword)
      case PasswordSetResponse.Ok => Right(())
    }

  private def insert(user: User): StepIO[User] =
    transactor.execute(userRepository.insert(user)).seal
}

object UserCreateService {
  final case class UserCreateRequest(email: UserEmail, password: PasswordPlain, name: UserName)

  enum UserCreateResult:
    case Ok(user: User)
    case EmailAlreadyExist
    case InvalidPassword
}
