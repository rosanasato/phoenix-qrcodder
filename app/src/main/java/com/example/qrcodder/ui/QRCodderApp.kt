package com.example.qrcodder.ui

import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.qrcodder.R
import com.example.qrcodder.ui.home.HomeScreen

@Composable
fun QRCodderApp() {

    Scaffold(
        topBar = { TopAppBar() },
        //bottomBar = { BottomAppBar() }
    ) { innerPadding ->
        HomeScreen(modifier = Modifier.padding(innerPadding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar() {
    val activity = LocalActivity.current

    CenterAlignedTopAppBar(
        title = { Text(stringResource(R.string.app_name)) },
        navigationIcon = {
            IconButton(onClick = {
                // TODO Maybe remove the buttons and add them to the menu to have more space available on the screen
                Toast.makeText(activity,
                    activity?.getString(R.string.nothing_here), Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.menu))
            }
        },
        actions = {
            IconButton(onClick = { activity?.finish() }) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close))
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun BottomAppBar() {
    BottomAppBar(
        actions = {},
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { },
                icon = { Icon(Icons.Filled.Add, "Search image") },
                text = { Text(text = "QR Code") }
            )
        }
    )
}
