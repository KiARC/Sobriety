package com.katiearose.sobriety

import java.io.Serializable

internal class CircularBuffer<T>(size: Int) : Serializable {
    private val buffer: ArrayList<T?> = ArrayList(size)

    init {
        for (i in 0 until size) {
            buffer.add(i, null)
        }
    }

    fun update(obj: T) {
        buffer[2] = buffer[1]
        buffer[1] = buffer[0]
        buffer[0] = obj
    }

    fun get(index: Int): T? {
        return buffer[index]
    }

    fun getAll(): List<T?> {
        return buffer.toList()
    }
}