package com.cleverhouse.spendless.budget.repositories.fixtures

import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.repositories.{BudgetRepository, BudgetRepositoryImpl}
import com.cleverhouse.spendless.utils.Generators
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

trait BudgetFixture {  deps: Generators =>
  implicit val ec: ExecutionContext

  private val _dbioBudgetRepository: BudgetRepository = new BudgetRepositoryImpl()()

  private val _budgetGenerator = allGenerators[Budget].normal
  def withBudget(obj: Budget = _budgetGenerator.generate): DBIO[Budget] =
    _dbioBudgetRepository.insert(obj)

}
