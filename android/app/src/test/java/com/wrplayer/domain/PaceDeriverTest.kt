package com.wrplayer.domain

import com.google.common.truth.Truth.assertThat
import com.wrplayer.domain.model.Pace
import org.junit.Test

/** Pace-bucket boundaries (PRD §9): slow < 90, medium 90–140, fast > 140. */
class PaceDeriverTest {
    @Test fun nullBpm_givesNull() = assertThat(PaceDeriver.fromBpm(null)).isNull()
    @Test fun below90_isSlow() = assertThat(PaceDeriver.fromBpm(89)).isEqualTo(Pace.SLOW)
    @Test fun at90_isMedium() = assertThat(PaceDeriver.fromBpm(90)).isEqualTo(Pace.MEDIUM)
    @Test fun at140_isMedium() = assertThat(PaceDeriver.fromBpm(140)).isEqualTo(Pace.MEDIUM)
    @Test fun above140_isFast() = assertThat(PaceDeriver.fromBpm(141)).isEqualTo(Pace.FAST)
}
