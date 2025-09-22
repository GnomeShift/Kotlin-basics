package com.gnomeshift

import com.gnomeshift.dao.ProductDAO
import com.gnomeshift.dao.Result
import com.gnomeshift.dao.UserDAO
import com.gnomeshift.dto.ProductRequest
import com.gnomeshift.security.JwtResponse
import com.gnomeshift.security.JwtService
import com.gnomeshift.security.LoginRequest
import com.gnomeshift.security.RegisterRequest
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mindrot.jbcrypt.BCrypt

suspend fun <T> ApplicationCall.respondResult(result: Result<T>) {
    when (result) {
        is Result.Success -> {
            val status = when (request.httpMethod) {
                HttpMethod.Post -> HttpStatusCode.Created
                HttpMethod.Delete -> HttpStatusCode.NoContent
                else -> HttpStatusCode.OK
            }

            if (result.data is Unit) {
                respond(status)
            }
            else {
                respond(status, result.data!!)
            }
        }
        is Result.Error -> {
            respond(HttpStatusCode.InternalServerError, result.exception)
        }
    }
}

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json()
    }
    install(StatusPages) {
        status(HttpStatusCode.NotFound) {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/about") {
            call.respondText("This is a Kotlin app with Ktor!")
        }
        get("/hello/{name}") {
            try {
                call.respondText("Hello, ${call.parameters["name"]}")
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "${e.message}")
            }
        }
        post("/echo") {
            call.respondText(call.receiveText())
        }
        get("/search") {
            call.respondText("Search: ${call.request.queryParameters["query"]}")
        }
        get("/ping") {
            call.respondText("pong")
        }
        post("/register") {
            val request = call.receive<RegisterRequest>()

            when (val result = UserDAO.create(request)) {
                is Result.Success -> call.respond(HttpStatusCode.Created, result.data)
                is Result.Error -> call.respondResult(result)
            }
        }
        post("/login") {
            val loginRequest = call.receive<LoginRequest>()

            when (val userResult = UserDAO.findByUsername(loginRequest.username)) {
                is Result.Success -> {
                    val user = userResult.data

                    if (BCrypt.checkpw(loginRequest.password, user.password)) {
                        val token = JwtService.generateToken(user.id, user.username)
                        call.respond(JwtResponse(token, user))
                    }
                    else {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                    }
                }
                else -> {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                }
            }
        }
        authenticate("jwt") {
            route("/users") {
                get {
                    call.respondResult(UserDAO.getAll())
                }
                route("/{id}") {
                    get {
                        val userId = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid user ID.")
                        )
                        call.respondResult(UserDAO.getById(userId))
                    }
                    put {
                        val userId = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid user ID.")
                        )

                        val updatedUser = call.receive<RegisterRequest>()
                        call.respondResult(UserDAO.update(userId, updatedUser))
                    }
                    delete {
                        val userId = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid user ID.")
                        )
                        call.respondResult(UserDAO.delete(userId))
                    }
                }
            }
            route("/products") {
                get {
                    call.respondResult(ProductDAO.getAll())
                }
                post {
                    val newProduct = call.receive<ProductRequest>()
                    call.respondResult(ProductDAO.create(newProduct))
                }
                route("/{id}") {
                    get {
                        val productId = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid product ID.")
                        )
                        call.respondResult(ProductDAO.getById(productId))
                    }
                    put {
                        val productId = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid product ID.")
                        )

                        val updatedProduct = call.receive<ProductRequest>()
                        call.respondResult(ProductDAO.update(productId, updatedProduct))
                    }
                    delete {
                        val productId = call.parameters["id"]?.toIntOrNull() ?: return@delete call.respond(
                            HttpStatusCode.BadRequest,
                            mapOf("error" to "Invalid product ID.")
                        )
                        call.respondResult(ProductDAO.delete(productId))
                    }
                }
            }
        }
    }
}
