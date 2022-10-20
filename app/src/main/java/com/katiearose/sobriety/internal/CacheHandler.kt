package com.katiearose.sobriety.internal

import androidx.appcompat.app.AppCompatActivity
import com.katiearose.sobriety.Addiction
import com.katiearose.sobriety.activities.Main
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
                ObjectInputStream(iis).use { ois ->
                    val cacheData = ois.readObject() as ArrayList<HashMap<Int, Any>>
                    cacheData.forEach {
                        result.add(Addiction.fromCacheable(it))
                    }
                }
            }
        } catch (e: Exception) {
            InflaterInputStream(cache.inputStream()).use { iis ->
                ObjectInputStream(iis).use {
                    result.addAll(it.readObject() as ArrayList<Addiction>)
                }
            }
        }
        return result
    }

    fun writeCache() {
        val output = ArrayList<HashMap<Int, Any>>()
        Main.addictions.forEach {
            output.add(it.toCacheable())
        }
        activity.openFileOutput("Sobriety.cache", AppCompatActivity.MODE_PRIVATE).use { fos ->
            DeflaterOutputStream(fos, true).use { dos ->
                ObjectOutputStream(dos).use {
                    it.writeObject(output)
                }
            }
        }
    }
}
