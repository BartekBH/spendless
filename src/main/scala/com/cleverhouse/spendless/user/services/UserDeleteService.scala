package com.cleverhouse.spendless.user.services

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.user.services.UserDeleteService.UserDeleteResult
import com.cleverhouse.spendless.user.services.UserDeleteService.UserDeleteResult.UserDoNotExist
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.log.Logging
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import org.typelevel.log4cats.Logger
import pl.iterators.sealedmonad.syntax.*

import java.util.UUID

class UserDeleteService(
  transactor: PostgresIOTransactor,
  userRepository: UserRepository
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[UserDeleteResult](ioRuntime)
    with Logging {

  def delete(authContext: AuthContext, userId: UserId): IO[UserDeleteResult] =
    (for {
      _    <- checkIfSelf(authContext, userId)
      user <- findUser(userId)
      _    <- deleteUser(user.id)
      _    <- Logger[IO].info(s"Deleting user ${user.id}").seal
    } yield UserDeleteResult.Ok).run
    
  private def checkIfSelf(authContext: AuthContext, userId: UserId): StepIO[Boolean] =
    IO.pure(authContext.id == userId).ensure(identity, UserDeleteResult.OperationNotPermitted) 

  private def findUser(userId: UserId): StepIO[User] =
    transactor
      .execute(userRepository.find(UserFilters(id = Some(userId))))
      .valueOr(UserDeleteResult.UserDoNotExist)

  private def deleteUser(userId: UserId): StepIO[Int] =
    transactor.execute(userRepository.purgeById(userId)).seal
}

object UserDeleteService {
  enum UserDeleteResult:
    case Ok
    case UserDoNotExist
    case OperationNotPermitted
}
