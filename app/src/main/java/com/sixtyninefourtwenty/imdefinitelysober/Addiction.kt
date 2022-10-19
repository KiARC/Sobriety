package com.sixtyninefourtwenty.imdefinitelysober

import com.sixtyninefourtwenty.imdefinitelysober.internal.CircularBuffer
import com.sixtyninefourtwenty.imdefinitelysober.utils.secondsFromNow
import java.io.Serializable
import java.time.Instant

class Addiction(
    val name: String,
    var lastRelapse: Instant,
    var isStopped: Boolean,
    var timeStopped: Long, //in milliseconds
    private val relapses: CircularBuffer<Long> = CircularBuffer(3) //Default is a new one, but you can provide your own (from a cache)
) : Serializable {
    var averageRelapseDuration = if (relapses.get(0) == null) -1 else calculateAverageRelapseDuration()
        private set

    fun relapse() {
        if (!isStopped)
            relapses.update(lastRelapse.secondsFromNow())
        else
            relapses.update(Instant.ofEpochMilli(timeStopped).epochSecond - lastRelapse.epochSecond)
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
        map[2] = isStopped
        map[3] = timeStopped
        map[4] = relapses
        return map
    }

    companion object {
        fun fromCacheable(map: HashMap<Int, Any>): Addiction {
            return Addiction(
                map[0] as String,
                map[1] as Instant,
                map[2] as Boolean,
                map[3] as Long,
                map[4] as CircularBuffer<Long>
            )
        }
    }
}