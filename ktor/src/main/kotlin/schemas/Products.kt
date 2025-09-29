package com.gnomeshift.schemas

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object Products : IntIdTable("products") {
    val name = varchar("name", 255)
    val price = double("price")
}