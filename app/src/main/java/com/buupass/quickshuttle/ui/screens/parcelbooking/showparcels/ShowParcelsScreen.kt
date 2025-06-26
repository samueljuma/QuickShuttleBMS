package com.buupass.quickshuttle.ui.screens.parcelbooking.showparcels

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.ui.screens.common.CustomAppBar
import com.buupass.quickshuttle.ui.screens.common.LoadingDialog
import com.buupass.quickshuttle.utils.formatAmount


@Composable
fun ShowParcelsScreen(
    date: String,
    navController: NavController,
    showParcelsViewModel: ShowParcelsViewModel
){
    val context = LocalContext.current

    val showParcelsUiState by showParcelsViewModel.uiState.collectAsStateWithLifecycle()
    val parcelList = showParcelsUiState.parcelList

    LaunchedEffect(Unit) {
        showParcelsViewModel.fetchUserBookedParcels(date)
    }

    LaunchedEffect(Unit) {
        showParcelsViewModel.showParcelsEvent.collect { event ->
            when (event) {
                is ShowParcelsEvent.ShowSuccessMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is ShowParcelsEvent.ShowErrorMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LoadingDialog(
        isLoading = showParcelsUiState.isLoading,
        message = showParcelsUiState.loadingMessage ?: "Loading"
    )

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Booked Parcels",
                navigationIcon = R.drawable.arrow_back_ic,
                actionIcon = R.drawable.close_ic,
                onNavigationIconClick = { navController.navigateUp() },
                onActionIconClick = { navController.navigateUp() }
            )
        },
        content = {paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when{
                    parcelList.isNullOrEmpty() -> {
                        if(showParcelsUiState.error?.isNotEmpty() == true){
                            ErrorContainer(
                                error = showParcelsUiState.error ?: "There was an Error!"
                            )
                        }else{
                            if(!showParcelsUiState.isLoading){
                                EmptyItemsContainer(
                                    message = "No parcels booked"
                                )
                            }

                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(items = parcelList){ parcel ->
                                ParcelContainer(
                                    parcel = parcel
                                )
                            }
                            item {
                                //Totals Row
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.End
                                ){
                                    Text(
                                        text = "Total Amount:",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "Ksh. ${parcelList.sumOf { it.total_amount}.formatAmount()}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    )
}


@Composable
fun ErrorContainer(error: String){
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            modifier = Modifier.size(40.dp),
            contentDescription = "Error Icon",
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = error,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun EmptyItemsContainer(
    message: String
){
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            modifier = Modifier.size(40.dp),
            contentDescription = "Info",
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}