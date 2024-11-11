package com.cleverhouse.spendless.budget.routers

import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.domain.BudgetDomain.BudgetId
import com.cleverhouse.spendless.budget.services.*
import com.cleverhouse.spendless.budget.services.BudgetAssignService.{BudgetAssignRequest, BudgetAssignResult}
import com.cleverhouse.spendless.budget.services.BudgetCreateService.{BudgetCreateRequest, BudgetCreateResult}
import com.cleverhouse.spendless.budget.services.BudgetDeassignService.{BudgetDeassignRequest, BudgetDeassignResult}
import com.cleverhouse.spendless.budget.services.BudgetDeleteService.BudgetDeleteResult
import com.cleverhouse.spendless.budget.services.BudgetFindService.BudgetFindResult
import com.cleverhouse.spendless.budget.services.BudgetFindService.BudgetFindResult.BudgetDoNotExist
import com.cleverhouse.spendless.budget.services.BudgetListService.BudgetListResult
import com.cleverhouse.spendless.budget.services.BudgetListService.BudgetListResult.Ok
import com.cleverhouse.spendless.budget.services.BudgetUpdateService.{BudgetUpdateRequest, BudgetUpdateResult}
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{JavaUUID, concat, delete, entity, get, patch, pathEndOrSingleSlash, pathPrefix, post}
import org.apache.pekko.http.scaladsl.server.{Directives, Route}
import com.cleverhouse.spendless.utils.http.CompleteDirectives.complete
import com.cleverhouse.spendless.utils.json.JsonProtocol
import org.apache.pekko.http.scaladsl.server.directives.MarshallingDirectives.*

class BudgetRouter(
  budgetCreateService: BudgetCreateService,
  budgetDeleteService: BudgetDeleteService,
  budgetFindService: BudgetFindService,
  budgetListService: BudgetListService,
  budgetUpdateService: BudgetUpdateService,
  budgetAssignService: BudgetAssignService,
  budgetDeassignService: BudgetDeassignService
)(implicit runtime: IORuntime) extends JsonProtocol {

  import BudgetRouter._

  def routes(authContext: AuthContext): Route = pathPrefix(budgetsPath) {
    concat(
      (get & pathEndOrSingleSlash) {
        complete(budgetListService.list(authContext)) {
          case BudgetListResult.Ok(budgets) => StatusCodes.OK -> budgets
        }
      },
      (post & entity(as[BudgetCreateRequest]) & pathEndOrSingleSlash) { request =>
        complete(budgetCreateService.create(authContext, request)) {
          case BudgetCreateResult.Ok(budget) => StatusCodes.OK -> budget
        }
      },
      pathPrefix(JavaUUID) { id =>
        concat(
          (get & pathEndOrSingleSlash) {
            complete(budgetFindService.find(authContext, BudgetId(id))) {
              case BudgetFindResult.Ok(budget)            => StatusCodes.OK -> budget
              case BudgetFindResult.BudgetDoNotExist      => StatusCodes.NotFound
              case BudgetFindResult.OperationNotPermitted => StatusCodes.Forbidden
            }
          },
          (patch & entity(as[BudgetUpdateRequest]) & pathEndOrSingleSlash) { request =>
            complete(budgetUpdateService.update(authContext, BudgetId(id), request)) {
              case BudgetUpdateResult.Ok(budget)            => StatusCodes.OK -> budget
              case BudgetUpdateResult.BudgetDoNotExist      => StatusCodes.NotFound
              case BudgetUpdateResult.OperationNotPermitted => StatusCodes.Forbidden
            }
          },
          (delete & pathEndOrSingleSlash) {
            complete(budgetDeleteService.delete(authContext, BudgetId(id))) {
              case BudgetDeleteResult.Ok                    => StatusCodes.OK
              case BudgetDeleteResult.BudgetDoNotExist      => StatusCodes.NotFound
              case BudgetDeleteResult.OperationNotPermitted => StatusCodes.Forbidden
            }
          },
          (post & pathPrefix(assignPath) & pathEndOrSingleSlash & entity(as[BudgetAssignRequest])) { request =>
            complete(budgetAssignService.assign(authContext, BudgetId(id), request)) {
              case BudgetAssignResult.Assigned => StatusCodes.OK
              case BudgetAssignResult.BudgetDoNotExist => StatusCodes.NotFound
              case BudgetAssignResult.OperationNotPermitted => StatusCodes.Forbidden
              case BudgetAssignResult.UserNotFound => StatusCodes.NotFound
              case BudgetAssignResult.UserAlreadyAssigned => StatusCodes.Conflict
            }
          },
          (post & pathPrefix(deassignPath) & pathEndOrSingleSlash & entity(as[BudgetDeassignRequest])) { request =>
            complete(budgetDeassignService.assign(authContext, BudgetId(id), request)) {
              case BudgetDeassignResult.Deassigned => StatusCodes.OK
              case BudgetDeassignResult.BudgetDoNotExist => StatusCodes.NotFound
              case BudgetDeassignResult.OperationNotPermitted => StatusCodes.Forbidden
              case BudgetDeassignResult.UserNotFound => StatusCodes.NotFound
              case BudgetDeassignResult.UserNotAssigned => StatusCodes.BadRequest
            }
          }
        )
      }
    )
  }
}

object BudgetRouter {
  private val budgetsPath = "budgets"
  private val assignPath = "assign"
  private val deassignPath = "deassign"
}
