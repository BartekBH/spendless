package com.cleverhouse.spendless.utils.json

import com.github.pjfanning.pekkohttpcirce.*
import io.circe.{Decoder, Encoder}
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.services.UserCreateService.UserCreateRequest
import com.cleverhouse.spendless.user.services.UserUpdateService.UserUpdateRequest

trait JsonProtocol extends ErrorAccumulatingCirceSupport {
  import io.circe.generic.semiauto._
  implicit val userDecoder: Decoder[User] = deriveDecoder
  implicit val userEncoder: Encoder[User] = deriveEncoder
  implicit val userSeqDecoder: Decoder[Seq[User]] = Decoder.decodeSeq
  implicit val userSeqEncoder: Encoder[Seq[User]] = Encoder.encodeSeq
  implicit val createUserRequestDecoder: Decoder[UserCreateRequest] = deriveDecoder
  implicit val createUserRequestEncoder: Encoder[UserCreateRequest] = deriveEncoder
  implicit val userUpdateRequestDecoder: Decoder[UserUpdateRequest] = deriveDecoder
  implicit val userUpdateRequestEncoder: Encoder[UserUpdateRequest] = deriveEncoder
}
