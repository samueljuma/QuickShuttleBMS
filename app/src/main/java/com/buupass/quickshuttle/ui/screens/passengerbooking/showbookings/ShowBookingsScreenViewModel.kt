package com.buupass.quickshuttle.ui.screens.passengerbooking.showbookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buupass.quickshuttle.data.network.NetworkResult
import com.buupass.quickshuttle.data.repositories.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShowBookingsScreenViewModel(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShowBookingsUIState())
    val uiState: StateFlow<ShowBookingsUIState> = _uiState.asStateFlow()

    fun fetchPastBookings(
        date: String,
        userId: Int
    ) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val result = bookingRepository.getPastBookings(date, userId)
            when (result) {
                is NetworkResult.Success -> {
                    val bookings = result.data.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            pastBookings = bookings.bookings,
                            successMessage = result.data.message,
                            errorMessage = null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            showErrorMessageDialog = true,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun resetShowErrorMessageDialog() {
        _uiState.update { it.copy(showErrorMessageDialog = false) }
    }
}