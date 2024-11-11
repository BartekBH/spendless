package com.cleverhouse.spendless.user

import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.auth.domain.AuthContext
import com.cleverhouse.spendless.auth.repositories.{UserPasswordRepository, UserPasswordRepositoryImpl}
import com.cleverhouse.spendless.auth.services.{PasswordService, PasswordSetService}
import com.cleverhouse.spendless.user.repositories.{UserRepository, UserRepositoryImpl}
import com.cleverhouse.spendless.user.services.*
import com.cleverhouse.spendless.user.routers.UserRouter
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.cleverhouse.spendless.utils.http.{AuthRouteProvider, RouteProvider}
import com.softwaremill.macwire.wire
import org.apache.pekko.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

trait UserModule extends AuthRouteProvider with RouteProvider {
  implicit val executor: ExecutionContext
  implicit val runtime: IORuntime

  val transactor: PostgresIOTransactor
  
  lazy val passwordSetService: PasswordSetService
  
  lazy val userRepository: UserRepository = wire[UserRepositoryImpl]

  lazy val userCreateService: UserCreateService = wire[UserCreateService]
  lazy val userDeleteService: UserDeleteService = wire[UserDeleteService]
  lazy val userFindService: UserFindService     = wire[UserFindService]
  lazy val userListService: UserListService     = wire[UserListService]
  lazy val userUpdateService: UserUpdateService = wire[UserUpdateService]

  lazy val userRouter: UserRouter = wire[UserRouter]

  abstract override def route: Route = super.route ~ userRouter.routes
  abstract override def route(auth: AuthContext): Route = super.route ~ userRouter.routes(auth)

}
