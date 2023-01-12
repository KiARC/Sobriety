package com.katiearose.sobriety

import com.katiearose.sobriety.internal.CircularBuffer
import com.katiearose.sobriety.utils.putLast
import com.katiearose.sobriety.utils.secondsFromNow
import com.katiearose.sobriety.utils.toJSONObject
import com.katiearose.sobriety.utils.toLinkedHashMap
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

class Addiction(
    val name: String,
    var lastRelapse: Instant,
    var isStopped: Boolean,
    var timeStopped: Long, //in milliseconds
    val history: LinkedHashMap<Long, Long>, //in milliseconds
    var priority: Priority,
    val dailyNotes: LinkedHashMap<LocalDate, String>,
    var timeSaving: LocalTime,
    val savings: LinkedHashMap<String, Pair<Double, String>>,
    val milestones: LinkedHashSet<Pair<Int, ChronoUnit>>,
    private val relapses: CircularBuffer<Long> = CircularBuffer(3) //Default is a new one, but you can provide your own (from a cache)
) : Serializable {
    var averageRelapseDuration = if (relapses.get(0) == null) -1 else calculateAverageRelapseDuration()
        private set

    enum class Priority {
        HIGH, MEDIUM, LOW
    }

    fun isFuture(): Boolean {
        return lastRelapse > Instant.now()
    }

    fun stopAbstaining() {
        isStopped = true
        timeStopped = System.currentTimeMillis()
        relapses.update(Instant.ofEpochMilli(timeStopped).epochSecond - lastRelapse.epochSecond)
        averageRelapseDuration = calculateAverageRelapseDuration()
        history.putLast(System.currentTimeMillis())
    }

    fun relapse() {
        if (!isStopped && !isFuture()) {
            relapses.update(lastRelapse.secondsFromNow())
            history.putLast(System.currentTimeMillis())
        }
        history[System.currentTimeMillis()] = 0
        isStopped = false
        averageRelapseDuration = calculateAverageRelapseDuration()
        lastRelapse = Instant.now()
    }

    private fun calculateAverageRelapseDuration(): Long {
        return relapses.getAll().filterNotNull().sumOf { it } / 3L
    }

    fun toJSON(): JSONObject {
        val addictionJSON: JSONObject = JSONObject();
        addictionJSON.put("name", name)
        addictionJSON.put("lastRelapse", lastRelapse.toString())
        addictionJSON.put("isStopped", isStopped)
        addictionJSON.put("timeStopped", timeStopped)
        addictionJSON.put("history", history.toJSONObject())
        addictionJSON.put("priority", priority)
        addictionJSON.put("dailyNotes", dailyNotes.toJSONObject())
        addictionJSON.put("timeSaving", timeSaving.toString())
        addictionJSON.put("savings", savings.toJSONObject(process_value = { pair ->
            JSONArray(pair.toList()) as Object
        }))
        addictionJSON.put("milestones", milestones.toMap().toJSONObject())
        addictionJSON.put("relapses", JSONArray(relapses.getAll()))
        return addictionJSON
    }

    fun toCacheable(): HashMap<Int, Any> {
        val map = HashMap<Int, Any>()
        map[0] = name
        map[1] = lastRelapse
        map[2] = isStopped
        map[3] = timeStopped
        map[4] = history
        map[5] = priority
        map[6] = dailyNotes
        map[7] = timeSaving
        map[8] = savings
        map[9] = milestones
        map[10] = relapses
        return map
    }

    companion object {
        fun fromJSON(json: JSONObject): Addiction {
            // Guaranteed keys
            val name = json.getString("name")
            val lastRelapse = Instant.parse(json.getString("lastRelapse"))
            val isStopped = json.getBoolean("isStopped")
            val timeStopped = json.getLong("timeStopped")
            val history = json.getJSONObject("history").toLinkedHashMap<Long, Long>()
            val priority = Priority.valueOf(json.getString("priority"))
            // Reconstruct CircularBuffer with arbitrary length
            val relapsesJSON = json.getJSONArray("relapses")
            val relapses = CircularBuffer<Long>(relapsesJSON.length())
            for (i in 0 until relapsesJSON.length()) {
                if (relapsesJSON.isNull(i))
                    continue
                relapses.update(relapsesJSON.getLong(i))
            }

            // New Keys
            val dailyNotes = if (json.has("dailyNotes")) {
                json.getJSONObject("dailyNotes").toLinkedHashMap<LocalDate, String>()
            } else LinkedHashMap()

            val timeSaving = if (json.has("timeSaving")) {
                LocalTime.parse(json.getString("timeSaving"))
            } else LocalTime.of(0, 0)

            val savings = if (json.has("savings")) {
                json.getJSONObject("savings").toLinkedHashMap<String, Pair<Double, String>>(
                    process_value = {obj ->
                        val array = obj as JSONArray
                        Pair<Double, String>(array.getDouble(0), array.getString(1))
                    }
                )
            } else LinkedHashMap()

            val milestones = if (json.has("milestones")) {
                val hashmap = json.getJSONObject("milestones").toLinkedHashMap<Int, ChronoUnit>(
                    process_value = {obj ->
                        // ChronoUnit enum values are uppercase of toString()
                        ChronoUnit.valueOf((obj as String).uppercase())
                    }
                )
                val hashset = LinkedHashSet<Pair<Int, ChronoUnit>>()
                for ((k, v) in hashmap) {
                    hashset.add(Pair(k, v))
                }
                hashset
            } else java.util.LinkedHashSet()

            return Addiction(
                name,
                lastRelapse,
                isStopped,
                timeStopped,
                history,
                priority,
                dailyNotes,
                timeSaving,
                savings,
                milestones,
                relapses
            )
        }

        fun fromCacheable(map: HashMap<Int, Any>): Addiction {
            //Migration strategies
            if (map.size == 7) { //is it version 6.1.1?
                return Addiction(
                    map[0] as String,
                    map[1] as Instant,
                    map[2] as Boolean,
                    map[3] as Long,
                    map[4] as LinkedHashMap<Long, Long>,
                    map[5] as Priority,
                    //default values...
                    LinkedHashMap(),
                    LocalTime.of(0, 0),
                    LinkedHashMap(),
                    LinkedHashSet(),
                    map[6] as CircularBuffer<Long>
                )
            } else return Addiction(
                map[0] as String,
                map[1] as Instant,
                map[2] as Boolean,
                map[3] as Long,
                map[4] as LinkedHashMap<Long, Long>,
                map[5] as Priority,
                map[6] as LinkedHashMap<LocalDate, String>,
                map[7] as LocalTime,
                map[8] as LinkedHashMap<String, Pair<Double, String>>,
                map[9] as LinkedHashSet<Pair<Int, ChronoUnit>>,
                map[10] as CircularBuffer<Long>
            )
        }
    }
}