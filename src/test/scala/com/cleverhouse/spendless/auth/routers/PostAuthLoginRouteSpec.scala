package com.cleverhouse.spendless.auth.routers

import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.auth.services.LoginByPasswordService
import com.cleverhouse.spendless.auth.services.LoginByPasswordService.{LoginRequest, LoginResponse, LoginResponseData}
import com.cleverhouse.spendless.utils.RouteSpec
import io.circe.Json
import org.apache.pekko.http.scaladsl.model.HttpMethods
import org.apache.pekko.http.scaladsl.model.StatusCodes.*
import org.easymock.EasyMock.{expect, replay, verify}
import org.scalatestplus.easymock.EasyMockSugar.mock

class PostAuthLoginRouteSpec extends RouteSpec {

  val routePath = "/auth/login"
  val routeMethod = "POST"
  lazy val TestRequest = new RequestBuilder(HttpMethods.getForKeyCaseInsensitive(routeMethod).get)

  trait TestCase extends BaseScope {
    override val authContextForTest: Option[AuthContext] =  Some(allGenerators[AuthContext].normal.generate)

    override lazy val loginByPasswordService: LoginByPasswordService = mock[LoginByPasswordService]
  }


  "Login user" should {
    "returns BadRequest when incoming request is not json" in new TestCase {
      Post(routePath, emptyString) ~> allRoutes ~> check {
        response.status shouldEqual BadRequest
      }
    }

    "returns BadRequest when incoming request is empty json" in new TestCase {
      TestRequest(routePath, emptyJson) ~> allRoutes ~> check {
        response.status shouldEqual BadRequest
      }
    }

    "returns Unauthorized when service returns UserOrPasswordNotFound" in new TestCase {
      val requestObject = allGenerators[LoginRequest].normal.generate

      expect(loginByPasswordService.login(requestObject))
        .andReturn(io(LoginResponse.UserOrPasswordNotFound)).once()
      replay(loginByPasswordService)

      TestRequest(routePath, requestObject) ~> allRoutes ~> check {
        response.status shouldEqual Unauthorized

        verify(loginByPasswordService)
      }
    }

    "returns Unauthorized when service returns InvalidCredentials" in new TestCase {
      val requestObject = allGenerators[LoginRequest].normal.generate

      expect(loginByPasswordService.login(requestObject))
        .andReturn(io(LoginResponse.InvalidCredentials)).once()
      replay(loginByPasswordService)

      TestRequest(routePath, requestObject) ~> allRoutes ~> check {
        response.status shouldEqual Unauthorized

        verify(loginByPasswordService)
      }
    }

    "returns Ok when service returns Ok" in new TestCase {
      val requestObject = allGenerators[LoginRequest].normal.generate
      val responseObject = allGenerators[LoginResponseData].normal.generate

      expect(loginByPasswordService.login(requestObject))
        .andReturn(io(LoginResponse.Ok(responseObject))).once()
      replay(loginByPasswordService)

      TestRequest(routePath, requestObject) ~> allRoutes ~> check {
        response.status shouldEqual OK
        responseAs[Json] shouldEqual loginResponseDataRequestEncoder(responseObject)

        verify(loginByPasswordService)
      }
    }

  }
}
