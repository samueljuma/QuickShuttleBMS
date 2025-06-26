package com.buupass.quickshuttle.ui.screens.passengerbooking.manualconfirm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.ui.screens.common.CustomAppBar

@Composable
fun ManualConfirmScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Confirm Booking",
                navigationIcon = R.drawable.arrow_back_ic,
                actionIcon = R.drawable.close_ic,
                onActionIconClick = { navController.popBackStack() },
                onNavigationIconClick = { navController.popBackStack() }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Manual Confirm Screen")
            }
        }

    )
}