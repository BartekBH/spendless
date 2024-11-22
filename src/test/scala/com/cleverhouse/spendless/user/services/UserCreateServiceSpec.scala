package com.cleverhouse.spendless.user.services

import com.cleverhouse.spendless.auth.domain.AuthDomain.PasswordPlain
import com.cleverhouse.spendless.auth.services.PasswordSetService
import com.cleverhouse.spendless.auth.services.PasswordSetService.PasswordSetResponse
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.user.services.UserCreateService.{UserCreateRequest, UserCreateResult}
import com.cleverhouse.spendless.utils.ServiceSpec
import com.softwaremill.macwire.wire
import pl.iterators.kebs.scalacheck.AllGenerators

class UserCreateServiceSpec extends ServiceSpec {

  val requestGenerator: AllGenerators[UserCreateRequest] = allGenerators[UserCreateRequest]
  val userGenerator: AllGenerators[User]                 = allGenerators[User]

  class MockablePasswordSetService extends PasswordSetService(null, null, null, null)(ioRuntime)

  trait TestCase extends BaseScope {
    val userRepository: UserRepository         = stub[UserRepository]
    val passwordSetService: PasswordSetService = stub[MockablePasswordSetService]

    val userCreateService: UserCreateService = wire[UserCreateService]
  }

  "create" should {
    "returns EmailAlreadyExist if user exists in repository" in new TestCase {
      val request = requestGenerator.normal.generate
      val user    = userGenerator.normal.generate

      (userRepository.find _).when(UserFilters(email = Some(request.email))).returns(Some(user).asDBIO)

      userCreateService.create(request).unsafeRunSync() shouldEqual UserCreateResult.EmailAlreadyExist

      (userRepository.find _).verify(UserFilters(email = Some(request.email)))
    }
    "returns InvalidPassword if email do not exist and password is provided but its invalid" in new TestCase {
      val password = PasswordPlain("pass")
      val request  = requestGenerator.normal.generate.copy(password = password)

      (userRepository.find _).when(UserFilters(email = Some(request.email))).returns(None.asDBIO)
      (passwordSetService.passwordSet _).when(*, *).returns(PasswordSetResponse.InvalidPassword.asIO)

      userCreateService.create(request).unsafeRunSync() shouldEqual UserCreateResult.InvalidPassword

      (userRepository.find _).verify(UserFilters(email = Some(request.email)))
      (passwordSetService.passwordSet _).verify(*, *)
    }
    "returns OK if email do not exist insert user" in new TestCase {
      val password = PasswordPlain("pass")
      val request  = requestGenerator.normal.generate.copy(password = password)
      val user     = userGenerator.maximal.generate

      (userRepository.find _).when(UserFilters(email = Some(request.email))).returns(None.asDBIO)
      (passwordSetService.passwordSet _).when(*, *).returns(PasswordSetResponse.Ok.asIO)
      (userRepository.insert _).when(*).returns(user.asDBIO)

      userCreateService.create(request).unsafeRunSync() shouldEqual UserCreateResult.Ok(user)

      (userRepository.find _).verify(UserFilters(email = Some(request.email)))
      (passwordSetService.passwordSet _).verify(*, *)
      (userRepository.insert _).verify(*)

    }
  }

}
