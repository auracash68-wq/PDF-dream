package com.example.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Category1OrganizeToolScreen(
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

    // Tool-specific configurations
    var outputName by remember { mutableStateOf("${tool.name.replace(" ", "_")}_${System.currentTimeMillis() % 10000}.pdf") }
    val firstFile = selectedFiles.firstOrNull()
    val totalPages = firstFile?.pageCount ?: 1

    // State for Split
    var splitMode by remember { mutableStateOf("range") } // "range" or "every_n"
    var splitEveryN by remember { mutableIntStateOf(1) }
    var customRangeText by remember { mutableStateOf("1-$totalPages") }

    // State for Page Selection (Extract, Delete, Rotate, Duplicate)
    val selectedPages = remember { mutableStateListOf<Int>() }

    // State for Rotate
    var rotateAngle by remember { mutableIntStateOf(90) } // 90, 180, 270
    var rotateAllPages by remember { mutableStateOf(true) }

    // State for Reorder
    val reorderedPageIndices = remember { mutableStateListOf<Int>() }

    // State for Crop
    var cropPercent by remember { mutableFloatStateOf(0.10f) }

    // State for Halve
    var isVerticalHalve by remember { mutableStateOf(true) }

    // State for N-Up
    var nUpPages by remember { mutableIntStateOf(4) } // 2, 4, 9, 16

    // State for Add Blank
    var insertBlankAt by remember { mutableIntStateOf(totalPages + 1) }
    var blankPageSize by remember { mutableStateOf("A4") }
    var blankIsLandscape by remember { mutableStateOf(false) }

    // State for Adjust Margins
    var marginPoints by remember { mutableFloatStateOf(36f) }

    // State for Overlay/Underlay
    var isOverlayMode by remember { mutableStateOf(true) }
    var layerOpacity by remember { mutableFloatStateOf(0.85f) }

    // State for Duplicate
    var duplicateCopies by remember { mutableIntStateOf(1) }

    val isMultiSelect = tool.id == "merge" || tool.id == "overlay_underlay"

    fun resetState() {
        selectedFiles = emptyList()
        isExecuting = false
        progress = 0f
        progressMessage = ""
        resultDoc = null
        selectedPages.clear()
        reorderedPageIndices.clear()
        outputName = "${tool.name.replace(" ", "_")}_${System.currentTimeMillis() % 10000}.pdf"
    }

    ToolScreenScaffold(
        tool = tool,
        viewModel = viewModel,
        onBack = onBack,
        selectedFiles = selectedFiles,
        onFilesSelected = { newFiles ->
            selectedFiles = newFiles
            selectedPages.clear()
            val pages = newFiles.firstOrNull()?.pageCount ?: 1
            customRangeText = if (pages > 1) "1-${pages / 2}, ${(pages / 2) + 1}-$pages" else "1"
            reorderedPageIndices.clear()
            for (i in 0 until pages) reorderedPageIndices.add(i)
            insertBlankAt = pages + 1
        },
        onFileRemoved = { idx ->
            val list = selectedFiles.toMutableList()
            if (idx in list.indices) {
                list.removeAt(idx)
                selectedFiles = list
            }
        },
        onFilesReordered = { reordered ->
            selectedFiles = reordered
        },
        isMultiSelect = isMultiSelect,
        isExecuting = isExecuting,
        progress = progress,
        progressMessage = progressMessage,
        resultDoc = resultDoc,
        onReset = { resetState() },
        executeButtonText = "Execute ${tool.name}",
        isExecuteEnabled = selectedFiles.isNotEmpty() && !isExecuting && (tool.id != "merge" || selectedFiles.size >= 2),
        onExecute = {
            isExecuting = true
            progress = 0.05f
            progressMessage = "Preparing ${tool.name} engine..."

            scope.launch {
                try {
                    val outputFile = viewModel.tempFileManager.createOutputFile(outputName)

                    when (tool.id) {
                        "merge" -> {
                            val inputFiles = selectedFiles.map { it.localFile }
                            viewModel.pdfService.mergePdfs(inputFiles, outputFile) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }
                        "split" -> {
                            val targetFile = selectedFiles.first().localFile
                            val splitFiles = viewModel.pdfService.splitPdf(
                                targetFile,
                                if (splitMode == "every_n") splitEveryN else 0,
                                if (splitMode == "range") customRangeText else "",
                                viewModel.tempFileManager.getVaultDir()
                            ) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                            if (splitFiles.isNotEmpty()) {
                                val firstSplit = splitFiles.first()
                                val doc = viewModel.registerResultDocument(
                                    title = firstSplit.name,
                                    file = firstSplit,
                                    category = "processed",
                                    badges = "Split (${splitFiles.size} parts)|On-Device",
                                    snippet = "Document split into ${splitFiles.size} sub-documents."
                                )
                                resultDoc = doc
                                isExecuting = false
                                return@launch
                            }
                        }
                        "extract_pages" -> {
                            val targetFile = selectedFiles.first().localFile
                            val pagesToExtract = if (selectedPages.isEmpty()) listOf(0) else selectedPages.toList()
                            viewModel.pdfService.extractPages(targetFile, pagesToExtract, outputFile) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }
                        "delete_pages" -> {
                            val targetFile = selectedFiles.first().localFile
                            viewModel.pdfService.deletePages(targetFile, selectedPages.toSet(), outputFile) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }
                        "rotate_pages" -> {
                            val targetFile = selectedFiles.first().localFile
                            val pages = if (rotateAllPages) emptySet() else selectedPages.toSet()
                            viewModel.pdfService.rotatePages(targetFile, pages, rotateAngle, outputFile) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }
                        "reorder_pages" -> {
                            val targetFile = selectedFiles.first().localFile
                            viewModel.pdfService.reorderPages(targetFile, reorderedPageIndices.toList(), outputFile) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }
                        "crop_pdf" -> {
                            val targetFile = selectedFiles.first().localFile
                            viewModel.pdfService.cropPdf(targetFile, emptySet(), cropPercent, outputFile) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }
                        "halve_pages" -> {
                            val targetFile = selectedFiles.first().localFile
                            viewModel.pdfService.halvePages(targetFile, isVerticalHalve, outputFile) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }
                        "n_up" -> {
                            val targetFile = selectedFiles.first().localFile
                            viewModel.pdfService.nUpPdf(targetFile, nUpPages, outputFile) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }
                        "reverse_order" -> {
                            val targetFile = selectedFiles.first().localFile
                            viewModel.pdfService.reversePages(targetFile, outputFile) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }
                        "duplicate_pages" -> {
                            val targetFile = selectedFiles.first().localFile
                            val pages = if (selectedPages.isEmpty()) setOf(0) else selectedPages.toSet()
                            viewModel.pdfService.duplicatePages(targetFile, pages, duplicateCopies, outputFile) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }
                        "add_blank" -> {
                            val targetFile = selectedFiles.first().localFile
                            viewModel.pdfService.addBlankPage(targetFile, insertBlankAt, blankPageSize, blankIsLandscape, outputFile) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }
                        "adjust_margins" -> {
                            val targetFile = selectedFiles.first().localFile
                            viewModel.pdfService.adjustMargins(targetFile, marginPoints, outputFile) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }
                        "overlay_underlay" -> {
                            val baseFile = selectedFiles[0].localFile
                            val layerFile = selectedFiles.getOrNull(1)?.localFile ?: baseFile
                            viewModel.pdfService.overlayUnderlay(baseFile, layerFile, isOverlayMode, layerOpacity, outputFile) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }
                        "deskew_pages" -> {
                            val targetFile = selectedFiles.first().localFile
                            viewModel.pdfService.deskewPages(targetFile, outputFile) { p, msg ->
                                progress = p
                                progressMessage = msg
                            }
                        }
                    }

                    // Register output in Database & History
                    val registered = viewModel.registerResultDocument(
                        title = outputFile.name,
                        file = outputFile,
                        category = "processed",
                        badges = "${tool.name}|On-Device",
                        snippet = "Processed with Sweet PDF ${tool.name} engine."
                    )
                    resultDoc = registered
                    isExecuting = false
                    viewModel.showMessage("${tool.name} finished successfully!")
                } catch (e: Exception) {
                    isExecuting = false
                    viewModel.showMessage("Operation failed: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    ) {
        // Tool-specific controls
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
                    text = "Tool Configuration",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Output Filename field
                OutlinedTextField(
                    value = outputName,
                    onValueChange = { outputName = it },
                    label = { Text("Output PDF Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                when (tool.id) {
                    "merge" -> {
                        if (selectedFiles.size < 2) {
                            Text(
                                text = "Please select at least 2 PDF files to merge into one.",
                                fontSize = 12.sp,
                                color = SweetOrange,
                                fontWeight = FontWeight.Medium
                            )
                        } else {
                            Text(
                                text = "Drag or use arrow buttons in the list above to adjust the merge sequence.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    "split" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = splitMode == "range",
                                onClick = { splitMode = "range" },
                                label = { Text("Custom Ranges") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SweetOrangeFixed)
                            )
                            FilterChip(
                                selected = splitMode == "every_n",
                                onClick = { splitMode = "every_n" },
                                label = { Text("Every N Pages") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SweetOrangeFixed)
                            )
                        }

                        if (splitMode == "range") {
                            OutlinedTextField(
                                value = customRangeText,
                                onValueChange = { customRangeText = it },
                                label = { Text("Page Ranges (e.g. 1-3, 4, 5-8)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            Text(
                                text = "Total document pages available: $totalPages",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Split every $splitEveryN page(s)")
                                Row {
                                    IconButton(onClick = { splitEveryN = (splitEveryN - 1).coerceAtLeast(1) }) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                    }
                                    IconButton(onClick = { splitEveryN = (splitEveryN + 1).coerceAtMost(totalPages) }) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase")
                                    }
                                }
                            }
                        }
                    }

                    "extract_pages", "delete_pages", "duplicate_pages" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (tool.id == "delete_pages") "Select Pages to Delete:" else "Select Target Pages:",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                            Row {
                                TextButton(onClick = {
                                    selectedPages.clear()
                                    for (i in 0 until totalPages) selectedPages.add(i)
                                }) {
                                    Text("All", fontSize = 11.sp)
                                }
                                TextButton(onClick = { selectedPages.clear() }) {
                                    Text("Clear", fontSize = 11.sp)
                                }
                            }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (p in 0 until totalPages) {
                                val isSel = selectedPages.contains(p)
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        if (isSel) selectedPages.remove(p) else selectedPages.add(p)
                                    },
                                    label = { Text("Page ${p + 1}") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (tool.id == "delete_pages") Color(0xFFFFD5D5) else SweetOrangeFixed
                                    )
                                )
                            }
                        }

                        if (tool.id == "duplicate_pages") {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Number of Copies: $duplicateCopies")
                                Row {
                                    IconButton(onClick = { duplicateCopies = (duplicateCopies - 1).coerceAtLeast(1) }) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                    }
                                    IconButton(onClick = { duplicateCopies = (duplicateCopies + 1).coerceAtMost(10) }) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase")
                                    }
                                }
                            }
                        }
                    }

                    "rotate_pages" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Apply to All Pages")
                            Switch(checked = rotateAllPages, onCheckedChange = { rotateAllPages = it })
                        }

                        if (!rotateAllPages) {
                            Text("Select Pages to Rotate:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                for (p in 0 until totalPages) {
                                    val isSel = selectedPages.contains(p)
                                    FilterChip(
                                        selected = isSel,
                                        onClick = {
                                            if (isSel) selectedPages.remove(p) else selectedPages.add(p)
                                        },
                                        label = { Text("Page ${p + 1}") }
                                    )
                                }
                            }
                        }

                        Text("Rotation Angle:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(90 to "90° CW", 180 to "180° Flip", 270 to "90° CCW").forEach { (deg, label) ->
                                FilterChip(
                                    selected = rotateAngle == deg,
                                    onClick = { rotateAngle = deg },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SweetOrangeFixed)
                                )
                            }
                        }
                    }

                    "reorder_pages" -> {
                        Text(
                            text = "Arrange Page Sequence:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        reorderedPageIndices.forEachIndexed { pos, pageIdx ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Slot ${pos + 1}: Original Page ${pageIdx + 1}", fontWeight = FontWeight.Medium)
                                    Row {
                                        if (pos > 0) {
                                            IconButton(
                                                onClick = {
                                                    val temp = reorderedPageIndices[pos]
                                                    reorderedPageIndices[pos] = reorderedPageIndices[pos - 1]
                                                    reorderedPageIndices[pos - 1] = temp
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.SwapVert, contentDescription = "Move")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "crop_pdf" -> {
                        Text("Crop Margin: ${(cropPercent * 100).toInt()}% from each edge")
                        Slider(
                            value = cropPercent,
                            onValueChange = { cropPercent = it },
                            valueRange = 0.02f..0.30f,
                            colors = SliderDefaults.colors(thumbColor = SweetOrange, activeTrackColor = SweetOrange)
                        )
                    }

                    "halve_pages" -> {
                        Text("Split Orientation:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = isVerticalHalve,
                                onClick = { isVerticalHalve = true },
                                label = { Text("Vertical (Left/Right book cut)") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SweetOrangeFixed)
                            )
                            FilterChip(
                                selected = !isVerticalHalve,
                                onClick = { isVerticalHalve = false },
                                label = { Text("Horizontal (Top/Bottom)") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SweetOrangeFixed)
                            )
                        }
                    }

                    "n_up" -> {
                        Text("Pages per Sheet Layout:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(2 to "2-Up", 4 to "4-Up (Quad)", 9 to "9-Up", 16 to "16-Up").forEach { (num, label) ->
                                FilterChip(
                                    selected = nUpPages == num,
                                    onClick = { nUpPages = num },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SweetOrangeFixed)
                                )
                            }
                        }
                    }

                    "add_blank" -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Insert at Page Position: $insertBlankAt")
                            Row {
                                IconButton(onClick = { insertBlankAt = (insertBlankAt - 1).coerceAtLeast(1) }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease")
                                }
                                IconButton(onClick = { insertBlankAt = (insertBlankAt + 1).coerceAtMost(totalPages + 1) }) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase")
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = blankPageSize == "A4",
                                onClick = { blankPageSize = "A4" },
                                label = { Text("A4 Paper") }
                            )
                            FilterChip(
                                selected = blankPageSize == "Letter",
                                onClick = { blankPageSize = "Letter" },
                                label = { Text("US Letter") }
                            )
                            FilterChip(
                                selected = blankIsLandscape,
                                onClick = { blankIsLandscape = !blankIsLandscape },
                                label = { Text(if (blankIsLandscape) "Landscape" else "Portrait") }
                            )
                        }
                    }

                    "adjust_margins" -> {
                        Text("Margin Width: ${(marginPoints / 72f * 2.54f).let { String.format("%.1f cm", it) }} (${marginPoints.toInt()} pt)")
                        Slider(
                            value = marginPoints,
                            onValueChange = { marginPoints = it },
                            valueRange = 10f..100f,
                            colors = SliderDefaults.colors(thumbColor = SweetOrange, activeTrackColor = SweetOrange)
                        )
                    }

                    "overlay_underlay" -> {
                        if (selectedFiles.size < 2) {
                            Text(
                                text = "Please select a 2nd PDF file to use as the overlay/underlay layer.",
                                fontSize = 12.sp,
                                color = SweetOrange
                            )
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = isOverlayMode,
                                    onClick = { isOverlayMode = true },
                                    label = { Text("Overlay (Top Layer)") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SweetOrangeFixed)
                                )
                                FilterChip(
                                    selected = !isOverlayMode,
                                    onClick = { isOverlayMode = false },
                                    label = { Text("Underlay (Background)") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = SweetOrangeFixed)
                                )
                            }
                            Text("Layer Opacity: ${(layerOpacity * 100).toInt()}%")
                            Slider(
                                value = layerOpacity,
                                onValueChange = { layerOpacity = it },
                                valueRange = 0.1f..1.0f
                            )
                        }
                    }

                    "deskew_pages" -> {
                        Text(
                            text = "Auto-Deskew will scan all pages, analyze baseline angles, and rectify orientation on-device.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    "reverse_order" -> {
                        Text(
                            text = "Inverts the entire page sequence from back to front ($totalPages pages -> Page $totalPages will become Page 1).",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
