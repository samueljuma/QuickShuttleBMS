package com.buupass.quickshuttle.ui.screens.reprintticket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buupass.quickshuttle.data.models.reprint.toTicketDetails
import com.buupass.quickshuttle.data.network.NetworkResult
import com.buupass.quickshuttle.data.repositories.AuthRepository
import com.buupass.quickshuttle.data.repositories.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReprintTicketViewModel(
    private val bookingRepository: BookingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReprintTicketScreenUIState())
    val uiState: StateFlow<ReprintTicketScreenUIState> = _uiState.asStateFlow()

    fun updateBookingId(bookingId: String) {
        _uiState.update {
            it.copy(
                bookingId = bookingId,
                ticketDetails = null,
                errorMessage = null
            )
        }
    }

    fun fetchTicketDetails() {
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingDialogMessage = "Fetching ticket details..."
            )
        }
        viewModelScope.launch {
            val result = bookingRepository.fetchTicketsForReprint(uiState.value.bookingId)
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            ticketDetails = result.data.toTicketDetails(
                                authRepository.getUserDetails()
                            ),
                            isLoading = false,
                            showSuccessTicketFetchDialog = true
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            errorMessage = result.message,
                            isLoading = false,
                            showErrorMessageDialog = true
                        )
                    }
                }
            }
        }
    }

    fun resetTicketFetchStatusDialog() {
        _uiState.update {
            it.copy(
                showSuccessTicketFetchDialog = false,
                bookingId = ""
            )
        }
    }

    fun clearTicketDetails() {
        _uiState.update {
            it.copy(
                ticketDetails = null
            )
        }
    }

    fun resetShowErrorMessageDialog() {
        _uiState.update {
            it.copy(
                showErrorMessageDialog = false,
                errorMessage = null,
                bookingId = ""
            )
        }
    }

    fun clearUiState() {
        _uiState.update {
            ReprintTicketScreenUIState()
        }
    }

    override fun onCleared() {
        clearUiState()
    }
}