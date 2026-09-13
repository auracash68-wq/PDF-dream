package com.example.data.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.tom_roush.pdfbox.pdmodel.PDDocument
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SelectedFileItem(
    val uri: Uri,
    val name: String,
    val sizeBytes: Long,
    val pageCount: Int,
    val localFile: File
) {
    val formattedSize: String
        get() {
            val kb = sizeBytes / 1024.0
            val mb = kb / 1024.0
            return if (mb >= 1.0) {
                String.format("%.1f MB", mb)
            } else {
                String.format("%d KB", kb.toLong().coerceAtLeast(1))
            }
        }
}

class TempFileManager(private val context: Context) {

    fun getVaultDir(): File {
        val dir = File(context.filesDir, "vault")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getTempDir(): File {
        val dir = File(context.cacheDir, "temp_pdf")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    suspend fun copyUriToTemp(uri: Uri, prefix: String = "input_"): SelectedFileItem = withContext(Dispatchers.IO) {
        var displayName = "document_${System.currentTimeMillis()}.pdf"
        var size: Long = 0

        // Resolve display name and size from ContentResolver
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex != -1) displayName = cursor.getString(nameIndex) ?: displayName
                if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
            }
        }

        val cleanName = displayName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        val tempFile = File(getTempDir(), "${prefix}${System.currentTimeMillis()}_$cleanName")

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Unable to open stream for URI: $uri")

        if (size <= 0) {
            size = tempFile.length()
        }

        // Determine page count safely
        var pages = 1
        try {
            if (cleanName.endsWith(".pdf", ignoreCase = true) || tempFile.name.endsWith(".pdf", ignoreCase = true)) {
                PDDocument.load(tempFile).use { doc ->
                    pages = doc.numberOfPages
                }
            }
        } catch (_: Exception) {
            pages = 1
        }

        SelectedFileItem(
            uri = uri,
            name = displayName,
            sizeBytes = size,
            pageCount = pages,
            localFile = tempFile
        )
    }

    fun createOutputFile(title: String): File {
        val safeName = if (title.endsWith(".pdf", ignoreCase = true)) title else "$title.pdf"
        val cleanName = safeName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
        return File(getVaultDir(), cleanName)
    }

    suspend fun exportToUri(sourceFile: File, targetUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(targetUri)?.use { outStream ->
                sourceFile.inputStream().use { inStream ->
                    inStream.copyTo(outStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun cleanupTempFiles() {
        try {
            val dir = getTempDir()
            dir.listFiles()?.forEach { file ->
                if (System.currentTimeMillis() - file.lastModified() > 1000 * 60 * 60) { // older than 1 hr
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
