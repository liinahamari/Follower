package com.example.follower

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.follower.base.BaseActivity
import com.example.follower.screens.MainScreenViewPagerAdapter
import com.example.follower.screens.show_trace.ShowTraceActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(R.layout.activity_main) {
    override fun onCreateOptionsMenu(menu: Menu?): Boolean = true.also { menuInflater.inflate(R.menu.menu, menu) }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.showTrace -> startActivity(Intent(this, ShowTraceActivity::class.java))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pager.adapter = MainScreenViewPagerAdapter(supportFragmentManager)
        globalMenu.setupWithViewPager(pager)
    }
}