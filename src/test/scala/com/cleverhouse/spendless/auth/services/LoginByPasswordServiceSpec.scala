package com.cleverhouse.spendless.auth.services

import com.cleverhouse.spendless.auth.domain.{AuthDomain, UserPassword}
import com.cleverhouse.spendless.auth.repositories.UserPasswordRepository
import com.cleverhouse.spendless.auth.services.LoginByPasswordService.{LoginRequest, LoginResponse, LoginResponseData}
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.repositories.UserRepository
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.utils.ServiceSpec
import com.softwaremill.macwire.wire
import pl.iterators.kebs.scalacheck.AllGenerators

class LoginByPasswordServiceSpec extends ServiceSpec {
  val userGenerator: AllGenerators[User]                            = allGenerators[User]
  val configGenerator: AllGenerators[LoginByPasswordService.Config] = allGenerators[LoginByPasswordService.Config]
  val jwtGenerator: AllGenerators[AuthDomain.Jwt]                   = allGenerators[AuthDomain.Jwt]
  val loginRequestGenerator: AllGenerators[LoginRequest]            = allGenerators[LoginRequest]
  val userPasswordGenerator: AllGenerators[UserPassword]            = allGenerators[UserPassword]

  trait TestCase extends BaseScope {
    val userRepository: UserRepository                 = stub[UserRepository]
    val userPasswordRepository: UserPasswordRepository = stub[UserPasswordRepository]
    val passwordService: PasswordService               = stub[PasswordService]
    val jwtService: JwtService                         = stub[JwtService]
    val config                                         = configGenerator.normal.generate

    val loginService: LoginByPasswordService = wire[LoginByPasswordService]
  }

  "login" should  {
    "return UserOrPasswordNotFound user with certain email is not present in db" in new TestCase {
      val request = loginRequestGenerator.normal.generate
      val filter  = UserFilters(email = Some(request.email))

      (userRepository.find _).when(filter).returns(None.asDBIO)

      assert(loginService.login(request).unsafeRunSync() == LoginResponse.UserOrPasswordNotFound)

      (userRepository.find _).verify(filter)
    }

    "return UserOrPasswordNotFound when user do not have generated password" in new TestCase {
      val user    = userGenerator.normal.generate
      val request = loginRequestGenerator.normal.generate.copy(email = user.email)
      val filter  = UserFilters(email = Some(request.email))

      (userRepository.find _).when(filter).returns(Some(user).asDBIO)

      (userPasswordRepository.find _).when(user.id).returns(None.asDBIO)

      assert(loginService.login(request).unsafeRunSync() == LoginResponse.UserOrPasswordNotFound)

      (userRepository.find _).verify(filter)

      (userPasswordRepository.find _).verify(user.id)
    }

    "return InvalidCredentials when password is not correct" in new TestCase {
      val user         = userGenerator.normal.generate
      val request      = loginRequestGenerator.normal.generate.copy(email = user.email)
      val userPassword = userPasswordGenerator.normal.generate.copy(userId = user.id)
      val filter       = UserFilters(email = Some(request.email))

      (userRepository.find _).when(filter).returns(Some(user).asDBIO)

      (userPasswordRepository.find _).when(user.id).returns(Some(userPassword).asDBIO)

      (passwordService.check _).when(request.password, userPassword.password).returns(false)

      assert(loginService.login(request).unsafeRunSync() == LoginResponse.InvalidCredentials)

      (userRepository.find _).verify(filter)

      (userPasswordRepository.find _).verify(user.id)

      (passwordService.check _).verify(request.password, userPassword.password)
    }

    "return Ok otherwise, generating jwt token for user" in new TestCase {
      val user         = userGenerator.normal.generate
      val request      = loginRequestGenerator.normal.generate.copy(email = user.email)
      val userPassword = userPasswordGenerator.normal.generate.copy(userId = user.id)
      val jwt          = jwtGenerator.normal.generate
      val filter       = UserFilters(email = Some(request.email))

      (userRepository.find _).when(filter).returns(Some(user).asDBIO)

      (userPasswordRepository.find _).when(user.id).returns(Some(userPassword).asDBIO)

      (passwordService.check _).when(request.password, userPassword.password).returns(true)

      (jwtService.encode _).when(*, *, *, *, *, *, *, *).returns(jwt.value)

      assert(loginService.login(request).unsafeRunSync() == LoginResponse.Ok(LoginResponseData(jwt)))

      (userRepository.find _).verify(filter)

      (userPasswordRepository.find _).verify(user.id)

      (passwordService.check _).verify(request.password, userPassword.password)

      (jwtService.encode _).verify(*, *, *, *, *, *, *, *)
    }
  }

}
