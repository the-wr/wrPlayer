package com.wrplayer.domain

/** Title/Artist/Album extracted from a filename for tag-sheet pre-fill (PRD §5.3). */
data class ParsedNames(val title: String, val artist: String?, val album: String?)

/**
 * Pre-fills Title/Artist (and sometimes Album) by parsing a file path when ID3 tags are absent
 * (PRD §5.3). Tries, in order: `number - track` (with parent folders giving artist/album),
 * `artist - track`, else the whole filename as the title.
 */
object FilenameParser {
    private val NUMBER_TRACK = Regex("""^\d+\s*[-.]\s*(.+)$""")
    private const val SEPARATOR = " - "

    fun parse(path: String): ParsedNames {
        val segments = path.split('/', '\\').filter { it.isNotBlank() }
        val fileName = (segments.lastOrNull() ?: "").substringBeforeLast('.').trim()
        val parents = segments.dropLast(1)

        NUMBER_TRACK.matchEntire(fileName)?.let { match ->
            val track = match.groupValues[1].trim()
            // .../$artist/$album/$number - $track
            val album = parents.getOrNull(parents.lastIndex)
            val artist = parents.getOrNull(parents.lastIndex - 1)
            return ParsedNames(track, artist, album)
        }

        val sep = fileName.indexOf(SEPARATOR)
        if (sep > 0) {
            val artist = fileName.substring(0, sep).trim()
            val title = fileName.substring(sep + SEPARATOR.length).trim()
            return ParsedNames(title, artist.ifBlank { null }, null)
        }

        return ParsedNames(fileName, null, null)
    }
}
