package com.katiearose.sobriety.shared

import kotlinx.datetime.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = AddictionSerializer::class)
@SerialName("addiction")
data class Addiction(
    val name: String,
    var lastRelapse: Instant,
    var isStopped: Boolean,
    var timeStopped: Long, //in milliseconds
    val history: LinkedHashMap<Long, Long>, //in milliseconds
    var priority: Priority,
    val dailyNotes: LinkedHashMap<LocalDate, String>,
    var timeSaving: LocalTime,
    val savings: LinkedHashMap<String, Pair<Double, String>>,
    val milestones: LinkedHashSet<Pair<Int, DateTimeUnit>>,
    internal val relapses: CircularBuffer<Long> = CircularBuffer(3) //Default is a new one, but you can provide your own (from a cache)
) {

    enum class Priority {
        HIGH, MEDIUM, LOW
    }

    fun isFuture(): Boolean = lastRelapse > Clock.System.now()

    /**
     * @param whichAttempts list of map indices
     */
    fun calculateAvgRelapseDuration(whichAttempts: List<Int>): Long {
        var totalDuration = 0L
        val list = history.toList()
        for (attemptIndex in whichAttempts) {
            totalDuration += (list[attemptIndex].second - list[attemptIndex].first)
        }
        return totalDuration / whichAttempts.size
    }

    /**
     * @param numAttempts number of attempts to calculate
     * @return average duration in milliseconds or null if no history
     */
    fun calculateRecentAverage(numAttempts: Int): Long? {
        if (history.size <= 1) return null
        val currentAttempt = history.size - 1
        val minimum = (currentAttempt - numAttempts).coerceAtLeast(0)
        return calculateAvgRelapseDuration((minimum until currentAttempt).toList())
    }

    /**
     * @return - the goal point in milliseconds
     * - the progress as an int in range 0..100
     */
    fun calculateMilestoneProgressionPercentage(milestone: Pair<Int, DateTimeUnit>): Pair<Long, Int> {
        val goal = lastRelapse.toEpochMilliseconds() + milestone.first * milestone.second.toMillis()
        if (isStopped) {
            return Pair(goal, 0)
        }
        return Pair(goal, (((Clock.System.now().toEpochMilliseconds() - lastRelapse.toEpochMilliseconds()).toFloat() /
                (goal - lastRelapse.toEpochMilliseconds())) * 100).toInt())
    }

    fun stopAbstaining() {
        isStopped = true
        timeStopped = Clock.System.now().toEpochMilliseconds()
        history.putLast(Clock.System.now().toEpochMilliseconds())
    }

    fun relapse() {
        if (!isStopped && !isFuture()) {
            history.putLast(Clock.System.now().toEpochMilliseconds())
        }
        history[Clock.System.now().toEpochMilliseconds()] = 0
        isStopped = false
        lastRelapse = Clock.System.now()
    }

    fun getDailyNotesList(sort: SortMode): List<Pair<LocalDate, String>> {
        return when (sort) {
            SortMode.ASC -> dailyNotes.toList().sortedWith { n1, n2 -> n1.first.compareTo(n2.first) }
            SortMode.DESC -> dailyNotes.toList().sortedWith { n1, n2 -> n2.first.compareTo(n1.first) }
            SortMode.NONE -> dailyNotes.toList()
        }
    }

    fun getMilestonesList(sort: SortMode, hideComplete: Boolean): List<Pair<Int, DateTimeUnit>> {
        var list = when (sort) {
            SortMode.ASC -> milestones.toList().sortedWith { m1, m2 ->
                (m1.first * m1.second.toMillis()).compareTo(m2.first * m2.second.toMillis())
            }
            SortMode.DESC -> milestones.toList().sortedWith { m1, m2 ->
                (m2.first * m2.second.toMillis()).compareTo(m1.first * m1.second.toMillis())
            }
            SortMode.NONE -> milestones.toList()
        }
        if (hideComplete)
            list = list.filter {
                Clock.System.now().toEpochMilliseconds() < lastRelapse.toEpochMilliseconds() + it.first * it.second.toMillis()
            }
        return list
    }

    companion object {
        fun newInstance(name: String, millis: Long, priority: Priority) = Addiction(
            name,
            Instant.fromEpochMilliseconds(millis),
            false,
            0,
            LinkedHashMap(),
            priority,
            LinkedHashMap(),
            LocalTime(0, 0),
            LinkedHashMap(),
            LinkedHashSet()
        )
    }

}