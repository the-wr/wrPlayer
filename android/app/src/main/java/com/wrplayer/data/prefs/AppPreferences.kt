package com.wrplayer.data.prefs

import android.content.Context
import com.wrplayer.domain.model.SortOrder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Small key/value app state that has no ID3 equivalent: last sort order, theme/accent, and the
 * persisted Play Mode queue + position (PRD §4.1 / §6.3). Backed by SharedPreferences.
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("wrplayer", Context.MODE_PRIVATE)

    var lastSortOrder: SortOrder
        get() = SortOrder.fromKey(prefs.getString(KEY_SORT_ORDER, null))
        set(value) = prefs.edit().putString(KEY_SORT_ORDER, value.key).apply()

    /** Persisted Play Mode queue (ordered document URIs) and the index/position within it (§6.3). */
    var queueUris: List<String>
        get() = prefs.getString(KEY_QUEUE, null)?.split('\n')?.filter { it.isNotEmpty() }.orEmpty()
        set(value) = prefs.edit().putString(KEY_QUEUE, value.joinToString("\n")).apply()

    var queueIndex: Int
        get() = prefs.getInt(KEY_QUEUE_INDEX, 0)
        set(value) = prefs.edit().putInt(KEY_QUEUE_INDEX, value).apply()

    var queuePositionMs: Long
        get() = prefs.getLong(KEY_QUEUE_POSITION, 0L)
        set(value) = prefs.edit().putLong(KEY_QUEUE_POSITION, value).apply()

    private companion object {
        const val KEY_SORT_ORDER = "last_sort_order"
        const val KEY_QUEUE = "queue_uris"
        const val KEY_QUEUE_INDEX = "queue_index"
        const val KEY_QUEUE_POSITION = "queue_position"
    }
}
