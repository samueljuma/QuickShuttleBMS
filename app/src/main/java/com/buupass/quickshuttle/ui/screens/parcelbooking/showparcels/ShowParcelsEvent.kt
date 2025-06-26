package com.buupass.quickshuttle.ui.screens.parcelbooking.showparcels

sealed class ShowParcelsEvent {
    data class ShowSuccessMessage(val message: String) : ShowParcelsEvent()
    data class ShowErrorMessage(val message: String) : ShowParcelsEvent()
}