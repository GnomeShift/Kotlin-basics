package com.gnomeshift.schemas

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class ProductService(db: Database) {
    object Products : IntIdTable("products") {
        val name = varchar("name", 255)
        val price = double("price")
    }

    init {
        transaction(db) {
            SchemaUtils.create(Products)
        }
    }
}