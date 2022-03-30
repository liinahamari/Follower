@file:Suppress("DEPRECATION")

package dev.liinahamari.feature.crash_screen.sample

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.core.Single

class CrashingActivity : AppCompatActivity(R.layout.activity_main) {
    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupOnClickListeners()
    }

    private fun setupOnClickListeners() {
        findViewById<Button>(R.id.asyncTaskCrashingBtn).setOnClickListener {
            class AsyncTaskImpl : AsyncTask<Void, Void, Void>() {
                override fun doInBackground(vararg params: Void?): Void {
                    throw IllegalArgumentException()
                }
            }

            AsyncTaskImpl().execute()
        }

        findViewById<Button>(R.id.mainThreadCrashingBtn).setOnClickListener {
            throw IllegalArgumentException()
        }

        findViewById<Button>(R.id.nonMainThreadCrashingBtn).setOnClickListener {
            Thread {
                throw IllegalArgumentException()
            }.start()
        }

        findViewById<Button>(R.id.rxJavaCrashingBtn).setOnClickListener {
            Single.error<Int>(IllegalArgumentException())
                .subscribe()
        }

        findViewById<Button>(R.id.rxJavaNotCrashingBtn).setOnClickListener {
            Single.error<Int>(IllegalArgumentException())
                .subscribe({}, { e -> e.printStackTrace() })
        }
    }
}