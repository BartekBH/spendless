package com.cleverhouse.spendless.auth.repositories

import com.cleverhouse.spendless.auth.domain.AuthDomain.PasswordHash
import com.cleverhouse.spendless.auth.domain.UserPassword
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import com.cleverhouse.spendless.utils.PgRepositorySpec
import pl.iterators.kebs.scalacheck.Generator

import java.util.UUID

class UserPasswordRepositoryImplSpec extends PgRepositorySpec {
  
  private val sut: UserPasswordRepository = new UserPasswordRepositoryImpl()

  val userPasswordGenerator: Generator[UserPassword] = allGenerators[UserPassword].normal
  
  "upsert and filter" should {
    "works as follows" in withDatabase {
      val userPassword1 = userPasswordGenerator.generate
      val userPassword2 = userPasswordGenerator.generate

      val userPassword1Changed = userPassword1.copy(password = userPasswordGenerator.generate.password)

      for {
        f1 <- sut.find(userPassword1.userId)
        f2 <- sut.find(userPassword2.userId)

        _ <- sut.upsert(userPassword1)
        _ <- sut.upsert(userPassword2)

        f3 <- sut.find(userPassword1.userId)
        f4 <- sut.find(userPassword2.userId)

        _ <- sut.upsert(userPassword1Changed)

        f5 <- sut.find(userPassword1.userId)
        f6 <- sut.find(userPassword2.userId)

      } yield {
        assert(f1.isEmpty)
        assert(f2.isEmpty)

        assert(f3.contains(userPassword1))
        assert(f4.contains(userPassword2))

        assert(f5.contains(userPassword1Changed))
        assert(f6.contains(userPassword2))
      }
    }
  }

}
