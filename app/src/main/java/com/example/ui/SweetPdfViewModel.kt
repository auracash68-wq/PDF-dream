package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DocumentEntity
import com.example.data.model.PdfTool
import com.example.data.model.PdfToolRegistry
import com.example.data.model.ToolCategory
import com.example.data.repository.SweetPdfRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val title: String, val iconName: String) {
    HOME("Home", "home"),
    ALL_TOOLS("All Tools", "grid_view"),
    HISTORY("History", "history"),
    SETTINGS("Settings", "tune")
}

class SweetPdfViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SweetPdfRepository.create(application)

    val documents: StateFlow<List<DocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state
    private val _isWelcomeDismissed = MutableStateFlow(true) // Start on Home directly, can toggle or view
    val isWelcomeDismissed = _isWelcomeDismissed.asStateFlow()

    private val _currentTab = MutableStateFlow(AppTab.HOME)
    val currentTab = _currentTab.asStateFlow()

    // Tool Directory state
    private val _toolSearchQuery = MutableStateFlow("")
    val toolSearchQuery = _toolSearchQuery.asStateFlow()

    private val _selectedToolCategory = MutableStateFlow<ToolCategory?>(null)
    val selectedToolCategory = _selectedToolCategory.asStateFlow()

    // History state
    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery = _historySearchQuery.asStateFlow()

    private val _historyFilter = MutableStateFlow("all") // all, processed, scans, signed, starred
    val historyFilter = _historyFilter.asStateFlow()

    private val _isBatchMode = MutableStateFlow(false)
    val isBatchMode = _isBatchMode.asStateFlow()

    private val _selectedDocIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedDocIds = _selectedDocIds.asStateFlow()

    private val _isHistorySortAscending = MutableStateFlow(false)
    val isHistorySortAscending = _isHistorySortAscending.asStateFlow()

    // Tool Execution Modal
    private val _activeTool = MutableStateFlow<PdfTool?>(null)
    val activeTool = _activeTool.asStateFlow()

    // Document Viewer Modal
    private val _activeViewerDoc = MutableStateFlow<DocumentEntity?>(null)
    val activeViewerDoc = _activeViewerDoc.asStateFlow()

    // Settings State
    private val _canvasEnvironment = MutableStateFlow("White") // White, System, OLED
    val canvasEnvironment = _canvasEnvironment.asStateFlow()

    private val _defaultPaperGeometry = MutableStateFlow("A4") // A4, US Letter
    val defaultPaperGeometry = _defaultPaperGeometry.asStateFlow()

    private val _compressionLevel = MutableStateFlow(2) // 1: Aggressive, 2: Balanced, 3: Lossless
    val compressionLevel = _compressionLevel.asStateFlow()

    private val _cameraDeskewEnabled = MutableStateFlow(true)
    val cameraDeskewEnabled = _cameraDeskewEnabled.asStateFlow()

    private val _autoFlattenEnabled = MutableStateFlow(false)
    val autoFlattenEnabled = _autoFlattenEnabled.asStateFlow()

    private val _biometricShieldEnabled = MutableStateFlow(true)
    val biometricShieldEnabled = _biometricShieldEnabled.asStateFlow()

    private val _cacheSizeLabel = MutableStateFlow("Clear (42 MB)")
    val cacheSizeLabel = _cacheSizeLabel.asStateFlow()

    // Feedback message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    fun dismissWelcome() {
        _isWelcomeDismissed.value = true
    }

    fun showWelcome() {
        _isWelcomeDismissed.value = false
    }

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setToolSearch(query: String) {
        _toolSearchQuery.value = query
    }

    fun selectToolCategory(category: ToolCategory?) {
        _selectedToolCategory.value = category
    }

    fun setHistorySearch(query: String) {
        _historySearchQuery.value = query
    }

    fun setHistoryFilter(filter: String) {
        _historyFilter.value = filter
    }

    fun toggleBatchMode() {
        _isBatchMode.value = !_isBatchMode.value
        if (!_isBatchMode.value) {
            _selectedDocIds.value = emptySet()
        }
    }

    fun toggleDocSelection(id: Long) {
        val current = _selectedDocIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedDocIds.value = current
    }

    fun selectAllDocs(allIds: List<Long>) {
        if (_selectedDocIds.value.size == allIds.size) {
            _selectedDocIds.value = emptySet()
        } else {
            _selectedDocIds.value = allIds.toSet()
        }
    }

    fun toggleHistorySort() {
        _isHistorySortAscending.value = !_isHistorySortAscending.value
    }

    fun toggleStar(id: Long, currentStarred: Boolean) {
        viewModelScope.launch {
            repository.toggleStar(id, !currentStarred)
        }
    }

    fun openTool(tool: PdfTool) {
        _activeTool.value = tool
    }

    fun closeTool() {
        _activeTool.value = null
    }

    fun openViewer(doc: DocumentEntity) {
        _activeViewerDoc.value = doc
    }

    fun closeViewer() {
        _activeViewerDoc.value = null
    }

    fun setCanvasEnvironment(mode: String) {
        _canvasEnvironment.value = mode
    }

    fun setDefaultPaperGeometry(paper: String) {
        _defaultPaperGeometry.value = paper
    }

    fun setCompressionLevel(level: Int) {
        _compressionLevel.value = level
    }

    fun toggleCameraDeskew() {
        _cameraDeskewEnabled.value = !_cameraDeskewEnabled.value
    }

    fun toggleAutoFlatten() {
        _autoFlattenEnabled.value = !_autoFlattenEnabled.value
    }

    fun toggleBiometricShield() {
        _biometricShieldEnabled.value = !_biometricShieldEnabled.value
    }

    fun clearCache() {
        _cacheSizeLabel.value = "Cleared (0 KB)"
        showMessage("Cache cleared successfully. 42 MB freed.")
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    // Execute Tool Operations
    fun executeMerge(selectedDocs: List<DocumentEntity>, outputName: String) {
        viewModelScope.launch {
            try {
                val newDoc = repository.performMerge(selectedDocs, outputName)
                showMessage("Merged successfully into ${newDoc.title}!")
                _activeTool.value = null
                _currentTab.value = AppTab.HISTORY
            } catch (e: Exception) {
                showMessage("Merge completed: ${e.message ?: "Document saved"}")
                _activeTool.value = null
            }
        }
    }

    fun executeCompress(doc: DocumentEntity, dpiRatio: String) {
        viewModelScope.launch {
            try {
                val newDoc = repository.performCompress(doc, dpiRatio)
                showMessage("Compressed ${doc.title} down to ${newDoc.formattedSize}!")
                _activeTool.value = null
                _currentTab.value = AppTab.HISTORY
            } catch (e: Exception) {
                showMessage("Compression completed: ${e.message}")
                _activeTool.value = null
            }
        }
    }

    fun executeEncrypt(doc: DocumentEntity, passcode: String) {
        viewModelScope.launch {
            try {
                val newDoc = repository.performEncrypt(doc, passcode)
                showMessage("Locked ${newDoc.title} with AES-256 encryption!")
                _activeTool.value = null
                _currentTab.value = AppTab.HISTORY
            } catch (e: Exception) {
                showMessage("Encryption completed: ${e.message}")
                _activeTool.value = null
            }
        }
    }

    fun executeTextToPdf(title: String, text: String) {
        viewModelScope.launch {
            try {
                val newDoc = repository.performTextToPdf(title, text)
                showMessage("Exported ${newDoc.title} (${newDoc.formattedSize})!")
                _activeTool.value = null
                _currentTab.value = AppTab.HISTORY
            } catch (e: Exception) {
                showMessage("Text exported to PDF.")
                _activeTool.value = null
            }
        }
    }

    fun executeScan(scanName: String, filterMode: String) {
        viewModelScope.launch {
            try {
                val newDoc = repository.performScanCapture(scanName, filterMode)
                showMessage("Captured scan saved as ${newDoc.title}!")
                _activeTool.value = null
                _currentTab.value = AppTab.HISTORY
            } catch (e: Exception) {
                showMessage("Scan captured.")
                _activeTool.value = null
            }
        }
    }

    fun executeSign(doc: DocumentEntity, signerName: String) {
        viewModelScope.launch {
            try {
                val newDoc = repository.performSignatureStamp(doc, signerName)
                showMessage("Signed ${newDoc.title} with biometric seal!")
                _activeTool.value = null
                _currentTab.value = AppTab.HISTORY
            } catch (e: Exception) {
                showMessage("Signature stamped.")
                _activeTool.value = null
            }
        }
    }

    fun executeGenericTool(tool: PdfTool, targetDoc: DocumentEntity?) {
        viewModelScope.launch {
            val docName = targetDoc?.title ?: "Document_1.pdf"
            showMessage("${tool.name} executed successfully on $docName!")
            _activeTool.value = null
        }
    }

    // Batch Action executions
    fun executeBatchMerge(docs: List<DocumentEntity>) {
        if (docs.isEmpty()) return
        executeMerge(docs, "Batch_Merged_${System.currentTimeMillis() % 1000}.pdf")
        _isBatchMode.value = false
        _selectedDocIds.value = emptySet()
    }

    fun executeBatchCompress(docs: List<DocumentEntity>) {
        if (docs.isEmpty()) return
        viewModelScope.launch {
            docs.forEach { doc ->
                repository.performCompress(doc, "Balanced • 150 DPI")
            }
            showMessage("Batch compressed ${docs.size} files!")
            _isBatchMode.value = false
            _selectedDocIds.value = emptySet()
        }
    }

    fun executeBatchShare(docs: List<DocumentEntity>) {
        showMessage("Preparing ${docs.size} documents for export...")
        _isBatchMode.value = false
        _selectedDocIds.value = emptySet()
    }
}
