package com.pokevault.mobile.ui.feature.pickup.qr

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.pokevault.mobile.R
import com.pokevault.mobile.ui.theme.MarketOrange
import com.pokevault.mobile.ui.theme.Muted
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun QrScannerScreen(
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onBack: () -> Unit,
    onAuthorized: (PickupQrScanResult) -> Unit,
    viewModel: QrScannerViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    val cameraProviderFuture = remember(context) { ProcessCameraProvider.getInstance(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onEvent(QrScannerEvent.OnCameraPermissionResult(granted))
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }

    DisposableEffect(state.shouldAnalyzeFrames, lifecycleOwner) {
        if (!state.shouldAnalyzeFrames) {
            if (cameraProviderFuture.isDone) {
                cameraProviderFuture.get().unbindAll()
            }
            onDispose { }
        } else {
            val mainExecutor = ContextCompat.getMainExecutor(context)
            val listener = Runnable {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            processImageProxy(
                                imageProxy = imageProxy,
                                barcodeScanner = barcodeScanner,
                                onQrDetected = { rawValue ->
                                    viewModel.onEvent(QrScannerEvent.OnQrCodeDetected(rawValue))
                                },
                            )
                        }
                    }

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis,
                )
            }
            cameraProviderFuture.addListener(listener, mainExecutor)

            onDispose {
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101116))
            .padding(contentPadding)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = stringResource(R.string.qr_scanner_back), tint = Color.White)
            }
            Column {
                Text(stringResource(R.string.qr_scanner_title), color = Color.White, fontWeight = FontWeight.ExtraBold)
                Text(
                    stringResource(R.string.qr_scanner_subtitle, state.orderCode),
                    color = Muted,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF17181F), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.cameraPermissionGranted -> {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize(),
                    )
                    ScanOverlay(modifier = Modifier.align(Alignment.Center))
                }

                state.cameraPermissionDenied -> {
                    PermissionDeniedContent(
                        onRetry = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    )
                }

                else -> {
                    CameraPreparingContent()
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        StatusCard(
            state = state,
            onRetry = { viewModel.onEvent(QrScannerEvent.OnRetryScan) },
            onConfirmAuthorization = { result -> onAuthorized(result) },
        )
    }
}

@Composable
private fun CameraPreparingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = MarketOrange, modifier = Modifier.size(42.dp))
        Text(stringResource(R.string.qr_scanner_preparing), color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PermissionDeniedContent(
    onRetry: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.padding(24.dp),
    ) {
        Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = MarketOrange, modifier = Modifier.size(48.dp))
        Text(
            text = stringResource(R.string.qr_scanner_permission_denied_title),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = stringResource(R.string.qr_scanner_permission_denied_message),
            color = Muted,
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MarketOrange, contentColor = Color.Black),
        ) {
            Text(stringResource(R.string.qr_scanner_permission_retry), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatusCard(
    state: QrScannerUiState,
    onRetry: () -> Unit,
    onConfirmAuthorization: (PickupQrScanResult) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1D23)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val scanResult = state.scanResult
            when {
                scanResult?.isAuthorized == true -> {
                    Text(stringResource(R.string.qr_scanner_success_title), color = MarketOrange, fontWeight = FontWeight.ExtraBold)
                    Text(stringResource(R.string.qr_scanner_success_message, scanResult.rawValue), color = Color.White)
                    Button(
                        onClick = { onConfirmAuthorization(scanResult) },
                        colors = ButtonDefaults.buttonColors(containerColor = MarketOrange, contentColor = Color.Black),
                    ) {
                        Text(stringResource(R.string.qr_scanner_use_result), fontWeight = FontWeight.Bold)
                    }
                }

                state.errorMessage == "QR_INVALIDO" && scanResult != null -> {
                    Text(stringResource(R.string.qr_scanner_invalid_title), color = Color(0xFFFF6B6B), fontWeight = FontWeight.ExtraBold)
                    Text(
                        stringResource(R.string.qr_scanner_invalid_message, state.orderCode, scanResult.rawValue),
                        color = Color.White,
                    )
                    Button(
                        onClick = onRetry,
                        colors = ButtonDefaults.buttonColors(containerColor = MarketOrange, contentColor = Color.Black),
                    ) {
                        Text(stringResource(R.string.qr_scanner_retry), fontWeight = FontWeight.Bold)
                    }
                }

                state.isProcessingScan -> {
                    Text(stringResource(R.string.qr_scanner_processing), color = Color.White, fontWeight = FontWeight.Bold)
                }

                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = MarketOrange)
                        Text(
                            text = stringResource(R.string.qr_scanner_instruction),
                            color = Color.White,
                            modifier = Modifier.padding(start = 10.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanOverlay(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.72f)
            .height(220.dp)
            .border(width = 2.dp, color = MarketOrange, shape = RoundedCornerShape(20.dp)),
    ) {
        Text(
            text = stringResource(R.string.qr_scanner_frame_hint),
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(999.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    barcodeScanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onQrDetected: (String) -> Unit,
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
    barcodeScanner.process(inputImage)
        .addOnSuccessListener { barcodes ->
            barcodes.firstOrNull { barcode -> barcode.rawValue != null }?.rawValue?.let(onQrDetected)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}
