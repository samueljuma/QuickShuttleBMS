package com.buupass.quickshuttle.ui.screens.parcelbooking.dispatchparcel

import com.buupass.quickshuttle.data.models.City
import com.buupass.quickshuttle.data.models.parcel.Fleet

data class DispatchReceiveParcelUiState(
    val isLoading: Boolean = false,
    val loadingMessage: String = "Loading ...",
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val cityList: List<City>? = null,
    val fleetList: List<Fleet>? = null,
    val cityFrom: City? = null,
    val cityTo: City? = null,
    val selectedFleet: Fleet? = null,
    val parcelsToProcess: List<String>? = null,
    val parcelCodeToBeAdded: String = ""
)