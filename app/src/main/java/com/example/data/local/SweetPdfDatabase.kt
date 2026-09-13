package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DocumentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [DocumentEntity::class], version = 1, exportSchema = false)
abstract class SweetPdfDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao

    companion object {
        @Volatile
        private var INSTANCE: SweetPdfDatabase? = null

        fun getDatabase(context: Context): SweetPdfDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SweetPdfDatabase::class.java,
                    "sweet_pdf_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                populateInitialDocuments(database.documentDao())
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialDocuments(dao: DocumentDao) {
            val now = System.currentTimeMillis()
            val initial = listOf(
                DocumentEntity(
                    id = 1L,
                    title = "Legal_Partnership_Contract_v2.pdf",
                    pageCount = 12,
                    fileSizeBytes = 1468006L,
                    formattedSize = "1.4 MB",
                    dateString = "15 mins ago",
                    timestamp = now - 15 * 60 * 1000,
                    category = "signed processed",
                    isStarred = false,
                    isEncrypted = true,
                    badgesPipeSeparated = "Signed (PKCS#12)|AES-256",
                    contentSnippet = "CONFIDENTIAL PARTNERSHIP AGREEMENT: Executed with on-device PKCS#12 cryptographic signature..."
                ),
                DocumentEntity(
                    id = 2L,
                    title = "Medical_Scans_Batch_Merged.pdf",
                    pageCount = 15,
                    fileSizeBytes = 5033164L,
                    formattedSize = "4.8 MB",
                    dateString = "Today at 10:24 AM",
                    timestamp = now - 2 * 3600 * 1000,
                    category = "scans processed",
                    isStarred = false,
                    isEncrypted = false,
                    badgesPipeSeparated = "Merged (15 pages)|Deskewed & B&W",
                    contentSnippet = "Diagnostic imaging and lab series combined via fast native merge pipeline with Otsu thresholding..."
                ),
                DocumentEntity(
                    id = 3L,
                    title = "University_Thesis_Clean_OCR.pdf",
                    pageCount = 88,
                    fileSizeBytes = 3355443L,
                    formattedSize = "3.2 MB",
                    dateString = "Yesterday",
                    timestamp = now - 24 * 3600 * 1000,
                    category = "processed starred",
                    isStarred = true,
                    isEncrypted = false,
                    badgesPipeSeparated = "OCR Searchable|Compressed (-62%)",
                    contentSnippet = "Doctoral Dissertation on Distributed Neural Edge Computing. Searchable text layer created offline..."
                ),
                DocumentEntity(
                    id = 4L,
                    title = "Passport_Dual_Scan_2in1.pdf",
                    pageCount = 1,
                    fileSizeBytes = 460800L,
                    formattedSize = "450 KB",
                    dateString = "Oct 24",
                    timestamp = now - 3 * 24 * 3600 * 1000,
                    category = "scans",
                    isStarred = false,
                    isEncrypted = false,
                    badgesPipeSeparated = "ID Scanner|Redacted",
                    contentSnippet = "Official Travel Identity & Biometric Page 2-in-1 print layout with national ID redacted..."
                ),
                DocumentEntity(
                    id = 5L,
                    title = "Q4_Financial_Statements_Flattened.pdf",
                    pageCount = 32,
                    fileSizeBytes = 2202009L,
                    formattedSize = "2.1 MB",
                    dateString = "Oct 22",
                    timestamp = now - 5 * 24 * 3600 * 1000,
                    category = "processed",
                    isStarred = false,
                    isEncrypted = false,
                    badgesPipeSeparated = "Flattened AcroForms|PDF/A Archival",
                    contentSnippet = "Audited Q4 Balance Sheets and Cash Flow Projections converted to ISO 19005 standard..."
                )
            )
            dao.insertAll(initial)
        }
    }
}
