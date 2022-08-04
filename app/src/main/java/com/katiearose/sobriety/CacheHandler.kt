package com.katiearose.sobriety

import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

class CacheHandler(private val activity: Main) {
    fun readCache(input: InputStream): ArrayList<Addiction> {
        val result = ArrayList<Addiction>()
        val cache = input.readBytes()
        try {
            InflaterInputStream(cache.inputStream()).use { iis ->
                ObjectInputStream(iis).use {
                    result.addAll(it.readObject() as ArrayList<Addiction>)
                }
            }
        } catch (e: Exception) {
        }
        return result
    }

    fun writeCache() {
        activity.openFileOutput("Sobriety.cache", AppCompatActivity.MODE_PRIVATE).use { fos ->
            DeflaterOutputStream(fos, true).use { dos ->
                ObjectOutputStream(dos).use {
                    it.writeObject(Main.addictions)
                }
            }
        }
    }
}
