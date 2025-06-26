package com.buupass.quickshuttle.ui.screens.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    label: String? = null, // ✅ Add this
    prefix: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    errorMessage: String? = null,
    isError: Boolean = false,
    textAlign: TextAlign = TextAlign.Start
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle.Default.copy(
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            textAlign = textAlign
        ),
        label = label?.let {
            { Text(text = it, color = MaterialTheme.colorScheme.onSurface) }
        },
        placeholder = {
            if (placeholder.isNotEmpty()) {
                Text(text = placeholder, color = Color.Gray)
            }
        },
        prefix = {
            if (prefix.isNotEmpty()) {
                Text(text = prefix, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        shape = TextFieldDefaults.shape,
        isError = isError,
        supportingText = {
            if (isError) {
                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Gray,
            errorContainerColor = Color.Transparent
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = keyboardType
        )
    )
}
