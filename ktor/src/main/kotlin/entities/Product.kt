package com.gnomeshift.entities

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Int,
    var name: String,
    var price: Double
)