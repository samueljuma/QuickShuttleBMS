package com.buupass.quickshuttle.ui.screens.passengerbooking.booking

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.domain.auth.UserDomain
import com.buupass.quickshuttle.utils.getInitials

@Composable
fun BookingScreenDrawerContent(
    user: UserDomain,
    onItemSelected: (String) -> Unit
) {
    val drawerWidth = LocalConfiguration.current.screenWidthDp.dp * 0.7f
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(drawerWidth)
            .background(MaterialTheme.colorScheme.primary)
            .statusBarsPadding()
    ) {
        ProfileHeader(
            user = user
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            val bookingsDrawerItemLabels = BookingsDrawerItemLabels.entries.toTypedArray()
            val parcelDrawerItemLabels = ParcelDrawerItemLabels.entries.toTypedArray()

            Spacer(modifier = Modifier.height(16.dp))
            bookingsDrawerItemLabels.forEach {
                DrawerItem(
                    icon = it.icon,
                    label = it.label,
                    onItemClick = {
                        onItemSelected(it.label)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))

            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Parcel",
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))
            parcelDrawerItemLabels.forEach {
                DrawerItem(
                    icon = it.icon,
                    label = it.label,
                    onItemClick = {
                        onItemSelected(it.label)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // App Version at the bottom
            Text(
                text = "App Version 1.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
fun DrawerItem(
    icon: Int,
    label: String,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onItemClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = label,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Icon(
            modifier = Modifier.padding(end = 10.dp)
                .size(16.dp),
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ProfileHeader(
    user: UserDomain
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface
                )
        ) {
            Text(
                text = user.getInitials(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = user.full_name ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = user.email ?: "",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.Center
        )
    }
}

enum class BookingsDrawerItemLabels(val label: String, val icon: Int) {
    BOOK_PASSENGER("Book Passenger", R.drawable.passenger_ic),
    CREATE_SCHEDULE("Create Schedule", R.drawable.create_schedule_ic),
    ASSIGN_TOKEN("Assign Token", R.drawable.assign_token_ic),
    SHOW_SCHEDULES("Show Schedules", R.drawable.show_schedules_ic),
    SHOW_BOOKINGS("Show Bookings", R.drawable.bookings_ic),

}

enum class ParcelDrawerItemLabels(val label: String, val icon: Int) {
    BOOK_PARCEL("Book Parcel", R.drawable.parcel_ic),
    DISPATCH_OR_RECEIVE_PARCEL("Dispatch/Recieve Parcel", R.drawable.dispatch_parcel_ic),
    SHOW_PARCELS("Show Parcels", R.drawable.show_parcels_ic)
}