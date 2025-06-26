package com.buupass.quickshuttle.ui.screens.passengercheckin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.buupass.quickshuttle.data.models.onboardingpassenger.PassengerToOnboard
import com.buupass.quickshuttle.utils.getPickUpAndDropOff
import com.buupass.quickshuttle.R

@Composable
fun PassengerOnboardingDialog(
    onDismiss: () -> Unit,
    passenger: PassengerToOnboard,
    onOnboardPassengerClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    onPhoneIconClicked: (String) -> Unit
) {

    val seatColor = if(passenger.onboarded)
        colorResource(R.color.green)
    else Color.LightGray.copy(0.7f)

    val seatNumberColor = if(passenger.onboarded)
        MaterialTheme.colorScheme.surface
    else MaterialTheme.colorScheme.tertiary

    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Column {
                        Text(
                            text = passenger.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = passenger.getPickUpAndDropOff(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Box(
                        modifier = Modifier.padding(end = 20.dp),
                        contentAlignment = Alignment.Center
                    ){
                        Icon(
                            imageVector = Icons.Filled.Chair,
                            contentDescription = "Seat Icon",
                            modifier = Modifier.size(40.dp),
                            tint = seatColor
                        )
                        Text(
                            text = passenger.seat_number,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = seatNumberColor,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            modifier = Modifier.padding(bottom = 14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier.padding(end = 4.dp),
                                    text = "PNR: ",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = passenger.passenger_id,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    modifier = Modifier.padding(end = 4.dp),
                                    text = "Booking ID: ",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = passenger.booking_id,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.padding(end = 4.dp),
                                text = "Passenger ID: ",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = passenger.id_number,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            IconButton(
                                onClick = {
                                    onPhoneIconClicked(passenger.phone_number)
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.Phone,
                                    contentDescription = "Phone Icon",
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = passenger.phone_number,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // Action Buttons Section
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {

                            TextButton(
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(4.dp),
                                onClick = {
                                onCancelClicked()
                            }
                            ) {
                                Text(
                                    text = "CLOSE",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            if(!passenger.onboarded){
                                Spacer(modifier = Modifier.width(10.dp))
                                TextButton(
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    onClick = {
                                        onOnboardPassengerClicked()
                                    }
                                ) {
                                    Text(
                                        text = "ONBOARD",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}
