package com.cleverhouse.spendless.user.services

import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.UserName
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.user.services.UserUpdateService.{UserUpdateRequest, UserUpdateResult}
import com.cleverhouse.spendless.utils.ServiceSpec
import com.softwaremill.macwire.wire
import pl.iterators.kebs.scalacheck.AllGenerators

class UserUpdateServiceSpec extends ServiceSpec {

  val authContextGenerator: AllGenerators[AuthContext]   = allGenerators[AuthContext]
  val userGenerator: AllGenerators[User]                 = allGenerators[User]
  val requestGenerator: AllGenerators[UserUpdateRequest] = allGenerators[UserUpdateRequest]

  trait TestCase extends BaseScope {
    val userRepository: UserRepository = stub[UserRepository]

    lazy val userUpdateService: UserUpdateService = wire[UserUpdateService]
  }
  
  "update" should {
    "returns OperationNotPermitted if user try to update other user" in new TestCase {
      val authContext = authContextGenerator.normal.generate
      val user        = userGenerator.maximal.generate
      val request     = requestGenerator.maximal.generate

      userUpdateService.update(authContext, user.id, request).unsafeRunSync() shouldEqual UserUpdateResult.OperationNotPermitted
    }

    "returns UserDoNotExist if user do not exist in repository" in new TestCase {
      val authContext = authContextGenerator.normal.generate
      val user        = userGenerator.maximal.generate.copy(id = authContext.id)
      val request     = requestGenerator.maximal.generate

      (userRepository.find _).when(UserFilters(id = Some(user.id))).returns(None.asDBIO)

      userUpdateService.update(authContext, user.id, request).unsafeRunSync() shouldEqual UserUpdateResult.UserDoNotExist

      (userRepository.find _).verify(UserFilters(id = Some(user.id)))
    }
    "returns Ok if user exists in repository" in new TestCase {
      val authContext = authContextGenerator.normal.generate
      val user        = userGenerator.maximal.generate.copy(id = authContext.id)
      val request     = requestGenerator.maximal.generate

      (userRepository.find _).when(UserFilters(id = Some(user.id))).returns(Some(user).asDBIO)
      (userRepository.update _).when(*).returns(Some(user).asDBIO)

      userUpdateService.update(authContext, user.id, request).unsafeRunSync() shouldEqual UserUpdateResult.Ok(user)

      (userRepository.find _).verify(UserFilters(id = Some(user.id)))
      (userRepository.update _).verify(*)
    }
    
  }
}
