package com.cleverhouse.spendless.user.services

import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.user.services.UserFindService.UserFindResult
import com.cleverhouse.spendless.utils.ServiceSpec
import com.softwaremill.macwire.wire
import pl.iterators.kebs.scalacheck.AllGenerators

class UserFindServiceSpec extends ServiceSpec {

  val authContextGenerator: AllGenerators[AuthContext] = allGenerators[AuthContext]
  val userGenerator: AllGenerators[User]               = allGenerators[User]

  trait TestCase extends BaseScope {
    val userRepository: UserRepository = stub[UserRepository]

    val userFindService: UserFindService = wire[UserFindService]
  }
  
  "find" should {
    "returns UserDoNotExist when repository do not return user" in new TestCase {
      val user = userGenerator.maximal.generate

      (userRepository.find _).when(UserFilters(id = Some(user.id))).returns(None.asDBIO)

      userFindService.find(user.id).unsafeRunSync() shouldEqual UserFindResult.UserDoNotExist

      (userRepository.find _).verify(UserFilters(id = Some(user.id)))
    }
    "returns Ok with user data otherwise" in new TestCase {
      val user = userGenerator.maximal.generate

      (userRepository.find _).when(UserFilters(id = Some(user.id))).returns(Some(user).asDBIO)

      userFindService.find(user.id).unsafeRunSync() shouldEqual UserFindResult.Ok(user)

      (userRepository.find _).verify(UserFilters(id = Some(user.id)))
    }
  }

}
