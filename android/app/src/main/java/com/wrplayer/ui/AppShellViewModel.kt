package com.wrplayer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wrplayer.data.prefs.AppPreferences
import com.wrplayer.data.saf.SafTreeManager
import com.wrplayer.data.scan.ScanTrigger
import com.wrplayer.ui.nav.DefaultMode
import com.wrplayer.ui.nav.StartDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppShellState(
    val resolved: Boolean = false,
    val startInSettings: Boolean = false,
)

/**
 * Resolves the launch route (PRD §3) and kicks the on-open reconciliation walk (§8.2): with no
 * watched folder the app opens on Settings → Watched Folders; otherwise it opens in Play Mode
 * (restored at the persisted queue, or the empty-queue state).
 */
@HiltViewModel
class AppShellViewModel @Inject constructor(
    private val safTreeManager: SafTreeManager,
    private val scanTrigger: ScanTrigger,
    private val prefs: AppPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(AppShellState())
    val state: StateFlow<AppShellState> = _state.asStateFlow()

    init {
        scanTrigger.requestScan() // background walk on app open (§8.2)
        viewModelScope.launch {
            val hasFolders = safTreeManager.persistedTrees().isNotEmpty()
            val destination = DefaultMode.resolve(hasFolders, prefs.queueUris.isNotEmpty())
            _state.value = AppShellState(
                resolved = true,
                startInSettings = destination == StartDestination.WATCHED_FOLDERS,
            )
        }
    }
}
