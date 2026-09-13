package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentEntity
import com.example.data.model.PdfToolRegistry
import com.example.ui.SweetPdfViewModel
import com.example.ui.theme.SweetBlue
import com.example.ui.theme.SweetBlueFixed
import com.example.ui.theme.SweetEmerald
import com.example.ui.theme.SweetEmeraldFixed
import com.example.ui.theme.SweetOrange
import com.example.ui.theme.SweetOrangeFixed

@Composable
fun HistoryScreen(
    viewModel: SweetPdfViewModel
) {
    val documents by viewModel.documents.collectAsState()
    val searchQuery by viewModel.historySearchQuery.collectAsState()
    val activeFilter by viewModel.historyFilter.collectAsState()
    val isBatchMode by viewModel.isBatchMode.collectAsState()
    val selectedDocIds by viewModel.selectedDocIds.collectAsState()
    val isAscending by viewModel.isHistorySortAscending.collectAsState()

    // Filter and sort documents
    val filteredDocs = documents.filter { doc ->
        val matchesSearch = searchQuery.isBlank() ||
            doc.title.contains(searchQuery, ignoreCase = true) ||
            doc.badgesPipeSeparated.contains(searchQuery, ignoreCase = true)

        val matchesTab = when (activeFilter) {
            "all" -> true
            "processed" -> doc.category.contains("processed")
            "scans" -> doc.category.contains("scans")
            "signed" -> doc.category.contains("signed")
            "starred" -> doc.isStarred
            else -> true
        }
        matchesSearch && matchesTab
    }.let { list ->
        if (isAscending) list.sortedBy { it.timestamp } else list.sortedByDescending { it.timestamp }
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = if (isBatchMode && selectedDocIds.isNotEmpty()) 140.dp else 80.dp)
        ) {
            // Header: Title & Batch Select Action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "History & Files",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Track and manage all processed documents",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Button(
                    onClick = { viewModel.toggleBatchMode() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBatchMode) SweetOrangeFixed else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("history_batch_toggle_btn")
                ) {
                    Text(
                        text = if (isBatchMode) "Cancel" else "Select",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBatchMode) SweetOrange else Color(0xFF1E293B)
                    )
                }
            }

            // Storage Impact Dashboard Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .shadow(3.dp, RoundedCornerShape(16.dp))
                    .testTag("storage_impact_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SweetOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = SweetOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Offline Vault Active",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Local Sandboxed Storage",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Pulsing Zero-Cloud badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF064E3B).copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SweetEmerald)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Zero-Cloud",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SweetEmerald
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2-Metric Columns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Space Saved
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Space Saved",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "2.4 GB",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = SweetOrange
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "via Flate & Bicubic downsampling",
                                fontSize = 9.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 12.sp
                            )
                        }

                        // On-Device Engine
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "On-Device Engine",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "100%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = SweetEmerald
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Zero network telemetry",
                                fontSize = 9.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 12.sp
                            )
                        }
                    }
                }
            }

            // Search Bar & Filter Tabs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setHistorySearch(it) },
                    placeholder = {
                        Text(
                            text = "Search by title or badge...",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setHistorySearch("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = SweetOrange,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Filter Tabs Carousel + Sort
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HistoryTabPill(
                            title = "All Files (${documents.size})",
                            isSelected = activeFilter == "all"
                        ) { viewModel.setHistoryFilter("all") }

                        HistoryTabPill(
                            title = "Processed (${documents.count { it.category.contains("processed") }})",
                            isSelected = activeFilter == "processed"
                        ) { viewModel.setHistoryFilter("processed") }

                        HistoryTabPill(
                            title = "Scans (${documents.count { it.category.contains("scans") }})",
                            isSelected = activeFilter == "scans"
                        ) { viewModel.setHistoryFilter("scans") }

                        HistoryTabPill(
                            title = "Signed (${documents.count { it.category.contains("signed") }})",
                            isSelected = activeFilter == "signed"
                        ) { viewModel.setHistoryFilter("signed") }

                        HistoryTabPill(
                            title = "Starred (${documents.count { it.isStarred }})",
                            isSelected = activeFilter == "starred"
                        ) { viewModel.setHistoryFilter("starred") }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { viewModel.toggleHistorySort() },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = if (isAscending) SweetOrange else Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Documents List
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredDocs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No documents found in this view",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                } else {
                    filteredDocs.forEach { doc ->
                        val isSelected = selectedDocIds.contains(doc.id)

                        HistoryDocumentRowCard(
                            doc = doc,
                            isBatchMode = isBatchMode,
                            isSelected = isSelected,
                            onToggleSelect = { viewModel.toggleDocSelection(doc.id) },
                            onToggleStar = { viewModel.toggleStar(doc.id, doc.isStarred) },
                            onOpen = { viewModel.openViewer(doc) },
                            onShare = {
                                viewModel.showMessage("Sharing ${doc.title}...")
                            },
                            onMore = {
                                viewModel.showMessage("Options for ${doc.title}")
                            }
                        )
                    }
                }
            }
        }

        // Floating Batch Action Tray (When files are selected in Batch Mode)
        if (isBatchMode && selectedDocIds.isNotEmpty()) {
            val selectedDocsList = documents.filter { selectedDocIds.contains(it.id) }

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(20.dp))
                    .testTag("batch_action_tray"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedDocIds.size} Selected",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Batch execution ready",
                            fontSize = 10.sp,
                            color = SweetOrange
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.executeBatchMerge(selectedDocsList) },
                            colors = ButtonDefaults.buttonColors(containerColor = SweetOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallMerge,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Merge", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.executeBatchCompress(selectedDocsList) },
                            colors = ButtonDefaults.buttonColors(containerColor = SweetBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Compress,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Compress", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.executeBatchShare(selectedDocsList) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryTabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) SweetOrange else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.White else Color(0xFF64748B)
        )
    }
}

@Composable
private fun HistoryDocumentRowCard(
    doc: DocumentEntity,
    isBatchMode: Boolean,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onToggleStar: () -> Unit,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onMore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(14.dp))
            .clickable {
                if (isBatchMode) onToggleSelect() else onOpen()
            }
            .testTag("history_item_${doc.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Checkbox if in batch mode
                if (isBatchMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggleSelect() },
                        colors = CheckboxDefaults.colors(checkedColor = SweetOrange),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Mini document thumbnail preview
                Box(
                    modifier = Modifier
                        .size(44.dp, 56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(4.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(
                                    if (doc.isEncrypted) SweetBlue else SweetOrange,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Icon(
                            imageVector = if (doc.isEncrypted) Icons.Default.Lock else Icons.Default.Description,
                            contentDescription = null,
                            tint = if (doc.isEncrypted) SweetBlue else SweetOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "${doc.pageCount}P",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Middle Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = doc.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = onToggleStar,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (doc.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Star",
                                tint = if (doc.isStarred) SweetOrange else Color(0xFFCBD5E1),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${doc.pageCount} pages • ${doc.formattedSize} • ${doc.dateString}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Badges row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        doc.getBadges().forEach { badge ->
                            val badgeColor = when {
                                badge.contains("Signed", ignoreCase = true) -> SweetBlue
                                badge.contains("AES", ignoreCase = true) -> SweetEmerald
                                badge.contains("OCR", ignoreCase = true) -> SweetEmerald
                                badge.contains("Merged", ignoreCase = true) -> SweetOrange
                                else -> SweetBlue
                            }
                            val badgeBg = when {
                                badge.contains("Signed", ignoreCase = true) -> SweetBlueFixed
                                badge.contains("AES", ignoreCase = true) -> SweetEmeraldFixed
                                badge.contains("OCR", ignoreCase = true) -> SweetEmeraldFixed
                                badge.contains("Merged", ignoreCase = true) -> SweetOrangeFixed
                                else -> SweetBlueFixed
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = SweetBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Open", fontSize = 11.sp, color = SweetBlue, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onShare,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onMore,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
