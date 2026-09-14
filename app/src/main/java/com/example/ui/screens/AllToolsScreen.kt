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
import androidx.compose.material.icons.filled.Approval
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.BuildCircle
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChromeReaderMode
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.DynamicForm
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterBAndW
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.Flare
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Grid4x4
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.HighlightAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.ManageSearch
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapCalls
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.TabUnselected
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Toc
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PdfTool
import com.example.data.model.PdfToolRegistry
import com.example.data.model.ToolCategory
import com.example.ui.SweetPdfViewModel
import com.example.ui.theme.Border
import com.example.ui.theme.Primary
import com.example.ui.theme.PrimaryDark
import com.example.ui.theme.PrimaryLight
import com.example.ui.theme.PrimarySoft
import com.example.ui.theme.Success
import com.example.ui.theme.SurfaceSecondary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun AllToolsScreen(
    viewModel: SweetPdfViewModel
) {
    val searchQuery by viewModel.toolSearchQuery.collectAsState()
    val selectedCategory by viewModel.selectedToolCategory.collectAsState()

    // Filter tools based on search and optional category
    val allTools = PdfToolRegistry.ALL_TOOLS
    val filteredTools = allTools.filter { tool ->
        val matchesQuery = searchQuery.isBlank() ||
            tool.name.contains(searchQuery, ignoreCase = true) ||
            tool.description.contains(searchQuery, ignoreCase = true) ||
            tool.actionBadge.contains(searchQuery, ignoreCase = true)

        val matchesCategory = selectedCategory == null || tool.category == selectedCategory
        matchesQuery && matchesCategory
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp)
    ) {
        // Top Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Toolkit Directory",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryLight)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ENGINE v4.2",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Primary
                            )
                        }
                    }
                    Text(
                        text = "65 on-device high-performance native PDF tools",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                // Verified Badge Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimarySoft)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "${filteredTools.size} Found" else "65 Tools",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }

            // Quick Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setToolSearch(it) },
                placeholder = {
                    Text(
                        text = "Filter by action or tool name...",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setToolSearch("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Border
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toolkit_search_input")
            )

            // Category Filter Pills Carousel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToolCategory.entries.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    val pillBg = if (isSelected) Color(cat.themeColor) else SurfaceSecondary
                    val textColor = if (isSelected) Color.White else TextPrimary
                    val iconColor = if (isSelected) Color.White else Color(cat.themeColor)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(pillBg)
                            .clickable {
                                if (isSelected) viewModel.selectToolCategory(null)
                                else viewModel.selectToolCategory(cat)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(cat),
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${cat.shortName} (${cat.countLabel.removeSuffix(" Tools")})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor
                        )
                    }
                }
            }
        }

        // Empty state when filter yields zero results
        if (filteredTools.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(PrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ManageSearch,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No matching utility",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Try keywords like 'merge', 'sign', 'ocr', 'protect', or 'compress'",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Group and display by Category
            val categoriesToShow = ToolCategory.entries.filter { cat ->
                filteredTools.any { it.category == cat }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                categoriesToShow.forEach { cat ->
                    val toolsInCat = filteredTools.filter { it.category == cat }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Category Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(cat.chipBgColor)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(cat),
                                        contentDescription = null,
                                        tint = Color(cat.themeColor),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = cat.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceSecondary)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${toolsInCat.size} Tools",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                            }
                        }

                        // 2-Column Grid
                        for (i in toolsInCat.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val t1 = toolsInCat[i]
                                ToolGridCard(
                                    tool = t1,
                                    cat = cat,
                                    modifier = Modifier.weight(1f)
                                ) { viewModel.openTool(t1) }

                                if (i + 1 < toolsInCat.size) {
                                    val t2 = toolsInCat[i + 1]
                                    ToolGridCard(
                                        tool = t2,
                                        cat = cat,
                                        modifier = Modifier.weight(1f)
                                    ) { viewModel.openTool(t2) }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolGridCard(
    tool: PdfTool,
    cat: ToolCategory,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(148.dp)
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("tool_card_${tool.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(cat.chipBgColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getToolIcon(tool.iconName),
                        contentDescription = null,
                        tint = Color(cat.themeColor),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = tool.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = tool.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    lineHeight = 15.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tool.actionBadge.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(cat.themeColor),
                    letterSpacing = 0.5.sp
                )

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(cat.chipBgColor)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(cat.themeColor),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

private fun getCategoryIcon(cat: ToolCategory): ImageVector {
    return when (cat) {
        ToolCategory.ORGANIZE -> Icons.Default.AutoStories
        ToolCategory.VIEWING -> Icons.Default.RemoveRedEye
        ToolCategory.MARKUP -> Icons.Default.Draw
        ToolCategory.SCAN_CV -> Icons.Default.DocumentScanner
        ToolCategory.CONVERT -> Icons.Default.Transform
        ToolCategory.SECURITY -> Icons.Default.Security
        ToolCategory.FORMS_REPAIR -> Icons.Default.BuildCircle
    }
}

private fun getToolIcon(name: String): ImageVector {
    return when (name) {
        "call_merge" -> Icons.Default.CallMerge
        "call_split" -> Icons.Default.CallSplit
        "tab_unselected" -> Icons.Default.TabUnselected
        "delete_sweep" -> Icons.Default.DeleteSweep
        "rotate_right" -> Icons.Default.RotateRight
        "swap_vert" -> Icons.Default.SwapVert
        "crop" -> Icons.Default.Crop
        "view_column" -> Icons.Default.ViewColumn
        "grid_4x4" -> Icons.Default.Grid4x4
        "swap_driving_apps_wheel" -> Icons.Default.SwapCalls
        "content_copy" -> Icons.Default.ContentCopy
        "note_add" -> Icons.Default.NoteAdd
        "aspect_ratio" -> Icons.Default.AspectRatio
        "layers" -> Icons.Default.Layers
        "straighten" -> Icons.Default.Straighten
        "chrome_reader_mode" -> Icons.Default.ChromeReaderMode
        "dark_mode" -> Icons.Default.DarkMode
        "find_in_page" -> Icons.Default.FindInPage
        "bookmarks" -> Icons.Default.Bookmarks
        "toc" -> Icons.Default.Toc
        "volume_up" -> Icons.Default.VolumeUp
        "grid_view" -> Icons.Default.GridView
        "ink_highlighter" -> Icons.Default.BorderColor
        "format_underlined" -> Icons.Default.FormatUnderlined
        "edit" -> Icons.Default.Edit
        "highlight_alt" -> Icons.Default.HighlightAlt
        "sticky_note_2" -> Icons.Default.StickyNote2
        "shapes" -> Icons.Default.Category
        "approval" -> Icons.Default.Approval
        "square_foot" -> Icons.Default.SquareFoot
        "document_scanner" -> Icons.Default.DocumentScanner
        "filter_center_focus" -> Icons.Default.FilterCenterFocus
        "transform" -> Icons.Default.Transform
        "flare" -> Icons.Default.Flare
        "auto_fix_high" -> Icons.Default.AutoFixHigh
        "filter_b_and_w" -> Icons.Default.FilterBAndW
        "badge" -> Icons.Default.Badge
        "menu_book" -> Icons.Default.MenuBook
        "font_download" -> Icons.Default.FontDownload
        "photo_library" -> Icons.Default.PhotoLibrary
        "image" -> Icons.Default.Image
        "collections_bookmark" -> Icons.Default.CollectionsBookmark
        "description" -> Icons.Default.Description
        "code_blocks" -> Icons.Default.Code
        "qr_code_2" -> Icons.Default.QrCode2
        "photo_camera" -> Icons.Default.PhotoCamera
        "lock" -> Icons.Default.Lock
        "lock_open" -> Icons.Default.LockOpen
        "ink_eraser" -> Icons.Default.Clear
        "info" -> Icons.Default.Info
        "verified_user" -> Icons.Default.VerifiedUser
        "signature" -> Icons.Default.Draw
        "security" -> Icons.Default.Security
        "attachment" -> Icons.Default.Attachment
        "compress" -> Icons.Default.Compress
        "layers_clear" -> Icons.Default.LayersClear
        "healing" -> Icons.Default.Healing
        "tag" -> Icons.Default.Tag
        "branding_watermark" -> Icons.Default.BrandingWatermark
        "inventory_2" -> Icons.Default.Inventory2
        "grain" -> Icons.Default.Grain
        "dynamic_form" -> Icons.Default.DynamicForm
        "dataset" -> Icons.Default.Dataset
        "speed" -> Icons.Default.Speed
        "text_fields" -> Icons.Default.TextFields
        "palette" -> Icons.Default.Palette
        else -> Icons.Default.Description
    }
}
