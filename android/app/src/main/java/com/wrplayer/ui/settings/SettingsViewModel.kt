package com.wrplayer.ui.settings

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wrplayer.data.db.TrackDao
import com.wrplayer.data.saf.SafTreeManager
import com.wrplayer.data.scan.ScanStatus
import com.wrplayer.data.scan.ScanTrigger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** One watched-folder row in Settings (PRD §3 / §8). */
data class WatchedFolder(
    val treeUri: String,
    val name: String,
    val path: String,
    val isSdCard: Boolean,
    val available: Boolean,
)

data class SettingsUiState(
    val folders: List<WatchedFolder> = emptyList(),
    val scanning: Boolean = false,
    val libraryCount: Int = 0,
    val loaded: Boolean = false,
)

/**
 * Drives Settings → Watched Folders (PRD §3 / §8): lists persisted SAF trees, adds/removes them, and
 * triggers the manual rescan. Adding or removing a folder kicks a reconciliation walk.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val safTreeManager: SafTreeManager,
    private val scanTrigger: ScanTrigger,
    private val scanStatus: ScanStatus,
    private val trackDao: TrackDao,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        refresh()
        viewModelScope.launch { scanStatus.isScanning.collect { s -> _state.update { it.copy(scanning = s) } } }
        viewModelScope.launch { trackDao.observeLibraryCount().collect { n -> _state.update { it.copy(libraryCount = n) } } }
    }

    fun refresh() {
        viewModelScope.launch {
            val folders = withContext(Dispatchers.IO) {
                safTreeManager.persistedTrees().map { uri -> describe(uri) }
            }
            _state.update { it.copy(folders = folders, loaded = true) }
        }
    }

    fun addFolder(treeUri: Uri) {
        safTreeManager.persist(treeUri)
        scanTrigger.requestScan()
        refresh()
    }

    fun removeFolder(treeUri: String) {
        safTreeManager.release(Uri.parse(treeUri))
        scanTrigger.requestScan()
        refresh()
    }

    fun rescan() = scanTrigger.requestScan()

    private fun describe(treeUri: String): WatchedFolder {
        val doc = runCatching { DocumentFile.fromTreeUri(context, Uri.parse(treeUri)) }.getOrNull()
        val docId = Uri.parse(treeUri).lastPathSegment.orEmpty() // e.g. "primary:Music" or "0BFA-1C3E:Beats"
        val volume = docId.substringBefore(':', "")
        val relative = docId.substringAfter(':', docId)
        val isSdCard = volume.isNotEmpty() && !volume.equals("primary", ignoreCase = true)
        val path = if (isSdCard) "/storage/$volume/$relative" else "/storage/emulated/0/$relative"
        return WatchedFolder(
            treeUri = treeUri,
            name = doc?.name ?: relative.substringAfterLast('/').ifEmpty { relative },
            path = path,
            isSdCard = isSdCard,
            available = doc != null && doc.exists() && doc.canRead(),
        )
    }
}
