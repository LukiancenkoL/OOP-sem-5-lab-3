package com.example.qrscanner

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val actions = QrActions(this)
    private lateinit var history: HistoryStorage
    private lateinit var historyAdapter: ArrayAdapter<String>

    private lateinit var resultText: TextView
    private lateinit var copyButton: Button
    private lateinit var searchButton: Button

    private var currentValue: String? = null

    private val scanLauncher =
        registerForActivityResult(ScanContract()) { result ->
            val contents = result.contents
            if (contents == null) {
                Timber.i("Scan cancelled by user")
                Toast.makeText(this, R.string.scan_cancelled, Toast.LENGTH_SHORT).show()
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
        copyButton.setOnClickListener { currentValue?.let(actions::copy) }
        searchButton.setOnClickListener { currentValue?.let(actions::openOrSearch) }
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
            if (value != null && actions.canOpen(value)) R.string.open else R.string.search
        )
    }

    private fun refreshHistory() {
        historyAdapter.clear()
        historyAdapter.addAll(history.getAll())
    }
}