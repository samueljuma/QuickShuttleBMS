package com.buupass.quickshuttle.ui.screens.passengerbooking.payment

import com.buupass.quickshuttle.data.models.payment.InitiateBookingRequest
import com.buupass.quickshuttle.ui.screens.common.printer.TicketDetails

data class PaymentUiState(
    val initiateBookingRequest: InitiateBookingRequest? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showMpesaConfirmationDialog: Boolean = false,
    val showErrorMessageDialog: Boolean = false,
    val showSuccessMessageDialog: Boolean = false,
    val paymentMethod: String? = null,
    val ticketDetails: TicketDetails? = null,
    val bookingIDForMpesaConfirmation: String? = null,
    val paymentProcessed: Boolean = false,
    val loadingMessage: String? = null
)