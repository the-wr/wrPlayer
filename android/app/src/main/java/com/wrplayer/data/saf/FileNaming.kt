package com.wrplayer.data.saf

/** Collision-free file naming for promotion into a flat `Library/` folder (PRD §8.1). */
object FileNaming {
    /**
     * Returns [name] if it is not already taken, otherwise appends `_2`, `_3`, … before the
     * extension until the name is unique within [existing].
     */
    fun uniqueName(name: String, existing: Set<String>): String {
        if (name !in existing) return name
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var n = 2
        while (true) {
            val candidate = "${base}_$n$ext"
            if (candidate !in existing) return candidate
            n++
        }
    }
}
