package com.example.qrscanner

import android.os.Bundle
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import timber.log.Timber

class ScannerActivity : CaptureActivity() {

    private lateinit var scannerView: DecoratedBarcodeView

    override fun initializeContent(): DecoratedBarcodeView {
        scannerView = super.initializeContent()
        return scannerView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerView.cameraSettings.isContinuousFocusEnabled = true
        Timber.d("Scanner opened with continuous autofocus")
    }
}