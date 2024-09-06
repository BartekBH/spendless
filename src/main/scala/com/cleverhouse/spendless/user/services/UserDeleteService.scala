package com.cleverhouse.spendless.user.services

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.user.services.UserDeleteService.UserDeleteResult
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

  def delete(userId: UUID): IO[UserDeleteResult] =
    (for {
      user              <- findUser(userId)
      _                 <- deleteUser(user.id)
      _                 <- Logger[IO].info(s"Deleting user ${user.id}").seal
    } yield UserDeleteResult.Ok).run

  private def findUser(userId: UUID): StepIO[User] =
    transactor
      .execute(userRepository.find(UserFilters(id = Some(userId))))
      .valueOr(UserDeleteResult.UserDoNotExist)

  private def deleteUser(userId: UUID): StepIO[Int] =
    transactor.execute(userRepository.purgeById(userId)).seal
}

object UserDeleteService {
  enum UserDeleteResult:
    case Ok
    case UserDoNotExist
}
