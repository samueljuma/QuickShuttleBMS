package com.buupass.quickshuttle.utils

import com.buupass.quickshuttle.data.models.parcel.BookParcelRequest
import com.buupass.quickshuttle.data.models.parcel.BookParcelResponse
import com.buupass.quickshuttle.data.models.parcel.ParcelItem
import com.buupass.quickshuttle.domain.auth.UserDomain
import com.buupass.quickshuttle.domain.parcel.ParcelItemDomain
import com.buupass.quickshuttle.domain.parcel.ParcelReceiptDetails
import com.buupass.quickshuttle.domain.parcel.ParcelStickerDetails
import com.buupass.quickshuttle.ui.screens.parcelbooking.bookparcel.BookParcelUiState


fun ParcelItemDomain.toParcelItem(): ParcelItem {
    return ParcelItem(
        content = name,
        quantity = quantity,
        weight = weight
    )
}

fun BookParcelUiState.toBookParcelRequest(): BookParcelRequest{
    return BookParcelRequest(
        destination_point = "${selectedDropOffPoint.id}",
        origin_point = "${selectedPickupPoint.id}",
        parcel_items = parcelItems.map { it.toParcelItem() },
        payee_phone_number = payeePhoneNumber.value.generateValidPhoneNumber(),
        payment_type = selectedPaymentMethod.name,
        receiver_customer_type = "",
        receiver_name = receiverDetails.name,
        receiver_phone_number = receiverDetails.phoneNumber.generateValidPhoneNumberWithCode(),
        route = selectedParcelRoute?.id.toString(),
        sender_customer_type = "",
        sender_name = senderDetails.name,
        sender_national_id = senderDetails.idNumber, //Required
        sender_phone_number = senderDetails.phoneNumber.generateValidPhoneNumberWithCode(),
        total_amount = totalCost.value,
        split_cash_amount = "",
        split_mpesa_amount = "",
        discount = "0.0" //Required
    )
}


fun BookParcelResponse.toParcelReceiptDetails(
    state: BookParcelUiState,
    user: UserDomain
): ParcelReceiptDetails{
    return ParcelReceiptDetails(
        pickupLocation = state.selectedPickupPoint.name,
        dropOffLocation = state.selectedDropOffPoint.name,
        senderName = data.sender_name,
        senderPhoneNUmber = data.sender_phone_number,
        receiverName = data.receiver_name,
        receiverPhoneNumber = data.receiver_phone_number,
        parcelItems = data.receipt_parcel_items,
        waybill = data.waybill,
        parcelRef = data.parcel_code,
        totalCost = data.total_amount.toString(),
        servedBy = user.full_name ?: user.username
    )
}

fun BookParcelResponse.toParcelStickerDetails(
    state: BookParcelUiState
): ParcelStickerDetails{
    return ParcelStickerDetails(
        pickupLocation = state.selectedPickupPoint.name,
        dropOffLocation = state.selectedDropOffPoint.name,
        senderName = data.sender_name,
        senderPhoneNUmber = data.sender_phone_number,
        receiverName = data.receiver_name,
        receiverPhoneNumber = data.receiver_phone_number,
        parcelItems = data.parcel_items,
        paymentType = data.payment_type,
        amount = data.total_amount.toString(),
        waybill = data.waybill
    )
}