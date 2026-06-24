package com.smpn1damai.exambro // Sesuaikan jika package Anda berbeda

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class ExambroApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}