package com.wrplayer.domain

/** Thresholds for the sort-score state machine (PRD §2.2). */
object SortScore {
    const val PROMOTE_THRESHOLD = 2
    const val DELETE_THRESHOLD = -2

    /** A +1 vote from [persisted] crosses the promote threshold (opens the Tag Sheet, §5.2). */
    fun plusOneCrossesPromote(persisted: Int): Boolean = persisted + 1 >= PROMOTE_THRESHOLD

    /** A −1 vote from [persisted] reaches the delete threshold (permanent delete, §2.2). */
    fun minusOneReachesDelete(persisted: Int): Boolean = persisted - 1 <= DELETE_THRESHOLD
}

/** A single track's sort session: its persisted score plus an in-flight, changeable +1 (PRD §5.2). */
data class SortSession(val persistedScore: Int, val pendingPlusOne: Boolean = false)

/** The result of a vote/advance action in Sort Mode. */
sealed interface SortOutcome {
    /** Track keeps playing; the +1 selection may have changed. [openTagSheet] when +2 is crossed. */
    data class StayPlaying(val session: SortSession, val openTagSheet: Boolean) : SortOutcome

    /** Leave the track, committing [committedScore] to the DB, and advance to the next. */
    data class Advance(val committedScore: Int) : SortOutcome

    /** Track hit −2: commit and permanently delete it. */
    data object Delete : SortOutcome
}

/**
 * Pure reducer for the Sort Mode voting rules (PRD §2.2 / §5.2). A +1 is changeable while the track
 * plays (toggle/deselect, or replace with a final −1); a −1 advances immediately. A single session
 * contributes at most a net ±1, committed when the track is left.
 */
object SortReducer {

    /** Tap +1: toggles the changeable selection; opens the Tag Sheet when selecting crosses +2. */
    fun tapPlusOne(session: SortSession): SortOutcome.StayPlaying {
        val nowSelected = !session.pendingPlusOne
        val openTagSheet = nowSelected && SortScore.plusOneCrossesPromote(session.persistedScore)
        return SortOutcome.StayPlaying(session.copy(pendingPlusOne = nowSelected), openTagSheet)
    }

    /** Tap −1: final, advances immediately; deletes if it reaches −2. */
    fun tapMinusOne(session: SortSession): SortOutcome =
        if (SortScore.minusOneReachesDelete(session.persistedScore)) {
            SortOutcome.Delete
        } else {
            SortOutcome.Advance(session.persistedScore - 1)
        }

    /** Skip / Next / track-end: commit the pending +1 (if any) and advance. */
    fun advance(session: SortSession): SortOutcome.Advance {
        val committed = if (session.pendingPlusOne) session.persistedScore + 1 else session.persistedScore
        return SortOutcome.Advance(committed)
    }
}
