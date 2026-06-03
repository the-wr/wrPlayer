package com.wrplayer.data.bpm

import java.io.File
import javax.inject.Inject

/**
 * No-op detector that always returns null (PRD §9). Serves as the production fallback if real
 * detection is disabled, and as a test double.
 */
class StubBpmDetector @Inject constructor() : BpmDetector {
    override suspend fun detect(file: File): Int? = null
}
