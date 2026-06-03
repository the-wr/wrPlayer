package com.wrplayer.data.bpm

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.File
import java.nio.ByteOrder

/** Decoded mono 16-bit little-endian PCM plus its sample rate. */
class PcmAudio(val samples: ByteArray, val sampleRate: Int)

/**
 * Decodes the first audio track of an MP3 to mono 16-bit PCM using the platform [MediaCodec]
 * (PRD §9). TarsosDSP's default audio I/O is desktop-only, so we produce raw PCM here and feed it
 * to the detector ourselves. Decoding is capped at [maxSeconds] to keep detection fast.
 */
object PcmDecoder {

    fun decodeMono16(file: File, maxSeconds: Int = 60): PcmAudio? {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(file.absolutePath)
            val trackIndex = (0 until extractor.trackCount).firstOrNull { i ->
                extractor.getTrackFormat(i).getString(MediaFormat.KEY_MIME)
                    ?.startsWith("audio/") == true
            } ?: return null

            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: return null
            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val maxSamples = sampleRate.toLong() * maxSeconds

            val codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()
            try {
                drainToMonoPcm(extractor, codec, channels, maxSamples)?.let {
                    PcmAudio(it, sampleRate)
                }
            } finally {
                codec.stop()
                codec.release()
            }
        } catch (e: Exception) {
            null
        } finally {
            extractor.release()
        }
    }

    private fun drainToMonoPcm(
        extractor: MediaExtractor,
        codec: MediaCodec,
        channels: Int,
        maxSamples: Long,
    ): ByteArray? {
        val out = java.io.ByteArrayOutputStream()
        val bufferInfo = MediaCodec.BufferInfo()
        var inputDone = false
        var outputDone = false
        var monoSamples = 0L
        val timeoutUs = 10_000L

        while (!outputDone) {
            if (!inputDone) {
                val inIndex = codec.dequeueInputBuffer(timeoutUs)
                if (inIndex >= 0) {
                    val inBuf = codec.getInputBuffer(inIndex)!!
                    val size = extractor.readSampleData(inBuf, 0)
                    if (size < 0) {
                        codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                    } else {
                        codec.queueInputBuffer(inIndex, 0, size, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }
            }

            val outIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            if (outIndex >= 0) {
                val outBuf = codec.getOutputBuffer(outIndex)!!.order(ByteOrder.LITTLE_ENDIAN)
                if (bufferInfo.size > 0) {
                    monoSamples += downmix(outBuf, bufferInfo.size, channels, out)
                }
                codec.releaseOutputBuffer(outIndex, false)
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                if (monoSamples >= maxSamples) outputDone = true
            }
        }
        val bytes = out.toByteArray()
        return bytes.takeIf { it.isNotEmpty() }
    }

    /** Average interleaved channels down to mono 16-bit LE; returns the number of mono samples written. */
    private fun downmix(
        buffer: java.nio.ByteBuffer,
        sizeBytes: Int,
        channels: Int,
        out: java.io.ByteArrayOutputStream,
    ): Int {
        val shorts = (buffer as java.nio.ByteBuffer).asShortBuffer()
        val totalShorts = sizeBytes / 2
        var i = 0
        var written = 0
        while (i + channels <= totalShorts) {
            var sum = 0
            for (c in 0 until channels) sum += shorts.get(i + c).toInt()
            val mono = (sum / channels)
            out.write(mono and 0xFF)
            out.write((mono shr 8) and 0xFF)
            i += channels
            written++
        }
        return written
    }
}
