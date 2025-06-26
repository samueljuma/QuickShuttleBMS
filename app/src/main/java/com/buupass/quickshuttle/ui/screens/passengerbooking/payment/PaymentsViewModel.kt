package com.buupass.quickshuttle.ui.screens.passengerbooking.payment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buupass.quickshuttle.data.models.booking.Schedule
import com.buupass.quickshuttle.data.models.payment.InitiateBookingRequest
import com.buupass.quickshuttle.data.models.payment.toTicketDetails
import com.buupass.quickshuttle.data.network.NetworkResult
import com.buupass.quickshuttle.data.repositories.AuthRepository
import com.buupass.quickshuttle.data.repositories.PaymentRepository
import com.buupass.quickshuttle.utils.PaymentErrors
import com.buupass.quickshuttle.utils.PaymentMethod
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentsViewModel(
    private val paymentRepository: PaymentRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    fun getCurrentUser() = authRepository.getUserDetails()

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    private val _paymentEvent = MutableSharedFlow<PaymentEvent>()
    val paymentEvent: SharedFlow<PaymentEvent> = _paymentEvent.asSharedFlow()

    fun initializeBookingRequest(bookingRequest: InitiateBookingRequest) {
        _uiState.update {
            it.copy(
                initiateBookingRequest = bookingRequest.copy(
                    payment_type = PaymentMethod.Cash.name
                )
            )
        }
        Log.d("PaymentsViewModel", "BookingRequest: ${uiState.value.initiateBookingRequest}")
    }

    fun updateBookingRequest(property: FieldsToUpdate, value: Any) {
        when (property) {
            FieldsToUpdate.PaymentType -> {
                _uiState.update {
                    it.copy(
                        initiateBookingRequest = it.initiateBookingRequest?.copy(
                            payment_type = value as String
                        )
                    )
                }
            }
            FieldsToUpdate.PayeePhone -> {
                _uiState.update {
                    it.copy(
                        initiateBookingRequest = it.initiateBookingRequest?.copy(
                            payee_phone_number = value as String
                        )
                    )
                }
            }
        }
        Log.d("PaymentsViewModel", "updateBookingRequest: ${uiState.value.initiateBookingRequest}")
    }

    fun initiateBooking(schedule: Schedule) {
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "Processing Payment..."
            )
        }

        viewModelScope.launch {
            val bookingRequest = _uiState.value.initiateBookingRequest
            bookingRequest?.let { request ->
                val result = paymentRepository.initiateBooking(request)
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = result.data.message,
                                showSuccessMessageDialog = true,
                                ticketDetails = result.data.toTicketDetails(getCurrentUser(), schedule),
                                paymentProcessed = true
                            )
                        }
                        _paymentEvent.emit(PaymentEvent.ShowSuccessMessageToast)
                    }
                    is NetworkResult.Error -> {
                        val bookingId = result.extra?.get("booking_id") as? String
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message,
                                bookingIDForMpesaConfirmation = bookingId,
                                ticketDetails = null,
                                successMessage = null,
                                showErrorMessageDialog = true,
                                paymentProcessed = true
                            )
                        }

                        when (result.message) {
                            PaymentErrors.PAYMENT_TIME_OUT_ERROR -> {
                                _uiState.update {
                                    it.copy(
                                        showMpesaConfirmationDialog = true
                                    )
                                }
                            }
                            else -> {
                                _paymentEvent.emit(PaymentEvent.ShowErrorMessageToast)
                            }
                        }
                    }
                }
            }
        }

        Log.d("PaymentsViewModel", "uiState: ${uiState.value}")
    }

    fun confirmMpesaPayment(schedule: Schedule) {
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "Confirming Mpesa Payment..."
            )
        }
        viewModelScope.launch {
            val bookingId = _uiState.value.bookingIDForMpesaConfirmation
            if (bookingId.isNullOrEmpty()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Booking ID is null or empty",
                        paymentProcessed = true
                    )
                }
                return@launch
            }

            val result = paymentRepository.confirmMpesaPayment(bookingId)
            when (result) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = result.data.message,
                            ticketDetails = result.data.toTicketDetails(getCurrentUser(), schedule = schedule ),
                            showMpesaConfirmationDialog = false,
                            showSuccessMessageDialog = true,
                            paymentProcessed = true
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message,
                            ticketDetails = null,
                            showMpesaConfirmationDialog = false,
                            showErrorMessageDialog = true,
                            paymentProcessed = true
                        )
                    }
                }
            }
        }
    }

    fun resetShowMpesaConfirmationDialog() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                showMpesaConfirmationDialog = false,
                showErrorMessageDialog = false
            )
        }
    }

    fun resetShowErrorMessageDialog() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                showErrorMessageDialog = false
            )
        }
    }

    fun resetShowSuccessMessageDialog() {
        _uiState.update {
            it.copy(
                successMessage = null,
                showSuccessMessageDialog = false
            )
        }
    }

    fun resetPaymentProcessed() {
        _uiState.update {
            it.copy(
                paymentProcessed = false
            )
        }
    }

    fun clearUIState() {
        // reset all states
        _uiState.update { PaymentUiState() }
    }

    fun triggerNavigationToBooking(){
        viewModelScope.launch {
            _paymentEvent.emit(PaymentEvent.NavigateBackToBookingScreen)
        }
    }

    override fun onCleared() {
        clearUIState()
    }
}

enum class FieldsToUpdate {
    PaymentType,
    PayeePhone
}