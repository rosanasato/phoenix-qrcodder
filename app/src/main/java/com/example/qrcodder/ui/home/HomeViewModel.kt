package com.example.qrcodder.ui.home

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrcodder.R
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class HomeViewModel(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    // Holds the state of the UI
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Waiting)
    val uiState = _uiState.asStateFlow()

    /**
     * Returns the QR Code information formatted according to the type
     *
     * @param qrcode a [QrCodeTypes] object
     * @return A Pair of String where first value is the formatted string and the second value is the raw value
     */
    fun getFormattedQrCodeString(qrCode: QrCodeTypes?): Pair<String, String> {
        return when (qrCode) {
            is QrCodeTypes.Email -> {
                Pair(first = "Type: ${qrCode.type} \nTo: ${qrCode.address} \nSubject: ${qrCode.subject} \nBody: ${qrCode.body}",
                    second = "${qrCode.rawValue}") }
            is QrCodeTypes.Text -> {
                Pair(first = "Text: ${qrCode.rawValue}",
                    second = "${qrCode.rawValue}") }
            is QrCodeTypes.Url -> {
                Pair(first = "Title: ${qrCode.title.toString()} \nURL: ${qrCode.url.toString()}",
                    second = "${qrCode.rawValue}") }
            is QrCodeTypes.Wifi -> {
                Pair(first = "Encryption Type: ${qrCode.encryptionType.toString()} \nSSID: ${qrCode.ssid.toString()}\nPassword: ${qrCode.ssid.toString()}",
                    second = "${qrCode.rawValue}") }

            is QrCodeTypes.Unknown -> { Pair(qrCode.text, qrCode.rawValue) }
            else -> { Pair("","") }
        }
    }

    /**
     * Convert a R.drawable.resource into URI
     */
    private fun convertToUri(context: Context, image: Int): Uri {
        return "android.resource://${context.packageName}/${image}".toUri()
    }

    /**
     * Reads the selected QR Code
     */
    suspend fun readQrCodeFromGallery(imageUri: Uri, context: Context) = withContext(dispatcher) {

        // TODO How to handle ANR exception correctly?

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                //Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_ALL_FORMATS
            )
            .enableAllPotentialBarcodes()
            .build()

        try {
            _uiState.update { HomeUiState.Loading }

            Log.d(TAG, "HomeViewModel: Read image from file path: ${imageUri}")

            val image = InputImage.fromFilePath(context, imageUri)

            Log.d(TAG, "HomeViewModel: Generated InputImage: ${image}")

            val scanner = BarcodeScanning.getClient(options)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isEmpty()) {
                        val text = context.getString(R.string.qr_code_not_recognized)
                        _uiState.update { HomeUiState.Success(QrCodeTypes.Unknown(text, text)) }

                        Log.d(TAG, "HomeViewModel: QR Code not recognized: ${imageUri}. Barcode size: ${barcodes.size}")
                    } else {
                        for (barcode in barcodes) {
                            _uiState.update { HomeUiState.Success(getQrCodeValueType(barcode)) }

                            Log.d(TAG, "HomeViewModel: QR Code format: ${barcode.format}")
                            Log.d(TAG, "HomeViewModel: QR Code value type: ${barcode.valueType}")
                            Log.d(TAG, "HomeViewModel: QR Code raw value: ${barcode.rawValue}")
                            Log.d(TAG, "HomeViewModel: uiState: ${_uiState.value}")
                        }
                    }
                }
                .addOnFailureListener {
                    HomeUiState.Error(R.string.the_selected_qr_code_cannot_be_read)
                    Log.e(TAG, "HomeViewModel: Fail to read QR Code: ${imageUri}")
                }
                .addOnCompleteListener { scanner.close() }

        } catch (e: IOException) {
            e.printStackTrace()
            HomeUiState.Error(R.string.the_selected_qr_code_cannot_be_read)
            Log.e(TAG, "HomeViewModel: Fail to read QR Code: ${imageUri}")
        }
    }

    /**
     * Generate new QR Code with the provided data
     */
    fun generateQRCode(data: String): Painter? {
        // TODO The QR Code must be generated in the same format as the one read
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512) // Adjust width/height as needed
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap[x, y] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }

            Log.d(TAG, "HomeViewModel: QR Code created from data: $data")

            return BitmapPainter(bitmap.asImageBitmap())

        } catch (e: Exception) {
            e.printStackTrace()
            return null
            Log.e(TAG, "HomeViewModel: Fail to generate QR Code: $data")
        }
    }

    /**
     * Returns a [QrCodeTypes] object based on the Barcode Value Type
     */
    private fun getQrCodeValueType(barcode: Barcode): QrCodeTypes? {
        val barcode =
        when (barcode.valueType) {
            Barcode.TYPE_UNKNOWN -> QrCodeTypes.Unknown("QR Code not recognized", "Raw value: ${barcode.rawValue}")
            Barcode.TYPE_CONTACT_INFO -> QrCodeTypes.Unknown("QR Code not supported yet", "Raw value: ${barcode.rawValue}")
            Barcode.TYPE_EMAIL -> { barcode.let{
                QrCodeTypes.Email(
                    type = it.email?.type.toString(),
                    body = it.email?.body,
                    address = it.email?.address,
                    subject = it.email?.subject,
                    rawValue = it.rawValue
                )
            } }
            Barcode.TYPE_ISBN -> QrCodeTypes.Unknown("QR Code not supported yet", "Raw value: ${barcode.rawValue}")
            Barcode.TYPE_PHONE -> QrCodeTypes.Unknown("QR Code not supported yet", "Raw value: ${barcode.rawValue}")
            Barcode.TYPE_PRODUCT -> QrCodeTypes.Unknown("QR Code not supported yet", "Raw value: ${barcode.rawValue}")
            Barcode.TYPE_SMS -> QrCodeTypes.Unknown("QR Code not supported yet", "Raw value: ${barcode.rawValue}")
            Barcode.TYPE_TEXT -> QrCodeTypes.Text(rawValue = barcode.rawValue)
            Barcode.TYPE_URL -> { barcode.let{
                QrCodeTypes.Url(
                    url = it.url?.url,
                    title = it.url?.title,
                    rawValue = it.rawValue
                )
            } }
            Barcode.TYPE_WIFI -> { barcode.let{
                QrCodeTypes.Wifi(
                    encryptionType = it.wifi?.encryptionType,
                    ssid = it.wifi?.ssid,
                    password = it.wifi?.password,
                    rawValue = it.rawValue
                )
            } }
            Barcode.TYPE_GEO -> QrCodeTypes.Unknown("QR Code not supported yet", "Raw value: ${barcode.rawValue}")
            Barcode.TYPE_CALENDAR_EVENT -> QrCodeTypes.Unknown("QR Code not supported yet", "Raw value: ${barcode.rawValue}")
            Barcode.TYPE_DRIVER_LICENSE -> QrCodeTypes.Unknown("QR Code not supported yet", "Raw value: ${barcode.rawValue}")
            else -> QrCodeTypes.Unknown("QR Code not recognized", "Raw value: ${barcode.rawValue}")
        }
        return barcode
    }

    /**
     * Read a QR Code directly from camera
     */
    fun readQrCodeFromCamera(context: Context) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom() // available on 16.1.0 and higher
            .build()

        val scanner = GmsBarcodeScanning.getClient(context, options)

        _uiState.update { HomeUiState.Loading }

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                _uiState.update { HomeUiState.Success(getQrCodeValueType(barcode)) }
                Log.d(TAG, "HomeViewModel: Read image from camera: ${barcode.rawValue}")
            }
            .addOnCanceledListener {
                // Task canceled
            }
            .addOnFailureListener { e ->
                // Task failed with an exception
            }

    }

    companion object {
        private const val TAG = "QRCodderApp"
    }
}

