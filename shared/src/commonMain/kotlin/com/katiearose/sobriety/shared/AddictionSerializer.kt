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
import kotlinx.serialization.serializer

object AddictionSerializer : KSerializer<Addiction> {
    private val historySerializer = MapSerializer(Long.serializer(), Long.serializer())
    private val dailyNotesSerializer = MapSerializer(LocalDateIso8601Serializer, String.serializer())
    private val savingsSerializer = MapSerializer(String.serializer(), PairSerializer(Double.serializer(), String.serializer()))
    private val milestonesSerializer = SetSerializer(PairSerializer(Int.serializer(), DateTimeUnitSerializer))
    private val bufferSerializer: KSerializer<CircularBuffer<Long>> = serializer()

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): Addiction =
        decoder.decodeStructure(descriptor) {
            var name = ""
            var lastRelapse = Clock.System.now()
            var isStopped = false
            var timeStopped = 0L
            var history = LinkedHashMap<Long, Long>()
            var priority = 0
            var dailyNotes = LinkedHashMap<LocalDate, String>()
            var timeSaving = LocalTime(0, 0)
            var savings = LinkedHashMap<String, Pair<Double, String>>()
            var milestones = LinkedHashSet<Pair<Int, DateTimeUnit>>()
            var relapses = CircularBuffer<Long>(3)
            if (decodeSequentially()) {
                name = decodeStringElement(descriptor, 0)
                lastRelapse = decodeSerializableElement(descriptor, 1, InstantComponentSerializer)
                isStopped = decodeBooleanElement(descriptor, 2)
                timeStopped = decodeLongElement(descriptor, 3)
                history = LinkedHashMap(decodeSerializableElement(descriptor, 4, historySerializer))
                priority = decodeIntElement(descriptor, 5)
                dailyNotes = LinkedHashMap(decodeSerializableElement(descriptor, 6, dailyNotesSerializer))
                timeSaving = decodeSerializableElement(descriptor, 7, LocalTimeIso8601Serializer)
                savings = LinkedHashMap(decodeSerializableElement(descriptor, 8, savingsSerializer))
                milestones = LinkedHashSet(decodeSerializableElement(descriptor, 9, milestonesSerializer))
                relapses = decodeSerializableElement(descriptor, 10, bufferSerializer)
            } else while (true) {
                when (decodeElementIndex(descriptor)) {
                    0 -> name = decodeStringElement(descriptor, 0)
                    1 -> lastRelapse = decodeSerializableElement(descriptor, 1, InstantComponentSerializer)
                    2 -> isStopped = decodeBooleanElement(descriptor, 2)
                    3 -> timeStopped = decodeLongElement(descriptor, 3)
                    4 -> history = LinkedHashMap(decodeSerializableElement(descriptor, 4, historySerializer))
                    5 -> priority = decodeIntElement(descriptor, 5)
                    6 -> dailyNotes = LinkedHashMap(decodeSerializableElement(descriptor, 6, dailyNotesSerializer))
                    7 -> timeSaving = decodeSerializableElement(descriptor, 7, LocalTimeIso8601Serializer)
                    8 -> savings = LinkedHashMap(decodeSerializableElement(descriptor, 8, savingsSerializer))
                    9 -> milestones = LinkedHashSet(decodeSerializableElement(descriptor, 9, milestonesSerializer))
                    10 -> relapses = decodeSerializableElement(descriptor, 10, bufferSerializer)
                    CompositeDecoder.DECODE_DONE -> break
                }
            }
            Addiction(name, lastRelapse, isStopped, timeStopped, history, Addiction.Priority.values()[priority],
            dailyNotes, timeSaving, savings, milestones, relapses)
        }

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("addiction") {
            element<String>("name")
            element<Instant>("last_relapse")
            element<Boolean>("is_stopped")
            element<Long>("time_stopped")
            element<LinkedHashMap<Long, Long>>("history")
            element<Int>("priority")
            element<LinkedHashMap<LocalDate, String>>("daily_notes")
            element<LocalTime>("time_saving")
            element<LinkedHashMap<String, Pair<Double, String>>>("savings")
            element<LinkedHashSet<Pair<Int, DateTimeUnit>>>("milestones")
            element<CircularBuffer<Long>>("relapses")
        }

    override fun serialize(encoder: Encoder, value: Addiction) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.name)
            encodeSerializableElement(descriptor, 1, InstantComponentSerializer, value.lastRelapse)
            encodeBooleanElement(descriptor, 2, value.isStopped)
            encodeLongElement(descriptor, 3, value.timeStopped)
            encodeSerializableElement(descriptor, 4, historySerializer, value.history)
            encodeIntElement(descriptor, 5, value.priority.ordinal)
            encodeSerializableElement(descriptor, 6, dailyNotesSerializer, value.dailyNotes)
            encodeSerializableElement(descriptor, 7, LocalTimeIso8601Serializer, value.timeSaving)
            encodeSerializableElement(descriptor, 8, savingsSerializer, value.savings)
            encodeSerializableElement(descriptor, 9, milestonesSerializer, value.milestones)
            encodeSerializableElement(descriptor, 10, bufferSerializer, value.relapses)
        }
    }
}