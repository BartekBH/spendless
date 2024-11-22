package com.cleverhouse.spendless.user.routers

import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import com.cleverhouse.spendless.user.services.UserFindService
import com.cleverhouse.spendless.user.services.UserFindService.UserFindResult
import com.cleverhouse.spendless.utils.RouteSpec
import io.circe.Json
import org.apache.pekko.http.scaladsl.model.HttpMethods
import org.apache.pekko.http.scaladsl.model.StatusCodes.*
import org.easymock.EasyMock.{expect, replay, verify}
import org.scalatestplus.easymock.EasyMockSugar.mock

import java.util.UUID

class GetUsersUserIdRouteSpec extends RouteSpec {

  val userId: UserId = UserId(UUID.randomUUID())
  val routePath = s"/users/$userId"
  val routeMethod = "GET"
  lazy val TestRequest = new RequestBuilder(HttpMethods.getForKeyCaseInsensitive(routeMethod).get)

  trait NoLoggedTestCase extends BaseScope {
    override val authContextForTest: Option[AuthContext] = None
  }

  trait LoggedTestCase extends BaseScope {
    override val authContextForTest: Option[AuthContext] = Some(allGenerators[AuthContext].normal.generate)

    override lazy val userFindService: UserFindService = mock[UserFindService]
  }
  
  "GET /users/{userId}" should {
    "should be inaccessible for no logged users" in new NoLoggedTestCase {
      TestRequest(routePath, emptyString) ~> allRoutes ~> check {
        response.status shouldEqual Unauthorized
      }
    }
    "returns NotFound for logged user if service returns UserDoNotExist" in new LoggedTestCase {
      expect(userFindService.find(userId))
        .andReturn(io(UserFindResult.UserDoNotExist))
      replay(userFindService)

      TestRequest(routePath, emptyString) ~> allRoutes ~> check {
        response.status shouldEqual NotFound

        verify(userFindService)
      }
    }
    "returns OK for logged user if service returns Ok" in new LoggedTestCase {
      val responseData = allGenerators[User].maximal.generate.copy(id = userId)

      expect(userFindService.find(userId))
        .andReturn(io(UserFindResult.Ok(responseData)))
      replay(userFindService)

      TestRequest(routePath, emptyString) ~> allRoutes ~> check {
        response.status shouldEqual OK
        responseAs[Json] shouldEqual userEncoder(responseData)

        verify(userFindService)
      }
    }
    
  }

}
