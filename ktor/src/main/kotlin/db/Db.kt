package com.gnomeshift.db

import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database

fun Application.configureDB() {
    val db by lazy {
        Database.connect(
            driver = environment.config.property("db.driver").getString(),
            url = environment.config.property("db.url").getString(),
            user = environment.config.property("db.user").getString(),
            password = environment.config.property("db.password").getString() // pragma: allowlist-secret
        )
    }
}