package com.katiearose.sobriety.shared

import kotlinx.serialization.Serializable

@Serializable
class CircularBuffer<T>(private val size: Int) {
    private val buffer: ArrayList<T?> = ArrayList(size)

    init {
        for (i in buffer.size until size) {
            buffer.add(i, null)
        }
    }

    fun update(obj: T) {
        for (i in buffer.indices.reversed()) {
            if (i > 0) buffer[i] = buffer[i - 1]
        }
        buffer[0] = obj
    }

    fun get(index: Int): T? {
        return buffer[index]
    }

    fun getAll(): List<T?> {
        return buffer.toList()
    }
}