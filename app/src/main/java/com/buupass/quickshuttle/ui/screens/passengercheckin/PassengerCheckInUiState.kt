package com.buupass.quickshuttle.ui.screens.passengercheckin

import com.buupass.quickshuttle.data.models.City
import com.buupass.quickshuttle.data.models.onboardingpassenger.ManifestTrip
import com.buupass.quickshuttle.data.models.onboardingpassenger.PassengerToOnboard

data class PassengerCheckInUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val loadingMessage: String = "",
    val error: String? = null,
    val cityList: List<City>? = null,
    val cityFrom: City = City(),
    val cityTo: City = City(),
    val tripList: List<ManifestTrip>? = null,
    val date: String? = null,
    val selectedTrip: ManifestTrip? = null,
    val passengersList: List<PassengerToOnboard> = emptyList()
)