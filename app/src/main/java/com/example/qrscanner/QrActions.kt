package com.example.qrscanner

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import timber.log.Timber

class QrActions(private val activity: Activity) {

    fun canOpen(value: String): Boolean = toUri(value).scheme in OPENABLE_SCHEMES

    fun openOrSearch(value: String) {
        if (canOpen(value)) open(value) else search(value)
    }

    fun copy(value: String) {
        val clipboard = activity.getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText(activity.getString(R.string.clip_label), value))
        Timber.i("Value copied to clipboard")
        toast(R.string.copied)
    }

    private fun open(value: String) {
        val uri = toUri(value)
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
            Timber.i("Opened value with scheme=%s", uri.scheme)
        } catch (e: ActivityNotFoundException) {
            Timber.w(e, "No app can open scheme=%s", uri.scheme)
            toast(R.string.cannot_open)
        }
    }

    private fun search(value: String) {
        val uri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(value))
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, uri))
            Timber.i("Opened web search")
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "No app found to open web search")
            toast(R.string.no_browser)
        }
    }

    private fun toUri(value: String): Uri = Uri.parse(value.trim()).normalizeScheme()

    private fun toast(resId: Int) = Toast.makeText(activity, resId, Toast.LENGTH_SHORT).show()

    private companion object {
        val OPENABLE_SCHEMES = setOf("http", "https", "tel", "mailto", "geo", "sms", "smsto")
    }
}