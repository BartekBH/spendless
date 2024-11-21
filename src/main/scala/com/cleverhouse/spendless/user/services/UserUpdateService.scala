package com.cleverhouse.spendless.user.services

import cats.effect.{Clock, IO}
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.{UserId, UserName, UserModifiedAt}
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

  def update(authContext: AuthContext, userId: UserId, request: UserUpdateRequest): IO[UserUpdateResult] =
    (for {
      _           <- checkIfSelf(authContext, userId)
      user        <- findUser(userId)
      updatedUser <- updatedUser(user, request)
      result      <- updateUser(updatedUser)
      _           <- Logger[IO].info(s"Updating user $userId").seal
    } yield UserUpdateResult.Ok(result)).run
    
  private def checkIfSelf(authContext: AuthContext, userId: UserId): StepIO[Boolean] =
    IO.pure(authContext.id == userId).ensure(identity, UserUpdateResult.OperationNotPermitted)


  private def findUser(userId: UserId): StepIO[User] =
    transactor
      .execute(userRepository.find(UserFilters(id = Some(userId))))
      .valueOr(UserUpdateResult.UserDoNotExist)

  private def updatedUser(user: User, request: UserUpdateRequest): StepIO[User] =
    Clock[IO].realTimeInstant.seal.map { now =>
      user.copy(
        name = request.name.getOrElse(user.name),
        modifiedAt = UserModifiedAt(now)
      )
    }

  private def updateUser(user: User): StepIO[User] =
    transactor.execute(userRepository.update(user)).valueOr(UserUpdateResult.UserDoNotExist)

}

object UserUpdateService {
  final case class UserUpdateRequest(name: Option[UserName])

  enum UserUpdateResult:
    case Ok(user: User)
    case UserDoNotExist
    case OperationNotPermitted
}