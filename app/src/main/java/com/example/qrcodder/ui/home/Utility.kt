package com.example.qrcodder.ui.home

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.ext.SdkExtensions
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 *
 */
object Utility {

    fun isPhotoPickerAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2
        } else {
            false
        }
    }

    /**
     * Extension function to get QR Code raw values
     */
    fun QrCodeTypes?.getRawValue(): String{
        return when (this) {
            is QrCodeTypes.Url -> { "Raw Value: ${this.rawValue}" }
            is QrCodeTypes.Unknown -> { "Raw Value: ${this.text}" }
            else -> { "" }
        }
    }

    /**
     * Converts the given text into a clickable URL
     */
    fun convertTextToUrl(text: String): AnnotatedString {
        val urlLink = buildAnnotatedString {
            append("URL: ")
            withLink(
                LinkAnnotation.Url(
                    url = text,
                    styles = TextLinkStyles(
                        style = SpanStyle(textDecoration = TextDecoration.Underline),
                        hoveredStyle = SpanStyle(textDecoration = TextDecoration.Underline)
                    )
                )
            ) { append(text) }
        }
        return urlLink
    }
}
