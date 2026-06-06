package com.wrplayer.data.scan

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Live reconciliation-walk status (PRD §4.1 scan spinner): true while the unique reconcile work is
 * enqueued or running. Backed by WorkManager so it reflects on-open and manual rescans alike.
 */
@Singleton
class ScanStatus @Inject constructor(
    @ApplicationContext context: Context,
) {
    val isScanning: Flow<Boolean> =
        WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(ReconciliationWorker.UNIQUE_NAME)
            .map { infos -> infos.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED } }
}
