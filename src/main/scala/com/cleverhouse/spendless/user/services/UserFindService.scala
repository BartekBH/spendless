package com.cleverhouse.spendless.user.services

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.user.services.UserFindService.UserFindResult
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.log.Logging
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import pl.iterators.sealedmonad.syntax.*

import java.util.UUID

class UserFindService(
  transactor: PostgresIOTransactor,
  userRepository: UserRepository
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[UserFindResult](ioRuntime)
    with Logging {

  def find(userId: UserId): IO[UserFindResult] =
    (for {
      user              <- fetchUser(userId)
    } yield UserFindResult.Ok(user)).run

  private def fetchUser(userId: UserId): StepIO[User] =
    transactor
      .execute(userRepository.find(UserFilters(id = Some(userId))))
      .valueOr(UserFindResult.UserDoNotExist)
}

object UserFindService {
  enum UserFindResult:
    case Ok(user: User)
    case UserDoNotExist
}
