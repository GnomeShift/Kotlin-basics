package com.gnomeshift.schemas

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object Users : IntIdTable("users") {
    val name = varchar("name", 255)
    val age = integer("age")
    val username = varchar("username", 255).uniqueIndex()
    val password = varchar("password", 255) // pragma: allowlist-secret
}