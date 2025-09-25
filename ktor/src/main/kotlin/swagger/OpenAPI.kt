package com.gnomeshift.swagger

import io.ktor.server.application.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.*

fun Application.configureOpenAPI() {
    routing {
        openAPI(path = "openapi", swaggerFile = "openapi/openapi.yaml")
        swaggerUI("swagger", "openapi/openapi.yaml") {
            version = "5.17.12"
        }
    }
}