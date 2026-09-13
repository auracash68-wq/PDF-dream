package com.example

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class SweetPdfApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            PDFBoxResourceLoader.init(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
