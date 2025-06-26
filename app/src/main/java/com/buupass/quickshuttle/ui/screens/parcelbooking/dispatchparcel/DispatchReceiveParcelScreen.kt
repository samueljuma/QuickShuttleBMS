package com.buupass.quickshuttle.ui.screens.parcelbooking.dispatchparcel

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.data.models.City
import com.buupass.quickshuttle.ui.screens.common.CitySelectionRow
import com.buupass.quickshuttle.ui.screens.common.CustomAppBar
import com.buupass.quickshuttle.ui.screens.common.CustomButton
import com.buupass.quickshuttle.ui.screens.common.LoadingDialog
import com.buupass.quickshuttle.ui.screens.common.MessageDialog

@Composable
fun DispatchReceiveParcelScreen(
    navController: NavController,
    dispatchReceiveParcelViewModel: DispatchReceiveParcelViewModel
) {

    val dispatchReceiveParcelUiState by dispatchReceiveParcelViewModel.uiState.collectAsStateWithLifecycle()
    var selectedAction by remember { mutableStateOf(ParcelActionType.DISPATCH) }
    val cityList = dispatchReceiveParcelUiState.cityList
    val cityFrom = dispatchReceiveParcelUiState.cityFrom
    val cityTo = dispatchReceiveParcelUiState.cityTo
    val parcelCodeToBeAdded = dispatchReceiveParcelUiState.parcelCodeToBeAdded
    var showAddParcelDialog by remember { mutableStateOf(false) }
    var showFleetSelectionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var showErrorMessageDialog by remember { mutableStateOf(false) }
    var showSuccessMessageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedAction) {
        dispatchReceiveParcelViewModel.clearParcelList()
    }

    when{
        showAddParcelDialog -> {
            AddParcelDialog(
                parcelCode = parcelCodeToBeAdded,
                onParcelCodeChange = {
                    dispatchReceiveParcelViewModel.updateParcelToBeAdded(it)
                },
                onDoneClicked = {
                    if(parcelCodeToBeAdded.isNotEmpty()) {
                        dispatchReceiveParcelViewModel.addParcelToParcelsForProcessing(
                            parcelCode = parcelCodeToBeAdded,
                            isDispatch = selectedAction == ParcelActionType.DISPATCH
                        )
                        showAddParcelDialog = false
                    }else{
                        Toast.makeText(context, "Please enter a parcel code", Toast.LENGTH_SHORT).show()
                    }
                },
                onCancelClicked = {
                    showAddParcelDialog = false
                },
                onDismiss = {
                    showAddParcelDialog = false
                }
            )
        }
        showFleetSelectionDialog -> {
            dispatchReceiveParcelUiState.fleetList?.let {
                SelectFleetDialog(
                    fleetList = it,
                    onFleetSelected = { fleet ->
                        showFleetSelectionDialog = false
                        dispatchReceiveParcelViewModel.fetchParcelsForDispatchOrReceipt(
                            fleet = fleet,
                            isDispatch = selectedAction == ParcelActionType.DISPATCH
                        )
                    },
                    onDismiss = {
                        showFleetSelectionDialog = false
                    }
                )
            }
        }
        showErrorMessageDialog -> {
            MessageDialog(
                isErrorMessage = true,
                dialogTitle = if(selectedAction == ParcelActionType.DISPATCH) "Dispatch Failed" else "Receive Failed",
                dialogText = dispatchReceiveParcelUiState.errorMessage ?: " Oops! Error occurred",
                onDismiss = {
                    showErrorMessageDialog = false
                },
                icon = Icons.Outlined.Delete,
            )
        }
        showSuccessMessageDialog -> {
            MessageDialog(
                isErrorMessage = false,
                dialogTitle = if(selectedAction == ParcelActionType.DISPATCH) "Dispatch Success" else "Receive Success",
                dialogText = dispatchReceiveParcelUiState.successMessage ?: "Success",
                onDismiss = {
                    showSuccessMessageDialog = false
                },
                icon = Icons.Outlined.Info,
            )
        }
    }
    LoadingDialog(
        isLoading = dispatchReceiveParcelUiState.isLoading,
        message = dispatchReceiveParcelUiState.loadingMessage
    )

    LaunchedEffect(Unit) {
        dispatchReceiveParcelViewModel.dispatchReceiveParcelEvent.collect{ event->
            when(event){
                is DispatchReceiveParcelEvent.ShowErrorMessage -> {
                    Toast.makeText(context, event.error, Toast.LENGTH_SHORT).show()
                }
                is DispatchReceiveParcelEvent.ShowSuccessMessage -> {
                    Toast.makeText(context, event.successMessage, Toast.LENGTH_SHORT).show()
                }
                is DispatchReceiveParcelEvent.ShowErrorMessageDialog -> {
                    showErrorMessageDialog = true
                }
                is DispatchReceiveParcelEvent.ShowSuccessMessageDialog -> {
                    showSuccessMessageDialog = true
                }
            }
        }
    }

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Dispatch/Receive Parcel",
                navigationIcon = R.drawable.arrow_back_ic,
                actionIconIsMoreVert = true,
                actionIcon = R.drawable.more_action_ic,
                menuItems = listOf(
                    "Clear List" to {
                        dispatchReceiveParcelViewModel.clearParcelList()
                    }
                ),
                onNavigationIconClick = { navController.navigateUp()}
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddParcelDialog = true
                },
                containerColor = MaterialTheme.colorScheme.tertiary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add"
                )
            }
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
            ) {
                DispatchOrReceiveParcelSelector(
                    actionType = selectedAction,
                    onActionTypeChange = {
                        selectedAction = it
                    }
                )
                CitySelectionRow(
                    city = cityFrom ?: City(),
                    isPickUp = true,
                    cityList = cityList,
                    onCitySelected = { city ->
                        dispatchReceiveParcelViewModel.updateDestination(city = city, isPickUp = true)
                    }
                )
                CitySelectionRow(
                    city = cityTo ?: City(),
                    isPickUp = false,
                    cityList = cityList,
                    onCitySelected = { city ->
                        dispatchReceiveParcelViewModel.updateDestination(city = city, isPickUp = false)
                    }
                )

                CustomButton(
                    text = "SEARCH",
                    onClick = {
                        if(dispatchReceiveParcelUiState.fleetList == null){
                            dispatchReceiveParcelViewModel.fetchParcelFleet()
                        }
                        showFleetSelectionDialog = true

                    },
                    modifier = Modifier.padding(16.dp)
                )
                AnimatedVisibility(dispatchReceiveParcelUiState.parcelsToProcess?.isNotEmpty() == true) {
                    ParcelList(
                        parcels = dispatchReceiveParcelUiState.parcelsToProcess ?: emptyList(),
                        onRemoveParcel = { parcelCode ->
                            dispatchReceiveParcelViewModel.removeParcelFromList(
                                parcelCode = parcelCode)
                        }
                    )
                }

                CustomButton(
                    text = if (selectedAction == ParcelActionType.DISPATCH) "DISPATCH" else "RECEIVE",
                    onClick = {
                        if(selectedAction == ParcelActionType.DISPATCH){
                            dispatchReceiveParcelViewModel.dispatchParcels()
                        }else{
                            dispatchReceiveParcelViewModel.receiveParcels()
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                )
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    )
}

@Composable
fun ParcelList(
    parcels: List<String>,
    onRemoveParcel: (parcel: String) -> Unit
){
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp)
    ) {
        parcels.forEach { parcel ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ){
                Text(
                    text = parcel,
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(
                    onClick = {
                        onRemoveParcel(parcel)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

        }
    }

}