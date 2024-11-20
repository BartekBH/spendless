package com.cleverhouse.spendless.utils.http

import com.cleverhouse.spendless.utils.json.JsonProtocol
import com.cleverhouse.spendless.utils.log.Logging
import org.apache.pekko.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError}
import org.apache.pekko.http.scaladsl.server.Directives.complete
import org.apache.pekko.http.scaladsl.server.ExceptionHandler


trait Handlers extends Logging with JsonProtocol {
  implicit val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: IllegalArgumentException =>
      complete(BadRequest -> e)
    case e: Exception =>
      complete(InternalServerError -> e)
  }
}