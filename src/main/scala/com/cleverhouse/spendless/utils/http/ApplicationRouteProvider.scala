package com.cleverhouse.spendless.utils.http

import org.apache.pekko.http.scaladsl.server.directives.RouteDirectives
import org.apache.pekko.http.scaladsl.server.{Directives, Route, RouteConcatenation}
import com.cleverhouse.spendless.auth.domain.AuthContext

trait ApplicationRouteProvider extends RouteProvider with AuthRouteProvider with Directives {
  override def route: Route = RouteDirectives.reject

  override def route(auth: AuthContext): Route = RouteDirectives.reject
}

trait RouteProvider extends RouteConcatenation {
  def route: Route
}

trait AuthRouteProvider extends RouteConcatenation {
  def route(auth: AuthContext): Route
}

