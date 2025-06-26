package com.buupass.quickshuttle.ui.screens.parcelbooking.showparcels

import com.buupass.quickshuttle.data.models.parcel.ParcelData

data class ShowParcelsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val loadingMessage: String? = null,
    val successMessage: String? = null,
    val parcelList: List<ParcelData>? = null
)