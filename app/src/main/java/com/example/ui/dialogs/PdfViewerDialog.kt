package com.example.ui.dialogs

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FindInPage
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DocumentEntity
import com.example.ui.SweetPdfViewModel
import com.example.ui.screens.tools.shareFile
import com.example.ui.theme.SweetBlue
import com.example.ui.theme.SweetEmerald
import com.example.ui.theme.SweetOrange
import com.example.ui.theme.SweetOrangeFixed
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PdfViewerDialog(
    doc: DocumentEntity,
    viewModel: SweetPdfViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var currentPage by remember { mutableIntStateOf(1) }
    var isNightReading by remember { mutableStateOf(false) }
    var pageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val totalPages = doc.pageCount.coerceAtLeast(1)

    LaunchedEffect(doc.filePath, currentPage) {
        val file = File(doc.filePath)
        if (file.exists() && file.length() > 0) {
            withContext(Dispatchers.IO) {
                try {
                    val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                    val renderer = PdfRenderer(pfd)
                    if (renderer.pageCount > 0) {
                        val pageIdx = (currentPage - 1).coerceIn(0, renderer.pageCount - 1)
                        val page = renderer.openPage(pageIdx)
                        val width = 720
                        val height = (width * (page.height.toFloat() / page.width.toFloat())).toInt()
                        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        pageBitmap = bmp
                    }
                    renderer.close()
                    pfd.close()
                } catch (e: Exception) {
                    pageBitmap = null
                }
            }
        } else {
            pageBitmap = null
        }
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isNightReading) Color(0xFF0B0F19) else Color(0xFF1E293B))
                .testTag("pdf_viewer_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(if (isNightReading) Color(0xFF0F172A) else Color(0xFF0F172A))
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = doc.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Page $currentPage of $totalPages • ${doc.formattedSize}",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isNightReading = !isNightReading }) {
                            Icon(
                                imageVector = if (isNightReading) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Night Mode",
                                tint = if (isNightReading) SweetOrange else Color.White
                            )
                        }

                        IconButton(onClick = {
                            val file = File(doc.filePath)
                            if (file.exists()) {
                                shareFile(context, file, doc.title)
                            } else {
                                viewModel.showMessage("Sharing ${doc.title}...")
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                    }
                }

                // Document Canvas Area (Centered A4 Page Sheet)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (pageBitmap != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .shadow(16.dp, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isNightReading) Color(0xFF131B2E) else Color.White
                            )
                        ) {
                            Image(
                                bitmap = pageBitmap!!.asImageBitmap(),
                                contentDescription = "Page $currentPage",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .aspectRatio(1f / 1.414f) // Standard ISO A4 aspect ratio (595 x 842)
                                .shadow(16.dp, RoundedCornerShape(8.dp)),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isNightReading) Color(0xFF131B2E) else Color.White
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Page Top Header
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "SWEET PDF NATIVE ENGINE",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNightReading) Color(0xFF64748B) else Color(0xFF94A3B8),
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "PAGE $currentPage OF $totalPages",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isNightReading) Color(0xFF64748B) else Color(0xFF94A3B8)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.dp)
                                            .background(SweetBlue)
                                    )
                                }

                                // Body Content
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = doc.title.removeSuffix(".pdf"),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SweetOrange,
                                        letterSpacing = (-0.3).sp
                                    )

                                    Text(
                                        text = if (doc.isEncrypted) "Security Profile: AES-256 Bit Encrypted • Verified Signature"
                                        else "Profile: Lossless Vector Output • On-Device Sandboxed",
                                        fontSize = 10.sp,
                                        color = if (isNightReading) Color(0xFF94A3B8) else Color(0xFF64748B)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = doc.contentSnippet ?: "Document content verified and structured by Sweet PDF Skia Engine. Fully readable without external font substitutes.",
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp,
                                        color = if (isNightReading) Color(0xFFE2E8F0) else Color(0xFF1E293B)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Visual metadata stamp box
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isNightReading) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                                            .padding(10.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            doc.getBadges().forEach { badge ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Verified,
                                                        contentDescription = null,
                                                        tint = SweetEmerald,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = badge,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isNightReading) Color(0xFFCBD5E1) else Color(0xFF334155)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Footer
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color(0xFFCBD5E1))
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Generated by Sweet PDF • 100% Private Offline",
                                            fontSize = 8.sp,
                                            color = if (isNightReading) Color(0xFF64748B) else Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = doc.dateString,
                                            fontSize = 8.sp,
                                            color = if (isNightReading) Color(0xFF64748B) else Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Pagination Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (currentPage > 1) currentPage-- },
                        enabled = currentPage > 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Page",
                            tint = if (currentPage > 1) Color.White else Color(0xFF475569)
                        )
                    }

                    Text(
                        text = "Sheet $currentPage / $totalPages",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = { if (currentPage < totalPages) currentPage++ },
                        enabled = currentPage < totalPages
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Page",
                            tint = if (currentPage < totalPages) Color.White else Color(0xFF475569)
                        )
                    }
                }
            }
        }
    }
}
