package com.cleverhouse.spendless.auth.routers

import org.apache.pekko
import pekko.http.scaladsl.model.{HttpMethods, StatusCodes}
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.auth.domain.AuthDomain.PasswordPlain
import com.cleverhouse.spendless.auth.services.{LoginByPasswordService, PasswordSetService}
import com.cleverhouse.spendless.auth.services.PasswordSetService.{PasswordSetRequest, PasswordSetResponse}
import com.cleverhouse.spendless.user.domain.UserDomain.UserEmail
import com.cleverhouse.spendless.utils.RouteSpec
import org.apache.pekko.http.scaladsl.model.StatusCodes.*
import org.easymock.EasyMock.{expect, replay, verify}
import org.scalatestplus.easymock.EasyMockSugar.mock
import pl.iterators.kebs.scalacheck.AllGenerators

class PatchAuthPasswordRouteSpec extends RouteSpec {

  val routePath = "/auth/password"
  val routeMethod = "PATCH"
  lazy val TestRequest = new RequestBuilder(HttpMethods.getForKeyCaseInsensitive(routeMethod).get)

  val configGenerator: AllGenerators[LoginByPasswordService.Config] = allGenerators[LoginByPasswordService.Config]

  trait NoLoggedTestCase extends BaseScope {
    val authContextForTest: Option[AuthContext] = None
  }

  trait LoggedTestCase extends BaseScope {
    val authContextForTest: Option[AuthContext] = Some(allGenerators[AuthContext].normal.generate)

    override lazy val passwordSetService: PasswordSetService = mock[PasswordSetService]
  }

  "PATCH /auth/password" should {
    "be inaccessible for no logged users" in new NoLoggedTestCase {
      TestRequest(routePath) ~> allRoutes ~> check {
        response.status shouldEqual Unauthorized
      }
    }

      "returns BadRequest when incoming request is not json" in new LoggedTestCase {
        TestRequest(routePath, emptyString)  ~> allRoutes ~> check {
          response.status shouldEqual BadRequest
        }
      }

      "returns BadRequest when incoming request is empty json" in new LoggedTestCase {
        TestRequest(routePath, emptyJson) ~> allRoutes ~> check {
          response.status shouldEqual BadRequest
        }
      }

      "returns BadRequest when service returns InvalidPassword" in new LoggedTestCase {
        val requestObject = allGenerators[PasswordSetRequest].normal.generate

        expect(passwordSetService.passwordSet(authContextForTest.get, requestObject))
          .andReturn(io(PasswordSetResponse.InvalidPassword)).once()
        replay(passwordSetService)

        TestRequest(routePath, requestObject) ~> allRoutes ~> check {
          response.status shouldEqual BadRequest

          verify(passwordSetService)
        }
      }

      "returns Ok when service returns Ok" in new LoggedTestCase {

        val requestObject = allGenerators[PasswordSetRequest].normal.generate

        expect(passwordSetService.passwordSet(authContextForTest.get, requestObject))
          .andReturn(io(PasswordSetResponse.Ok)).once()
        replay(passwordSetService)

        TestRequest(routePath, requestObject) ~> allRoutes ~> check {
          response.status shouldEqual OK

          verify(passwordSetService)
        }
      }

    }
}
