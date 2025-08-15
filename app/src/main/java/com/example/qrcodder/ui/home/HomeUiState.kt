package com.example.qrcodder.ui.home

sealed interface HomeUiState {
    data class Success(val qrCodeResult: QrCodeTypes?) : HomeUiState
    data object Loading : HomeUiState
    data object Waiting : HomeUiState
    data class Error(val errorMessage: Int) : HomeUiState
}