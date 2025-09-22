package com.gnomeshift.security

import com.auth0.jwk.JwkProviderBuilder
import com.gnomeshift.dao.Result
import com.gnomeshift.dao.UserDAO
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import java.security.Principal

data class UserIdPrincipal(val userId: Int, val username: String) : Principal {
    override fun getName(): String = username
}

fun Application.configureAuthentication() {
    val config = environment.config.config("jwt")
    val realm = config.property("realm").getString()
    val issuer = config.property("issuer").getString()

    JwtService.init(config)

    install(Authentication) {
        jwt("jwt") {
            this.realm = realm
            verifier(JwkProviderBuilder(issuer).build())

            validate { credential ->
                val userId = credential.payload.getClaim("userId")?.asInt()
                val username = credential.payload.getClaim("username")?.asString()

                println("user id: $userId")
                println("username: $username")

                if (userId != null && username != null) {
                    val userResult = UserDAO.getById(userId)

                    if (userResult is Result.Success && userResult.data.name == username) {
                         UserIdPrincipal(userId, username)
                     }
                     else {
                         null
                     }
                }
                else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Token is invalid or expired.")
                )
            }
        }
    }
}