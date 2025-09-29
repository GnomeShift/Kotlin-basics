package com.gnomeshift.entities

import com.gnomeshift.schemas.Products
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class ProductEntity(id: EntityID<Int>) : IntEntity(id) {
    var name by Products.name
    var price by Products.price

    companion object : IntEntityClass<ProductEntity>(Products)

    fun toDto(): Product = Product(id.value, name, price)
}
