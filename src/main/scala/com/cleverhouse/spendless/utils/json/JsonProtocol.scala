package com.cleverhouse.spendless.utils.json

import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.services.BudgetCreateService.BudgetCreateRequest
import com.cleverhouse.spendless.budget.services.BudgetUpdateService.BudgetUpdateRequest
import com.github.pjfanning.pekkohttpcirce.*
import io.circe.{Decoder, Encoder}
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.services.UserCreateService.UserCreateRequest
import com.cleverhouse.spendless.user.services.UserUpdateService.UserUpdateRequest

trait JsonProtocol extends ErrorAccumulatingCirceSupport {
  import io.circe.generic.semiauto._
  
  // User
  implicit val userDecoder: Decoder[User] = deriveDecoder
  implicit val userEncoder: Encoder[User] = deriveEncoder
  implicit val userSeqDecoder: Decoder[Seq[User]] = Decoder.decodeSeq
  implicit val userSeqEncoder: Encoder[Seq[User]] = Encoder.encodeSeq
  implicit val userCreateRequestDecoder: Decoder[UserCreateRequest] = deriveDecoder
  implicit val userCreateRequestEncoder: Encoder[UserCreateRequest] = deriveEncoder
  implicit val userUpdateRequestDecoder: Decoder[UserUpdateRequest] = deriveDecoder
  implicit val userUpdateRequestEncoder: Encoder[UserUpdateRequest] = deriveEncoder
  
  // Budget
  implicit val budgetDecoder: Decoder[Budget] = deriveDecoder
  implicit val budgetEncoder: Encoder[Budget] = deriveEncoder
  implicit val budgetSeqDecoder: Decoder[Seq[Budget]] = Decoder.decodeSeq
  implicit val budgetSeqEncoder: Encoder[Seq[Budget]] = Encoder.encodeSeq
  implicit val budgetCreateRequestDecoder: Decoder[BudgetCreateRequest] = deriveDecoder
  implicit val budgetCreateRequestEncoder: Encoder[BudgetCreateRequest] = deriveEncoder
  implicit val budgetUpdateRequestDecoder: Decoder[BudgetUpdateRequest] = deriveDecoder
  implicit val budgetUpdateRequestEncoder: Encoder[UserUpdateRequest] = deriveEncoder
}
