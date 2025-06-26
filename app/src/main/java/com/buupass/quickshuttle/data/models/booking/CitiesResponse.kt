package com.buupass.quickshuttle.data.models.booking

import com.buupass.quickshuttle.data.models.City
import kotlinx.serialization.Serializable

@Serializable
data class CitiesResponse(
    val status: Boolean,
    val message: String,
    val cities: List<City>
)