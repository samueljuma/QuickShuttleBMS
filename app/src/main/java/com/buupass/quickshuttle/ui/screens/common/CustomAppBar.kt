package com.buupass.quickshuttle.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAppBar(
    title: String,
    navigationIcon: Int? = null,
    actionIcon: Int? = null,
    onNavigationIconClick: (() -> Unit)? = null,
    onActionIconClick: (() -> Unit)? = null,
    actionIconIsMoreVert: Boolean = false,
    menuItems: List<Pair<String, () -> Unit>> = emptyList()
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            navigationIcon?.let {
                IconButton(onClick = { onNavigationIconClick?.invoke() }) {
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        },
        actions = {
            actionIcon?.let {
                Box {
                    IconButton(
                        onClick = {
                            if (actionIconIsMoreVert) {
                                showMenu = !showMenu
                            } else {
                                onActionIconClick?.invoke()
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = it),
                            contentDescription = "Action",
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }

                    if (actionIconIsMoreVert) {
                        DropdownMenu(
                            modifier = Modifier
                                .width(200.dp)
                                .background(
                                    color = Color.White,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            menuItems.forEach { (label, action) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        showMenu = false
                                        action()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        colors = topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}