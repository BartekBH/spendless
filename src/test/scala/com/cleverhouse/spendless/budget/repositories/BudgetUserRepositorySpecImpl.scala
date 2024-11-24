package com.cleverhouse.spendless.budget.repositories

import com.cleverhouse.spendless.budget.domain.{Budget, BudgetUser}
import com.cleverhouse.spendless.budget.repositories.BudgetUserRepository.BudgetUserFilters
import com.cleverhouse.spendless.budget.repositories.fixtures.BudgetFixture
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.UserEmail
import com.cleverhouse.spendless.user.fixtures.UserFixture
import com.cleverhouse.spendless.user.repositories.UserRepository.UserFilters
import com.cleverhouse.spendless.utils.PgRepositorySpec
import org.scalacheck.{Arbitrary, Gen}
import pl.iterators.kebs.scalacheck.Generator

import java.time.Instant

class BudgetUserRepositorySpecImpl extends PgRepositorySpec with BudgetFixture with UserFixture {

  private val rep: BudgetUserRepository = new BudgetUserRepositoryImpl()()

  val userGenerator: Generator[User] = allGenerators[User].maximal
  val budgetGenerator: Generator[Budget] = allGenerators[Budget].maximal

  "CRUD" should {
    "works as follow" in withDatabase {
      val uid1 = budgetGenerator.generate.createdBy
      val uid2 = budgetGenerator.generate.createdBy
      val b1   = budgetGenerator.generate.copy(createdBy = uid1, modifiedBy = uid1)
      val b2   = budgetGenerator.generate.copy(createdBy = uid1, modifiedBy = uid1)
      val b3   = budgetGenerator.generate.copy(createdBy = uid2, modifiedBy = uid2)
      for {
        _ <- withUser(uid1)
        _ <- withUser(uid2)
        _ <- withBudget(b1)
        _ <- withBudget(b2)
        _ <- withBudget(b3)

        r11 <- rep.insert(BudgetUser(b1.id, uid1))
        r21 <- rep.insert(BudgetUser(b2.id, uid1))
        r32 <- rep.insert(BudgetUser(b3.id, uid2))
        r12 <- rep.insert(BudgetUser(b1.id, uid2))

        f11 <- rep.find(BudgetUserFilters(budgetId = Some(b1.id), userId = Some(uid1)))
        f12 <- rep.find(BudgetUserFilters(budgetId = Some(b1.id), userId = Some(uid2)))
        f32 <- rep.find(BudgetUserFilters(budgetId = Some(b3.id), userId = Some(uid2)))

        l0 <- rep.list(BudgetUserFilters())
        l1 <- rep.list(BudgetUserFilters(userId = Some(uid1)))
        l2 <- rep.list(BudgetUserFilters(budgetId = Some(b1.id)))

        _ <- rep.delete(r11)
        _ <- rep.delete(r12)
        _ <- rep.delete(r21)

        ld0 <- rep.list(BudgetUserFilters())
        ld1 <- rep.list(BudgetUserFilters(userId = Some(uid1)))
        ld2 <- rep.list(BudgetUserFilters(budgetId = Some(b1.id)))

      } yield {
        f11 shouldEqual Some(r11)
        f12 shouldEqual Some(r12)
        f32 shouldEqual Some(r32)

        l0 should contain theSameElementsAs Seq(r11, r21, r32, r12)
        l1 should contain theSameElementsAs Seq(r11, r21)
        l2 should contain theSameElementsAs Seq(r11, r12)

        ld0 should contain theSameElementsAs Seq(r32)
        ld1 should contain theSameElementsAs Seq.empty
        ld2 should contain theSameElementsAs Seq.empty
      }
    }

    "filter by provided params" should {
      "userId" in withDatabase {
        val uid1 = budgetGenerator.generate.createdBy
        val uid2 = budgetGenerator.generate.createdBy
        val uid3 = budgetGenerator.generate.createdBy
        val b1   = budgetGenerator.generate.copy(createdBy = uid1, modifiedBy = uid1)
        val b2   = budgetGenerator.generate.copy(createdBy = uid1, modifiedBy = uid1)
        val b3   = budgetGenerator.generate.copy(createdBy = uid2, modifiedBy = uid2)
        for {
          _ <- withUser(uid1)
          _ <- withUser(uid2)
          _ <- withUser(uid3)
          _ <- withBudget(b1)
          _ <- withBudget(b2)
          _ <- withBudget(b3)

          r11 <- rep.insert(BudgetUser(b1.id, uid1))
          r21 <- rep.insert(BudgetUser(b2.id, uid1))
          r31 <- rep.insert(BudgetUser(b3.id, uid1))
          r32 <- rep.insert(BudgetUser(b3.id, uid2))

          l0 <- rep.list(BudgetUserFilters())
          l1 <- rep.list(BudgetUserFilters(userId = Some(uid1)))
          l2 <- rep.list(BudgetUserFilters(userId = Some(uid2)))
          l3 <- rep.list(BudgetUserFilters(userId = Some(uid3)))
        } yield {

          l0 should contain theSameElementsAs Seq(r11, r21, r31, r32)
          l1 should contain theSameElementsAs Seq(r11, r21, r31)
          l2 should contain theSameElementsAs Seq(r32)
          l3 should contain theSameElementsAs Seq.empty
        }
      }

      "budgetId" in withDatabase {
        val uid1 = budgetGenerator.generate.createdBy
        val uid2 = budgetGenerator.generate.createdBy
        val uid3 = budgetGenerator.generate.createdBy
        val b1   = budgetGenerator.generate.copy(createdBy = uid1, modifiedBy = uid1)
        val b2   = budgetGenerator.generate.copy(createdBy = uid1, modifiedBy = uid1)
        val b3   = budgetGenerator.generate.copy(createdBy = uid2, modifiedBy = uid2)
        for {
          _ <- withUser(uid1)
          _ <- withUser(uid2)
          _ <- withUser(uid3)
          _ <- withBudget(b1)
          _ <- withBudget(b2)
          _ <- withBudget(b3)

          r11 <- rep.insert(BudgetUser(b1.id, uid1))
          r12 <- rep.insert(BudgetUser(b1.id, uid2))
          r13 <- rep.insert(BudgetUser(b1.id, uid3))
          r23 <- rep.insert(BudgetUser(b2.id, uid3))

          l0 <- rep.list(BudgetUserFilters())
          l1 <- rep.list(BudgetUserFilters(budgetId = Some(b1.id)))
          l2 <- rep.list(BudgetUserFilters(budgetId = Some(b2.id)))
          l3 <- rep.list(BudgetUserFilters(budgetId = Some(b3.id)))
        } yield {

          l0 should contain theSameElementsAs Seq(r11, r12, r13, r23)
          l1 should contain theSameElementsAs Seq(r11, r12, r13)
          l2 should contain theSameElementsAs Seq(r23)
          l3 should contain theSameElementsAs Seq.empty
        }
      }
    }

  }

}
