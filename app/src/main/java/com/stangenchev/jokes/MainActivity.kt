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
import com.stangenchev.jokes.utils.ConvertUtils
import com.stangenchev.jokes.utils.CyrillicDecode
import com.stangenchev.jokes.utils.LZSSdecompress
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.InputStream
import java.util.*


class MainActivity : AppCompatActivity() {

    private val joke = ""
    var generator = Random()
    private var nJokes = 0
    var prevRandom = -1
    private val cyrillicDecode = CyrillicDecode()
    private val convertUtils = ConvertUtils()
    private lateinit var clipboard: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(bar)
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        jokeView.text = readJoke()
        fab.setOnClickListener(floatingActionClicked())
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
        }
        return true
    }

    private fun floatingActionClicked(): View.OnClickListener? {
        return View.OnClickListener {
            jokeView.text = readJoke()
        }
    }

    @Throws(IOException::class)
    fun skip(`is`: InputStream, nBytes: Long) {
        var skipped: Long = 0
        while (skipped < nBytes) {
            skipped += `is`.skip(nBytes - skipped)
        }
    }

    private fun readJoke(): String? {
        var n: Int
        val is2: InputStream = resources.openRawResource(R.raw.jokes)
        val head = ByteArray(16)
        is2.read(head)
        val bInt = ByteArray(4)
        is2.read(bInt)
        this.nJokes = convertUtils.byteArrayToInt(bInt) - 1
        var tries = 3
        while (true) {
            tries--
            if (tries != 0) {
                n = (this.generator.nextInt() and 65535) % this.nJokes
                if (n != this.prevRandom) {
                    break
                }
            } else {
                n = this.prevRandom + 1
                break
            }
        }
        this.prevRandom = n
        skip(is2, n.toLong() * 4)
        val b8 = ByteArray(8)
        is2.read(b8)
        System.arraycopy(b8, 0, bInt, 0, 4)
        val nOffset = convertUtils.byteArrayToInt(bInt)
        System.arraycopy(b8, 4, bInt, 0, 4)
        val size = convertUtils.byteArrayToInt(bInt) - nOffset
        skip(is2, (nOffset - 20 - 8).toLong() - n.toLong() * 4)
        val b = ByteArray(size)
        val b2 = LZSSdecompress().decompress(b, is2.read(b))
        val sb = StringBuffer()
        for (i in b2.indices) {
            if (b2[i].toInt() != 13) {
                sb.append(cyrillicDecode.cp1251Map[b2[i].toInt() and 255])
            }
        }
        val stringBuffer = sb.toString()
        return try {
            is2.close()
            stringBuffer
        } catch (e4: Exception) {
            val inputStream2 = is2
            stringBuffer
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