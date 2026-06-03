package com.wrplayer.data.id3

/**
 * The full set of ID3 fields wrPlayer reads from / writes to an MP3 (PRD §2.3). The file is the
 * source of truth; this is the in-memory shape exchanged with JAudioTagger. Multi-value dimensions
 * are modeled as lists and persisted as native ID3v2.4 null-separated frames (§5.3).
 */
data class Mp3TagData(
    val title: String? = null,        // TIT2
    val artist: String? = null,       // TPE1
    val album: String? = null,        // TALB
    val genres: List<String> = emptyList(),   // TCON (multi)
    val moods: List<String> = emptyList(),     // TMOO (multi)
    val pace: String? = null,         // TXXX:PACE
    val bpm: Int? = null,             // TBPM
    val labels: List<String> = emptyList(),    // TXXX:LABELS (multi)
    val status: String? = null,       // TXXX:STATUS  (inbox / library)
    val hasArt: Boolean = false,      // APIC presence (not the bytes)
)
