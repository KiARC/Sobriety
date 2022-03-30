package com.katiearose.sobriety

import java.io.Serializable
import java.time.Instant

class Addiction(val name: String, var lastRelapse: Instant) : Serializable {
    var averageRelapseDuration = Main.timeSinceInstant(lastRelapse)
        private set
    private var relapses = CircularBuffer<Long>(3)

    fun relapse() {
        relapses.update(Main.timeSinceInstant(lastRelapse))
        averageRelapseDuration = calculateAverageRelapseDuration()
        lastRelapse = Instant.now()
    }

    private fun calculateAverageRelapseDuration(): Long {
        return relapses.getAll().sumOf { it!! } / 3L
    }
}