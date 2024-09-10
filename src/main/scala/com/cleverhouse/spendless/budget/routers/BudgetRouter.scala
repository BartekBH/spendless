package com.cleverhouse.spendless.budget.routers

import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.services.*
import com.cleverhouse.spendless.budget.services.BudgetCreateService.{BudgetCreateRequest, BudgetCreateResult}
import com.cleverhouse.spendless.budget.services.BudgetDeleteService.BudgetDeleteResult
import com.cleverhouse.spendless.budget.services.BudgetFindService.BudgetFindResult
import com.cleverhouse.spendless.budget.services.BudgetFindService.BudgetFindResult.BudgetDoNotExist
import com.cleverhouse.spendless.budget.services.BudgetListService.BudgetListResult
import com.cleverhouse.spendless.budget.services.BudgetListService.BudgetListResult.Ok
import com.cleverhouse.spendless.budget.services.BudgetUpdateService.{BudgetUpdateRequest, BudgetUpdateResult}
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{JavaUUID, delete, concat, entity, get, patch, pathEndOrSingleSlash, pathPrefix, post}
import org.apache.pekko.http.scaladsl.server.{Directives, Route}
import com.cleverhouse.spendless.utils.http.CompleteDirectives.complete
import com.cleverhouse.spendless.utils.json.JsonProtocol
import org.apache.pekko.http.scaladsl.server.directives.MarshallingDirectives.*

class BudgetRouter(
  budgetCreateService: BudgetCreateService,
  budgetDeleteService: BudgetDeleteService,
  budgetFindService: BudgetFindService,
  budgetListService: BudgetListService,
  budgetUpdateService: BudgetUpdateService
)(implicit runtime: IORuntime) extends JsonProtocol {

  import BudgetRouter._

  def route: Route = pathPrefix(budgetsPath) {
    concat(
      (get & pathEndOrSingleSlash) {
        complete(budgetListService.list()) {
          case BudgetListResult.Ok(budgets) => StatusCodes.OK -> budgets
        }
      },
      (post & entity(as[BudgetCreateRequest]) & pathEndOrSingleSlash) { request =>
        complete(budgetCreateService.create(request)) {
          case BudgetCreateResult.Ok(budget) => StatusCodes.OK -> budget
        }
      },
      pathPrefix(JavaUUID) { id =>
        concat(
          (get & pathEndOrSingleSlash) {
            complete(budgetFindService.find(id)) {
              case BudgetFindResult.Ok(budget) => StatusCodes.OK -> budget
              case BudgetFindResult.BudgetDoNotExist => StatusCodes.NotFound
            }
          },
          (patch & entity(as[BudgetUpdateRequest]) & pathEndOrSingleSlash) { request =>
            complete(budgetUpdateService.update(id, request)) {
              case BudgetUpdateResult.Ok(budget) => StatusCodes.OK -> budget
              case BudgetUpdateResult.BudgetDoNotExist => StatusCodes.NotFound
            }
          },
          (delete & pathEndOrSingleSlash) {
            complete(budgetDeleteService.delete(id)) {
              case BudgetDeleteResult.Ok => StatusCodes.OK
              case BudgetDeleteResult.BudgetDoNotExist => StatusCodes.NotFound
            }
          }
        )
      }
    )
  }
}

object BudgetRouter {
  private val budgetsPath = "budgets"
}
