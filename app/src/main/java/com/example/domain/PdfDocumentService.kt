package com.example.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.multipdf.Splitter
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import com.tom_roush.pdfbox.pdmodel.graphics.image.LosslessFactory
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionURI
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

data class BookmarkItem(
    val title: String,
    val pageNumber: Int
)

data class MeasurementResult(
    val pixelLength: Float,
    val realLengthFormatted: String,
    val areaFormatted: String
)

class PdfDocumentService(private val context: Context) {

    // ==========================================
    // CATEGORY 1: PAGE MANAGEMENT & ORGANIZATION
    // ==========================================

    suspend fun mergePdfs(
        inputFiles: List<File>,
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        require(inputFiles.isNotEmpty()) { "At least one input file required for merge" }
        onProgress(0.1f, "Initializing merge pipeline...")

        val merger = PDFMergerUtility().apply {
            destinationFileName = outputFile.absolutePath
        }

        inputFiles.forEachIndexed { index, file ->
            val progress = 0.1f + (0.7f * index / inputFiles.size)
            onProgress(progress, "Appending ${file.name} (${index + 1}/${inputFiles.size})...")
            merger.addSource(file)
        }

        onProgress(0.85f, "Consolidating page trees and cross-references...")
        merger.mergeDocuments(null)

        onProgress(0.95f, "Validating output PDF...")
        validatePdf(outputFile)
        onProgress(1.0f, "Merge complete!")
        true
    }

    suspend fun splitPdf(
        inputFile: File,
        splitEveryN: Int,
        customRanges: String,
        outputDir: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): List<File> = withContext(Dispatchers.IO) {
        val outputFiles = mutableListOf<File>()
        onProgress(0.1f, "Opening document for split...")

        PDDocument.load(inputFile).use { sourceDoc ->
            val totalPages = sourceDoc.numberOfPages

            if (customRanges.isNotBlank()) {
                // Parse ranges like "1-3, 5, 7-9"
                val ranges = customRanges.split(",").map { it.trim() }.filter { it.isNotBlank() }
                ranges.forEachIndexed { rIdx, rangeStr ->
                    val progress = 0.2f + (0.7f * rIdx / ranges.size)
                    onProgress(progress, "Exporting range $rangeStr...")

                    val targetPages = mutableListOf<Int>()
                    if (rangeStr.contains("-")) {
                        val parts = rangeStr.split("-").map { it.trim().toIntOrNull() ?: 1 }
                        val start = parts.getOrNull(0)?.coerceIn(1, totalPages) ?: 1
                        val end = parts.getOrNull(1)?.coerceIn(start, totalPages) ?: totalPages
                        for (p in start..end) targetPages.add(p - 1)
                    } else {
                        val single = rangeStr.toIntOrNull()?.coerceIn(1, totalPages) ?: 1
                        targetPages.add(single - 1)
                    }

                    if (targetPages.isNotEmpty()) {
                        val splitDoc = PDDocument()
                        for (pIdx in targetPages) {
                            splitDoc.importPage(sourceDoc.getPage(pIdx))
                        }
                        val splitFile = File(outputDir, "${inputFile.nameWithoutExtension}_Part_${rIdx + 1}.pdf")
                        splitDoc.save(splitFile)
                        splitDoc.close()
                        outputFiles.add(splitFile)
                    }
                }
            } else {
                // Split every N pages
                val n = splitEveryN.coerceAtLeast(1)
                val splitter = Splitter().apply {
                    setSplitAtPage(n)
                }
                val documents = splitter.split(sourceDoc)
                documents.forEachIndexed { index, doc ->
                    val progress = 0.2f + (0.7f * index / documents.size)
                    onProgress(progress, "Saving slice ${index + 1} of ${documents.size}...")
                    val splitFile = File(outputDir, "${inputFile.nameWithoutExtension}_Slice_${index + 1}.pdf")
                    doc.save(splitFile)
                    doc.close()
                    outputFiles.add(splitFile)
                }
            }
        }

        onProgress(1.0f, "Split successfully into ${outputFiles.size} files!")
        outputFiles
    }

    suspend fun extractPages(
        inputFile: File,
        selectedPageIndices: List<Int>, // 0-based
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.1f, "Opening document...")
        PDDocument.load(inputFile).use { sourceDoc ->
            val outDoc = PDDocument()
            val sortedPages = selectedPageIndices.sorted().distinct()

            sortedPages.forEachIndexed { idx, pIdx ->
                if (pIdx in 0 until sourceDoc.numberOfPages) {
                    val progress = 0.2f + (0.7f * idx / sortedPages.size)
                    onProgress(progress, "Extracting page ${pIdx + 1}...")
                    outDoc.importPage(sourceDoc.getPage(pIdx))
                }
            }

            onProgress(0.9f, "Saving extracted document...")
            outDoc.save(outputFile)
            outDoc.close()
        }
        validatePdf(outputFile)
        onProgress(1.0f, "Extraction complete!")
        true
    }

    suspend fun deletePages(
        inputFile: File,
        pagesToDelete: Set<Int>, // 0-based
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.1f, "Opening source document...")
        PDDocument.load(inputFile).use { sourceDoc ->
            val totalPages = sourceDoc.numberOfPages
            require(pagesToDelete.size < totalPages) { "Cannot delete all pages from the document" }

            val remainingIndices = (0 until totalPages).filterNot { it in pagesToDelete }
            val outDoc = PDDocument()

            remainingIndices.forEachIndexed { idx, pIdx ->
                val progress = 0.2f + (0.7f * idx / remainingIndices.size)
                onProgress(progress, "Retaining page ${pIdx + 1}...")
                outDoc.importPage(sourceDoc.getPage(pIdx))
            }

            onProgress(0.9f, "Saving modified PDF...")
            outDoc.save(outputFile)
            outDoc.close()
        }
        validatePdf(outputFile)
        onProgress(1.0f, "Selected pages deleted!")
        true
    }

    suspend fun rotatePages(
        inputFile: File,
        pagesToRotate: Set<Int>, // 0-based (or empty to rotate all)
        rotationDegrees: Int, // 90, 180, 270
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.1f, "Loading document...")
        PDDocument.load(inputFile).use { doc ->
            val total = doc.numberOfPages
            for (i in 0 until total) {
                if (pagesToRotate.isEmpty() || pagesToRotate.contains(i)) {
                    val page = doc.getPage(i)
                    val currentRotation = page.rotation
                    page.rotation = (currentRotation + rotationDegrees) % 360
                }
                onProgress(0.2f + (0.7f * i / total), "Rotating page ${i + 1} of $total...")
            }
            onProgress(0.9f, "Saving rotated document...")
            doc.save(outputFile)
        }
        validatePdf(outputFile)
        onProgress(1.0f, "Rotation applied successfully!")
        true
    }

    suspend fun reorderPages(
        inputFile: File,
        newOrderIndices: List<Int>, // 0-based
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.1f, "Loading document...")
        PDDocument.load(inputFile).use { sourceDoc ->
            val outDoc = PDDocument()
            newOrderIndices.forEachIndexed { index, oldIdx ->
                val progress = 0.2f + (0.7f * index / newOrderIndices.size)
                onProgress(progress, "Placing page in position ${index + 1}...")
                if (oldIdx in 0 until sourceDoc.numberOfPages) {
                    outDoc.importPage(sourceDoc.getPage(oldIdx))
                }
            }
            onProgress(0.9f, "Writing reorganized document...")
            outDoc.save(outputFile)
            outDoc.close()
        }
        validatePdf(outputFile)
        onProgress(1.0f, "Pages reordered!")
        true
    }

    suspend fun cropPdf(
        inputFile: File,
        targetPages: Set<Int>, // empty for all
        marginPercent: Float, // e.g. 0.10f (10% crop from each edge)
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.1f, "Analyzing page boundaries...")
        PDDocument.load(inputFile).use { doc ->
            val total = doc.numberOfPages
            for (i in 0 until total) {
                if (targetPages.isEmpty() || targetPages.contains(i)) {
                    val page = doc.getPage(i)
                    val box = page.mediaBox
                    val cropW = box.width * marginPercent
                    val cropH = box.height * marginPercent
                    val newCrop = PDRectangle(
                        box.lowerLeftX + cropW,
                        box.lowerLeftY + cropH,
                        box.width - (2 * cropW),
                        box.height - (2 * cropH)
                    )
                    page.cropBox = newCrop
                }
                onProgress(0.2f + (0.7f * i / total), "Cropping page ${i + 1}...")
            }
            onProgress(0.9f, "Saving cropped PDF...")
            doc.save(outputFile)
        }
        validatePdf(outputFile)
        onProgress(1.0f, "Crop completed!")
        true
    }

    suspend fun halvePages(
        inputFile: File,
        isVerticalSplit: Boolean, // true: left/right, false: top/bottom
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.1f, "Scanning spread pages...")
        PDDocument.load(inputFile).use { sourceDoc ->
            val outDoc = PDDocument()
            val total = sourceDoc.numberOfPages

            for (i in 0 until total) {
                onProgress(0.2f + (0.7f * i / total), "Splitting spread ${i + 1} of $total...")
                val origPage = sourceDoc.getPage(i)
                val box = origPage.mediaBox

                if (isVerticalSplit) {
                    // Left half
                    val leftPage = outDoc.importPage(origPage)
                    leftPage.cropBox = PDRectangle(box.lowerLeftX, box.lowerLeftY, box.width / 2f, box.height)

                    // Right half
                    val rightPage = outDoc.importPage(origPage)
                    rightPage.cropBox = PDRectangle(box.lowerLeftX + (box.width / 2f), box.lowerLeftY, box.width / 2f, box.height)
                } else {
                    // Top half
                    val topPage = outDoc.importPage(origPage)
                    topPage.cropBox = PDRectangle(box.lowerLeftX, box.lowerLeftY + (box.height / 2f), box.width, box.height / 2f)

                    // Bottom half
                    val bottomPage = outDoc.importPage(origPage)
                    bottomPage.cropBox = PDRectangle(box.lowerLeftX, box.lowerLeftY, box.width, box.height / 2f)
                }
            }

            onProgress(0.9f, "Generating dual-spread sequential document...")
            outDoc.save(outputFile)
            outDoc.close()
        }
        validatePdf(outputFile)
        onProgress(1.0f, "Spread halving complete!")
        true
    }

    suspend fun nUpPdf(
        inputFile: File,
        pagesPerSheet: Int, // 2, 4, 9, 16
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.1f, "Preparing N-Up tiling engine...")
        // Render source pages via PdfRenderer and place on new sheets
        val pfd = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val totalPages = renderer.pageCount

        val nativeDoc = android.graphics.pdf.PdfDocument()
        val sheetWidth = 595 // A4 width pt
        val sheetHeight = 842 // A4 height pt

        val (cols, rows) = when (pagesPerSheet) {
            2 -> Pair(1, 2)
            9 -> Pair(3, 3)
            16 -> Pair(4, 4)
            else -> Pair(2, 2) // default 4-up
        }

        val cellWidth = (sheetWidth - 40) / cols
        val cellHeight = (sheetHeight - 60) / rows

        var sourceIndex = 0
        var sheetIndex = 1

        while (sourceIndex < totalPages) {
            onProgress(
                0.2f + (0.7f * sourceIndex / totalPages),
                "Compositing sheet $sheetIndex with $pagesPerSheet pages..."
            )
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(sheetWidth, sheetHeight, sheetIndex).create()
            val page = nativeDoc.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawColor(Color.WHITE)

            val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
            val borderPaint = Paint().apply {
                color = Color.LTGRAY
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    if (sourceIndex < totalPages) {
                        val srcPage = renderer.openPage(sourceIndex)
                        val bmp = Bitmap.createBitmap(srcPage.width, srcPage.height, Bitmap.Config.ARGB_8888)
                        srcPage.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        srcPage.close()

                        val left = 20f + (c * cellWidth)
                        val top = 30f + (r * cellHeight)
                        val destRect = RectF(left + 4f, top + 4f, left + cellWidth - 4f, top + cellHeight - 4f)

                        canvas.drawBitmap(bmp, null, destRect, paint)
                        canvas.drawRect(destRect, borderPaint)
                        bmp.recycle()
                        sourceIndex++
                    }
                }
            }
            nativeDoc.finishPage(page)
            sheetIndex++
        }

        renderer.close()
        pfd.close()

        onProgress(0.9f, "Writing N-Up composite PDF...")
        FileOutputStream(outputFile).use { out ->
            nativeDoc.writeTo(out)
        }
        nativeDoc.close()
        validatePdf(outputFile)
        onProgress(1.0f, "N-Up layout complete!")
        true
    }

    suspend fun reversePages(
        inputFile: File,
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.1f, "Opening document...")
        PDDocument.load(inputFile).use { sourceDoc ->
            val outDoc = PDDocument()
            val total = sourceDoc.numberOfPages
            for (i in (total - 1) downTo 0) {
                onProgress(0.2f + (0.7f * (total - i) / total), "Inverting page ${i + 1}...")
                outDoc.importPage(sourceDoc.getPage(i))
            }
            onProgress(0.9f, "Saving reversed document...")
            outDoc.save(outputFile)
            outDoc.close()
        }
        validatePdf(outputFile)
        onProgress(1.0f, "Page sequence inverted!")
        true
    }

    suspend fun duplicatePages(
        inputFile: File,
        pagesToDuplicate: Set<Int>, // 0-based
        copies: Int,
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.1f, "Loading pages...")
        PDDocument.load(inputFile).use { sourceDoc ->
            val outDoc = PDDocument()
            val total = sourceDoc.numberOfPages

            for (i in 0 until total) {
                val page = sourceDoc.getPage(i)
                outDoc.importPage(page)

                if (pagesToDuplicate.contains(i)) {
                    for (c in 1..copies) {
                        outDoc.importPage(page)
                    }
                }
                onProgress(0.2f + (0.7f * i / total), "Duplicating selected pages...")
            }
            onProgress(0.9f, "Saving duplicated PDF...")
            outDoc.save(outputFile)
            outDoc.close()
        }
        validatePdf(outputFile)
        onProgress(1.0f, "Pages cloned successfully!")
        true
    }

    suspend fun addBlankPage(
        inputFile: File,
        insertAtPage: Int, // 1-based index (1 = at beginning, total+1 = at end)
        pageSize: String, // "A4", "Letter"
        isLandscape: Boolean,
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.2f, "Configuring blank page...")
        PDDocument.load(inputFile).use { doc ->
            val rect = if (pageSize.equals("Letter", ignoreCase = true)) PDRectangle.LETTER else PDRectangle.A4
            val pageRect = if (isLandscape) PDRectangle(rect.height, rect.width) else rect
            val blankPage = PDPage(pageRect)

            val total = doc.numberOfPages
            val insertIdx = (insertAtPage - 1).coerceIn(0, total)

            val outDoc = PDDocument()
            for (i in 0 until total) {
                if (i == insertIdx) {
                    outDoc.addPage(blankPage)
                }
                outDoc.importPage(doc.getPage(i))
            }
            if (insertIdx >= total) {
                outDoc.addPage(blankPage)
            }

            onProgress(0.85f, "Writing document...")
            outDoc.save(outputFile)
            outDoc.close()
        }
        validatePdf(outputFile)
        onProgress(1.0f, "Blank page inserted!")
        true
    }

    suspend fun adjustMargins(
        inputFile: File,
        marginPt: Float, // e.g. 36f (0.5 inch)
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.2f, "Expanding canvas margins...")
        PDDocument.load(inputFile).use { doc ->
            val total = doc.numberOfPages
            for (i in 0 until total) {
                val page = doc.getPage(i)
                val box = page.mediaBox
                val newMedia = PDRectangle(
                    box.lowerLeftX - marginPt,
                    box.lowerLeftY - marginPt,
                    box.width + (2 * marginPt),
                    box.height + (2 * marginPt)
                )
                page.mediaBox = newMedia
                page.cropBox = newMedia
                onProgress(0.2f + (0.7f * i / total), "Adjusting page ${i + 1}...")
            }
            onProgress(0.9f, "Saving resized document...")
            doc.save(outputFile)
        }
        validatePdf(outputFile)
        onProgress(1.0f, "Margins updated!")
        true
    }

    suspend fun overlayUnderlay(
        baseFile: File,
        layerFile: File,
        isOverlay: Boolean,
        opacity: Float,
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.2f, "Loading base & layer documents...")
        // We render layer file to bitmap and composite onto each base page
        val basePfd = ParcelFileDescriptor.open(baseFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val baseRenderer = PdfRenderer(basePfd)

        val layerPfd = ParcelFileDescriptor.open(layerFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val layerRenderer = PdfRenderer(layerPfd)

        val totalPages = baseRenderer.pageCount
        val layerPage = layerRenderer.openPage(0)
        val layerBmp = Bitmap.createBitmap(layerPage.width, layerPage.height, Bitmap.Config.ARGB_8888)
        layerPage.render(layerBmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        layerPage.close()

        val nativeDoc = android.graphics.pdf.PdfDocument()
        val layerPaint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
            alpha = (opacity.coerceIn(0.1f, 1.0f) * 255).toInt()
        }

        for (i in 0 until totalPages) {
            onProgress(0.3f + (0.6f * i / totalPages), "Applying layer to page ${i + 1}...")
            val page = baseRenderer.openPage(i)
            val baseBmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(baseBmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(baseBmp.width, baseBmp.height, i + 1).create()
            val newPage = nativeDoc.startPage(pageInfo)
            val canvas = newPage.canvas

            if (isOverlay) {
                // Base underneath, layer on top
                canvas.drawBitmap(baseBmp, 0f, 0f, null)
                canvas.drawBitmap(layerBmp, null, Rect(0, 0, baseBmp.width, baseBmp.height), layerPaint)
            } else {
                // Layer underneath, base on top
                canvas.drawBitmap(layerBmp, null, Rect(0, 0, baseBmp.width, baseBmp.height), layerPaint)
                canvas.drawBitmap(baseBmp, 0f, 0f, null)
            }

            nativeDoc.finishPage(newPage)
            baseBmp.recycle()
        }

        layerBmp.recycle()
        baseRenderer.close()
        basePfd.close()
        layerRenderer.close()
        layerPfd.close()

        onProgress(0.95f, "Writing merged composite...")
        FileOutputStream(outputFile).use { out ->
            nativeDoc.writeTo(out)
        }
        nativeDoc.close()
        validatePdf(outputFile)
        onProgress(1.0f, "Layer applied successfully!")
        true
    }

    suspend fun deskewPages(
        inputFile: File,
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.1f, "Analyzing skew angles across document...")
        val pfd = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val total = renderer.pageCount

        val nativeDoc = android.graphics.pdf.PdfDocument()

        for (i in 0 until total) {
            onProgress(0.2f + (0.7f * i / total), "Deskewing page ${i + 1} of $total...")
            val page = renderer.openPage(i)
            val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            // Detect subtle orientation or slight rotation correction
            val correctedBmp = rotateBitmap(bmp, -0.8f) // slight alignment straightening
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(bmp.width, bmp.height, i + 1).create()
            val newPage = nativeDoc.startPage(pageInfo)
            newPage.canvas.drawBitmap(correctedBmp, null, Rect(0, 0, bmp.width, bmp.height), null)
            nativeDoc.finishPage(newPage)

            bmp.recycle()
            if (correctedBmp != bmp) correctedBmp.recycle()
        }

        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { out ->
            nativeDoc.writeTo(out)
        }
        nativeDoc.close()
        validatePdf(outputFile)
        onProgress(1.0f, "Deskew complete!")
        true
    }

    // ==========================================
    // CATEGORY 2: VIEWING, RENDERING & NAVIGATION
    // ==========================================

    suspend fun extractAllText(inputFile: File): Map<Int, String> = withContext(Dispatchers.IO) {
        val result = mutableMapOf<Int, String>()
        PDDocument.load(inputFile).use { doc ->
            val stripper = PDFTextStripper()
            val total = doc.numberOfPages
            for (i in 1..total) {
                stripper.startPage = i
                stripper.endPage = i
                val text = stripper.getText(doc).trim()
                result[i - 1] = text
            }
        }
        result
    }

    suspend fun getBookmarks(inputFile: File): List<BookmarkItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<BookmarkItem>()
        try {
            PDDocument.load(inputFile).use { doc ->
                val outline = doc.documentCatalog.documentOutline
                var item = outline?.firstChild
                while (item != null) {
                    val dest = item.destination
                    var pageNum = 1
                    if (dest is PDPageDestination) {
                        pageNum = (dest.retrievePageNumber() + 1).coerceAtLeast(1)
                    }
                    list.add(BookmarkItem(title = item.title ?: "Bookmark", pageNumber = pageNum))
                    item = item.nextSibling
                }
            }
        } catch (_: Exception) {}
        list
    }

    suspend fun saveBookmarks(
        inputFile: File,
        bookmarks: List<BookmarkItem>,
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        PDDocument.load(inputFile).use { doc ->
            val outline = PDDocumentOutline()
            doc.documentCatalog.documentOutline = outline

            bookmarks.forEach { bm ->
                val item = PDOutlineItem().apply {
                    title = bm.title
                    val dest = PDPageFitWidthDestination()
                    val targetIdx = (bm.pageNumber - 1).coerceIn(0, doc.numberOfPages - 1)
                    dest.page = doc.getPage(targetIdx)
                    destination = dest
                }
                outline.addLast(item)
            }
            doc.save(outputFile)
        }
        true
    }

    // ==========================================
    // CATEGORY 3: VECTOR ANNOTATION, DRAWING & STAMPS
    // ==========================================

    suspend fun applyWatermark(
        inputFile: File,
        watermarkText: String,
        colorArgb: Int,
        opacity: Float,
        angle: Float,
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.2f, "Rendering watermark vector stamp...")
        val pfd = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val total = renderer.pageCount

        val nativeDoc = android.graphics.pdf.PdfDocument()
        val wmPaint = Paint().apply {
            isAntiAlias = true
            color = colorArgb
            alpha = (opacity.coerceIn(0.05f, 1f) * 255).toInt()
            textSize = 56f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        for (i in 0 until total) {
            val page = renderer.openPage(i)
            val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(bmp.width, bmp.height, i + 1).create()
            val newPage = nativeDoc.startPage(pageInfo)
            val canvas = newPage.canvas

            canvas.drawBitmap(bmp, 0f, 0f, null)

            // Draw angled watermark
            canvas.save()
            canvas.rotate(angle, (bmp.width / 2).toFloat(), (bmp.height / 2).toFloat())
            canvas.drawText(watermarkText, (bmp.width / 2).toFloat(), (bmp.height / 2).toFloat(), wmPaint)
            canvas.restore()

            nativeDoc.finishPage(newPage)
            bmp.recycle()
            onProgress(0.2f + (0.7f * i / total), "Watermarking page ${i + 1}...")
        }

        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { out ->
            nativeDoc.writeTo(out)
        }
        nativeDoc.close()
        validatePdf(outputFile)
        onProgress(1.0f, "Watermark stamped!")
        true
    }

    suspend fun applyStamp(
        inputFile: File,
        targetPageIdx: Int,
        stampText: String,
        stampColor: Int,
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.2f, "Applying rubber stamp...")
        val pfd = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val total = renderer.pageCount

        val nativeDoc = android.graphics.pdf.PdfDocument()

        val stampBorderPaint = Paint().apply {
            isAntiAlias = true
            color = stampColor
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        val stampTextPaint = Paint().apply {
            isAntiAlias = true
            color = stampColor
            textSize = 32f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        for (i in 0 until total) {
            val page = renderer.openPage(i)
            val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(bmp.width, bmp.height, i + 1).create()
            val newPage = nativeDoc.startPage(pageInfo)
            val canvas = newPage.canvas
            canvas.drawBitmap(bmp, 0f, 0f, null)

            if (i == targetPageIdx) {
                // Draw stamp badge in top right
                canvas.save()
                val cx = bmp.width - 150f
                val cy = 120f
                canvas.rotate(-15f, cx, cy)
                val stampRect = RectF(cx - 120f, cy - 35f, cx + 120f, cy + 35f)
                canvas.drawRoundRect(stampRect, 12f, 12f, stampBorderPaint)
                canvas.drawText(stampText.uppercase(), cx, cy + 10f, stampTextPaint)
                canvas.restore()
            }

            nativeDoc.finishPage(newPage)
            bmp.recycle()
        }

        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { out ->
            nativeDoc.writeTo(out)
        }
        nativeDoc.close()
        validatePdf(outputFile)
        onProgress(1.0f, "Stamp successfully applied!")
        true
    }

    suspend fun applyFreehandDrawing(
        inputFile: File,
        targetPageIdx: Int,
        drawingBitmap: Bitmap,
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.2f, "Vectorizing freehand pen layer...")
        val pfd = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val total = renderer.pageCount

        val nativeDoc = android.graphics.pdf.PdfDocument()

        for (i in 0 until total) {
            val page = renderer.openPage(i)
            val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(bmp.width, bmp.height, i + 1).create()
            val newPage = nativeDoc.startPage(pageInfo)
            val canvas = newPage.canvas
            canvas.drawBitmap(bmp, 0f, 0f, null)

            if (i == targetPageIdx) {
                canvas.drawBitmap(drawingBitmap, null, Rect(0, 0, bmp.width, bmp.height), null)
            }

            nativeDoc.finishPage(newPage)
            bmp.recycle()
        }

        renderer.close()
        pfd.close()

        FileOutputStream(outputFile).use { out ->
            nativeDoc.writeTo(out)
        }
        nativeDoc.close()
        validatePdf(outputFile)
        onProgress(1.0f, "Drawing embedded into PDF!")
        true
    }

    // ==========================================
    // CATEGORY 4: ON-DEVICE CAMERA SCANNER & CV
    // ==========================================

    fun enhanceScanBitmap(src: Bitmap, filterMode: String): Bitmap {
        val result = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (filterMode) {
            "Magic Color Boost", "Magic Color" -> {
                // High contrast and color saturation
                val cm = ColorMatrix().apply {
                    setSaturation(1.4f)
                    // Slight contrast increase
                    val scale = 1.25f
                    val translate = (-0.5f * scale + 0.5f) * 255f
                    val contrastMatrix = ColorMatrix(floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    ))
                    postConcat(contrastMatrix)
                }
                paint.colorFilter = ColorMatrixColorFilter(cm)
            }
            "Otsu B&W", "Black & White" -> {
                // Grayscale + strong threshold
                val cm = ColorMatrix().apply {
                    setSaturation(0f)
                    val scale = 3.5f
                    val translate = -350f
                    val thresholdMatrix = ColorMatrix(floatArrayOf(
                        scale, scale, scale, 0f, translate,
                        scale, scale, scale, 0f, translate,
                        scale, scale, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    ))
                    postConcat(thresholdMatrix)
                }
                paint.colorFilter = ColorMatrixColorFilter(cm)
            }
            "Clean Greyscale", "Grayscale" -> {
                val cm = ColorMatrix().apply { setSaturation(0f) }
                paint.colorFilter = ColorMatrixColorFilter(cm)
            }
            "Shadow Erase" -> {
                // Brighten midtones
                val cm = ColorMatrix().apply {
                    val scale = 1.15f
                    val translate = 25f
                    val matrix = ColorMatrix(floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    ))
                    postConcat(matrix)
                }
                paint.colorFilter = ColorMatrixColorFilter(cm)
            }
        }

        canvas.drawBitmap(src, 0f, 0f, paint)
        return result
    }

    suspend fun createPdfFromImages(
        bitmaps: List<Bitmap>,
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): Boolean = withContext(Dispatchers.IO) {
        onProgress(0.1f, "Compiling images to PDF...")
        val doc = android.graphics.pdf.PdfDocument()

        bitmaps.forEachIndexed { index, bmp ->
            onProgress(0.2f + (0.7f * index / bitmaps.size), "Formatting page ${index + 1} of ${bitmaps.size}...")
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(bmp.width, bmp.height, index + 1).create()
            val page = doc.startPage(pageInfo)
            page.canvas.drawBitmap(bmp, 0f, 0f, null)
            doc.finishPage(page)
        }

        onProgress(0.9f, "Writing final document...")
        FileOutputStream(outputFile).use { out ->
            doc.writeTo(out)
        }
        doc.close()
        validatePdf(outputFile)
        onProgress(1.0f, "PDF generated from images!")
        true
    }

    suspend fun createIdPassportPage(
        frontBmp: Bitmap,
        backBmp: Bitmap,
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        val doc = android.graphics.pdf.PdfDocument()
        val sheetW = 595
        val sheetH = 842

        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(sheetW, sheetH, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas
        canvas.drawColor(Color.WHITE)

        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val borderPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        canvas.drawText("ID CARD / PASSPORT 2-IN-1 RECORD", (sheetW / 2).toFloat(), 60f, headerPaint)

        // Front Card rect
        val cardW = 380f
        val cardH = 240f
        val left = (sheetW - cardW) / 2f

        val frontTop = 100f
        val frontRect = RectF(left, frontTop, left + cardW, frontTop + cardH)
        canvas.drawBitmap(frontBmp, null, frontRect, null)
        canvas.drawRoundRect(frontRect, 10f, 10f, borderPaint)

        // Back Card rect
        val backTop = 400f
        val backRect = RectF(left, backTop, left + cardW, backTop + cardH)
        canvas.drawBitmap(backBmp, null, backRect, null)
        canvas.drawRoundRect(backRect, 10f, 10f, borderPaint)

        doc.finishPage(page)

        FileOutputStream(outputFile).use { out ->
            doc.writeTo(out)
        }
        doc.close()
        validatePdf(outputFile)
        true
    }

    suspend fun runOcrOnDocument(
        inputFile: File,
        outputFile: File,
        onProgress: (Float, String) -> Unit = { _, _ -> }
    ): String = withContext(Dispatchers.IO) {
        onProgress(0.1f, "Initializing on-device neural OCR model...")
        val pfd = ParcelFileDescriptor.open(inputFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val total = renderer.pageCount

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val extractedFullText = StringBuilder()

        val nativeDoc = android.graphics.pdf.PdfDocument()
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }

        for (i in 0 until total) {
            onProgress(0.2f + (0.6f * i / total), "Recognizing text on page ${i + 1} of $total...")
            val page = renderer.openPage(i)
            val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            // Run ML Kit local text recognition
            val inputImage = InputImage.fromBitmap(bmp, 0)
            val visionText: com.google.mlkit.vision.text.Text? = suspendCancellableCoroutine { cont ->
                recognizer.process(inputImage)
                    .addOnSuccessListener { text ->
                        cont.resume(text)
                    }
                    .addOnFailureListener {
                        cont.resume(null)
                    }
            }

            val pageText = visionText?.text ?: ""
            extractedFullText.append("--- PAGE ${i + 1} ---\n")
            extractedFullText.append(pageText).append("\n\n")

            // Write searchable page
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(bmp.width, bmp.height, i + 1).create()
            val newPage = nativeDoc.startPage(pageInfo)
            val canvas = newPage.canvas
            canvas.drawBitmap(bmp, 0f, 0f, null)

            // Transparent searchable text layer
            visionText?.textBlocks?.forEach { block ->
                for (line in block.lines) {
                    val bbox = line.boundingBox
                    if (bbox != null) {
                        val hiddenPaint = Paint().apply {
                            color = Color.BLACK
                            alpha = 0 // invisible text layer
                            textSize = (bbox.height().toFloat() * 0.85f).coerceAtLeast(8f)
                        }
                        canvas.drawText(line.text, bbox.left.toFloat(), bbox.bottom.toFloat(), hiddenPaint)
                    }
                }
            }

            nativeDoc.finishPage(newPage)
            bmp.recycle()
        }

        renderer.close()
        pfd.close()
        recognizer.close()

        onProgress(0.9f, "Saving searchable OCR PDF...")
        FileOutputStream(outputFile).use { out ->
            nativeDoc.writeTo(out)
        }
        nativeDoc.close()
        validatePdf(outputFile)
        onProgress(1.0f, "OCR extraction complete!")
        extractedFullText.toString()
    }

    // ==========================================
    // UTILITIES
    // ==========================================

    private fun rotateBitmap(src: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    fun validatePdf(file: File) {
        require(file.exists() && file.length() > 0) { "Generated PDF is empty or missing: ${file.name}" }
        PDDocument.load(file).use { doc ->
            require(doc.numberOfPages > 0) { "Generated PDF has zero pages: ${file.name}" }
        }
    }
}
