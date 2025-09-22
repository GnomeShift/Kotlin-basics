package com.gnomeshift.db

import com.gnomeshift.schemas.ProductService
import com.gnomeshift.schemas.UserService
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

lateinit var db: Database

fun Application.configureDB() {
    if (!::db.isInitialized) {
        db = Database.connect(
            driver = environment.config.property("db.driver").getString(),
            url = environment.config.property("db.url").getString(),
            user = environment.config.property("db.user").getString(),
            password = environment.config.property("db.password").getString() // pragma: allowlist-secret
        )
    }

    transaction {
        SchemaUtils.create(UserService.Users, ProductService.Products)
        SchemaUtils.addMissingColumnsStatements(UserService.Users, ProductService.Products)
        SchemaUtils.createMissingTablesAndColumns(UserService.Users, ProductService.Products)
    }
}