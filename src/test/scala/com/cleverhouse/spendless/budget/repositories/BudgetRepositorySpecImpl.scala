package com.cleverhouse.spendless.budget.repositories

import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.repositories.BudgetRepository.BudgetFilters
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.fixtures.UserFixture
import com.cleverhouse.spendless.utils.PgRepositorySpec
import org.scalacheck.{Arbitrary, Gen}
import pl.iterators.kebs.scalacheck.Generator

import java.time.Instant

class BudgetRepositorySpecImpl extends PgRepositorySpec with UserFixture {

  private val rep: BudgetRepository = new BudgetRepositoryImpl()

  val budgetGenerator: Generator[Budget] = allGenerators[Budget].maximal

  "CRUD" should {
    "works as follow" in withDatabase {
      val uid1 = budgetGenerator.generate.createdBy
      val uid2 = budgetGenerator.generate.createdBy
      val uid3 = budgetGenerator.generate.createdBy

      val b1 = budgetGenerator.generate.copy(createdBy = uid1, modifiedBy = uid1)
      val b2 = budgetGenerator.generate.copy(createdBy = uid2, modifiedBy = uid2)
      val b3 = budgetGenerator.generate.copy(createdBy = uid3, modifiedBy = uid3)

      for {
        _ <- withUser(uid1)
        _ <- withUser(uid2)
        _ <- withUser(uid3)

        r1 <- rep.insert(b1)
        r2 <- rep.insert(b2)
        r3 <- rep.insert(b3)

        f1 <- rep.find(BudgetFilters(id = Some(r1.id)))
        f2 <- rep.find(BudgetFilters(id = Some(r2.id)))
        f3 <- rep.find(BudgetFilters(id = Some(r3.id)))

        l0  <- rep.list(BudgetFilters())
        l1  <- rep.list(BudgetFilters(id = Some(r1.id)))
        l2  <- rep.list(BudgetFilters(id = Some(r2.id)))
        l3  <- rep.list(BudgetFilters(id = Some(r3.id)))

        ru1 = r1.copy(name = budgetGenerator.generate.name)
        ru2 = r2.copy(name = budgetGenerator.generate.name)

        _ <- rep.update(ru1)
        _ <- rep.update(ru2)

        lu0  <- rep.list(BudgetFilters())
        lu1  <- rep.list(BudgetFilters(id = Some(r1.id)))
        lu2  <- rep.list(BudgetFilters(id = Some(r2.id)))
        lu3  <- rep.list(BudgetFilters(id = Some(r3.id)))

        rd2 <- rep.purgeById(r2.id)
        rd3 <- rep.purgeById(r3.id)

        ld0 <- rep.list(BudgetFilters())
        ld1 <- rep.list(BudgetFilters(id = Some(r1.id)))
        ld2 <- rep.list(BudgetFilters(id = Some(r2.id)))
        ld3 <- rep.list(BudgetFilters(id = Some(r3.id)))

      } yield {
        f1 shouldEqual Some(r1)
        f2 shouldEqual Some(r2)
        f3 shouldEqual Some(r3)

        l0 should contain theSameElementsAs Seq(r1, r2, r3)
        l1 should contain theSameElementsAs Seq(r1)
        l2 should contain theSameElementsAs Seq(r2)
        l3 should contain theSameElementsAs Seq(r3)

        lu0 should contain theSameElementsAs Seq(ru1, ru2, r3)
        lu1 should contain theSameElementsAs Seq(ru1)
        lu2 should contain theSameElementsAs Seq(ru2)
        lu3 should contain theSameElementsAs Seq(r3)

        ld0 should contain theSameElementsAs Seq(ru1)
        ld1 should contain theSameElementsAs Seq(ru1)
        ld2 should contain theSameElementsAs Nil
        ld3 should contain theSameElementsAs Nil
      }
    }
    "filter by provided params" should {
      "id" in withDatabase {
        val uid1 = budgetGenerator.generate.createdBy
        val uid2 = budgetGenerator.generate.createdBy
        val uid3 = budgetGenerator.generate.createdBy

        for {
          _ <- withUser(uid1)
          _ <- withUser(uid2)
          _ <- withUser(uid3)

          r1 <- rep.insert(budgetGenerator.generate.copy(createdBy = uid1, modifiedBy = uid1))
          r2 <- rep.insert(budgetGenerator.generate.copy(createdBy = uid2, modifiedBy = uid2))
          r3 <- rep.insert(budgetGenerator.generate.copy(createdBy = uid3, modifiedBy = uid3))

          l1 <- rep.list(BudgetFilters(id = Some(r1.id)))
          l2 <- rep.list(BudgetFilters(id = Some(r2.id)))
          l3 <- rep.list(BudgetFilters(id = Some(r3.id)))
        } yield {
          l1 should contain theSameElementsAs Seq(r1)
          l2 should contain theSameElementsAs Seq(r2)
          l3 should contain theSameElementsAs Seq(r3)
        }
      }
    }
    "createdBy" in withDatabase {
      val uid1 = budgetGenerator.generate.createdBy
      val uid2 = budgetGenerator.generate.createdBy
      val uid3 = budgetGenerator.generate.createdBy
      val uid4 = budgetGenerator.generate.createdBy
      for {
        _ <- withUser(uid1)
        _ <- withUser(uid2)
        _ <- withUser(uid3)
        _ <- withUser(uid4)

        r1 <- rep.insert(budgetGenerator.generate.copy(createdBy = uid1, modifiedBy = uid1))
        r2 <- rep.insert(budgetGenerator.generate.copy(createdBy = uid2, modifiedBy = uid2))
        r3 <- rep.insert(budgetGenerator.generate.copy(createdBy = uid2, modifiedBy = uid2))
        r4 <- rep.insert(budgetGenerator.generate.copy(createdBy = uid2, modifiedBy = uid2))
        _  <- rep.insert(budgetGenerator.generate.copy(createdBy = uid4, modifiedBy = uid4))

        l1 <- rep.list(BudgetFilters(createdBy = Some(uid1)))
        l2 <- rep.list(BudgetFilters(createdBy = Some(uid2)))
        l3 <- rep.list(BudgetFilters(createdBy = Some(uid3)))
      } yield {

        l1 should contain theSameElementsAs Seq(r1)
        l2 should contain theSameElementsAs Seq(r2, r3, r4)
        l3 should contain theSameElementsAs Seq.empty
      }

    }

  }

}
