package com.cleverhouse.spendless.auth.domain

import com.cleverhouse.spendless.auth.domain.AuthDomain.PasswordHash
import com.cleverhouse.spendless.user.domain.UserDomain.UserId

case class UserPassword(userId: UserId, password: PasswordHash)
