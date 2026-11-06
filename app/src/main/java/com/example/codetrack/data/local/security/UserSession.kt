package com.example.codetrack.data.local.security

/**
 * Clean data model representing the currently active local user session.
 */
data class UserSession(
    val token: String,
    val userId: Long? = null,
    val name: String? = null,
    val email: String? = null
)
