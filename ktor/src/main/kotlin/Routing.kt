package com.gnomeshift

import com.gnomeshift.dao.ProductDAO
import com.gnomeshift.entities.User
import com.gnomeshift.dao.Result
import com.gnomeshift.dao.UserDAO
import com.gnomeshift.dto.ProductRequest
import com.gnomeshift.dto.UserRequest
import com.gnomeshift.entities.Product
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

suspend fun <T> respondResult(call: ApplicationCall, result: Result<T>) {
    when (result) {
        is Result.Success -> {
            val status = when (call.request.httpMethod) {
                HttpMethod.Post -> HttpStatusCode.Created
                HttpMethod.Delete -> HttpStatusCode.NoContent
                else -> HttpStatusCode.OK
            }

            if (result.data is Unit) {
                call.respond(status)
            }
            else {
                call.respond(status, result.data!!)
            }
        }
        is Result.Error -> {
            call.respond(HttpStatusCode.InternalServerError, result.exception)
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
        route("/users") {
            get {
                respondResult(call, UserDAO.getAll())
            }
            post {
                val newUser = call.receive<UserRequest>()
                respondResult(call, UserDAO.create(newUser))
            }
            route("/{id}") {
                get {
                    val userId = call.parameters["id"]?.toIntOrNull()

                    if (userId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid user id")
                        return@get
                    }
                    respondResult(call, UserDAO.getById(userId))
                }
                put {
                    val userId = call.parameters["id"]?.toIntOrNull()

                    if (userId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid user id")
                        return@put
                    }

                    val updatedUser = call.receive<User>()
                    respondResult(call, UserDAO.update(updatedUser))
                }
                delete {
                    val userId = call.parameters["id"]?.toIntOrNull()

                    if (userId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid user id")
                        return@delete
                    }
                    respondResult(call, UserDAO.delete(userId))
                }
            }
        }
        route("/products") {
            get {
                respondResult(call, ProductDAO.getAll())
            }
            post {
                val newProduct = call.receive<ProductRequest>()
                respondResult(call, ProductDAO.create(newProduct))
            }
            route("/{id}") {
                get {
                    val productId = call.parameters["id"]?.toIntOrNull()

                    if (productId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid product id")
                        return@get
                    }
                    respondResult(call, ProductDAO.getById(productId))
                }
                put {
                    val productId = call.parameters["id"]?.toIntOrNull()

                    if (productId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid product id")
                        return@put
                    }

                    val updatedProduct = call.receive<Product>()
                    respondResult(call, ProductDAO.update(updatedProduct))
                }
                delete {
                    val productId = call.parameters["id"]?.toIntOrNull()

                    if (productId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid product id")
                        return@delete
                    }
                    respondResult(call, ProductDAO.delete(productId))
                }
            }
        }
    }
}
