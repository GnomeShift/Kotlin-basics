package com.gnomeshift.security

import com.auth0.jwt.JWT
import com.gnomeshift.dao.Result
import com.gnomeshift.dao.UserDAO
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory
import java.security.Principal

private val logger = LoggerFactory.getLogger("Auth")

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
                    when (val userResult = UserDAO.getById(userId)) {
                        is Result.Success -> {
                            val user = userResult.data

                            if (user.username == username) {
                                UserIdPrincipal(userId, username)
                            }
                            else {
                                logger.warn("Detected username mismatch for id: $userId. Token username: $username, DB username: ${user.username}")
                                null
                            }
                        }
                        is Result.Error -> {
                            logger.error("Error getting user by ID $userId during token validation.", userResult.exception)
                            null
                        }
                    }
                }
                else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Token is invalid or expired."))
            }
        }
    }
}