package com.wrplayer.domain.model

/** Three-state cycle of a Queue Editor chip (PRD §6.2). */
enum class ChipState {
    UNSELECTED,
    INCLUDED,
    EXCLUDED;

    /** Next state in the tap cycle: unselected → included → excluded → unselected. */
    fun next(): ChipState = when (this) {
        UNSELECTED -> INCLUDED
        INCLUDED -> EXCLUDED
        EXCLUDED -> UNSELECTED
    }
}
