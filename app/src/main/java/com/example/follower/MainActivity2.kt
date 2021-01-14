package com.example.follower

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.commit
import com.example.follower.screens.map.MapFragment
import kotlinx.android.synthetic.main.activity_main2.*
import org.osmdroid.config.Configuration

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        // set the path for osmdroid's files (e.g. tile cache)
        Configuration.getInstance().osmdroidBasePath = this.getExternalFilesDir(null)

        supportFragmentManager.commit {
            replace(R.id.container, MapFragment())
        }
    }
}