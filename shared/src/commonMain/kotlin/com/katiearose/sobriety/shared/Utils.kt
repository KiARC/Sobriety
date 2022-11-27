package com.katiearose.sobriety.shared

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant

fun Instant.secondsFromNow(): Long = Clock.System.now().epochSeconds - this.epochSeconds

fun DateTimeUnit.toMillis(): Long {
    return when (this) {
        is DateTimeUnit.TimeBased -> nanoseconds / 1_000_000
        is DateTimeUnit.DayBased -> days.toLong() * 24 * 60 * 60 * 1000
        is DateTimeUnit.MonthBased -> months.toLong() * 31 * 24 * 60 * 60 * 1000
    }
}

/**
 * Puts the specified value to the last key in this map.
 */
fun <K, V> LinkedHashMap<K, V>.putLast(value: V) {
    val lastKey = keys.map { it }.last()
    put(lastKey, value)
}