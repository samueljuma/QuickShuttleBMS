package com.buupass.quickshuttle.data.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val status: Boolean,
    val message: String,
    val data: UserDTO? = null
)

@Serializable
data class UserDTO(
    val id: Int? = null,
    val token: String? = null,
    val full_name: String? = null,
    val user_id: Int? = null,
    val username: String? = null,
    val user_group: String? = null,
    val email: String? = null,
    val permissions: List<String>? = null,
    val can_reprint: Int? = null,
    val currency: String? = null
)