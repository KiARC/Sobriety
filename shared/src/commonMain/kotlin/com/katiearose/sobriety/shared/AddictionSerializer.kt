package com.katiearose.sobriety.shared

import kotlinx.datetime.*
import kotlinx.datetime.serializers.DateTimeUnitSerializer
import kotlinx.datetime.serializers.InstantComponentSerializer
import kotlinx.datetime.serializers.LocalDateIso8601Serializer
import kotlinx.datetime.serializers.LocalTimeIso8601Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*

object AddictionSerializer : KSerializer<Addiction> {
    private val historySerializer = MapSerializer(Long.serializer(), Long.serializer())
    private val dailyNotesSerializer = MapSerializer(LocalDateIso8601Serializer, String.serializer())
    private val savingsSerializer = MapSerializer(String.serializer(), PairSerializer(Double.serializer(), String.serializer()))
    private val milestonesSerializer = SetSerializer(PairSerializer(Int.serializer(), DateTimeUnitSerializer))

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): Addiction =
        decoder.decodeStructure(descriptor) {
            var name = ""
            var status = Addiction.Status.Ongoing
            val history = LinkedHashMap<Long, Long>()
            var priority = Addiction.Priority.MEDIUM
            var dailyNotes = LinkedHashMap<LocalDate, String>()
            var timeSaving = LocalTime(0, 0)
            var savings = LinkedHashMap<String, Pair<Double, String>>()
            var milestones = LinkedHashSet<Pair<Int, DateTimeUnit>>()
            while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    0 -> name = decodeStringElement(descriptor, i)
                    1 -> status = Addiction.Status.valueOf(decodeStringElement(descriptor, i))
                    // putAll rather than reassign for last_relapse legacy key
                    2 -> history.putAll(LinkedHashMap(decodeSerializableElement(descriptor, i, historySerializer)))
                    3 -> priority = Addiction.Priority.values()[decodeIntElement(descriptor, i)]
                    4 -> dailyNotes = LinkedHashMap(decodeSerializableElement(descriptor, i, dailyNotesSerializer))
                    5 -> timeSaving = decodeSerializableElement(descriptor, i, LocalTimeIso8601Serializer)
                    6 -> savings = LinkedHashMap(decodeSerializableElement(descriptor, i, savingsSerializer))
                    7 -> milestones = LinkedHashSet(decodeSerializableElement(descriptor, i, milestonesSerializer))

                    // Legacy keys
                    // last_relapse
                    8 -> {
                        val lastRelapse = decodeSerializableElement(descriptor, i, InstantComponentSerializer)
                        if (lastRelapse > Clock.System.now()) {
                            status = Addiction.Status.Future
                            history[lastRelapse.toEpochMilliseconds()] = 0
                        }
                    }
                    // is_stopped
                    9 -> if (decodeBooleanElement(descriptor, i)) status = Addiction.Status.Stopped

                    CompositeDecoder.DECODE_DONE -> break
                }
            }

            Addiction(name, status, history, priority, dailyNotes, timeSaving, savings, milestones)
        }

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("addiction") {
            element<String>("name")
            element<String>("status")
            element<LinkedHashMap<Long, Long>>("history")
            element<Int>("priority")
            element<LinkedHashMap<LocalDate, String>>("daily_notes")
            element<LocalTime>("time_saving")
            element<LinkedHashMap<String, Pair<Double, String>>>("savings")
            element<LinkedHashSet<Pair<Int, DateTimeUnit>>>("milestones")

            // Legacy keys
            element<Instant>("last_relapse")
            element<Boolean>("is_stopped")
        }

    override fun serialize(encoder: Encoder, value: Addiction) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.name)
            encodeStringElement(descriptor, 1, value.status.name)
            encodeSerializableElement(descriptor, 2, historySerializer, value.history)
            encodeIntElement(descriptor, 3, value.priority.ordinal)
            encodeSerializableElement(descriptor, 4, dailyNotesSerializer, value.dailyNotes)
            encodeSerializableElement(descriptor, 5, LocalTimeIso8601Serializer, value.timeSaving)
            encodeSerializableElement(descriptor, 6, savingsSerializer, value.savings)
            encodeSerializableElement(descriptor, 7, milestonesSerializer, value.milestones)
        }
    }
}