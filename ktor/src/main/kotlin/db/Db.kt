package com.gnomeshift.db

import com.gnomeshift.entities.RoleEntity
import com.gnomeshift.entities.UserRole
import com.gnomeshift.schemas.ProductService
import com.gnomeshift.schemas.Roles
import com.gnomeshift.schemas.UserRoles
import com.gnomeshift.schemas.UserService
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("DB")
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
        SchemaUtils.create(UserService.Users, ProductService.Products, Roles, UserRoles)
        SchemaUtils.addMissingColumnsStatements(UserService.Users, ProductService.Products, Roles, UserRoles)
        SchemaUtils.createMissingTablesAndColumns(UserService.Users, ProductService.Products, Roles, UserRoles)

        val predefinedRoles = RoleEntity.all().map { it.name }.toSet()
        UserRole.entries.forEach { role ->
            if (role.name !in predefinedRoles) {
                try {
                    RoleEntity.new { name = role.name }
                    logger.info("Predefined role $role created.")
                }
                catch (e: Exception) {
                    logger.error("Failed to create predefined role $role.", e)
                }
            }
        }
    }
}