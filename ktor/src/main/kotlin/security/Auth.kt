package com.gnomeshift.security

import com.auth0.jwt.JWT
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
    val jwtAudience = config.property("audience").getString()
    val jwtRealm = config.property("realm").getString()
    val jwtIssuer = config.property("issuer").getString()

    JwtService.init(config)

    install(Authentication) {
        jwt("jwt") {
            verifier(JWT.require(JwtService.algorithm).withAudience(jwtAudience).withIssuer(jwtIssuer).build())
            realm = jwtRealm

            validate { credential ->
                val userId = credential.payload.getClaim("userId")?.asInt()
                val username = credential.subject

                if (userId != null && username != null) {
                    val userResult = UserDAO.getById(userId)

                    if (userResult is Result.Success && userResult.data.username == username) {
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