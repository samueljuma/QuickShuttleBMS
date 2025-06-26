package com.buupass.quickshuttle.data.models.parcel

import com.buupass.quickshuttle.data.models.City
import kotlinx.serialization.Serializable

@Serializable
data class ParcelRoutesResponse(
    val message: String,
    val routes: List<ParcelRoute>,
    val status: Boolean
)

@Serializable
data class ParcelRoute(
    val id: Int,
    val name: String,
    val dropoff_points: List<City>,
    val pickup_points: List<City>
)

