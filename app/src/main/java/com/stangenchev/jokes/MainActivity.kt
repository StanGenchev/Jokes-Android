package com.stangenchev.jokes

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.stangenchev.jokes.fragments.BottomNavigationDrawerFragment
import com.stangenchev.jokes.utils.SqliteHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private val joke = ""
    private lateinit var dbHelper: SqliteHelper
    private lateinit var clipboard: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(bar)
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        jokeView.text = getString(R.string.load_text)
        GlobalScope.launch(Dispatchers.Default) {
            dbHelper = SqliteHelper(
                applicationContext,
                "${resources.getResourceEntryName(R.raw.jokes)}.db",
                R.raw.jokes
            )
            runOnUiThread {
                jokeView.text = dbHelper.getJoke()
                fab.setOnClickListener(floatingActionClicked())
            }
        }
    }

    override fun onStop() {
        dbHelper.closeDb()
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.copy -> {
                val clip = ClipData.newPlainText(getString(R.string.clip_label), jokeView.text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.joke_copied),
                    Toast.LENGTH_SHORT
                ).show()
            }
            R.id.share -> shareIt(jokeView.text.toString())
            R.id.add_to_favorites -> {
                dbHelper.addToFavorites()
            }
            android.R.id.home -> {
                val bottomNavDrawerFragment = BottomNavigationDrawerFragment()
                bottomNavDrawerFragment.show(supportFragmentManager, bottomNavDrawerFragment.tag)
            }
        }
        return true
    }

    private fun floatingActionClicked(): View.OnClickListener? {
        return View.OnClickListener {
            jokeView.text = dbHelper.getJoke()
        }
    }

    private fun shareIt(shareBody: String) {
        val sharingIntent = Intent("android.intent.action.SEND")
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra("android.intent.extra.SUBJECT", getString(R.string.clip_label))
        sharingIntent.putExtra("android.intent.extra.TEXT", shareBody)
        startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_via)))
    }
}