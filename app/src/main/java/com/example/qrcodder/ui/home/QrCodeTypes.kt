package com.example.qrcodder.ui.home

//TODO Remove QRCodeTypes. No need to create a new object, pass barcode to state and get data directly from barcode instead
/**
 * Contains information of each type of QR Code defined in [com.google.mlkit.vision.barcode.common.Barcode.BarcodeValueType]
 */
sealed interface QrCodeTypes {
    data class ContactInfo(val title: String?,
                           val name: String?,
                           val phones: String?,
                           val addresses: String?,
                           val emails: String?,
                           val organization: String?,
                           val urls: String?,
                           val rawValue: String?) : QrCodeTypes
    data class Email(val type: String?, val address: String?, val subject: String?, val body: String?, val rawValue: String?) : QrCodeTypes
    data class Wifi(val encryptionType: Int?, val ssid: String?, val password: String?, val rawValue: String?) : QrCodeTypes
    data class Url(val url: String?, val title: String?, val rawValue: String?) : QrCodeTypes
    data class Text(val rawValue: String?) : QrCodeTypes
    data class Unknown(val text: String,  val rawValue: String) : QrCodeTypes
}