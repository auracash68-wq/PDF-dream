package com.example.ui.dialogs


import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.PrimarySoft
import com.example.ui.theme.Success
import com.example.ui.theme.AppBackground
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.DisabledText
import com.example.ui.theme.Surface
import com.example.ui.theme.Border
import com.example.ui.theme.SweetOledBg
import com.example.ui.theme.SweetOledSurface

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DocumentEntity
import com.example.data.model.PdfTool
import com.example.ui.SweetPdfViewModel









@Composable
fun ToolExecutionDialog(
    tool: PdfTool,
    viewModel: SweetPdfViewModel,
    onDismiss: () -> Unit
) {
    val documents by viewModel.documents.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("tool_execution_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimarySoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = tool.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = tool.actionBadge.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                // Dedicated view based on tool type
                when (tool.id) {
                    "merge" -> MergeToolView(documents, viewModel)
                    "camera_scan" -> CameraScanToolView(viewModel)
                    "pdf_compression" -> CompressToolView(documents, viewModel)
                    "password_protect" -> PasswordProtectToolView(documents, viewModel)
                    "drawn_signature" -> SignatureToolView(documents, viewModel)
                    "txt_to_pdf", "markdown_to_pdf" -> TextToPdfToolView(viewModel, tool.id == "markdown_to_pdf")
                    else -> GenericToolRunnerView(tool, documents, viewModel)
                }
            }
        }
    }
}

@Composable
private fun MergeToolView(
    documents: List<DocumentEntity>,
    viewModel: SweetPdfViewModel
) {
    val selectedDocs = remember { mutableStateListOf<DocumentEntity>().apply { addAll(documents.take(2)) } }
    var outputName by remember { mutableStateOf("Consolidated_Master.pdf") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Select documents to combine into a unified file:",
            fontSize = 12.sp,
            color = TextSecondary
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppBackground)
                .padding(6.dp)
        ) {
            items(documents) { doc ->
                val isSelected = selectedDocs.contains(doc)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (isSelected) selectedDocs.remove(doc) else selectedDocs.add(doc)
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                if (isSelected) selectedDocs.remove(doc) else selectedDocs.add(doc)
                            },
                            colors = CheckboxDefaults.colors(checkedColor = Primary),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = doc.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "${doc.pageCount}p",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                }
            }
        }

        OutlinedTextField(
            value = outputName,
            onValueChange = { outputName = it },
            label = { Text("Output PDF Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                if (selectedDocs.isNotEmpty()) {
                    viewModel.executeMerge(selectedDocs.toList(), outputName)
                }
            },
            enabled = selectedDocs.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("execute_merge_btn")
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Execute Fast Merge (${selectedDocs.size} files)")
        }
    }
}

@Composable
private fun CameraScanToolView(
    viewModel: SweetPdfViewModel
) {
    var scanName by remember { mutableStateOf("Scan_${System.currentTimeMillis() % 10000}.pdf") }
    var filterMode by remember { mutableStateOf("Magic Color Boost") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Viewfinder simulation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(TextPrimary),
            contentAlignment = Alignment.Center
        ) {
            // Corner Reticles
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .border(1.dp, PrimaryLight, RoundedCornerShape(8.dp))
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.DocumentScanner,
                    contentDescription = null,
                    tint = PrimaryLight,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "QUAD EDGE PERSPECTIVE LOCKED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryLight,
                    letterSpacing = 1.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Magic Color Boost", "Otsu B&W", "Clean Greyscale").forEach { filter ->
                val isSel = filterMode == filter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) Primary else SurfaceSecondary)
                        .clickable { filterMode = filter }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.split(" ").first(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else TextSecondary
                    )
                }
            }
        }

        OutlinedTextField(
            value = scanName,
            onValueChange = { scanName = it },
            label = { Text("Scan Document Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = { viewModel.executeScan(scanName, filterMode) },
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("execute_camera_scan_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Capture & Unwarp Page")
        }
    }
}

@Composable
private fun CompressToolView(
    documents: List<DocumentEntity>,
    viewModel: SweetPdfViewModel
) {
    var selectedDoc by remember { mutableStateOf(documents.firstOrNull()) }
    var dpiRatio by remember { mutableStateOf("Balanced • 150 DPI") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Choose target file to downsample:",
            fontSize = 12.sp,
            color = TextSecondary
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppBackground)
                .padding(6.dp)
        ) {
            items(documents) { doc ->
                val isSel = selectedDoc?.id == doc.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) PrimarySoft else Color.Transparent)
                        .clickable { selectedDoc = doc }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = doc.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) Primary else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = doc.formattedSize,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("72 DPI (-80%)", "150 DPI (-65%)", "300 DPI (-30%)").forEach { opt ->
                val isSel = dpiRatio.contains(opt.split(" ").first())
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) Primary else SurfaceSecondary)
                        .clickable { dpiRatio = opt }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = opt,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else TextSecondary
                    )
                }
            }
        }

        Button(
            onClick = {
                selectedDoc?.let { doc ->
                    viewModel.executeCompress(doc, dpiRatio)
                }
            },
            enabled = selectedDoc != null,
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("execute_compress_btn")
        ) {
            Icon(imageVector = Icons.Default.Compress, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Compress Document")
        }
    }
}

@Composable
private fun PasswordProtectToolView(
    documents: List<DocumentEntity>,
    viewModel: SweetPdfViewModel
) {
    var selectedDoc by remember { mutableStateOf(documents.firstOrNull()) }
    var password by remember { mutableStateOf("sweet2026") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Select document to lock with AES-256 bit encryption:",
            fontSize = 12.sp,
            color = TextSecondary
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppBackground)
                .padding(6.dp)
        ) {
            items(documents) { doc ->
                val isSel = selectedDoc?.id == doc.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) PrimarySoft else Color.Transparent)
                        .clickable { selectedDoc = doc }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = doc.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) Primary else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Encryption Passcode") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                selectedDoc?.let { doc ->
                    viewModel.executeEncrypt(doc, password)
                }
            },
            enabled = selectedDoc != null && password.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("execute_encrypt_btn")
        ) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Encrypt & Protect (AES-256)")
        }
    }
}

@Composable
private fun SignatureToolView(
    documents: List<DocumentEntity>,
    viewModel: SweetPdfViewModel
) {
    var selectedDoc by remember { mutableStateOf(documents.firstOrNull()) }
    var signerName by remember { mutableStateOf("Alex Rivera") }
    val points = remember { mutableStateListOf<Offset>() }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Draw handwritten signature:",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Text(
                text = "Clear Pad",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Primary,
                modifier = Modifier.clickable { points.clear() }
            )
        }

        // Interactive Drawing Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .background(Surface)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        points.add(change.position)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (points.isNotEmpty()) {
                    val path = Path()
                    path.moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        path.lineTo(points[i].x, points[i].y)
                    }
                    drawPath(
                        path = path,
                        color = PrimaryDark,
                        style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                }
            }

            if (points.isEmpty()) {
                Text(
                    text = "✍ Sign here with finger or stylus",
                    fontSize = 12.sp,
                    color = DisabledText,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        OutlinedTextField(
            value = signerName,
            onValueChange = { signerName = it },
            label = { Text("Signer Full Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                selectedDoc?.let { doc ->
                    viewModel.executeSign(doc, signerName)
                }
            },
            enabled = selectedDoc != null && signerName.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("execute_sign_btn")
        ) {
            Icon(imageVector = Icons.Default.Draw, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Certify & Stamp Signature")
        }
    }
}

@Composable
private fun TextToPdfToolView(
    viewModel: SweetPdfViewModel,
    isMarkdown: Boolean
) {
    var title by remember { mutableStateOf(if (isMarkdown) "Project_Documentation.pdf" else "Meeting_Minutes.pdf") }
    var textContent by remember {
        mutableStateOf(
            if (isMarkdown) "# Document Architecture\n\n- Zero latency\n- Offline Skia rendering\n- 100% On-device privacy\n\n```kt\nval ready = true\n```"
            else "Agenda item 1: Review quarterly metrics.\nAction items approved by executive team.\nFollow-up scheduled for next Tuesday."
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Target PDF Filename") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = textContent,
            onValueChange = { textContent = it },
            label = { Text(if (isMarkdown) "Markdown Input" else "Text Notes Input") },
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = { viewModel.executeTextToPdf(title, textContent) },
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("execute_text_to_pdf_btn")
        ) {
            Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Generate & Format PDF")
        }
    }
}

@Composable
private fun GenericToolRunnerView(
    tool: PdfTool,
    documents: List<DocumentEntity>,
    viewModel: SweetPdfViewModel
) {
    var selectedDoc by remember { mutableStateOf(documents.firstOrNull()) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = tool.description,
            fontSize = 12.sp,
            color = TextSecondary
        )

        Text(
            text = "Select target document:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AppBackground)
                .padding(6.dp)
        ) {
            items(documents) { doc ->
                val isSel = selectedDoc?.id == doc.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) PrimarySoft else Color.Transparent)
                        .clickable { selectedDoc = doc }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = doc.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) Primary else TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${doc.pageCount}p",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceSecondary)
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Engine profile: Hardware Accelerated Vector Execution",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        Button(
            onClick = { viewModel.executeGenericTool(tool, selectedDoc) },
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("execute_generic_tool_btn")
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Apply ${tool.name}")
        }
    }
}
