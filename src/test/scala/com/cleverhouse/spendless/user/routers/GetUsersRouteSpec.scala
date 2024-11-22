package com.cleverhouse.spendless.user.routers

import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.services.UserListService
import com.cleverhouse.spendless.user.services.UserListService.UserListResult
import com.cleverhouse.spendless.utils.RouteSpec
import io.circe.Json
import org.apache.pekko.http.scaladsl.model.HttpMethods
import org.apache.pekko.http.scaladsl.model.StatusCodes.*
import org.easymock.EasyMock.{expect, replay, verify}
import org.scalatestplus.easymock.EasyMockSugar.mock

class GetUsersRouteSpec extends RouteSpec {

  val routePath = "/users"
  val routeMethod = "GET"
  lazy val TestRequest = new RequestBuilder(HttpMethods.getForKeyCaseInsensitive(routeMethod).get)

  trait NoLoggedTestCase extends BaseScope {
    override val authContextForTest: Option[AuthContext] = None
  }

  trait LoggedTestCase extends BaseScope {
    override val authContextForTest: Option[AuthContext] = Some(allGenerators[AuthContext].normal.generate)

    override lazy val userListService: UserListService = mock[UserListService]
  }

  "GET /users" should {
    "should be inaccessible for no logged users" in new NoLoggedTestCase {
      TestRequest(routePath, emptyString) ~> allRoutes ~> check {
        response.status shouldEqual Unauthorized
      }
    }
    "returns OK with proper json when service returns OK" in new LoggedTestCase {
      val responseData = (1 to 10).map(_ => allGenerators[User].maximal.generate)

      expect(userListService.list())
        .andReturn(io(UserListResult.Ok(responseData))).once()
      replay(userListService)

      TestRequest(routePath, emptyString) ~> allRoutes ~> check {
        response.status shouldEqual OK
        responseAs[Json] shouldEqual userSeqEncoder(responseData)

        verify(userListService)
      }
    }

  }
}
