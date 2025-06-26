package com.buupass.quickshuttle.domain.parcel

import com.buupass.quickshuttle.data.models.parcel.ParcelItemDto
import com.buupass.quickshuttle.data.models.parcel.ReceiptParcelItem
import com.buupass.quickshuttle.ui.screens.common.printer.OperatorDetails
import com.buupass.quickshuttle.utils.timeNow
import com.buupass.quickshuttle.utils.formatted
import com.buupass.quickshuttle.utils.formattedWithTime
import com.buupass.quickshuttle.utils.todayDate

data class SenderReceiver(
    val name: String = "",
    val kraPin: String = "",
    val idNumber: String = "",
    val phoneNumber: String = "",
    val nameError: String? = null,
    val kraPinError: String? = null,
    val idNumberError: String? = null,
    val phoneNumberError: String? = null
){
    private val noBlankSenderFields = name.isNotBlank()
        && kraPin.isNotBlank() && idNumber.isNotBlank()
        && phoneNumber.isNotBlank()
    private val noBlankReceiverFields = name.isNotBlank()
        && idNumber.isNotBlank()
        && phoneNumber.isNotBlank()

    val isValidSenderDetails = listOf(
        nameError,
        kraPinError,
        idNumberError,
        phoneNumberError
    ).all { it == null } && noBlankSenderFields

    val isValidReceiverDetails = listOf(
        nameError,
        idNumberError,
        phoneNumberError
    ).all { it == null } && noBlankReceiverFields
}


data class ParcelItemDomain(
    val name: String = "",
    val quantity: String = "",
    val weight: String = "",
    val nameError: String? = null,
    val quantityError: String? = null,
    val weightError: String? = null,
){
    private val noBlankFields = name.isNotBlank() &&
        quantity.isNotBlank() && weight.isNotBlank()

    val isValid = listOf(
        nameError,
        quantityError,
        weightError,
    ).all { it == null } && noBlankFields
}

data class TotalCost(
    val value: String = "",
    val error: String? = null
){
    val isValid = error == null && value.isNotBlank()
}

data class PayeePhoneNumber(
    val value: String = "",
    val error: String? = null
){
    val isValid = error == null && value.isNotBlank()
}

data class ParcelStickerDetails(
    val operatorDetails: OperatorDetails = OperatorDetails(),
    val pickupLocation: String,
    val dropOffLocation: String,
    val senderName: String,
    val senderPhoneNUmber: String,
    val receiverName: String,
    val receiverPhoneNumber: String,
    val parcelItems: List<ParcelItemDto>,
    val paymentType: String,
    val amount: String,
    val waybill: String,
)

data class ParcelReceiptDetails(
    val operatorDetails: OperatorDetails = OperatorDetails(),
    val pickupLocation: String,
    val dropOffLocation: String,
    val senderName: String,
    val senderPhoneNUmber: String,
    val receiverName: String,
    val receiverPhoneNumber: String,
    val parcelItems: List<ReceiptParcelItem>,
    val waybill: String,
    val parcelRef: String,
    val totalCost: String,
    val date: String = todayDate.formatted(),
    val poweredByString: String = "Powered by buupass.com\n",
    val printedAt: String = "Printed on: ${timeNow.formattedWithTime()}\n",
    val tAndCs : String = "Terms & conditions apply\n",
    val servedBy: String
)

