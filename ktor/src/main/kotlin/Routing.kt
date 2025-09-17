package com.gnomeshift

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
        get("/product/{id}") {
            try {
                call.respond(Product(call.parameters["id"]!!.toInt(), "Яблоко"))
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "${e.message}")
            }
        }
        post("/user") {
            try {
                val user = call.receive<UserRequest>()
                call.respond(HttpStatusCode.Created, user)
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "${e.message}")
            }
        }
        get("/users") {
            call.respond(listOf(User(1, "Bob", 18), User(2, "Alice", 19)))
        }
        get("/search") {
            call.respondText("Search: ${call.request.queryParameters["query"]}")
        }
        get("/ping") {
            call.respondText("pong")
        }
        delete("/{id}") {
            try {
                call.respondText("User deleted")
            }
            catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "${e.message}")
            }
        }
    }
}
