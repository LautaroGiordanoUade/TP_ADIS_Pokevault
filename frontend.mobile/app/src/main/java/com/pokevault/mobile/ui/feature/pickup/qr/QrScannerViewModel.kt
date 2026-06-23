package com.pokevault.mobile.ui.feature.pickup.qr

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pokevault.mobile.ui.navigation.PokeMarketDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QrScannerUiState(
    val orderCode: String = "",
    val cameraPermissionGranted: Boolean = false,
    val cameraPermissionDenied: Boolean = false,
    val isProcessingScan: Boolean = false,
    val hasResolvedScan: Boolean = false,
    val scanResult: PickupQrScanResult? = null,
    val errorMessage: String? = null,
) {
    val shouldAnalyzeFrames: Boolean = cameraPermissionGranted && !isProcessingScan && !hasResolvedScan
}

sealed interface QrScannerEvent {
    data class OnCameraPermissionResult(val granted: Boolean) : QrScannerEvent
    data class OnQrCodeDetected(val rawValue: String) : QrScannerEvent
    data object OnRetryScan : QrScannerEvent
}

@HiltViewModel
class QrScannerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val orderCode: String = checkNotNull(savedStateHandle[PokeMarketDestination.QrScanner.orderCodeArgument])

    private val _uiState = MutableStateFlow(QrScannerUiState(orderCode = orderCode))
    val uiState = _uiState.asStateFlow()

    private var lastHandledRawValue: String? = null

    fun onEvent(event: QrScannerEvent) {
        when (event) {
            is QrScannerEvent.OnCameraPermissionResult -> {
                _uiState.update {
                    it.copy(
                        cameraPermissionGranted = event.granted,
                        cameraPermissionDenied = !event.granted,
                    )
                }
            }

            is QrScannerEvent.OnQrCodeDetected -> handleQrDetection(event.rawValue)
            QrScannerEvent.OnRetryScan -> {
                lastHandledRawValue = null
                _uiState.update {
                    it.copy(
                        isProcessingScan = false,
                        hasResolvedScan = false,
                        scanResult = null,
                        errorMessage = null,
                    )
                }
            }
        }
    }

    private fun handleQrDetection(rawValue: String) {
        val normalizedValue = rawValue.trim()
        if (normalizedValue.isBlank()) return
        if (_uiState.value.hasResolvedScan || _uiState.value.isProcessingScan) return
        if (lastHandledRawValue == normalizedValue) return

        lastHandledRawValue = normalizedValue
        _uiState.update { it.copy(isProcessingScan = true, errorMessage = null) }

        viewModelScope.launch {
            val result = PickupQrValidator.validate(
                rawValue = normalizedValue,
                expectedOrderCode = orderCode,
            )
            if (result.isAuthorized) {
                _uiState.update {
                    it.copy(
                        isProcessingScan = false,
                        hasResolvedScan = true,
                        scanResult = result,
                        errorMessage = null,
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isProcessingScan = false,
                        hasResolvedScan = true,
                        scanResult = result,
                        errorMessage = "QR_INVALIDO",
                    )
                }
            }
        }
    }
}
