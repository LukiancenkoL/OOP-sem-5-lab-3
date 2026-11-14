package com.example.qrscanner

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import timber.log.Timber
import android.widget.ArrayAdapter
import android.widget.ListView

class MainActivity : AppCompatActivity() {

    private lateinit var resultText: TextView
    private lateinit var copyButton: Button
    private lateinit var searchButton: Button
    private lateinit var history: HistoryStorage
    private lateinit var historyAdapter: ArrayAdapter<String>

    private var currentValue: String? = null

    private val scanLauncher =
        registerForActivityResult(ScanContract()) { result ->
            val contents = result.contents
            if (contents == null) {
                Timber.i("Scan cancelled by user")
                toast(R.string.scan_cancelled)
            } else {
                Timber.i("Scanned QR code, length=%d", contents.length)
                history.add(contents)
                refreshHistory()
                showValue(contents)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Timber.d("MainActivity created")

        history = HistoryStorage(this)

        resultText = findViewById(R.id.resultText)
        copyButton = findViewById(R.id.copyButton)
        searchButton = findViewById(R.id.searchButton)

        findViewById<Button>(R.id.scanButton).setOnClickListener { startScan() }
        copyButton.setOnClickListener { currentValue?.let(::copyToClipboard) }
        searchButton.setOnClickListener {
            currentValue?.let { if (canOpen(it)) openValue(it) else searchInWeb(it) }
        }

        findViewById<Button>(R.id.clearButton).setOnClickListener {
            history.clear()
            refreshHistory()
        }

        historyAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        val historyList = findViewById<ListView>(R.id.historyList)
        historyList.adapter = historyAdapter
        historyList.setOnItemClickListener { _, _, position, _ ->
            historyAdapter.getItem(position)?.let(::showValue)
        }

        refreshHistory()

        showValue(null)

        if (savedInstanceState == null) {
            startScan()
        }
    }

    private fun startScan() {
        Timber.d("Starting scanner")
        val options =
            ScanOptions()
                .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt(getString(R.string.scan_prompt))
                .setBeepEnabled(false)
                .setCaptureActivity(ScannerActivity::class.java)
        scanLauncher.launch(options)
    }

    private fun showValue(value: String?) {
        currentValue = value
        resultText.text = value ?: getString(R.string.no_result)
        copyButton.isEnabled = value != null
        searchButton.isEnabled = value != null
        searchButton.setText(
            if (value != null && canOpen(value)) R.string.open else R.string.search
        )
    }

    private fun copyToClipboard(value: String) {
        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText(getString(R.string.clip_label), value))
        Timber.i("Value copied to clipboard")
        toast(R.string.copied)
    }

    private fun searchInWeb(value: String) {
        val uri = Uri.parse("https://www.google.com/search?q=" + Uri.encode(value))
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
            Timber.i("Opened web search")
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "No app found to open web search")
            toast(R.string.no_browser)
        }
    }

    private fun canOpen(value: String): Boolean = toUri(value).scheme in OPENABLE_SCHEMES

    private fun openValue(value: String) {
        val uri = toUri(value)
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
            Timber.i("Opened value with scheme=%s", uri.scheme)
        } catch (e: ActivityNotFoundException) {
            Timber.w(e, "No app can open scheme=%s", uri.scheme)
            toast(R.string.cannot_open)
        }
    }

    private fun refreshHistory() {
        historyAdapter.clear()
        historyAdapter.addAll(history.getAll())
    }

    private fun toast(resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()

    private fun toUri(value: String): Uri = Uri.parse(value.trim()).normalizeScheme()

    private companion object {
        val OPENABLE_SCHEMES = setOf("http", "https", "tel", "mailto", "geo", "sms", "smsto")
    }
}
