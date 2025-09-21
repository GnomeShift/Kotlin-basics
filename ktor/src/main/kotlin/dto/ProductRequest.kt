package com.gnomeshift.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductRequest (
    val name: String,
    val price: Double
)
