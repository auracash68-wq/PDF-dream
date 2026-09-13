package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val pageCount: Int,
    val fileSizeBytes: Long,
    val formattedSize: String,
    val dateString: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "processed", // e.g. "all", "processed", "scans", "signed", "starred"
    val isStarred: Boolean = false,
    val isEncrypted: Boolean = false,
    val badgesPipeSeparated: String = "", // e.g. "Signed (PKCS#12)|AES-256"
    val filePath: String? = null,
    val contentSnippet: String? = null
) {
    fun getBadges(): List<String> {
        if (badgesPipeSeparated.isBlank()) return emptyList()
        return badgesPipeSeparated.split("|").filter { it.isNotBlank() }
    }
}
