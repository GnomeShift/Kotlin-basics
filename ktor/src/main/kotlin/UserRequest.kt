package com.gnomeshift

import kotlinx.serialization.Serializable

@Serializable
data class UserRequest(
    val name: String,
    val age: Int
)