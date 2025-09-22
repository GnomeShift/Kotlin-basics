package com.gnomeshift.security

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val name: String,
    val age: Int,
    val username: String,
    val password: String // pragma: allowlist-secret
)
