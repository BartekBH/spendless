package com.cleverhouse.spendless.user.services

import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.user.services.UserListService.UserListResult
import com.cleverhouse.spendless.user.services.UserListService.UserListResult.Ok
import com.cleverhouse.spendless.utils.ServiceSpec
import com.softwaremill.macwire.wire
import pl.iterators.kebs.scalacheck.AllGenerators

class UserListServiceSpec extends ServiceSpec {

  val authContextGenerator: AllGenerators[AuthContext] = allGenerators[AuthContext]

  trait TestCase extends BaseScope {
    val userRepository: UserRepository = stub[UserRepository]

    val userListService: UserListService = wire[UserListService]
  }

  "list" should {
    "returns Ok with values from repository" in new TestCase {
      val result = (1 to 10).map(_ => allGenerators[User].maximal.generate)

      (userRepository.list _).when(UserFilters()).returns(result.asDBIO)

      userListService.list().unsafeRunSync() shouldEqual UserListResult.Ok(result)

      (userRepository.list _).verify(UserFilters())
    }

  }

}
