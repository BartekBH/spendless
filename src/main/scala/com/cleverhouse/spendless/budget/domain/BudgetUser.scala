package com.cleverhouse.spendless.budget.domain

import com.cleverhouse.spendless.budget.domain.BudgetDomain.BudgetId
import com.cleverhouse.spendless.user.domain.UserDomain.UserId

import java.util.UUID

case class BudgetUser(
  budgetId: BudgetId,
  userId: UserId)

