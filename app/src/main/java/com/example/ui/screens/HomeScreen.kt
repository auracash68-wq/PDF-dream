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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentEntity
import com.example.data.model.PdfTool
import com.example.data.model.PdfToolRegistry
import com.example.data.model.ToolCategory
import com.example.ui.AppTab
import com.example.ui.SweetPdfViewModel
import com.example.ui.components.OrigamiBrandMark
import com.example.ui.theme.SweetBlue
import com.example.ui.theme.SweetBlueFixed
import com.example.ui.theme.SweetEmerald
import com.example.ui.theme.SweetEmeraldFixed
import com.example.ui.theme.SweetOrange
import com.example.ui.theme.SweetOrangeFixed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: SweetPdfViewModel
) {
    val documents by viewModel.documents.collectAsState()
    val searchQuery by viewModel.toolSearchQuery.collectAsState()

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
                .padding(bottom = 80.dp)
        ) {
            // 1. Top App Title & Top Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 14.dp, end = 16.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SweetOrangeFixed),
                        contentAlignment = Alignment.Center
                    ) {
                        OrigamiBrandMark(size = 34.dp, showProBadge = false)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sweet PDF",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        letterSpacing = (-0.5).sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.showMessage("No unread alerts. Local sandbox secure.") },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .shadow(1.dp, RoundedCornerShape(12.dp))
                            .testTag("home_notifications_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = SweetBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Button(
                        onClick = {
                            val tool = PdfToolRegistry.ALL_TOOLS.find { it.id == "camera_scan" }
                            if (tool != null) viewModel.openTool(tool)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SweetOrange),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("home_scan_header_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = "Scan",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // 2. Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        viewModel.setToolSearch(it)
                        if (it.isNotBlank()) {
                            viewModel.selectTab(AppTab.ALL_TOOLS)
                        }
                    },
                    placeholder = {
                        Text(
                            text = "Search PDF tools...",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = SweetOrange,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.selectTab(AppTab.ALL_TOOLS) }) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Filter",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = SweetOrange,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp, RoundedCornerShape(14.dp))
                        .testTag("home_search_input")
                )
            }

            // 3. Express Workflow Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .testTag("express_workflow_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
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
                                    .width(4.dp)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(SweetOrange)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Express Workflow",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SweetEmeraldFixed)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Ready to process",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF064E3B)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quad buttons: Camera, Merge, Compress, E-Sign
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ExpressActionTile(
                            icon = Icons.Default.PhotoCamera,
                            label = "Camera",
                            bgColor = SweetOrange,
                            iconColor = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            val tool = PdfToolRegistry.ALL_TOOLS.find { it.id == "camera_scan" }
                            if (tool != null) viewModel.openTool(tool)
                        }

                        ExpressActionTile(
                            icon = Icons.Default.CallMerge,
                            label = "Merge",
                            bgColor = SweetBlue,
                            iconColor = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            val tool = PdfToolRegistry.ALL_TOOLS.find { it.id == "merge" }
                            if (tool != null) viewModel.openTool(tool)
                        }

                        ExpressActionTile(
                            icon = Icons.Default.Compress,
                            label = "Compress",
                            bgColor = Color(0xFFE2E7FF),
                            iconColor = SweetOrange,
                            modifier = Modifier.weight(1f)
                        ) {
                            val tool = PdfToolRegistry.ALL_TOOLS.find { it.id == "pdf_compression" }
                            if (tool != null) viewModel.openTool(tool)
                        }

                        ExpressActionTile(
                            icon = Icons.Default.Draw,
                            label = "E-Sign",
                            bgColor = SweetEmerald,
                            iconColor = Color.White,
                            modifier = Modifier.weight(1f)
                        ) {
                            val tool = PdfToolRegistry.ALL_TOOLS.find { it.id == "drawn_signature" }
                            if (tool != null) viewModel.openTool(tool)
                        }
                    }
                }
            }

            // 4. Category Carousel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Category",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DomainPill(title = "Organization", isSelected = true) {
                        viewModel.selectToolCategory(ToolCategory.ORGANIZE)
                        viewModel.selectTab(AppTab.ALL_TOOLS)
                    }
                    DomainPill(title = "Scanner CV", isSelected = false) {
                        viewModel.selectToolCategory(ToolCategory.SCAN_CV)
                        viewModel.selectTab(AppTab.ALL_TOOLS)
                    }
                    DomainPill(title = "Security", isSelected = false) {
                        viewModel.selectToolCategory(ToolCategory.SECURITY)
                        viewModel.selectTab(AppTab.ALL_TOOLS)
                    }
                    DomainPill(title = "Converters", isSelected = false) {
                        viewModel.selectToolCategory(ToolCategory.CONVERT)
                        viewModel.selectTab(AppTab.ALL_TOOLS)
                    }
                    DomainPill(title = "Markup & Draw", isSelected = false) {
                        viewModel.selectToolCategory(ToolCategory.MARKUP)
                        viewModel.selectTab(AppTab.ALL_TOOLS)
                    }
                    DomainPill(title = "Repair & Forms", isSelected = false) {
                        viewModel.selectToolCategory(ToolCategory.FORMS_REPAIR)
                        viewModel.selectTab(AppTab.ALL_TOOLS)
                    }
                    DomainPill(title = "View & Navigate", isSelected = false) {
                        viewModel.selectToolCategory(ToolCategory.VIEWING)
                        viewModel.selectTab(AppTab.ALL_TOOLS)
                    }
                }
            }

            // 5. Pinned Tools (6 Cards in 2 columns)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = null,
                            tint = SweetOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pinned Tools",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Text(
                        text = "Customize",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SweetBlue,
                        modifier = Modifier.clickable { viewModel.selectTab(AppTab.ALL_TOOLS) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Pinned 2-column Grid
                val pinnedTools = PdfToolRegistry.ALL_TOOLS.filter { it.isPinned }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (i in pinnedTools.indices step 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val tool1 = pinnedTools[i]
                            PinnedToolCard(
                                tool = tool1,
                                modifier = Modifier.weight(1f)
                            ) { viewModel.openTool(tool1) }

                            if (i + 1 < pinnedTools.size) {
                                val tool2 = pinnedTools[i + 1]
                                PinnedToolCard(
                                    tool = tool2,
                                    modifier = Modifier.weight(1f)
                                ) { viewModel.openTool(tool2) }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 6. Highlight Card: Batch Mode
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .shadow(1.dp, RoundedCornerShape(16.dp))
                    .clickable {
                        viewModel.showMessage("Batch Queue active: 12 optimized jobs ready to execute.")
                    }
                    .testTag("batch_mode_hero_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEAEDFF))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left document icon tile
                    Box(
                        modifier = Modifier
                            .size(56.dp, 70.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .shadow(2.dp, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(SweetOrange)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = SweetOrange,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "PDF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SweetOrange
                            )
                        }
                    }

                    // Middle details
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(SweetOrangeFixed)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "BATCH MODE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C2E00)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Merge & Compress 12 Files",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Queue is active. Run localized batch optimization without cloud upload latency.",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            maxLines = 2,
                            lineHeight = 15.sp,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Right Play button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SweetOrange)
                            .clickable {
                                viewModel.executeBatchMerge(documents)
                            }
                            .testTag("batch_play_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Process Batch",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // 7. Recent Documents Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = SweetBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Recent Documents",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Text(
                        text = "See all",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SweetBlue,
                        modifier = Modifier.clickable { viewModel.selectTab(AppTab.HISTORY) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Documents items
                documents.take(3).forEach { doc ->
                    HomeDocumentCard(
                        doc = doc,
                        onView = { viewModel.openViewer(doc) },
                        onShare = {
                            viewModel.showMessage("Sharing ${doc.title} via system share sheet...")
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }

        // 8. Sticky Quick Scan Floating Action Button
        Button(
            onClick = {
                val tool = PdfToolRegistry.ALL_TOOLS.find { it.id == "camera_scan" }
                if (tool != null) viewModel.openTool(tool)
            },
            colors = ButtonDefaults.buttonColors(containerColor = SweetEmerald),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 20.dp)
                .height(52.dp)
                .shadow(8.dp, RoundedCornerShape(28.dp))
                .testTag("home_quick_scan_fab")
        ) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Quick Scan",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
private fun ExpressActionTile(
    icon: ImageVector,
    label: String,
    bgColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1E293B)
        )
    }
}

@Composable
private fun DomainPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) SweetBlue else Color(0xFFEAEDFF))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color.White else Color(0xFF1E293B)
        )
    }
}

@Composable
private fun PinnedToolCard(
    tool: PdfTool,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(116.dp)
            .shadow(1.dp, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("pinned_tool_${tool.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconBg = when (tool.id) {
                    "merge" -> SweetOrangeFixed
                    "camera_scan" -> SweetBlueFixed
                    "pdf_compression" -> Color(0xFFEAEDFF)
                    "password_protect" -> SweetEmeraldFixed
                    "offline_ocr" -> SweetOrangeFixed
                    else -> SweetBlueFixed
                }
                val iconColor = when (tool.id) {
                    "merge" -> SweetOrange
                    "camera_scan" -> SweetBlue
                    "pdf_compression" -> SweetOrange
                    "password_protect" -> SweetEmerald
                    "offline_ocr" -> SweetOrange
                    else -> SweetBlue
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (tool.id) {
                            "merge" -> Icons.Default.CallMerge
                            "camera_scan" -> Icons.Default.DocumentScanner
                            "pdf_compression" -> Icons.Default.Compress
                            "password_protect" -> Icons.Default.Lock
                            "offline_ocr" -> Icons.Default.TextFields
                            else -> Icons.Default.Draw
                        },
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = tool.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tool.description,
                    fontSize = 10.sp,
                    color = Color(0xFF64748B),
                    maxLines = 2,
                    lineHeight = 14.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HomeDocumentCard(
    doc: DocumentEntity,
    onView: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(14.dp))
            .clickable { onView() }
            .testTag("home_doc_${doc.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Miniature thumbnail
                Box(
                    modifier = Modifier
                        .size(42.dp, 54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
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
                                    if (doc.isEncrypted) SweetBlue else SweetEmerald,
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        Icon(
                            imageVector = if (doc.isEncrypted) Icons.Default.Lock else Icons.Default.AssignmentTurnedIn,
                            contentDescription = null,
                            tint = if (doc.isEncrypted) SweetBlue else SweetEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${doc.pageCount}P",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = doc.formattedSize,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(text = "•", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        Text(
                            text = "${doc.pageCount} pages",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(text = "•", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        Text(
                            text = if (doc.isEncrypted) "Encrypted" else "Signed today",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (doc.isEncrypted) SweetBlue else SweetEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action ribbon: View, Share, and actual Date & Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onView,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
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
                        Text(text = "View", fontSize = 11.sp, color = SweetBlue, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onShare,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Share", fontSize = 11.sp, color = Color(0xFF475569), fontWeight = FontWeight.SemiBold)
                    }

                    // Display actual creation/generated date and time of the PDF instead of Compress action
                    val formattedDateTime = remember(doc.timestamp) {
                        val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
                        sdf.format(Date(doc.timestamp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formattedDateTime,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SweetEmerald,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
