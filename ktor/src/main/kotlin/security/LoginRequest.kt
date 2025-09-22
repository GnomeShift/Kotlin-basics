package com.gnomeshift.security

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String // pragma: allowlist-secret
)
