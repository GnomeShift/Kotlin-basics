package com.gnomeshift.entities

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    var name: String,
    var age: Int,
    val username: String,
    val password: String, // pragma: allowlist-secret
    val roles: List<UserRole>
)