package com.cleverhouse.spendless.user.routers

import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import com.cleverhouse.spendless.user.services.*
import com.cleverhouse.spendless.user.services.UserCreateService.{UserCreateRequest, UserCreateResult}
import com.cleverhouse.spendless.user.services.UserDeleteService.UserDeleteResult
import com.cleverhouse.spendless.user.services.UserFindService.UserFindResult
import com.cleverhouse.spendless.user.services.UserFindService.UserFindResult.UserDoNotExist
import com.cleverhouse.spendless.user.services.UserListService.UserListResult
import com.cleverhouse.spendless.user.services.UserListService.UserListResult.Ok
import com.cleverhouse.spendless.user.services.UserUpdateService.{UserUpdateRequest, UserUpdateResult}
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{JavaUUID, concat, delete, entity, get, patch, pathEndOrSingleSlash, pathPrefix, post}
import org.apache.pekko.http.scaladsl.server.{Directives, Route}
import com.cleverhouse.spendless.utils.http.CompleteDirectives.complete
import com.cleverhouse.spendless.utils.json.JsonProtocol
import org.apache.pekko.http.scaladsl.server.directives.MarshallingDirectives.*

class UserRouter(
                  userCreateService: UserCreateService,
                  userDeleteService: UserDeleteService,
                  userFindService: UserFindService,
                  userListService: UserListService,
                  userUpdateService: UserUpdateService
                )(implicit runtime: IORuntime) extends JsonProtocol {
  import UserRouter._
    
  def routes(authContext: AuthContext): Route = pathPrefix(usersPath) {
    concat(
      (get & pathEndOrSingleSlash) {
        complete(userListService.list()) {
          case UserListResult.Ok(users) => StatusCodes.OK -> users
        }
      },
      pathPrefix(JavaUUID) { id =>
        concat(
          (get & pathEndOrSingleSlash) {
            complete(userFindService.find(UserId(id))) {
              case UserFindResult.Ok(user) => StatusCodes.OK -> user
              case UserFindResult.UserDoNotExist => StatusCodes.NotFound
            }
          },
          (patch & entity(as[UserUpdateRequest]) & pathEndOrSingleSlash) { request =>
            complete(userUpdateService.update(authContext, UserId(id), request)) {
              case UserUpdateResult.Ok(user) => StatusCodes.OK -> user
              case UserUpdateResult.UserDoNotExist => StatusCodes.NotFound
              case UserUpdateResult.OperationNotPermitted => StatusCodes.Forbidden
            }
          },
          (delete & pathEndOrSingleSlash) {
            complete(userDeleteService.delete(authContext, UserId(id))) {
              case UserDeleteResult.Ok => StatusCodes.OK
              case UserDeleteResult.UserDoNotExist => StatusCodes.NotFound
              case UserDeleteResult.OperationNotPermitted => StatusCodes.Forbidden
            }
          }
        )
      }
    )
  }

  def routes: Route = pathPrefix(usersPath) {
    (post & entity(as[UserCreateRequest]) & pathEndOrSingleSlash) { request =>
      complete(userCreateService.create(request)) {
        case UserCreateResult.Ok(user) => StatusCodes.OK -> user
        case UserCreateResult.EmailAlreadyExist => StatusCodes.Forbidden
        case UserCreateResult.InvalidPassword => StatusCodes.BadRequest
      }
    }
  }
  
}

object UserRouter {
  private val usersPath = "users"
}
