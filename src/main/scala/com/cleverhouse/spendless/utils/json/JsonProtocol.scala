package com.cleverhouse.spendless.utils.json

import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.auth.domain.AuthDomain.*
import com.cleverhouse.spendless.auth.services.LoginByPasswordService.{LoginRequest, LoginResponseData}
import com.cleverhouse.spendless.auth.services.PasswordSetService.PasswordSetRequest
import com.cleverhouse.spendless.budget.domain.Budget
import com.cleverhouse.spendless.budget.domain.BudgetDomain.*
import com.cleverhouse.spendless.budget.services.BudgetAssignService.BudgetAssignRequest
import com.cleverhouse.spendless.budget.services.BudgetCreateService.BudgetCreateRequest
import com.cleverhouse.spendless.budget.services.BudgetDeassignService.BudgetDeassignRequest
import com.cleverhouse.spendless.budget.services.BudgetUpdateService.BudgetUpdateRequest
import com.github.pjfanning.pekkohttpcirce.*
import io.circe.{Decoder, Encoder}
import com.cleverhouse.spendless.user.domain.User
import com.cleverhouse.spendless.user.domain.UserDomain.*
import com.cleverhouse.spendless.user.services.UserCreateService.UserCreateRequest
import com.cleverhouse.spendless.user.services.UserUpdateService.UserUpdateRequest

import java.time.Instant
import java.util.UUID


trait JsonProtocol extends ErrorAccumulatingCirceSupport {
  import io.circe.generic.semiauto._

  // Auth
  implicit val authDecoder: Decoder[AuthContext] = deriveDecoder
  implicit val authEncoder: Encoder[AuthContext] = deriveEncoder
  // AuthRequest
  implicit val passwordPlainDecoder: Decoder[PasswordPlain] = Decoder.decodeString.map(PasswordPlain.apply)
  implicit val passwordPlainEncoder: Encoder[PasswordPlain] = Encoder.encodeString.contramap(_.unwrap)
  implicit val loginRequestDecoder: Decoder[LoginRequest] = deriveDecoder
  implicit val loginRequestEncoder: Encoder[LoginRequest] = deriveEncoder
  implicit val passwordSetRequestDecoder: Decoder[PasswordSetRequest] = deriveDecoder
  implicit val passwordSetRequestEncoder: Encoder[PasswordSetRequest] = deriveEncoder
  // AuthResponse
  implicit val jwtDecoder: Decoder[Jwt] = Decoder.decodeString.map(Jwt.apply)
  implicit val jwtEncoder: Encoder[Jwt] = Encoder.encodeString.contramap(_.unwrap)
  implicit val loginResponseDataRequestDecoder: Decoder[LoginResponseData] = deriveDecoder
  implicit val loginResponseDataRequestEncoder: Encoder[LoginResponseData] = deriveEncoder

  // User
  implicit val userIdDecoder: Decoder[UserId] = Decoder.decodeUUID.map(UserId.apply)
  implicit val userIdEncoder: Encoder[UserId] = Encoder.encodeUUID.contramap(_.unwrap)
  implicit val userEmailDecoder: Decoder[UserEmail] = Decoder.decodeString.map(UserEmail.apply)
  implicit val userEmailEncoder: Encoder[UserEmail] = Encoder.encodeString.contramap(_.unwrap)
  implicit val userNameDecoder: Decoder[UserName] = Decoder.decodeString.map(UserName.apply)
  implicit val userNameEncoder: Encoder[UserName] = Encoder.encodeString.contramap(_.unwrap)
  implicit val userCreatedAtDecoder: Decoder[UserCreatedAt] = Decoder.decodeInstant.map(UserCreatedAt.apply)
  implicit val userCreatedAtEncoder: Encoder[UserCreatedAt] = Encoder.encodeInstant.contramap(_.unwrap)
  implicit val userModifiedAtDecoder: Decoder[UserModifiedAt] = Decoder.decodeInstant.map(UserModifiedAt.apply)
  implicit val userModifiedAtEncoder: Encoder[UserModifiedAt] = Encoder.encodeInstant.contramap(_.unwrap)
  implicit val userDecoder: Decoder[User] = deriveDecoder
  implicit val userEncoder: Encoder[User] = deriveEncoder
  implicit val userSeqDecoder: Decoder[Seq[User]] = Decoder.decodeSeq
  implicit val userSeqEncoder: Encoder[Seq[User]] = Encoder.encodeSeq
  // UserRequest
  implicit val userCreateRequestDecoder: Decoder[UserCreateRequest] = deriveDecoder
  implicit val userCreateRequestEncoder: Encoder[UserCreateRequest] = deriveEncoder
  implicit val userUpdateRequestDecoder: Decoder[UserUpdateRequest] = deriveDecoder
  implicit val userUpdateRequestEncoder: Encoder[UserUpdateRequest] = deriveEncoder

  // Budget
  implicit val budgetIdDecoder: Decoder[BudgetId] = Decoder.decodeUUID.map(BudgetId.apply)
  implicit val budgetIdEncoder: Encoder[BudgetId] = Encoder.encodeUUID.contramap(_.unwrap)
  implicit val budgetNameDecoder: Decoder[BudgetName] = Decoder.decodeString.map(BudgetName.apply)
  implicit val budgetNameEncoder: Encoder[BudgetName] = Encoder.encodeString.contramap(_.unwrap)
  implicit val budgetCreatedAtDecoder: Decoder[BudgetCreatedAt] = Decoder.decodeInstant.map(BudgetCreatedAt.apply)
  implicit val budgetCreatedAtEncoder: Encoder[BudgetCreatedAt] = Encoder.encodeInstant.contramap(_.unwrap)
  implicit val budgetModifiedAtDecoder: Decoder[BudgetModifiedAt] = Decoder.decodeInstant.map(BudgetModifiedAt.apply)
  implicit val budgetModifiedAtEncoder: Encoder[BudgetModifiedAt] = Encoder.encodeInstant.contramap(_.unwrap)
  implicit val budgetDecoder: Decoder[Budget] = deriveDecoder
  implicit val budgetEncoder: Encoder[Budget] = deriveEncoder
  implicit val budgetSeqDecoder: Decoder[Seq[Budget]] = Decoder.decodeSeq
  implicit val budgetSeqEncoder: Encoder[Seq[Budget]] = Encoder.encodeSeq
  // BudgetRequest
  implicit val budgetCreateRequestDecoder: Decoder[BudgetCreateRequest] = deriveDecoder
  implicit val budgetCreateRequestEncoder: Encoder[BudgetCreateRequest] = deriveEncoder
  implicit val budgetUpdateRequestDecoder: Decoder[BudgetUpdateRequest] = deriveDecoder
  implicit val budgetUpdateRequestEncoder: Encoder[BudgetUpdateRequest] = deriveEncoder
  implicit val budgetAssignRequestDecoder: Decoder[BudgetAssignRequest] = deriveDecoder
  implicit val budgetAssignRequestEncoder: Encoder[BudgetAssignRequest] = deriveEncoder
  implicit val budgetDeassignRequestDecoder: Decoder[BudgetDeassignRequest] = deriveDecoder
  implicit val budgetDeassignRequestEncoder: Encoder[BudgetDeassignRequest] = deriveEncoder
}
