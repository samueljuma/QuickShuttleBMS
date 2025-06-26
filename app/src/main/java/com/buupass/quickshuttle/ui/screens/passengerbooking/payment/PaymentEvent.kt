package com.buupass.quickshuttle.ui.screens.passengerbooking.payment

sealed class PaymentEvent {
    object ShowSuccessMessageToast : PaymentEvent()
    object ShowErrorMessageToast : PaymentEvent()

    object NavigateBackToBookingScreen : PaymentEvent()
}