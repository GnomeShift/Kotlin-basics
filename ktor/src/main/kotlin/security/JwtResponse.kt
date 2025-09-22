package com.gnomeshift.security

import com.gnomeshift.entities.User
import kotlinx.serialization.Serializable

@Serializable
data class JwtResponse(
    val token: String,
    val user: User
)
