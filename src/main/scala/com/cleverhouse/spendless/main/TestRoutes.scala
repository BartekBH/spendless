package com.cleverhouse.spendless.main

import org.apache.pekko.http.scaladsl.model.{ContentTypes, HttpEntity}
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.server.Directives.{complete, get, pathEndOrSingleSlash}

case class TestRoutes() {
  def route: Route = (get & pathEndOrSingleSlash) {
      complete(
        HttpEntity(
          ContentTypes.`text/html(UTF-8)`,
          "Hello world!"
        )
      )
    }
}
