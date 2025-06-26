package com.buupass.quickshuttle.ui.screens.passengerbooking.booking

sealed class BookingScreenEvent{
    data class NavigateToShowBookingScreen(val date : String): BookingScreenEvent()
    data class NavigateToShowParcels(val date : String): BookingScreenEvent()
}