package com.katiearose.sobriety

import java.io.Serializable
import java.time.Instant

class Addiction(val name: String, var lastRelapse: Instant) : Serializable {
    var averageRelapseDuration = 0L
    private var relapses = 0

    fun relapse() {
        relapses++
        averageRelapseDuration = calculateAverageRelapseDuration(Main.timeSinceInstant(lastRelapse))
        lastRelapse = Instant.now()
    }

    private fun calculateAverageRelapseDuration(new: Long): Long =
        ((averageRelapseDuration * (relapses - 1)) + new) / relapses
}