package com.buupass.quickshuttle.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buupass.quickshuttle.data.models.auth.User
import com.buupass.quickshuttle.data.network.NetworkResult
import com.buupass.quickshuttle.data.repositories.AuthRepository
import com.buupass.quickshuttle.domain.auth.UserDomain
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private var _uIState = MutableStateFlow(AuthUIState())
    val uiState: StateFlow<AuthUIState> = _uIState.asStateFlow()

    private val _navigateToBookingScreen = MutableSharedFlow<Unit>()
    val navigateToBookingScreen: SharedFlow<Unit> = _navigateToBookingScreen

    fun login(user: User) {
        _uIState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            when (val result = authRepository.login(user)) {
                is NetworkResult.Success -> {
                    _uIState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                    _navigateToBookingScreen.emit(Unit)
                }

                is NetworkResult.Error -> {
                    _uIState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
    fun getAuthToken(): String {
        return authRepository.getAuthToken()
    }

    fun getUserDetails(): UserDomain {
        return authRepository.getUserDetails()
    }

    fun clearAllUserDetails() {
        authRepository.clearAllUserDetails()
    }

    fun clearUiState() {
        _uIState.update {
            AuthUIState()
        }
    }

    override fun onCleared() {
        clearUiState()
    }
}