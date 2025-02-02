package com.cleverhouse.spendless.user.fixtures

import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import com.cleverhouse.spendless.user.repositories.UserRepositoryImpl
import com.cleverhouse.spendless.utils.Generators
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

trait UserFixture { deps: Generators =>
  implicit val ec: ExecutionContext

  private val _dbioUserRepository: UserRepositoryImpl = new UserRepositoryImpl()

  private val _userGenerator = allGenerators[User].normal
  def withUser(userId: UserId, obj: User = _userGenerator.generate): DBIO[User] =
    _dbioUserRepository.insert(obj.copy(id = userId))
}
