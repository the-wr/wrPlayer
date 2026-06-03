package com.wrplayer.data.scan

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Runs the background reconciliation walk off the main thread (PRD §8.2). Triggered on app open and
 * by the manual rescan button via [ScanTrigger].
 */
@HiltWorker
class ReconciliationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reconciler: LibraryReconciler,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        reconciler.reconcile()
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }

    companion object {
        const val UNIQUE_NAME = "wrplayer-reconcile"
    }
}

/** Enqueues a reconciliation walk, coalescing with any already pending (PRD §8.2). */
class ScanTrigger @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun requestScan() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            ReconciliationWorker.UNIQUE_NAME,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<ReconciliationWorker>().build(),
        )
    }
}
