package dev.liinahamari.feature.crash_screen.sample

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CrashingActivity : AppCompatActivity() {
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        throw IllegalArgumentException()
        @Suppress("UNREACHABLE_CODE")
        setContentView(R.layout.activity_main)
    }
}