package com.gnomeshift

import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        get("/about") {
            call.respondText("This is a Kotlin app with Ktor!")
        }
        get("/hello/{name}") {
            call.respondText("Hello, ${call.parameters["name"]}")
        }
        post("/echo") {
            call.respondText(call.receiveText())
        }
        get("/search") {
            call.respondText("Search: ${call.request.queryParameters["query"]}")
        }
    }
}
