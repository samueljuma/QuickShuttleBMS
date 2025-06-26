package com.buupass.quickshuttle.ui.screens.parcelbooking.dispatchparcel

sealed class DispatchReceiveParcelEvent{
    data class ShowErrorMessage(val error: String): DispatchReceiveParcelEvent()
    data class ShowSuccessMessage(val successMessage: String): DispatchReceiveParcelEvent()
    data class ShowErrorMessageDialog(val error: String): DispatchReceiveParcelEvent()
    data class ShowSuccessMessageDialog(val successMessage: String): DispatchReceiveParcelEvent()
}