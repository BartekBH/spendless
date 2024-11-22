package com.cleverhouse.spendless.user.routers

import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import com.cleverhouse.spendless.user.services.UserUpdateService
import com.cleverhouse.spendless.user.services.UserUpdateService.{UserUpdateRequest, UserUpdateResult}
import com.cleverhouse.spendless.utils.RouteSpec
import io.circe.Json
import org.apache.pekko.http.scaladsl.model.HttpMethods
import org.apache.pekko.http.scaladsl.model.StatusCodes.*
import org.easymock.EasyMock.{expect, replay, verify}
import org.scalatestplus.easymock.EasyMockSugar.mock

import java.util.UUID

class PatchUsersUserIdRouteSpec extends RouteSpec {

  val userId: UserId = UserId(UUID.randomUUID())
  val routePath = s"/users/$userId"
  val routeMethod = "PATCH"
  lazy val TestRequest = new RequestBuilder(HttpMethods.getForKeyCaseInsensitive(routeMethod).get)

  trait NoLoggedTestCase extends BaseScope {
    override val authContextForTest: Option[AuthContext] = None
  }

  trait LoggedTestCase extends BaseScope {
    override val authContextForTest: Option[AuthContext] = Some(allGenerators[AuthContext].normal.generate)

    override lazy val userUpdateService: UserUpdateService = mock[UserUpdateService]
  }
  
  "PATCH /users/{userId}" should {
    "should be inaccessible for no logged users" in new NoLoggedTestCase {
      TestRequest(routePath, emptyString) ~> allRoutes ~> check {
        response.status shouldEqual Unauthorized
      }
    }
    "returns Forbidden for logged user if service returns OperationNotPermitted" in new LoggedTestCase {
      val request = allGenerators[UserUpdateRequest].normal.generate

      expect(userUpdateService.update(authContextForTest.get, userId, request))
        .andReturn(io(UserUpdateResult.OperationNotPermitted))
      replay(userUpdateService)

      TestRequest(routePath, request) ~> allRoutes ~> check {
        response.status shouldEqual Forbidden

        verify(userUpdateService)
      }
    }
    "returns NotFound for logged user if service returns UserDoNotExist" in new LoggedTestCase {
      val request = allGenerators[UserUpdateRequest].normal.generate

      expect(userUpdateService.update(authContextForTest.get, userId, request))
        .andReturn(io(UserUpdateResult.UserDoNotExist))
      replay(userUpdateService)

      TestRequest(routePath, request) ~> allRoutes ~> check {
        response.status shouldEqual NotFound

        verify(userUpdateService)
      }
    }
    "returns OK for logged user if service returns Ok" in new LoggedTestCase {
      val request = allGenerators[UserUpdateRequest].normal.generate
      val responseData = allGenerators[User].maximal.generate.copy(id =userId)

      expect(userUpdateService.update(authContextForTest.get, userId, request))
        .andReturn(io(UserUpdateResult.Ok(responseData)))
      replay(userUpdateService)

      TestRequest(routePath, request) ~> allRoutes ~> check {
        response.status shouldEqual OK
        responseAs[Json] shouldEqual userEncoder(responseData)

        verify(userUpdateService)
      }
    }
    
  }
}
