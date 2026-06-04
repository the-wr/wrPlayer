package com.wrplayer.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Exhaustive coverage of the Sort Mode voting state machine (PRD §2.2 / §5.2). */
class SortReducerTest {

    @Test fun plusOne_selectsThenDeselects() {
        val s = SortSession(persistedScore = 0)
        val sel = SortReducer.tapPlusOne(s)
        assertThat(sel.session.pendingPlusOne).isTrue()
        assertThat(sel.openTagSheet).isFalse()
        val desel = SortReducer.tapPlusOne(sel.session)
        assertThat(desel.session.pendingPlusOne).isFalse()
    }

    @Test fun advance_commitsPendingPlusOne() {
        val s = SortSession(persistedScore = 0, pendingPlusOne = true)
        assertThat(SortReducer.advance(s).committedScore).isEqualTo(1)
    }

    @Test fun advance_withNoVote_keepsScore() {
        assertThat(SortReducer.advance(SortSession(1)).committedScore).isEqualTo(1)
    }

    @Test fun plusOne_atScoreOne_crossesPromoteThreshold() {
        // 1 → +1 reaches +2: Tag Sheet opens immediately while playing (§5.2).
        val result = SortReducer.tapPlusOne(SortSession(persistedScore = 1))
        assertThat(result.openTagSheet).isTrue()
    }

    @Test fun deselectingPlusOne_doesNotOpenTagSheet() {
        val selected = SortSession(persistedScore = 1, pendingPlusOne = true)
        assertThat(SortReducer.tapPlusOne(selected).openTagSheet).isFalse()
    }

    @Test fun reEnteredAtTwo_nextPlusOneReopensSheet() {
        // Dismissed at +2, score stayed 2; the next +1 vote (→3) reopens (§2.2).
        assertThat(SortReducer.tapPlusOne(SortSession(persistedScore = 2)).openTagSheet).isTrue()
    }

    @Test fun minusOne_advancesAndDecrements() {
        val out = SortReducer.tapMinusOne(SortSession(persistedScore = 0))
        assertThat(out).isInstanceOf(SortOutcome.Advance::class.java)
        assertThat((out as SortOutcome.Advance).committedScore).isEqualTo(-1)
    }

    @Test fun minusOne_atMinusOne_deletes() {
        // Reaching −2 deletes immediately (§2.2). Requires a −1 in two separate sessions.
        assertThat(SortReducer.tapMinusOne(SortSession(persistedScore = -1))).isEqualTo(SortOutcome.Delete)
    }

    @Test fun reachingTwoRequiresTwoSessions() {
        // Session 1: 0 → commit +1 → 1 (no sheet on a +1 that only reaches +1).
        assertThat(SortReducer.tapPlusOne(SortSession(0)).openTagSheet).isFalse()
        assertThat(SortReducer.advance(SortSession(0, pendingPlusOne = true)).committedScore).isEqualTo(1)
        // Session 2: 1 → +1 crosses +2.
        assertThat(SortReducer.tapPlusOne(SortSession(1)).openTagSheet).isTrue()
    }
}
