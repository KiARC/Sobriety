package com.katiearose.sobriety.shared

import android.content.Context
import android.net.Uri
import android.widget.Toast
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.*
import java.util.*

class CacheHandler(private val context: Context) {

    companion object {
        private val JsonSerializer = Json { ignoreUnknownKeys = true }
        private val listSerializer = ListSerializer(AddictionSerializer)
    }

    fun readCache(input: InputStream): List<Addiction> {
        BufferedInputStream(input).use { stream ->
            Scanner(stream).useDelimiter("\\A").use {
                return JsonSerializer.decodeFromString(it.next())
            }
        }
    }

    fun writeCache(addictions: List<Addiction>) {
        BufferedWriter(FileWriter(File(context.filesDir, "Sobriety.cache"))).use {
            it.write(JsonSerializer.encodeToString(listSerializer, addictions))
        }
    }

    fun exportData(addictions: List<Addiction>, output: Uri) {
        BufferedWriter(OutputStreamWriter(context.contentResolver.openOutputStream(output))).use {
            it.write(JsonSerializer.encodeToString(listSerializer, addictions))
        }
    }

    fun importData(input: Uri, resultConsumer: (List<Addiction>) -> Unit) {
        val stream = context.contentResolver.openInputStream(input)
        if (stream == null) {
            Toast.makeText(context, R.string.cant_import_data, Toast.LENGTH_SHORT).show()
            return
        }
        resultConsumer(readCache(stream))
    }
}