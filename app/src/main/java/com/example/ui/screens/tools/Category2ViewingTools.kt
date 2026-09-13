package com.example.ui.screens.tools

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.speech.tts.TextToSpeech
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentEntity
import com.example.data.model.PdfTool
import com.example.data.storage.SelectedFileItem
import com.example.domain.BookmarkItem
import com.example.ui.SweetPdfViewModel
import com.example.ui.theme.SweetBlue
import com.example.ui.theme.SweetEmerald
import com.example.ui.theme.SweetOrange
import com.example.ui.theme.SweetOrangeFixed
import java.io.File
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun Category2ViewingToolScreen(
    tool: PdfTool,
    viewModel: SweetPdfViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedFiles by remember { mutableStateOf<List<SelectedFileItem>>(emptyList()) }
    var isExecuting by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var progressMessage by remember { mutableStateOf("") }
    var resultDoc by remember { mutableStateOf<DocumentEntity?>(null) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Pair<Int, String>>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    // Bookmark state
    val bookmarkList = remember { mutableStateListOf<BookmarkItem>() }
    var newBookmarkTitle by remember { mutableStateOf("") }
    var newBookmarkPage by remember { mutableIntStateOf(1) }

    // Text to Speech
    var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
    var isSpeaking by remember { mutableStateOf(false) }
    var ttsRate by remember { mutableFloatStateOf(1.0f) }
    var ttsPage by remember { mutableIntStateOf(1) }
    var extractedDocText by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }

    // Thumbnails state
    var renderedThumbnails by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    // Setup TextToSpeech
    DisposableEffect(Unit) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
        ttsInstance = tts
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    LaunchedEffect(selectedFiles) {
        val file = selectedFiles.firstOrNull()?.localFile
        if (file != null && file.exists()) {
            // Load bookmarks
            scope.launch {
                val bms = viewModel.pdfService.getBookmarks(file)
                bookmarkList.clear()
                bookmarkList.addAll(bms)

                // Pre-extract text for search and TTS
                extractedDocText = viewModel.pdfService.extractAllText(file)

                // Render thumbnails
                if (tool.id == "thumbnail_grid") {
                    withContext(Dispatchers.IO) {
                        try {
                            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                            val renderer = PdfRenderer(pfd)
                            val list = mutableListOf<Bitmap>()
                            val count = renderer.pageCount.coerceAtMost(16)
                            for (i in 0 until count) {
                                val page = renderer.openPage(i)
                                val bmp = Bitmap.createBitmap(150, (150 * 1.414).toInt(), Bitmap.Config.ARGB_8888)
                                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                page.close()
                                list.add(bmp)
                            }
                            renderer.close()
                            pfd.close()
                            renderedThumbnails = list
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    ToolScreenScaffold(
        tool = tool,
        viewModel = viewModel,
        onBack = {
            ttsInstance?.stop()
            onBack()
        },
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
            ttsInstance?.stop()
            selectedFiles = emptyList()
            resultDoc = null
            searchResults = emptyList()
            searchQuery = ""
        },
        executeButtonText = when (tool.id) {
            "multi_layout_viewer", "dark_mode_sepia" -> "Open in Advanced Reader"
            "bookmark_editor" -> "Save Bookmarked PDF"
            "full_text_search" -> "Run Search"
            "tts_audio" -> "Play Page $ttsPage"
            else -> "Open Document"
        },
        onExecute = {
            val file = selectedFiles.firstOrNull()?.localFile ?: return@ToolScreenScaffold
            when (tool.id) {
                "multi_layout_viewer", "dark_mode_sepia", "toc_navigator", "thumbnail_grid" -> {
                    // Open document in app's internal PDF viewer
                    val docEntity = DocumentEntity(
                        title = file.name,
                        pageCount = selectedFiles.first().pageCount,
                        fileSizeBytes = file.length(),
                        formattedSize = selectedFiles.first().formattedSize,
                        dateString = "Just now",
                        timestamp = System.currentTimeMillis(),
                        category = "processed",
                        isStarred = false,
                        isEncrypted = false,
                        badgesPipeSeparated = "${tool.name}|Reader",
                        filePath = file.absolutePath,
                        contentSnippet = "Viewing native document in offline reader."
                    )
                    viewModel.openViewer(docEntity)
                }

                "full_text_search" -> {
                    if (searchQuery.isNotBlank()) {
                        isSearching = true
                        val hits = mutableListOf<Pair<Int, String>>()
                        extractedDocText.forEach { (pageIdx, text) ->
                            if (text.contains(searchQuery, ignoreCase = true)) {
                                val matchStart = text.indexOf(searchQuery, ignoreCase = true)
                                val snippetStart = (matchStart - 20).coerceAtLeast(0)
                                val snippetEnd = (matchStart + searchQuery.length + 30).coerceAtMost(text.length)
                                val snippet = "..." + text.substring(snippetStart, snippetEnd).replace("\n", " ") + "..."
                                hits.add(Pair(pageIdx + 1, snippet))
                            }
                        }
                        searchResults = hits
                        isSearching = false
                        viewModel.showMessage("Found ${hits.size} match${if (hits.size == 1) "" else "es"}")
                    }
                }

                "bookmark_editor" -> {
                    scope.launch {
                        isExecuting = true
                        progress = 0.3f
                        progressMessage = "Writing outlines..."
                        val outputFile = viewModel.tempFileManager.createOutputFile("${file.nameWithoutExtension}_Bookmarked.pdf")
                        viewModel.pdfService.saveBookmarks(file, bookmarkList.toList(), outputFile)
                        val doc = viewModel.registerResultDocument(
                            title = outputFile.name,
                            file = outputFile,
                            category = "processed",
                            badges = "Bookmarks (${bookmarkList.size})|On-Device",
                            snippet = "PDF with ${bookmarkList.size} navigation outlines."
                        )
                        resultDoc = doc
                        isExecuting = false
                    }
                }

                "tts_audio" -> {
                    val pageText = extractedDocText[ttsPage - 1] ?: "Page has no extractable text."
                    ttsInstance?.setSpeechRate(ttsRate)
                    ttsInstance?.speak(pageText, TextToSpeech.QUEUE_FLUSH, null, "tts_page_$ttsPage")
                    isSpeaking = true
                }
            }
        }
    ) {
        val firstItem = selectedFiles.firstOrNull()
        val totalPages = firstItem?.pageCount ?: 1

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
                when (tool.id) {
                    "multi_layout_viewer", "dark_mode_sepia" -> {
                        Text(
                            text = "Reading & Layout Controls",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Launch high-performance on-device reader with custom night reading, zoom, and continuous vertical scroll modes.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    "full_text_search" -> {
                        Text("Offline Document Search", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search keyword or phrase") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            shape = RoundedCornerShape(10.dp)
                        )

                        if (searchResults.isNotEmpty()) {
                            Text(
                                text = "Matches (${searchResults.size}):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SweetBlue
                            )
                            searchResults.forEach { (pageNum, snippet) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            firstItem?.let {
                                                val doc = DocumentEntity(
                                                    title = it.name,
                                                    pageCount = it.pageCount,
                                                    fileSizeBytes = it.sizeBytes,
                                                    formattedSize = it.formattedSize,
                                                    dateString = "Just now",
                                                    timestamp = System.currentTimeMillis(),
                                                    category = "processed",
                                                    isStarred = false,
                                                    isEncrypted = false,
                                                    badgesPipeSeparated = "Search Match|Page $pageNum",
                                                    filePath = it.localFile.absolutePath,
                                                    contentSnippet = snippet
                                                )
                                                viewModel.openViewer(doc)
                                            }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Page $pageNum", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SweetOrange)
                                        Text(snippet, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }

                    "bookmark_editor" -> {
                        Text("PDF Outline & Bookmarks", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newBookmarkTitle,
                                onValueChange = { newBookmarkTitle = it },
                                label = { Text("Bookmark Title") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = newBookmarkPage.toString(),
                                onValueChange = { newBookmarkPage = it.toIntOrNull()?.coerceIn(1, totalPages) ?: 1 },
                                label = { Text("Page") },
                                modifier = Modifier.width(80.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (newBookmarkTitle.isNotBlank()) {
                                        bookmarkList.add(BookmarkItem(newBookmarkTitle, newBookmarkPage))
                                        newBookmarkTitle = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Bookmark", tint = SweetBlue)
                            }
                        }

                        if (bookmarkList.isEmpty()) {
                            Text("No bookmarks defined yet. Add chapter marks above.", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            bookmarkList.forEachIndexed { idx, bm ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = SweetOrange, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("${bm.title} (Page ${bm.pageNumber})", fontSize = 13.sp)
                                    }
                                    IconButton(
                                        onClick = { bookmarkList.removeAt(idx) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    "toc_navigator" -> {
                        Text("Interactive Table of Contents", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (bookmarkList.isEmpty()) {
                            Text("This document does not contain an embedded table of contents.", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            bookmarkList.forEach { bm ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            firstItem?.let {
                                                val doc = DocumentEntity(
                                                    title = it.name,
                                                    pageCount = it.pageCount,
                                                    fileSizeBytes = it.sizeBytes,
                                                    formattedSize = it.formattedSize,
                                                    dateString = "Just now",
                                                    timestamp = System.currentTimeMillis(),
                                                    category = "processed",
                                                    isStarred = false,
                                                    isEncrypted = false,
                                                    badgesPipeSeparated = "TOC Jump|Page ${bm.pageNumber}",
                                                    filePath = it.localFile.absolutePath,
                                                    contentSnippet = "TOC Destination: ${bm.title}"
                                                )
                                                viewModel.openViewer(doc)
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(bm.title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("p. ${bm.pageNumber}", fontSize = 12.sp, color = SweetBlue)
                                }
                            }
                        }
                    }

                    "tts_audio" -> {
                        Text("Native Offline Text-to-Speech", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Target Page: $ttsPage of $totalPages")
                            Row {
                                IconButton(onClick = { ttsPage = (ttsPage - 1).coerceAtLeast(1) }) {
                                    Text("-", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                                IconButton(onClick = { ttsPage = (ttsPage + 1).coerceAtMost(totalPages) }) {
                                    Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Text("Voice Speech Rate: ${String.format("%.1fx", ttsRate)}")
                        Slider(
                            value = ttsRate,
                            onValueChange = { ttsRate = it },
                            valueRange = 0.5f..2.0f
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val text = extractedDocText[ttsPage - 1] ?: ""
                                    ttsInstance?.setSpeechRate(ttsRate)
                                    ttsInstance?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_$ttsPage")
                                    isSpeaking = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SweetBlue)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Speak Page")
                            }

                            Button(
                                onClick = {
                                    ttsInstance?.stop()
                                    isSpeaking = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop")
                            }
                        }
                    }

                    "thumbnail_grid" -> {
                        Text("Page Previews (${renderedThumbnails.size} loaded)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (renderedThumbnails.isEmpty()) {
                            Text("Rendering thumbnails...", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                itemsIndexed(renderedThumbnails) { idx, bmp ->
                                    Card(
                                        shape = RoundedCornerShape(6.dp),
                                        elevation = CardDefaults.cardElevation(2.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(4.dp)
                                        ) {
                                            Image(
                                                bitmap = bmp.asImageBitmap(),
                                                contentDescription = "Page ${idx + 1}",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .aspectRatio(1f / 1.414f)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("Page ${idx + 1}", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
