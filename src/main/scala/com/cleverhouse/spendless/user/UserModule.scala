package com.cleverhouse.spendless.user

import cats.effect.unsafe.IORuntime
import com.cleverhouse.spendless.user.repositories.{UserRepository, UserRepositoryImpl}
import com.cleverhouse.spendless.user.services.*
import com.cleverhouse.spendless.user.routers.UserRouter
import com.cleverhouse.spendless.utils.db.PostgresIOTransactor
import com.softwaremill.macwire.wire
import org.apache.pekko.http.scaladsl.server.Route

import scala.concurrent.ExecutionContext

class UserModule(
  val transactor: PostgresIOTransactor,
  implicit val executor: ExecutionContext,
  implicit val runtime: IORuntime){
  
  lazy val userRepository: UserRepository = wire[UserRepositoryImpl]

  lazy val userCreateService: UserCreateService = wire[UserCreateService]
  lazy val userDeleteService: UserDeleteService = wire[UserDeleteService]
  lazy val userFindService: UserFindService = wire[UserFindService]
  lazy val userListService: UserListService = wire[UserListService]
  lazy val userUpdateService: UserUpdateService = wire[UserUpdateService]

  lazy val userRouter: UserRouter = wire[UserRouter]

  def route: Route = userRouter.route

}
