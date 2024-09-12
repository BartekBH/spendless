package com.cleverhouse.spendless.auth.services

import org.mindrot.jbcrypt.BCrypt

class PasswordService {

  def encrypt(password: String): String =
    hashPassword(password)

  def check(plain: String, hash: String): Boolean =
    checkPassword(plain, hash)

  private def hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt(12))

  private def checkPassword(plain: String, hashed: String): Boolean = BCrypt.checkpw(plain, hashed)
}
