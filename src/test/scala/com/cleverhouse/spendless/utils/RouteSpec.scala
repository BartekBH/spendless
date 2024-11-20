package com.cleverhouse.spendless.utils

import cats.effect.IO
import cats.effect.testing.specs2.CatsEffect
import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.auth.routers.UserAuthenticator
import com.cleverhouse.spendless.main.ApplicationLoader
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.json.JsonProtocol
import org.apache.pekko.http.scaladsl.marshalling.PredefinedToEntityMarshallers
import org.apache.pekko.http.scaladsl.model.headers.{HttpChallenge, HttpCredentials}
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.server.directives.AuthenticationResult
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.easymock.EasyMockSugar.mock
import pl.iterators.kebs.scalacheck.KebsScalacheckGenerators

import scala.concurrent.{ExecutionContext, Future}

trait RouteSpec
  extends AnyWordSpec
    with ScalatestRouteTest
    with Matchers
    with JsonProtocol
    with CatsEffect
    with KebsScalacheckGenerators
    with PredefinedToEntityMarshallers {

  val emptyString = ""
  val emptyJson   = "{}"
  
  def io[T](value: T): IO[T] = IO.pure(value)

  private lazy val _runtime: IORuntime               = IORuntime.global
  private lazy val _executor: ExecutionContext       = _runtime.compute
  private lazy val _transactor: PostgresIOTransactor = mock[PostgresIOTransactor]
  
  abstract class BaseScope extends ApplicationLoader(testConfig, _executor, _runtime, _transactor) {
    lazy val allRoutes: Route = Route.seal(routes)

    override lazy val userAuthenticator: UserAuthenticator = (_: Option[HttpCredentials]) =>
      Future.successful(
        authContextForTest
          .map(AuthenticationResult.success)
          .getOrElse(AuthenticationResult.failWithChallenge(HttpChallenge(scheme = "Bearer", realm = "realm")))
      )

    val authContextForTest: Option[AuthContext]
  }

}