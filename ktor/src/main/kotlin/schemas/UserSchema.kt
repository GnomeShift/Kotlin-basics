package com.gnomeshift.schemas

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

class UserService() {
    object Users : IntIdTable("Users") {
        val name = varchar("name", 255)
        val age = integer("age")
    }
}