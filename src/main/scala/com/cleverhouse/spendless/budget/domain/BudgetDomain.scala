package com.cleverhouse.spendless.budget.domain

import pl.iterators.kebs.opaque.*

import java.time.Instant
import java.util.UUID

object BudgetDomain extends Domain

trait Domain {
  
  opaque type BudgetId = UUID
  object BudgetId extends Opaque[BudgetId, UUID]

  opaque type BudgetName = String
  object BudgetName extends Opaque[BudgetName, String]

  opaque type BudgetCreatedAt = Instant
  object BudgetCreatedAt extends Opaque[BudgetCreatedAt, Instant]

  opaque type BudgetModifiedAt = Instant
  object BudgetModifiedAt extends Opaque[BudgetModifiedAt, Instant]
  
}
