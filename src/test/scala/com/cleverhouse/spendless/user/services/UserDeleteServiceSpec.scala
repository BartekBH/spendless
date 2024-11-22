package com.cleverhouse.spendless.user.services

import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.user.services.UserDeleteService.UserDeleteResult
import com.cleverhouse.spendless.utils.ServiceSpec
import com.softwaremill.macwire.wire
import pl.iterators.kebs.scalacheck.AllGenerators

class UserDeleteServiceSpec extends ServiceSpec {

  val authContextGenerator: AllGenerators[AuthContext] = allGenerators[AuthContext]
  val userGenerator: AllGenerators[User]               = allGenerators[User]

  trait TestCase extends BaseScope {
    val userRepository: UserRepository = stub[UserRepository]

    lazy val userDeleteService: UserDeleteService = wire[UserDeleteService]
  }
  
  "delete" should {
    "returns OperationNotPermitted if user try to delete other user" in new TestCase {
      val authContext = authContextGenerator.normal.generate
      val user = userGenerator.maximal.generate

      userDeleteService.delete(authContext, user.id).unsafeRunSync() shouldEqual UserDeleteResult.OperationNotPermitted

    }
    "returns UserDoNotExist if user do not exist in repository" in new TestCase {
      val authContext = authContextGenerator.normal.generate
      val user = userGenerator.maximal.generate.copy(id = authContext.id)

      (userRepository.find _).when(UserFilters(id = Some(user.id))).returns(None.asDBIO)

      userDeleteService.delete(authContext, user.id).unsafeRunSync() shouldEqual UserDeleteResult.UserDoNotExist

      (userRepository.find _).verify(UserFilters(id = Some(user.id)))

    }
    "returns Ok if user exists in repository" in new TestCase {
      val authContext = authContextGenerator.normal.generate
      val user = userGenerator.maximal.generate.copy(id = authContext.id)

      (userRepository.find _).when(UserFilters(id = Some(user.id))).returns(Some(user).asDBIO)
      (userRepository.purgeById _).when(user.id).returns(1.asDBIO)

      userDeleteService.delete(authContext, user.id).unsafeRunSync() shouldEqual UserDeleteResult.Ok

      (userRepository.find _).verify(UserFilters(id = Some(user.id)))
      (userRepository.purgeById _).verify(user.id)
    }
  }
}
