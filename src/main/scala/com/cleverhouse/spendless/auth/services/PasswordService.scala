package com.cleverhouse.spendless.auth.services

import com.cleverhouse.spendless.auth.domain.AuthDomain._
import org.mindrot.jbcrypt.BCrypt

class PasswordService {

  def encrypt(password: PasswordPlain): PasswordHash =
    PasswordHash(hashPassword(password.unwrap))

  def check(plain: PasswordPlain, hash: PasswordHash): Boolean =
    checkPassword(plain.unwrap, hash.unwrap)

  private def hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(12))

  private def checkPassword(plain: String, hashed: String): Boolean = BCrypt.checkpw(plain, hashed)
}
