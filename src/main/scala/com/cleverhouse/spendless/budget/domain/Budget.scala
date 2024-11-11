package com.cleverhouse.spendless.budget.domain

import com.cleverhouse.spendless.budget.domain.BudgetDomain.*
import com.cleverhouse.spendless.user.domain.UserDomain.UserId

import java.time.Instant
import java.util.UUID

case class Budget(
  id: BudgetId,
  name: BudgetName,
  createdAt: BudgetCreatedAt,
  createdBy: UserId,
  modifiedAt: BudgetModifiedAt,
  modifiedBy: UserId)