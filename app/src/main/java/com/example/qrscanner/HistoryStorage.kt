package com.example.qrscanner

import android.content.Context
import org.json.JSONArray
import timber.log.Timber

class HistoryStorage(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAll(): List<String> {
        val json = prefs.getString(KEY_HISTORY, "[]") ?: "[]"
        val array = JSONArray(json)
        return List(array.length()) { array.getString(it) }
    }

    fun add(value: String) {
        val items = getAll().toMutableList()
        items.add(0, value)
        prefs.edit().putString(KEY_HISTORY, JSONArray(items).toString()).apply()
        Timber.d("Saved to history, total items: %d", items.size)
    }

    private companion object {
        const val PREFS_NAME = "qr_history"
        const val KEY_HISTORY = "items"
    }
}