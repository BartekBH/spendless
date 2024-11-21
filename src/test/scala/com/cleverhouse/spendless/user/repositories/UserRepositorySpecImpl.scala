package com.cleverhouse.spendless.user.repositories

import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.UserEmail
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.utils.PgRepositorySpec
import org.scalacheck.{Arbitrary, Gen}
import pl.iterators.kebs.scalacheck.Generator

import java.time.Instant

class UserRepositorySpecImpl extends PgRepositorySpec {

  implicit val arbInstant: Arbitrary[Instant] =
    Arbitrary {
      Gen.chooseNum(Int.MinValue, Int.MaxValue).map { value =>
        Instant.ofEpochMilli(System.currentTimeMillis()).plusMillis(value.toLong)
      }
    }

  private val sut: UserRepository = new UserRepositoryImpl()

  val userGenerator: Generator[User] = allGenerators[User].maximal

  "CRUD" should {
    "works as follow" in withDatabase {
      for {
        r1 <- sut.insert(userGenerator.generate)
        r2 <- sut.insert(userGenerator.generate)
        r3 <- sut.insert(userGenerator.generate)

        l0  <- sut.list(UserFilters())
        l1  <- sut.list(UserFilters(id = Some(r1.id)))
        l2  <- sut.list(UserFilters(id = Some(r2.id)))
        l3  <- sut.list(UserFilters(id = Some(r3.id)))
        l12 <- sut.list(UserFilters(ids = Some(List(r1.id, r2.id))))

        f1 <- sut.find(UserFilters(id = Some(r1.id)))
        f2 <- sut.find(UserFilters(id = Some(r2.id)))
        f3 <- sut.find(UserFilters(id = Some(r3.id)))

        ru1 = r1.copy(email = userGenerator.generate.email)
        ru2 = r2.copy(email = userGenerator.generate.email)
        ru3 = r3.copy(email = userGenerator.generate.email)

        _ <- sut.update(ru1)
        _ <- sut.update(ru2)
        _ <- sut.update(ru3)

        lu0  <- sut.list(UserFilters())
        lu1  <- sut.list(UserFilters(id = Some(r1.id)))
        lu2  <- sut.list(UserFilters(id = Some(r2.id)))
        lu3  <- sut.list(UserFilters(id = Some(r3.id)))

        fu1 <- sut.find(UserFilters(id = Some(r1.id)))
        fu2 <- sut.find(UserFilters(id = Some(r2.id)))
        fu3 <- sut.find(UserFilters(id = Some(r3.id)))
      } yield {
        l0 should contain theSameElementsAs(Seq(r1, r2, r3))
        l1 should contain theSameElementsAs(Seq(r1))
        l2 should contain theSameElementsAs(Seq(r2))
        l3 should contain theSameElementsAs(Seq(r3))
        l12 should contain theSameElementsAs(Seq(r1, r2))

        f1 shouldEqual(Some(r1))
        f2 shouldEqual(Some(r2))
        f3 shouldEqual(Some(r3))

        lu0 should contain theSameElementsAs(Seq(ru1, ru2, ru3))
        lu1 should contain theSameElementsAs(Seq(ru1))
        lu2 should contain theSameElementsAs(Seq(ru2))
        lu3 should contain theSameElementsAs(Seq(ru3))

        fu1 shouldEqual (Some(ru1))
        fu2 shouldEqual (Some(ru2))
        fu3 shouldEqual (Some(ru3))
      }
    }
    "filter by provided params" should {
      "email" in withDatabase {
        for {
          o1 <- sut.insert(userGenerator.generate)
          o2 <- sut.insert(userGenerator.generate)
          o3 <- sut.insert(userGenerator.generate)

          l1 <- sut.list(UserFilters(email = Some(o1.email)))
          l2 <- sut.list(UserFilters(email = Some(o2.email)))
          l3 <- sut.list(UserFilters(email = Some(o3.email)))
        } yield {
          l1 should contain theSameElementsAs(Seq(o1))
          l2 should contain theSameElementsAs(Seq(o2))
          l3 should contain theSameElementsAs(Seq(o3))
        }
      }
    }

  }

}
