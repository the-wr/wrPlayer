package com.wrplayer.data.db

/**
 * Multi-value tags are stored as a native ID3v2.4 null-separated (`0x00`) list, and the DB cache
 * mirrors that format for round-trip fidelity (PRD §5.3 / §10.3). The null byte cannot occur in
 * user input, so there is no delimiter-collision risk.
 *
 * Expressed via [Char] so the source file holds no literal control character.
 */
val MULTI_VALUE_SEPARATOR: String = Char(0).toString()

/** Split a null-separated cache column into its values, dropping empties. */
fun String?.splitMultiValue(): List<String> =
    if (this.isNullOrEmpty()) emptyList()
    else split(MULTI_VALUE_SEPARATOR).filter { it.isNotEmpty() }

/** Join values into a null-separated cache column, or null when empty. */
fun List<String>.joinMultiValue(): String? =
    filter { it.isNotEmpty() }.takeIf { it.isNotEmpty() }?.joinToString(MULTI_VALUE_SEPARATOR)
