package com.cleverhouse.spendless.user.services

import cats.effect.{Clock, IO}
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.user.services.UserUpdateService.{UserUpdateRequest, UserUpdateResult}
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.log.Logging
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import org.typelevel.log4cats.Logger
import pl.iterators.sealedmonad.syntax.*

import java.util.UUID

class UserUpdateService(
  transactor: PostgresIOTransactor,
  userRepository: UserRepository
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[UserUpdateResult](ioRuntime)
    with Logging {

  def update(userId: UUID, request: UserUpdateRequest): IO[UserUpdateResult] =
    (for {
      user              <- findUser(userId)
      updatedUser       <- updatedUser(user, request)
      result            <- updateUser(updatedUser)
      _                 <- Logger[IO].info(s"Updating user $userId").seal
    } yield UserUpdateResult.Ok(result)).run

  private def findUser(userId: UUID): StepIO[User] =
    transactor
      .execute(userRepository.find(UserFilters(id = Some(userId))))
      .valueOr(UserUpdateResult.UserDoNotExist)

  private def updatedUser(user: User, request: UserUpdateRequest): StepIO[User] =
    Clock[IO].realTimeInstant.seal.map { now =>
      user.copy(
        email = request.email.getOrElse(user.email),
        password = request.password.getOrElse(user.password),
        modifiedAt = now)
    }

  private def updateUser(user: User): StepIO[User] =
    transactor.execute(userRepository.update(user)).valueOr(UserUpdateResult.UserDoNotExist)

}

object UserUpdateService {
  final case class UserUpdateRequest(email: Option[String], password: Option[String])

  enum UserUpdateResult:
    case Ok(user: User)
    case UserDoNotExist
}