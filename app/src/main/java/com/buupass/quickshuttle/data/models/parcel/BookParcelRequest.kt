package com.buupass.quickshuttle.data.models.parcel

import kotlinx.serialization.Serializable


@Serializable
data class BookParcelRequest(
    val destination_point: String,
    val origin_point: String,
    val parcel_items: List<ParcelItem>,
    val payee_phone_number: String,
    val payment_type: String,
    val receiver_customer_type: String,
    val receiver_name: String,
    val receiver_phone_number: String,
    val route: String,
    val sender_customer_type: String,
    val sender_name: String,
    val sender_national_id: String,
    val sender_phone_number: String,
    val total_amount: String,
    val split_cash_amount: String,
    val split_mpesa_amount: String,
    val discount: String
)

@Serializable
data class ParcelItem(
    val content: String,
    val quantity: String,
    val weight: String
)
