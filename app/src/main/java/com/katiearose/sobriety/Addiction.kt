package com.katiearose.sobriety

import java.io.Serializable
import java.time.Instant

class Addiction : Serializable {
    val name: String
    var lastRelapse: Instant
    var averageRelapseDuration: Long
    var relapses: Int

    constructor(name: String, lastRelapse: Instant, averageRelapseDuration: Long, relapses: Int) {
        this.name = name
        this.lastRelapse = lastRelapse
        this.averageRelapseDuration = averageRelapseDuration
        this.relapses = relapses
    }

    constructor(name: String, start: Instant) {
        this.name = name
        this.lastRelapse = start
        this.relapses = 1
        this.averageRelapseDuration = Main.timeSinceInstant(start)
    }

    fun relapse() {
        relapses++
        averageRelapseDuration = calculateAverageRelapseDuration(Main.timeSinceInstant(lastRelapse))
        lastRelapse = Instant.now()
    }

    private fun calculateAverageRelapseDuration(new: Long): Long =
        ((averageRelapseDuration * (relapses - 1)) + new) / relapses
}