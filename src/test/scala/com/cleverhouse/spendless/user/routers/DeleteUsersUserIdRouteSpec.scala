package com.cleverhouse.spendless.user.routers

import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import com.cleverhouse.spendless.user.services.UserDeleteService
import com.cleverhouse.spendless.user.services.UserDeleteService.UserDeleteResult
import com.cleverhouse.spendless.utils.RouteSpec
import org.apache.pekko.http.scaladsl.model.HttpMethods
import org.apache.pekko.http.scaladsl.model.StatusCodes.*
import org.easymock.EasyMock.{expect, replay, verify}
import org.scalatestplus.easymock.EasyMockSugar.mock

import java.util.UUID

class DeleteUsersUserIdRouteSpec extends RouteSpec {

  val userId: UserId   = UserId(UUID.randomUUID())
  val routePath        = s"/users/$userId"
  val routeMethod      = "DELETE"
  lazy val TestRequest = new RequestBuilder(HttpMethods.getForKeyCaseInsensitive(routeMethod).get)

  trait NoLoggedTestCase extends BaseScope {
    override val authContextForTest: Option[AuthContext] = None
  }

  trait LoggedTestCase extends BaseScope {
    override val authContextForTest: Option[AuthContext] = Some(allGenerators[AuthContext].normal.generate)

    override lazy val userDeleteService: UserDeleteService = mock[UserDeleteService]
  }

  "DELETE /users/{userId}" should {
    "should be inaccessible for no logged users" in new NoLoggedTestCase {
      TestRequest(routePath, emptyString) ~> allRoutes ~> check {
        response.status shouldEqual Unauthorized
      }
    }
    "returns Forbidden when service returns OperationNotPermitted" in new LoggedTestCase {
      expect(userDeleteService.delete(authContextForTest.get, userId))
        .andReturn(io(UserDeleteResult.OperationNotPermitted)).once()
      replay(userDeleteService)

      TestRequest(routePath, emptyJson) ~> allRoutes ~> check {
        response.status shouldEqual Forbidden

        verify(userDeleteService)
      }
    }
    "returns NotFound when service returns UserDoNotExist" in new LoggedTestCase {
      expect(userDeleteService.delete(authContextForTest.get, userId))
        .andReturn(io(UserDeleteResult.UserDoNotExist)).once()
      replay(userDeleteService)

      TestRequest(routePath, emptyJson) ~> allRoutes ~> check {
        response.status shouldEqual NotFound

        verify(userDeleteService)
      }
    }
    "returns OK for logged user if service returns Ok" in new LoggedTestCase {
      expect(userDeleteService.delete(authContextForTest.get, userId))
        .andReturn(io(UserDeleteResult.Ok)).once()
      replay(userDeleteService)

      TestRequest(routePath, emptyJson) ~> allRoutes ~> check {
        response.status shouldEqual OK

        verify(userDeleteService)
      }
    }
  }

}
