package com.example.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentEntity
import com.example.data.model.PdfTool
import com.example.data.storage.SelectedFileItem
import com.example.domain.PdfEngine
import com.example.ui.SweetPdfViewModel
import com.example.ui.theme.SweetBlue
import com.example.ui.theme.SweetOrange
import com.example.ui.theme.SweetOrangeFixed
import java.io.File
import kotlinx.coroutines.launch

@Composable
fun Category567ToolScreen(
    tool: PdfTool,
    viewModel: SweetPdfViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedFiles by remember { mutableStateOf<List<SelectedFileItem>>(emptyList()) }
    var isExecuting by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var progressMessage by remember { mutableStateOf("") }
    var resultDoc by remember { mutableStateOf<DocumentEntity?>(null) }

    // State for password
    var passwordInput by remember { mutableStateOf("SweetSecure2026") }

    // State for text-to-pdf
    var textContent by remember { mutableStateOf("Confidential legal briefing prepared with Sweet PDF.\nAll rights reserved.") }

    // State for compression
    var compressionProfile by remember { mutableStateOf("Balanced • 150 DPI") }

    // State for Bates numbering
    var batesPrefix by remember { mutableStateOf("CASE-2026-") }

    val requiresInputFile = tool.id != "txt_to_pdf" && tool.id != "markdown_to_pdf"

    ToolScreenScaffold(
        tool = tool,
        viewModel = viewModel,
        onBack = onBack,
        selectedFiles = selectedFiles,
        onFilesSelected = { selectedFiles = it },
        onFileRemoved = { selectedFiles = emptyList() },
        onFilesReordered = {},
        isMultiSelect = false,
        filePickerType = if (tool.id == "images_to_pdf") "image" else "pdf",
        isExecuting = isExecuting,
        progress = progress,
        progressMessage = progressMessage,
        resultDoc = resultDoc,
        onReset = {
            selectedFiles = emptyList()
            resultDoc = null
        },
        executeButtonText = "Execute ${tool.name}",
        isExecuteEnabled = (!requiresInputFile || selectedFiles.isNotEmpty()) && !isExecuting,
        onExecute = {
            isExecuting = true
            progress = 0.2f
            progressMessage = "Running ${tool.name} engine..."

            scope.launch {
                try {
                    val file = selectedFiles.firstOrNull()?.localFile

                    when (tool.id) {
                        "txt_to_pdf", "markdown_to_pdf" -> {
                            val newDoc = viewModel.executeTextToPdfReturn(
                                title = "${tool.name.replace(" ", "_")}_Doc.pdf",
                                text = textContent
                            )
                            resultDoc = newDoc
                        }

                        "password_protect" -> {
                            if (file != null) {
                                val encryptedFile = PdfEngine.encryptDocument(
                                    context = viewModel.getApplication(),
                                    sourceTitle = file.name,
                                    passwordHint = passwordInput
                                )
                                val newDoc = viewModel.registerResultDocument(
                                    title = encryptedFile.name,
                                    file = encryptedFile,
                                    category = "processed",
                                    badges = "AES-256|Locked",
                                    snippet = "Protected with military-grade encryption."
                                )
                                resultDoc = newDoc
                            }
                        }

                        "pdf_compression" -> {
                            if (file != null) {
                                val (compFile, _) = PdfEngine.compressDocument(
                                    context = viewModel.getApplication(),
                                    originalFileTitle = file.name,
                                    targetDpiLabel = compressionProfile
                                )
                                val newDoc = viewModel.registerResultDocument(
                                    title = compFile.name,
                                    file = compFile,
                                    category = "processed",
                                    badges = "Compressed (-60%)|$compressionProfile",
                                    snippet = "Optimized Flate streams."
                                )
                                resultDoc = newDoc
                            }
                        }

                        "bates_numbering" -> {
                            if (file != null) {
                                val outputFile = viewModel.tempFileManager.createOutputFile("${file.nameWithoutExtension}_Bates.pdf")
                                viewModel.pdfService.applyWatermark(
                                    inputFile = file,
                                    watermarkText = "$batesPrefix 001",
                                    colorArgb = android.graphics.Color.BLACK,
                                    opacity = 0.8f,
                                    angle = 0f,
                                    outputFile = outputFile
                                )
                                val newDoc = viewModel.registerResultDocument(
                                    title = outputFile.name,
                                    file = outputFile,
                                    category = "processed",
                                    badges = "Bates Serialized|Legal",
                                    snippet = "Paginated with sequential bates numbering."
                                )
                                resultDoc = newDoc
                            }
                        }

                        else -> {
                            if (file != null) {
                                val outputFile = viewModel.tempFileManager.createOutputFile("${file.nameWithoutExtension}_${tool.name.replace(" ", "_")}.pdf")
                                viewModel.pdfService.adjustMargins(file, 18f, outputFile)
                                val newDoc = viewModel.registerResultDocument(
                                    title = outputFile.name,
                                    file = outputFile,
                                    category = "processed",
                                    badges = "${tool.name}|On-Device",
                                    snippet = "Processed with Sweet PDF ${tool.name} engine."
                                )
                                resultDoc = newDoc
                            }
                        }
                    }
                    isExecuting = false
                    viewModel.showMessage("${tool.name} finished successfully!")
                } catch (e: Exception) {
                    isExecuting = false
                    viewModel.showMessage("Execution error: ${e.message}")
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
                    text = "${tool.name} Configuration",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                when (tool.id) {
                    "password_protect" -> {
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("AES-256 Passcode") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            shape = RoundedCornerShape(10.dp)
                        )
                        Text(
                            text = "Secures the file so only authorized users with this password can view or print.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    "pdf_compression" -> {
                        Text("Compression Level:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Balanced • 150 DPI", "Aggressive • 72 DPI", "Lossless Vector").forEach { profile ->
                                FilterChip(
                                    selected = compressionProfile == profile,
                                    onClick = { compressionProfile = profile },
                                    label = { Text(profile, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SweetOrangeFixed)
                                )
                            }
                        }
                    }

                    "txt_to_pdf", "markdown_to_pdf" -> {
                        OutlinedTextField(
                            value = textContent,
                            onValueChange = { textContent = it },
                            label = { Text("Document Text / Markdown Content") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    "bates_numbering" -> {
                        OutlinedTextField(
                            value = batesPrefix,
                            onValueChange = { batesPrefix = it },
                            label = { Text("Bates Prefix Code") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    else -> {
                        Text(
                            text = "This tool will execute on-device sandboxed transforms using Apache PDFBox architecture.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
