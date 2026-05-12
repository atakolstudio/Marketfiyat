package com.marketfiyat.feature.ocr.ui

import android.Manifest
import android.util.Size
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.marketfiyat.core.ui.components.MarketFiyatTopBar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

data class OcrResult(
    val rawText: String,
    val detectedProductName: String?,
    val detectedPrice: String?,
    val detectedDate: String?,
    val detectedMarket: String?
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OcrScannerScreen(
    onResultReceived: (OcrResult) -> Unit,
    onNavigateBack: () -> Unit
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    var isCapturing by remember { mutableStateOf(false) }
    var capturedText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            MarketFiyatTopBar(title = "Fiş Oku", onNavigateBack = onNavigateBack)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (cameraPermission.status.isGranted) {
                OcrCameraPreview(
                    onTextDetected = { text ->
                        if (!isCapturing) {
                            capturedText = text
                        }
                    }
                )

                // Overlay
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    capturedText?.let { text ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Okunan Metin", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = text.take(200),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 5
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { capturedText = null },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Tekrar Tara") }
                                    Button(
                                        onClick = {
                                            val result = parseOcrText(text)
                                            onResultReceived(result)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Kullan") }
                                }
                            }
                        }
                    } ?: run {
                        Text(
                            "Fişi kameraya tutun",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(bottom = 32.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Kamera izni gerekli")
                    Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                        Text("İzin Ver")
                    }
                }
            }
        }
    }
}

@Composable
private fun OcrCameraPreview(onTextDetected: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    var lastProcessTime = remember { 0L }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(executor) { imageProxy ->
                            val now = System.currentTimeMillis()
                            if (now - lastProcessTime < 2000L) {
                                imageProxy.close()
                                return@setAnalyzer
                            }
                            lastProcessTime = now
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                recognizer.process(image)
                                    .addOnSuccessListener { visionText ->
                                        if (visionText.text.isNotBlank()) {
                                            onTextDetected(visionText.text)
                                        }
                                    }
                                    .addOnCompleteListener { imageProxy.close() }
                            }
                        }
                    }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            recognizer.close()
        }
    }
}

private fun parseOcrText(text: String): OcrResult {
    val lines = text.lines()
    var productName: String? = null
    var price: String? = null
    var date: String? = null
    var market: String? = null

    val knownMarkets = listOf("A101", "BİM", "ŞOK", "Migros", "CarrefourSA", "Hakmar", "Metro")
    val priceRegex = Regex("""(\d+[.,]\d{2})""")
    val dateRegex = Regex("""\d{2}[./]\d{2}[./]\d{4}""")

    lines.forEach { line ->
        val trimmed = line.trim()
        // Detect market
        if (market == null) {
            knownMarkets.forEach { m ->
                if (trimmed.contains(m, ignoreCase = true)) market = m
            }
        }
        // Detect date
        if (date == null && dateRegex.containsMatchIn(trimmed)) {
            date = dateRegex.find(trimmed)?.value
        }
        // Detect price (last price-like number)
        priceRegex.find(trimmed)?.let { price = it.value }
        // First non-empty line as potential product name
        if (productName == null && trimmed.length > 3 && !trimmed.all { it.isDigit() || it == '.' || it == ',' || it == ' ' }) {
            productName = trimmed
        }
    }

    return OcrResult(
        rawText = text,
        detectedProductName = productName,
        detectedPrice = price,
        detectedDate = date,
        detectedMarket = market
    )
}
