package com.katiearose.sobriety

import java.io.Serializable
import java.time.Instant

class Addiction(
    val name: String,
    var lastRelapse: Instant,
    var relapses: CircularBuffer<Long> = CircularBuffer(3) //Default is a new one, but you can provide your own (from a cache)
) : Serializable {
    var averageRelapseDuration = Main.timeSinceInstant(lastRelapse)
        private set

    fun relapse() {
        relapses.update(Main.timeSinceInstant(lastRelapse))
        averageRelapseDuration = calculateAverageRelapseDuration()
        lastRelapse = Instant.now()
    }

    private fun calculateAverageRelapseDuration(): Long {
        return relapses.getAll().filterNotNull().sumOf { it } / 3L
    }

    fun toCacheable(): HashMap<Int, Any> {
        val map = HashMap<Int, Any>()
        map[0] = name
        map[1] = lastRelapse
        map[2] = relapses
        return map
    }

    companion object {
        fun fromCacheable(map: HashMap<Int, Any>): Addiction {
            return Addiction(map[0] as String, map[1] as Instant, map[2] as CircularBuffer<Long>)
        }
    }
}