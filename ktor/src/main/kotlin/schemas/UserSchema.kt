package com.gnomeshift.schemas

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class UserService(db: Database) {
    object Users : IntIdTable("Users") {
        val name = varchar("name", 255)
        val age = integer("age")
    }

    init {
        transaction(db) {
            SchemaUtils.create(Users)
        }
    }
}