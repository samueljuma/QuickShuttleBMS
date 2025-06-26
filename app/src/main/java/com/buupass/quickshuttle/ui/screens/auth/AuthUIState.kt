package com.buupass.quickshuttle.ui.screens.auth

import com.buupass.quickshuttle.data.models.auth.User

data class AuthUIState(
    val isLoading: Boolean = false,
    val user: User = User(),
    val errorMessage: String? = null
)