package com.buupass.quickshuttle.ui.screens.passengerbooking.showbookings

import com.buupass.quickshuttle.data.models.booking.Booking

data class ShowBookingsUIState(
    val isLoading: Boolean = false,
    val showErrorMessageDialog: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val pastBookings: List<Booking> = emptyList(),
)