package com.cleverhouse.spendless.user.services

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.user.services.UserListService.UserListResult
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.log.Logging
import com.cleverhouse.spendless.utils.service.SealedMonadServiceIODBIO
import pl.iterators.sealedmonad.syntax.*

class UserListService(
  transactor: PostgresIOTransactor,
  userRepository: UserRepository
)(implicit ioRuntime: IORuntime)
  extends SealedMonadServiceIODBIO[UserListResult](ioRuntime)
    with Logging {

  def list(): IO[UserListResult] =
    (for {
      users             <- fetchUsers()
    } yield UserListResult.Ok(users)).run

  private def fetchUsers(): StepIO[Seq[User]] =
    transactor.execute(userRepository.list(UserFilters())).seal
}

object UserListService {
  enum UserListResult:
    case Ok(users: Seq[User])
}