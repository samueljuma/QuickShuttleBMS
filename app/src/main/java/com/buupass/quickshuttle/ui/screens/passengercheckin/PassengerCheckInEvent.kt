package com.buupass.quickshuttle.ui.screens.passengercheckin

sealed class PassengerCheckInEvent {
    data class ShowSuccessMessage(val message: String) : PassengerCheckInEvent()
    data class ShowErrorMessage(val message: String) : PassengerCheckInEvent()
}