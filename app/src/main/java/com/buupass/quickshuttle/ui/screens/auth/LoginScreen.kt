package com.buupass.quickshuttle.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.buupass.quickshuttle.R
import com.buupass.quickshuttle.data.models.auth.User
import com.buupass.quickshuttle.navigation.AppScreens
import com.buupass.quickshuttle.ui.screens.common.CustomButton
import com.buupass.quickshuttle.ui.screens.common.CustomOutlinedTextField
import com.buupass.quickshuttle.ui.screens.common.LoadingDialog
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = authUiState.isLoading
    val errorMessage = authUiState.errorMessage
    val passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.navigateToBookingScreen.collectLatest {
            navController.navigate(AppScreens.BookingScreen.route) {
                popUpTo(AppScreens.LoginScreen.route) {
                    inclusive = true
                }
            }
        }
    }

    LoadingDialog(
        isLoading = isLoading,
        message = "Logging In",
        onDismiss = {
            // Handle dismiss
        }
    )

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
            .imePadding() // Content goes above keyboard
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.3f))
        Image(
            modifier = Modifier.size(150.dp),
            painter = painterResource(id = R.drawable.quickshuttle_logo),
            contentDescription = "Logo",
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(24.dp))

        CustomOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            text = username,
            placeHolder = "Enter Username",
            onValueChange = { newUsername ->
                username = newUsername
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        CustomOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            text = password,
            isPassword = true,
            placeHolder = "Enter Password",
            onValueChange = { newPassword ->
                password = newPassword
            }
        )
        Spacer(modifier = Modifier.height(30.dp))

        AnimatedVisibility(!errorMessage.isNullOrEmpty()) {
            Text(
                modifier = Modifier.padding(bottom = 16.dp),
                text = errorMessage ?: "There was an error",
                color = MaterialTheme.colorScheme.error
            )
        }

        CustomButton(
            text = "Login",
            enabled = username.isNotBlank() && password.isNotBlank(),
            onClick = {
                authViewModel.login(
                    User(
                        username = username,
                        password = password
                    )
                )
            }
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}