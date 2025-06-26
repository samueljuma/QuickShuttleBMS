package com.buupass.quickshuttle.data.models

import kotlinx.serialization.Serializable

@Serializable
data class City(
    val id: Int = -1,
    val name: String = "Select City"
)