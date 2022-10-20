package com.katiearose.sobriety.internal

import java.io.Serializable

class CircularBuffer<T>(size: Int) : Serializable {
    private val buffer: ArrayList<T?> = ArrayList(size)

    init {
        for (i in 0 until size) {
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