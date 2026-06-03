package com.wrplayer.data.bpm

import java.io.File

/**
 * Detects the tempo (BPM) of an MP3 (PRD §9). Implementations run off the main thread and are
 * cancellable. Returns null when detection is not possible or fails — a null result is explicitly
 * acceptable (the user can set BPM manually), so callers must tolerate it.
 */
interface BpmDetector {
    suspend fun detect(file: File): Int?
}
