package com.buupass.quickshuttle.domain.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserDomain(
    val id: Int? = null,
    val token: String? = null,
    val username: String = "",
    val password: String = "",
    val full_name: String? = null,
    val email: String? = null,
    val can_reprint: Int? = null,
    val currency: String = "",
    val userCanLogin: Boolean = username.isBlank().not() && password.isBlank().not()
)