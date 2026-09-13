package com.example.ui.screens.tools

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentEntity
import com.example.data.model.PdfTool
import com.example.data.storage.SelectedFileItem
import com.example.ui.SweetPdfViewModel
import com.example.ui.theme.SweetEmerald
import com.example.ui.theme.SweetOrange
import com.example.ui.theme.SweetOrangeFixed
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun Category4ScanCvToolScreen(
    tool: PdfTool,
    viewModel: SweetPdfViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var selectedFiles by remember { mutableStateOf<List<SelectedFileItem>>(emptyList()) }
    var isExecuting by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var progressMessage by remember { mutableStateOf("") }
    var resultDoc by remember { mutableStateOf<DocumentEntity?>(null) }

    // Filter Mode
    var filterMode by remember {
        mutableStateOf(
            when (tool.id) {
                "bw_filter" -> "Otsu B&W"
                "shadow_erase" -> "Shadow Erase"
                "magic_color" -> "Magic Color Boost"
                else -> "Magic Color Boost"
            }
        )
    }

    // OCR extracted text
    var ocrExtractedText by remember { mutableStateOf("") }

    // ID / Passport 2-in-1 bitmaps
    var frontBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var backBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cameraPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        if (bmp != null) {
            scope.launch {
                val tempFile = File(viewModel.tempFileManager.getTempDir(), "camera_capture_${System.currentTimeMillis()}.jpg")
                tempFile.outputStream().use { out ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 92, out)
                }
                val item = SelectedFileItem(
                    uri = android.net.Uri.fromFile(tempFile),
                    name = "Camera_Scan_${System.currentTimeMillis() % 1000}.jpg",
                    sizeBytes = tempFile.length(),
                    pageCount = 1,
                    localFile = tempFile
                )
                if (tool.id == "id_passport") {
                    if (frontBitmap == null) frontBitmap = bmp else backBitmap = bmp
                }
                selectedFiles = listOf(item)
            }
        }
    }

    val isImageMode = tool.id in listOf("camera_scan", "perspective_fix", "shadow_erase", "magic_color", "bw_filter", "id_passport", "book_spine_flattener")

    ToolScreenScaffold(
        tool = tool,
        viewModel = viewModel,
        onBack = onBack,
        selectedFiles = selectedFiles,
        onFilesSelected = { selectedFiles = it },
        onFileRemoved = {
            selectedFiles = emptyList()
            frontBitmap = null
            backBitmap = null
        },
        onFilesReordered = {},
        isMultiSelect = false,
        filePickerType = if (isImageMode) "image" else "pdf",
        isExecuting = isExecuting,
        progress = progress,
        progressMessage = progressMessage,
        resultDoc = resultDoc,
        onReset = {
            selectedFiles = emptyList()
            resultDoc = null
            ocrExtractedText = ""
            frontBitmap = null
            backBitmap = null
        },
        executeButtonText = when (tool.id) {
            "offline_ocr" -> "Run Neural OCR Extraction"
            "id_passport" -> "Generate ID 2-in-1 Document"
            else -> "Apply CV Filter & Export PDF"
        },
        onExecute = {
            val file = selectedFiles.firstOrNull()?.localFile ?: return@ToolScreenScaffold
            isExecuting = true
            progress = 0.15f
            progressMessage = "Starting CV processing..."

            scope.launch {
                try {
                    val outputFile = viewModel.tempFileManager.createOutputFile("${tool.name.replace(" ", "_")}_Output.pdf")

                    when (tool.id) {
                        "offline_ocr" -> {
                            val text = viewModel.pdfService.runOcrOnDocument(file, outputFile) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                            ocrExtractedText = text
                        }

                        "id_passport" -> {
                            val f = frontBitmap ?: BitmapFactory.decodeFile(file.absolutePath)
                            val b = backBitmap ?: f
                            viewModel.pdfService.createIdPassportPage(f, b, outputFile)
                        }

                        else -> {
                            // Process image with CV filters and export to PDF
                            withContext(Dispatchers.IO) {
                                val srcBmp = BitmapFactory.decodeFile(file.absolutePath)
                                val filtered = viewModel.pdfService.enhanceScanBitmap(srcBmp, filterMode)
                                viewModel.pdfService.createPdfFromImages(listOf(filtered), outputFile) { p, msg ->
                                    progress = p
                                    progressMessage = msg
                                }
                                filtered.recycle()
                                srcBmp.recycle()
                            }
                        }
                    }

                    val doc = viewModel.registerResultDocument(
                        title = outputFile.name,
                        file = outputFile,
                        category = "scans processed",
                        badges = "${tool.name}|Neural CV",
                        snippet = if (ocrExtractedText.isNotBlank()) ocrExtractedText.take(100) else "Scanned with computer vision pipeline."
                    )
                    resultDoc = doc
                    isExecuting = false
                    viewModel.showMessage("${tool.name} finished successfully!")
                } catch (e: Exception) {
                    isExecuting = false
                    viewModel.showMessage("Processing error: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Scanner & CV Parameters",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (isImageMode) {
                    Button(
                        onClick = { cameraPhotoLauncher.launch(null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SweetEmerald),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Capture with Live Camera")
                    }
                }

                when (tool.id) {
                    "offline_ocr" -> {
                        Text(
                            text = "ML Kit On-Device Neural OCR will scan all pages, recognize Latin & numeric text locally, and generate a fully searchable PDF.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (ocrExtractedText.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Recognized Text:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                TextButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(ocrExtractedText))
                                    viewModel.showMessage("Copied extracted text to clipboard!")
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Text")
                                }
                            }
                            OutlinedTextField(
                                value = ocrExtractedText,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    "id_passport" -> {
                        Text("ID Card / Passport Dual Capture:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.4f)
                                    .clickable { cameraPhotoLauncher.launch(null) },
                                colors = CardDefaults.cardColors(containerColor = SweetOrangeFixed.copy(alpha = 0.3f))
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    if (frontBitmap != null) {
                                        Image(bitmap = frontBitmap!!.asImageBitmap(), contentDescription = "Front")
                                    } else {
                                        Text("Tap to capture FRONT side", fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.4f)
                                    .clickable { cameraPhotoLauncher.launch(null) },
                                colors = CardDefaults.cardColors(containerColor = SweetOrangeFixed.copy(alpha = 0.3f))
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    if (backBitmap != null) {
                                        Image(bitmap = backBitmap!!.asImageBitmap(), contentDescription = "Back")
                                    } else {
                                        Text("Tap to capture BACK side", fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }

                    else -> {
                        Text("CV Enhancement Preset:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Magic Color Boost", "Clean Greyscale", "Otsu B&W", "Shadow Erase").forEach { mode ->
                                FilterChip(
                                    selected = filterMode == mode,
                                    onClick = { filterMode = mode },
                                    label = { Text(mode, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SweetOrangeFixed)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
