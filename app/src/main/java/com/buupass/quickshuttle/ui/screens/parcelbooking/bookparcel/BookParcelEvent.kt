package com.buupass.quickshuttle.ui.screens.parcelbooking.bookparcel

sealed class BookParcelEvent {
    object ShowRoutesSelectionDialog: BookParcelEvent()
    object ShowPickupPointsSelectionDialog: BookParcelEvent()
    object ShowDropOffPointsSelectionDialog: BookParcelEvent()
    object ShowErrorMessageToast: BookParcelEvent()
    object ShowSuccessMessageToast: BookParcelEvent()
}