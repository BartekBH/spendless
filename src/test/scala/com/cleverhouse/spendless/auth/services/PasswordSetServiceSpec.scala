package com.cleverhouse.spendless.auth.services

import com.cleverhouse.spendless.auth.domain.{AuthContext, UserPassword}
import com.cleverhouse.spendless.auth.domain.AuthDomain.{PasswordHash, PasswordPlain}
import com.cleverhouse.spendless.auth.repositories.UserPasswordRepository
import com.cleverhouse.spendless.auth.services.PasswordSetService.PasswordSetResponse.Ok
import com.cleverhouse.spendless.auth.services.PasswordSetService.{PasswordSetRequest, PasswordSetResponse}
import com.cleverhouse.spendless.utils.ServiceSpec
import com.softwaremill.macwire.wire
import pl.iterators.kebs.scalacheck.AllGenerators

class PasswordSetServiceSpec extends ServiceSpec {
  val authContextGenerator: AllGenerators[AuthContext] = allGenerators[AuthContext]

  trait TestCase extends BaseScope {
    val userPasswordRepository: UserPasswordRepository = stub[UserPasswordRepository]
    val passwordService: PasswordService               = stub[PasswordService]
    val config: PasswordSetService.Config              = PasswordSetService.Config(minimumPasswordLength = 4)

    val user = authContextGenerator.normal.generate

    val passwordSetService: PasswordSetService = wire[PasswordSetService]
  }

  "login" should {
    "return InvalidPassword when password is too short" in new TestCase {
      val plainPassword = PasswordPlain("pas")
      val request       = PasswordSetRequest(plainPassword)

      assert(passwordSetService.passwordSet(user, request).unsafeRunSync() == PasswordSetResponse.InvalidPassword)
    }

    "return OK and set password in db otherwise" in new TestCase {
      val plainPassword  = PasswordPlain("password")
      val hashedPassword = PasswordHash("hashed")
      val request        = PasswordSetRequest(plainPassword)
      val userPassword   = UserPassword(user.id, hashedPassword)

      (passwordService.encrypt _).when(request.newPassword).returns(hashedPassword)

      (userPasswordRepository.upsert _).when(userPassword).returns(userPassword.asDBIO)

      assert(passwordSetService.passwordSet(user, request).unsafeRunSync() == PasswordSetResponse.Ok)

      (passwordService.encrypt _).verify(request.newPassword)

      (userPasswordRepository.upsert _).verify(userPassword)
    }

  }

}
