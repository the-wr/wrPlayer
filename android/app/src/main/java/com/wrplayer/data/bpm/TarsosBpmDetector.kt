package com.wrplayer.data.bpm

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.io.UniversalAudioInputStream
import be.tarsos.dsp.onsets.ComplexOnsetDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt

/**
 * Estimates BPM with TarsosDSP (PRD §9). Because TarsosDSP's default audio I/O is desktop-only,
 * we decode the MP3 to PCM via [PcmDecoder] and feed it through a [UniversalAudioInputStream].
 * Onsets are collected with a spectral-flux detector and folded into a tempo histogram over the
 * 90–180 BPM range; the peak bin is returned. Null when there isn't enough rhythmic content.
 */
class TarsosBpmDetector @Inject constructor() : BpmDetector {

    override suspend fun detect(file: File): Int? = withContext(Dispatchers.IO) {
        val pcm = PcmDecoder.decodeMono16(file) ?: return@withContext null
        coroutineContext.ensureActive()
        val onsets = collectOnsets(pcm)
        coroutineContext.ensureActive()
        estimateBpm(onsets)
    }

    private fun collectOnsets(pcm: PcmAudio): List<Double> {
        val bufferSize = 1024
        val overlap = 512
        val format = TarsosDSPAudioFormat(pcm.sampleRate.toFloat(), 16, 1, true, false)
        val stream = UniversalAudioInputStream(ByteArrayInputStream(pcm.samples), format)
        val dispatcher = AudioDispatcher(stream, bufferSize, overlap)
        val onsets = ArrayList<Double>()
        val detector = ComplexOnsetDetector(bufferSize)
        detector.setHandler { time, _ -> onsets.add(time) }
        dispatcher.addAudioProcessor(detector)
        dispatcher.run()
        return onsets
    }

    private fun estimateBpm(onsets: List<Double>): Int? {
        if (onsets.size < MIN_ONSETS) return null
        // Inter-onset intervals in the 30–300 BPM band.
        val intervals = onsets.zipWithNext { a, b -> b - a }.filter { it in 0.2..2.0 }
        if (intervals.size < 4) return null

        val histogram = IntArray(MAX_BPM + 1)
        for (interval in intervals) {
            var bpm = 60.0 / interval
            while (bpm < MIN_BPM) bpm *= 2
            while (bpm >= MAX_BPM) bpm /= 2
            histogram[bpm.roundToInt().coerceIn(MIN_BPM, MAX_BPM - 1)]++
        }
        val peak = (MIN_BPM until MAX_BPM).maxByOrNull { histogram[it] } ?: return null
        return if (histogram[peak] == 0) null else peak
    }

    private companion object {
        const val MIN_ONSETS = 8
        const val MIN_BPM = 90
        const val MAX_BPM = 180
    }
}
