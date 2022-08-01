package com.katiearose.sobriety

import androidx.appcompat.app.AppCompatActivity
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.time.Instant
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

class CacheHandler(private val activity: Main) {
    fun readCache(input: InputStream): ArrayList<Addiction> {
        val cache = input.readBytes()
        try {
            InflaterInputStream(cache.inputStream()).use { iis ->
                ObjectInputStream(iis).use {
                    return it.readObject() as ArrayList<Addiction>
                }
            }
        } catch (e: ClassCastException) {
            return readLegacyCache(cache.inputStream())
        }
    }

    private fun readLegacyCache(input: InputStream): ArrayList<Addiction> {
        val result = ArrayList<Addiction>()
        try {
            val a = HashMap<String, Pair<Instant, CircularBuffer<Long>>>()
            InflaterInputStream(input).use { iis ->
                ObjectInputStream(iis).use {
                    for (i in it.readObject() as HashMap<String, Pair<Instant, CircularBuffer<Long>>>) {
                        a[i.key] = i.value
                    }
                }
            }
            for (ad in a) {
                val addiction = Addiction(ad.key, ad.value.first)
                result.add(addiction)
            }
        } catch (e: Exception) {
            //Do nothing, i.e. if the cache is older than the previous version just ignore it, supporting every previous version would take more code than it's worth.
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
