package com.example.qrcodder.ui.home

import android.content.ClipData
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrcodder.R
import com.example.qrcodder.ui.AppViewModelProvider
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var selectedImage by remember { mutableStateOf<Uri?>(null) }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    //Log.d("QrCodderApp- HomeScreen","isPhotoPickerAvailable: ${isPhotoPickerAvailable()}")
    LaunchedEffect(selectedImage) { selectedImage?.let { viewModel.readQrCodeFromGallery(selectedImage!!, context) } }

    // Registers a photo picker activity launcher in single-select mode.
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { selectedImage = uri } }
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Contains top buttons
        SearchPanel(
            context = context,
            onGalleryButtonClick = { photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onCameraButtonClick = { viewModel.readQrCodeFromCamera(context) }
        )

        when (uiState) {
            is HomeUiState.Waiting -> WaitingContent()
            is HomeUiState.Loading -> LoadingContent()
            is HomeUiState.Error -> ErrorContent()
            is HomeUiState.Success -> ResultContent(
                context = context,
                result = viewModel.getFormattedQrCodeString((uiState as HomeUiState.Success).qrCodeResult),
                onGenerateQrCodeButtonClick = { qrCode -> viewModel.generateQRCode(qrCode) }
            )
        }
    }
}

@Composable
fun ResultContent(
    context: Context,
    result: Pair<String, String>,
    onGenerateQrCodeButtonClick: (String) -> Painter?,
    modifier: Modifier = Modifier
) {
    var textFieldRawValue by remember { mutableStateOf(TextFieldValue(result.second)) }
    val clipboardManager = LocalClipboard.current
    var showDialog by remember { mutableStateOf(false) }
    var qrCode by remember { mutableStateOf<Painter?>(null) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Dialog to show the generated QR Code
    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (qrCode != null) {
                        Image(
                            painter = qrCode!!,
                            contentDescription = stringResource(R.string.qr_code),
                            modifier = Modifier.size(200.dp)
                        )
                    } else {
                        Text(stringResource(R.string.unable_to_generate_the_qr_code))
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.Center) {
                        Button(onClick = { showDialog = false }) { Text(stringResource(R.string.close)) }
                        Spacer(modifier = Modifier.size(16.dp))
                        Button(
                            enabled = qrCode != null,
                            onClick = {
                                // TODO Create method to save new QR Code. Maybe export to another app?
                                showDialog = false
                                Toast.makeText(context, "Coming soon... Saving QR Code", Toast.LENGTH_SHORT).show()
                            }
                        ) { Text(stringResource(R.string.save)) }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top
    ) {
        // TextField to show formatted data
        OutlinedTextField(
            value = result.first,
            onValueChange = {},
            label = { Text(stringResource(R.string.qr_code_data)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )
        Spacer(Modifier.size(8.dp))

        // TextField to show raw values
        OutlinedTextField(
            value = textFieldRawValue,
            onValueChange = { textFieldRawValue = it },
            label = { Text(stringResource(R.string.qr_code_raw_values)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = false
        )
        Spacer(Modifier.size(8.dp))

        // Buttons to copy e generate QR Code
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            // Copy button
            IconButton(
                onClick = {
                    textFieldRawValue = textFieldRawValue.copy(
                        selection = TextRange(0, textFieldRawValue.text.length)
                    )

                    // Using native clipboard
//                    val clipData = ClipData.newPlainText("", textFieldRawValue.text)
//                    clipboardManager.nativeClipboard.setPrimaryClip(clipData)

                    // Using the new Clipboard Interface
                    val clipEntry = ClipData.newPlainText("", textFieldRawValue.text).toClipEntry()
                    coroutineScope.launch {
                        clipboardManager.setClipEntry(clipEntry)

                        // Only show a toast for Android 12 and lower.
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                            Toast.makeText(context, context.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.content_copy_24dp),
                    contentDescription = stringResource(R.string.copy_content)
                )
            }

            // Generate QR Code button
            IconButton(
                onClick = {
                    showDialog = true
                    qrCode = onGenerateQrCodeButtonClick(textFieldRawValue.text)
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.qr_code_2_add_24dp),
                    contentDescription = stringResource(R.string.generate_qr_code)
                )
            }
        }
    }
}
