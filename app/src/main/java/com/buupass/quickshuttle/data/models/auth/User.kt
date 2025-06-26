package com.buupass.quickshuttle.data.models.auth

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String = "",
    val password: String = "",
    val userCanLogin: Boolean = username.isBlank().not() && password.isBlank().not()
)