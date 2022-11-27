package com.katiearose.sobriety.shared

import android.content.Context
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.*
import java.util.*

class CacheHandler(private val context: Context) {

    companion object {
        private val listSerializer = ListSerializer(AddictionSerializer)
    }

    fun readCache(input: InputStream): List<Addiction> {
        BufferedInputStream(input).use { stream ->
            Scanner(stream).useDelimiter("\\A").use {
                return Json.decodeFromString(it.next())
            }
        }
    }

    fun writeCache(addictions: List<Addiction>) {
        BufferedWriter(FileWriter(File(context.filesDir, "Sobriety.cache"))).use {
            it.write(Json.encodeToString(listSerializer, addictions))
        }
    }
}