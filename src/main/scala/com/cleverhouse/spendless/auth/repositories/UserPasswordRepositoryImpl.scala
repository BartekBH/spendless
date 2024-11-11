package com.cleverhouse.spendless.auth.repositories

import com.cleverhouse.spendless.auth.domain.UserPassword
import com.cleverhouse.spendless.auth.repositories.tables.UserPasswordTable
import com.cleverhouse.spendless.user.domain.UserDomain.UserId
import slick.dbio.DBIO
import slick.lifted.TableQuery
import slick.jdbc.PostgresProfile.api.*

import java.util.UUID
import scala.concurrent.ExecutionContext

class UserPasswordRepositoryImpl(implicit e: ExecutionContext) extends UserPasswordRepository {
  import com.cleverhouse.spendless.user.repositories.tables.UserTable._
  
  override def upsert(userPassword: UserPassword): DBIO[UserPassword] =
    table.insertOrUpdate(userPassword).map(_ => userPassword)

  override def find(userId: UserId): DBIO[Option[UserPassword]] =
    table.filter(_.userId === userId).result.map(_.headOption)

  private lazy val table: TableQuery[UserPasswordTable] = TableQuery[UserPasswordTable]
}
