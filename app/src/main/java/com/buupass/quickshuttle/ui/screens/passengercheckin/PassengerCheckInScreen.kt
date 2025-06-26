package com.buupass.quickshuttle.ui.screens.passengercheckin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.data.models.City
import com.buupass.quickshuttle.navigation.AppScreens
import com.buupass.quickshuttle.ui.screens.common.CustomAppBar
import com.buupass.quickshuttle.ui.screens.common.CustomButton
import com.buupass.quickshuttle.utils.capitalizeFirstCharacter
import com.buupass.quickshuttle.utils.cityList
import org.koin.androidx.compose.koinViewModel

@Composable
fun PassengerCheckInScreen(
    cityList: List<City>,
    navController: NavController,
    passengerCheckInViewModel: PassengerCheckInViewModel
){

    val passengerCheckInUiState by passengerCheckInViewModel.uiState.collectAsStateWithLifecycle()
    val cityFrom = passengerCheckInUiState.cityFrom
    val cityTo = passengerCheckInUiState.cityTo



    LaunchedEffect(Unit) {
        if(cityFrom == City()){
            passengerCheckInViewModel.updateCityList(cityList)
        }
    }

    LaunchedEffect(Unit){
        passengerCheckInViewModel.resetDate()
    }

    val user = passengerCheckInViewModel.getCurrentUser()


    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Passenger Onboarding",
                navigationIcon = R.drawable.arrow_back_ic,
                actionIcon = R.drawable.close_ic,
                onNavigationIconClick = { navController.navigateUp() },
                onActionIconClick = { navController.navigateUp() }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                bottomEnd = 16.dp,
                                bottomStart = 16.dp
                            )
                        )
                        .background(MaterialTheme.colorScheme.primary),
                )
                {
                    Text(
                        modifier = Modifier
                            .padding(start = 40.dp),
                        text = "Good day ${user.full_name?.split(" ")?.get(0)?.capitalizeFirstCharacter()}, \uD83D\uDC4B",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "Please select origin city"
                )
                Spacer(modifier = Modifier.height(8.dp))
                DestinationSelectionButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    city = cityFrom,
                    cityList = cityList,
                    onCitySelected = {
                        passengerCheckInViewModel.updateDestinations(
                            isFrom = true,
                            city = it
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "Please select destination city"
                )
                Spacer(modifier = Modifier.height(8.dp))

                DestinationSelectionButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    city = cityTo,
                    cityList = cityList,
                    onCitySelected = {
                        passengerCheckInViewModel.updateDestinations(
                            isFrom = false,
                            city = it
                        )
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
                CustomButton(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "Continue",
                    onClick = {
                        navController.navigate(AppScreens.TripListScreen.route)
                    }
                )

            }
        }
    )

}

@Composable
@Preview(showBackground = true, widthDp = 320, heightDp = 640)
fun PassengerCheckInPreview(){
    PassengerCheckInScreen(
        navController = rememberNavController(),
        passengerCheckInViewModel = koinViewModel(),
        cityList = cityList
    )
}
