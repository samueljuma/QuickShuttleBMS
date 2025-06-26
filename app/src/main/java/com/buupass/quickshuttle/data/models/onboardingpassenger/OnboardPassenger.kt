package com.buupass.quickshuttle.data.models.onboardingpassenger

import kotlinx.serialization.Serializable

@Serializable
data class OnboardPassengerRequest(
    val passenger_id: String
)

@Serializable
data class OnboardPassengerResponse(
    val status: Boolean,
    val message: String,
)