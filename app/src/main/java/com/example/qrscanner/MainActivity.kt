package com.example.qrscanner

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

class MainActivity : AppCompatActivity() {

    private lateinit var resultText: TextView
    private lateinit var copyButton: Button
    private lateinit var searchButton: Button

    private var currentValue: String? = null

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        val contents = result.contents
        if (contents == null) {
            Timber.i("Scan cancelled by user")
            toast(R.string.scan_cancelled)
        } else {
            Timber.i("Scanned QR code, length=%d", contents.length)
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

        resultText = findViewById(R.id.resultText)
        copyButton = findViewById(R.id.copyButton)
        searchButton = findViewById(R.id.searchButton)

        findViewById<Button>(R.id.scanButton).setOnClickListener { startScan() }

        showValue(null)

        if (savedInstanceState == null) {
            startScan()
        }
        //end onCreate, after refreshHistory() і showValue(null).
    }

    private fun startScan() {
        Timber.d("Starting scanner")
        val options = ScanOptions()
            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setPrompt(getString(R.string.scan_prompt))
            .setBeepEnabled(false)
            .setOrientationLocked(false)
        scanLauncher.launch(options)
    }

    private fun showValue(value: String?) {
        currentValue = value
        resultText.text = value ?: getString(R.string.no_result)
        copyButton.isEnabled = value != null
        searchButton.isEnabled = value != null
    }

    private fun toast(resId: Int) = Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}