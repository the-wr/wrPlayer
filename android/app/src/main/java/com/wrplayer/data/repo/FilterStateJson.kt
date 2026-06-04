package com.wrplayer.data.repo

import com.wrplayer.domain.model.FilterState
import com.wrplayer.domain.model.TagDimension
import org.json.JSONArray
import org.json.JSONObject

/**
 * (De)serializes a [FilterState] to the JSON blob stored in `presets.filter_state` (PRD §6.2 / §10.3).
 *
 * Shape: `{"included":{"genre":["Rock","Jazz"]},"excluded":{"pace":["slow"]}}`. Dimension keys use
 * [TagDimension.key]; unknown keys and empty value arrays are dropped on decode so a stale schema can
 * never crash a load. Decoding malformed JSON yields [FilterState.EMPTY].
 */
object FilterStateJson {

    fun encode(filter: FilterState): String =
        JSONObject().apply {
            put("included", encodeMap(filter.included))
            put("excluded", encodeMap(filter.excluded))
        }.toString()

    fun decode(json: String): FilterState = try {
        val root = JSONObject(json)
        FilterState(
            included = decodeMap(root.optJSONObject("included")),
            excluded = decodeMap(root.optJSONObject("excluded")),
        )
    } catch (e: Exception) {
        FilterState.EMPTY
    }

    private fun encodeMap(map: Map<TagDimension, Set<String>>): JSONObject {
        val obj = JSONObject()
        for ((dimension, values) in map) {
            if (values.isEmpty()) continue
            obj.put(dimension.key, JSONArray(values.toList()))
        }
        return obj
    }

    private fun decodeMap(obj: JSONObject?): Map<TagDimension, Set<String>> {
        if (obj == null) return emptyMap()
        val result = LinkedHashMap<TagDimension, Set<String>>()
        for (key in obj.keys()) {
            val dimension = TagDimension.fromKey(key) ?: continue
            val array = obj.optJSONArray(key) ?: continue
            val values = (0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotEmpty) }.toSet()
            if (values.isNotEmpty()) result[dimension] = values
        }
        return result
    }
}
