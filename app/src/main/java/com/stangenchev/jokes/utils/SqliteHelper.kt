package com.stangenchev.jokes.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.*

class SqliteHelper(var context: Context, var dbName: String, var dbResId: Int) {

    private var internalDir: File = context.filesDir
    private var numOfJokes = 0
    private var prevRandom = -1
    private lateinit var database: SQLiteDatabase

    init {
        val dbPath = File("$internalDir/db")
        val dbFile = File("$internalDir/db/${dbName}")
        if (!dbPath.exists()) {
            dbPath.mkdirs()
        }
        if (!dbFile.exists()) {
            val `in`: InputStream = context.resources.openRawResource(dbResId)
            val file = File("$internalDir/db/${dbName}")
            try {
                val out: OutputStream =
                    FileOutputStream(file)
                val buffer = ByteArray(1024)
                var len: Int
                while (`in`.read(buffer, 0, buffer.size).also { len = it } != -1) {
                    out.write(buffer, 0, len)
                }
                `in`.close()
                out.close()
            } catch (e: FileNotFoundException) {
                Log.e("SqliteHelper", e.message.toString())
            } catch (e: IOException) {
                Log.e("SqliteHelper", e.message.toString())
            }
        }
        database = SQLiteDatabase.openDatabase(
            "$internalDir/db/${dbName}",
            null,
            0
        )
        createFavTable()
        getNumberOfJokes()
    }

    fun closeDb() {
        database.close()
    }

    @SuppressLint("Recycle")
    private fun getNumberOfJokes() {
        val cursor = database.rawQuery("SELECT COUNT(id) FROM jokes;", null)
        cursor.moveToFirst()
        numOfJokes = cursor.getInt(0)
    }

    @SuppressLint("Recycle")
    private fun createFavTable() {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS \"favorites\" (\"id\" INTEGER, \"joke_id\" INTEGER, PRIMARY KEY(\"id\"));"
        )
    }

    fun addToFavorites() {
        val values = ContentValues().apply {
            put("joke_id", prevRandom)
        }
        database.insert("favorites", null, values)
    }

    @SuppressLint("Recycle")
    fun getJoke(number: Int? = null): String? {
        var jokeNumber: Int = this.prevRandom
        if (number == null) {
            for (i in 1..3) {
                jokeNumber = (1..this.numOfJokes).random()
                if (jokeNumber != this.prevRandom) {
                    break
                }
            }
            if (jokeNumber == this.prevRandom) {
                when {
                    jokeNumber < this.numOfJokes -> jokeNumber + 1
                    else -> jokeNumber - 1
                }
            }
        } else jokeNumber = number
        this.prevRandom = jokeNumber
        val cursor = database.rawQuery("SELECT joke FROM jokes WHERE id = $jokeNumber", null)
        cursor.moveToFirst()
        return cursor.getString(0)
    }
}