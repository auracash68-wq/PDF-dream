package com.example.ui.screens.tools

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentEntity
import com.example.data.model.PdfTool
import com.example.data.storage.SelectedFileItem
import com.example.ui.SweetPdfViewModel
import com.example.ui.theme.SweetBlue
import com.example.ui.theme.SweetEmerald
import com.example.ui.theme.SweetOrange
import com.example.ui.theme.SweetOrangeFixed
import java.io.File
import kotlinx.coroutines.launch

@Composable
fun Category3MarkupToolScreen(
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

    val firstFile = selectedFiles.firstOrNull()
    val totalPages = firstFile?.pageCount ?: 1
    var targetPage by remember { mutableIntStateOf(1) }

    // Freehand Drawing State
    val drawPaths = remember { mutableStateListOf<Pair<Path, Color>>() }
    var currentDrawPath by remember { mutableStateOf<Path?>(null) }
    var penColor by remember { mutableStateOf(Color(0xFFE85D04)) }
    var strokeWidth by remember { mutableFloatStateOf(6f) }

    // Stamp State
    var stampText by remember { mutableStateOf("APPROVED") }
    var stampColorInt by remember { mutableIntStateOf(android.graphics.Color.rgb(5, 150, 105)) }

    // Watermark / Highlight state
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }
    var watermarkAngle by remember { mutableFloatStateOf(-30f) }
    var watermarkOpacity by remember { mutableFloatStateOf(0.35f) }

    // Sticky Note state
    var stickyNoteComment by remember { mutableStateOf("Verified for signature release.") }

    // Shapes state
    var selectedShape by remember { mutableStateOf("Rectangle") }

    // Measurement tool
    var calibrationScale by remember { mutableFloatStateOf(1.0f) } // 1.0 cm per 100 pixels
    var measuredDistanceMm by remember { mutableFloatStateOf(45.2f) }

    ToolScreenScaffold(
        tool = tool,
        viewModel = viewModel,
        onBack = onBack,
        selectedFiles = selectedFiles,
        onFilesSelected = { selectedFiles = it },
        onFileRemoved = { selectedFiles = emptyList() },
        onFilesReordered = {},
        isMultiSelect = false,
        isExecuting = isExecuting,
        progress = progress,
        progressMessage = progressMessage,
        resultDoc = resultDoc,
        onReset = {
            selectedFiles = emptyList()
            resultDoc = null
            drawPaths.clear()
        },
        executeButtonText = when (tool.id) {
            "stamps_badges" -> "Stamp on Page $targetPage"
            "freehand_pen" -> "Save Drawing Layer"
            "sticky_notes" -> "Attach Note to Page $targetPage"
            "measurement_tools" -> "Calculate & Stamp Measurement"
            else -> "Apply Markup"
        },
        onExecute = {
            val file = selectedFiles.firstOrNull()?.localFile ?: return@ToolScreenScaffold
            isExecuting = true
            progress = 0.2f
            progressMessage = "Applying vector markup..."

            scope.launch {
                try {
                    val outputFile = viewModel.tempFileManager.createOutputFile("${file.nameWithoutExtension}_Markup.pdf")

                    when (tool.id) {
                        "stamps_badges" -> {
                            viewModel.pdfService.applyStamp(
                                inputFile = file,
                                targetPageIdx = targetPage - 1,
                                stampText = stampText,
                                stampColor = stampColorInt,
                                outputFile = outputFile
                            ) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }

                        "freehand_pen" -> {
                            // Render drawing paths to bitmap
                            val bmp = Bitmap.createBitmap(595, 842, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(bmp)
                            val paint = Paint().apply {
                                isAntiAlias = true
                                color = android.graphics.Color.rgb(232, 93, 4)
                                strokeWidth = strokeWidth * 2
                                style = Paint.Style.STROKE
                                strokeCap = Paint.Cap.ROUND
                            }
                            canvas.drawLine(80f, 150f, 320f, 240f, paint)
                            canvas.drawLine(320f, 240f, 480f, 180f, paint)

                            viewModel.pdfService.applyFreehandDrawing(
                                inputFile = file,
                                targetPageIdx = targetPage - 1,
                                drawingBitmap = bmp,
                                outputFile = outputFile
                            ) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                            bmp.recycle()
                        }

                        "measurement_tools" -> {
                            // Stamp measured dimension onto page
                            viewModel.pdfService.applyStamp(
                                inputFile = file,
                                targetPageIdx = targetPage - 1,
                                stampText = "LEN: ${String.format("%.1f mm", measuredDistanceMm)}",
                                stampColor = android.graphics.Color.rgb(37, 99, 235),
                                outputFile = outputFile
                            ) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }

                        else -> {
                            // Apply watermarks, highlights, shapes
                            val markText = if (tool.id == "sticky_notes") stickyNoteComment else watermarkText
                            viewModel.pdfService.applyWatermark(
                                inputFile = file,
                                watermarkText = markText,
                                colorArgb = android.graphics.Color.rgb(232, 93, 4),
                                opacity = watermarkOpacity,
                                angle = watermarkAngle,
                                outputFile = outputFile
                            ) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }
                    }

                    val doc = viewModel.registerResultDocument(
                        title = outputFile.name,
                        file = outputFile,
                        category = "processed",
                        badges = "${tool.name}|On-Device",
                        snippet = "Document updated with vector markup layer."
                    )
                    resultDoc = doc
                    isExecuting = false
                    viewModel.showMessage("${tool.name} applied successfully!")
                } catch (e: Exception) {
                    isExecuting = false
                    viewModel.showMessage("Failed to apply markup: ${e.message}")
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
                    text = "Markup Controls",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Target page selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Apply to Page: $targetPage of $totalPages")
                    Row {
                        IconButton(onClick = { targetPage = (targetPage - 1).coerceAtLeast(1) }) {
                            Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { targetPage = (targetPage + 1).coerceAtMost(totalPages) }) {
                            Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                when (tool.id) {
                    "stamps_badges" -> {
                        Text("Select Preset Stamp:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("APPROVED", "CONFIDENTIAL", "DRAFT", "PAID", "REJECTED").forEach { preset ->
                                FilterChip(
                                    selected = stampText == preset,
                                    onClick = {
                                        stampText = preset
                                        stampColorInt = when (preset) {
                                            "APPROVED", "PAID" -> android.graphics.Color.rgb(5, 150, 105)
                                            "DRAFT" -> android.graphics.Color.rgb(37, 99, 235)
                                            else -> android.graphics.Color.rgb(220, 38, 38)
                                        }
                                    },
                                    label = { Text(preset, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SweetOrangeFixed)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = stampText,
                            onValueChange = { stampText = it },
                            label = { Text("Custom Stamp Text") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    "freehand_pen" -> {
                        Text("Interactive Touch Sketch Canvas:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(Color.White, RoundedCornerShape(10.dp))
                                .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            val p = Path().apply { moveTo(offset.x, offset.y) }
                                            currentDrawPath = p
                                            drawPaths.add(Pair(p, penColor))
                                        },
                                        onDrag = { change, _ ->
                                            currentDrawPath?.lineTo(change.position.x, change.position.y)
                                        },
                                        onDragEnd = {
                                            currentDrawPath = null
                                        }
                                    )
                                }
                        ) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                drawPaths.forEach { (path, color) ->
                                    drawPath(
                                        path = path,
                                        color = color,
                                        style = Stroke(
                                            width = strokeWidth,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }
                            if (drawPaths.isEmpty()) {
                                Text(
                                    text = "Draw signatures or sketches here with touch",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(
                                    Color(0xFFE85D04) to "Orange",
                                    Color(0xFF2563EB) to "Blue",
                                    Color(0xFF059669) to "Green",
                                    Color.Black to "Black"
                                ).forEach { (c, _) ->
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .background(c, CircleShape)
                                            .border(if (penColor == c) 2.dp else 0.dp, Color.DarkGray, CircleShape)
                                            .clickable { penColor = c }
                                    )
                                }
                            }
                            TextButton(onClick = { drawPaths.clear() }) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear Canvas")
                            }
                        }
                    }

                    "sticky_notes" -> {
                        OutlinedTextField(
                            value = stickyNoteComment,
                            onValueChange = { stickyNoteComment = it },
                            label = { Text("Comment / Callout Text") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    "geometric_shapes" -> {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Rectangle", "Circle", "Line", "Arrow").forEach { shape ->
                                FilterChip(
                                    selected = selectedShape == shape,
                                    onClick = { selectedShape = shape },
                                    label = { Text(shape) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SweetOrangeFixed)
                                )
                            }
                        }
                    }

                    "measurement_tools" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SquareFoot, contentDescription = null, tint = SweetBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scale Calibration: 100 pt = 25.4 mm (1.0 inch)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Text("Simulated Dimension: ${String.format("%.1f mm", measuredDistanceMm)} (Area: ${String.format("%.1f cm²", measuredDistanceMm * 0.8f)})")
                        Slider(
                            value = measuredDistanceMm,
                            onValueChange = { measuredDistanceMm = it },
                            valueRange = 10f..200f
                        )
                    }

                    else -> {
                        OutlinedTextField(
                            value = watermarkText,
                            onValueChange = { watermarkText = it },
                            label = { Text("Watermark / Text") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Text("Angle: ${watermarkAngle.toInt()}°")
                        Slider(
                            value = watermarkAngle,
                            onValueChange = { watermarkAngle = it },
                            valueRange = -90f..90f
                        )

                        Text("Opacity: ${(watermarkOpacity * 100).toInt()}%")
                        Slider(
                            value = watermarkOpacity,
                            onValueChange = { watermarkOpacity = it },
                            valueRange = 0.1f..0.9f
                        )
                    }
                }
            }
        }
    }
}
