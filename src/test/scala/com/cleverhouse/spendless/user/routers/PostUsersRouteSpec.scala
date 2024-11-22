package com.cleverhouse.spendless.user.routers

import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.services.UserCreateService
import com.cleverhouse.spendless.user.services.UserCreateService.{UserCreateRequest, UserCreateResult}
import com.cleverhouse.spendless.utils.RouteSpec
import io.circe.Json
import org.apache.pekko.http.scaladsl.model.HttpMethods
import org.apache.pekko.http.scaladsl.model.StatusCodes.*
import org.easymock.EasyMock.{expect, replay, verify}
import org.scalatestplus.easymock.EasyMockSugar.mock

class PostUsersRouteSpec extends RouteSpec {

  val routePath = s"/users/"
  val routeMethod = "POST"
  lazy val TestRequest = new RequestBuilder(HttpMethods.getForKeyCaseInsensitive(routeMethod).get)

  trait NoLoggedTestCase extends BaseScope {
    override val authContextForTest: Option[AuthContext] = None

    override lazy val userCreateService: UserCreateService = mock[UserCreateService]
  }
  
  "POST /users" should {
    "should be inaccessible for no logged users" in new NoLoggedTestCase {
      TestRequest(routePath, emptyString) ~> allRoutes ~> check {
        response.status shouldEqual Unauthorized
      }
    }
    "returns Conflict for nonlogged user if service returns EmailAlreadyExist" in new NoLoggedTestCase {
      val request = allGenerators[UserCreateRequest].normal.generate
      
      expect(userCreateService.create(request))
        .andReturn(io(UserCreateResult.EmailAlreadyExist))
      replay(userCreateService)

      TestRequest(routePath, request) ~> allRoutes ~> check {
        response.status shouldEqual Conflict

        verify(userCreateService)
      }
    }
    "returns BadRequest for nonlogged user if service returns EmailAlreadyExist" in new NoLoggedTestCase {
      val request = allGenerators[UserCreateRequest].normal.generate

      expect(userCreateService.create(request))
        .andReturn(io(UserCreateResult.InvalidPassword))
      replay(userCreateService)

      TestRequest(routePath, request) ~> allRoutes ~> check {
        response.status shouldEqual BadRequest

        verify(userCreateService)
      }
    }
    "returns OK for nonlogged user otherwise" in new NoLoggedTestCase {
      val request = allGenerators[UserCreateRequest].normal.generate
      val responseData = allGenerators[User].maximal.generate

      expect(userCreateService.create(request))
        .andReturn(io(UserCreateResult.Ok(responseData)))
      replay(userCreateService)

      TestRequest(routePath, request) ~> allRoutes ~> check {
        response.status shouldEqual OK
        responseAs[Json] shouldEqual userEncoder(responseData)

        verify(userCreateService)
      }
    }
    
  }

}
