package com.example.qrcodder.ui.home

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.qrcodder.R

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = Modifier
            .padding(start = 32.dp, end = 32.dp)
            .width(32.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
fun ErrorContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.the_selected_qr_code_cannot_be_read)
    )
}

@Composable
fun WaitingContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.please_select_a_qrcode_to_scan),
        textAlign = TextAlign.Center
    )
}

@Composable
fun SearchPanel(
    context: Context,
    onGalleryButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = { onGalleryButtonClick() }
        ) {
            Text(stringResource(R.string.from_gallery))
        }

        // TODO Create method to open camera and scan QR Code
        Button(
            onClick = { Toast.makeText(context,"Coming soon... Capture QR Code from camera.", Toast.LENGTH_SHORT).show() }
        ) {
            Text(stringResource(R.string.from_camera))
        }
    }
    Spacer(Modifier.size(16.dp))
}