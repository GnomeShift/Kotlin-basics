package com.gnomeshift.entities

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    var name: String,
    var age: Int
)