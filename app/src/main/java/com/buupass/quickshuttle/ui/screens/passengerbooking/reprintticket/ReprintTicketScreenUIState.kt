package com.buupass.quickshuttle.ui.screens.reprintticket

import com.buupass.quickshuttle.ui.screens.common.printer.TicketDetails

data class ReprintTicketScreenUIState(
    val isLoading: Boolean = false,
    val ticketDetails: TicketDetails? = null,
    val errorMessage: String? = null,
    val showSuccessTicketFetchDialog: Boolean = false,
    val showErrorMessageDialog: Boolean = false,
    val loadingDialogMessage: String = "",
    val bookingId: String = ""
)