package com.gnomeshift

import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: Int,
    var name: String,
)