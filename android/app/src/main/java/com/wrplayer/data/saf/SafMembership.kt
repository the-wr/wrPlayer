package com.wrplayer.data.saf

/** Whether a SAF document URI belongs to a given tree (PRD §8.2 / §8.3). */
object SafMembership {
    /**
     * Child document URIs built from a tree are `"<treeUri>/document/<encodedDocId>"`, so a track
     * is under a tree iff its URI carries that prefix.
     */
    fun isUnderTree(documentUri: String, treeUri: String): Boolean =
        documentUri.startsWith("$treeUri/document/")
}
