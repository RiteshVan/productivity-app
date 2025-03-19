package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LoginDetailsDatabase(
    context: Context,
) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        private const val DB_NAME = "login_details_holder.db"
        private const val DB_VERSION = 3
        private const val TABLE_NAME = "login_details"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery =
            "CREATE TABLE $TABLE_NAME ($COLUMN_ID integer primary key autoincrement, " +
                "$COLUMN_USERNAME text, " +
                "$COLUMN_PASSWORD text, " +
                "total_hours INTEGER DEFAULT 0)"

        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int,
    ) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        db?.execSQL(dropTableQuery)
        onCreate(db)
    }

    fun addUser(
        username: String,
        password: String,
    ): Boolean {
        val db = writableDatabase
        val details =
            ContentValues().apply {
                put(COLUMN_USERNAME, username)
                put(COLUMN_PASSWORD, password)
            }

        val operation = db.insert(TABLE_NAME, null, details)

        return operation != -1L
    }

    fun verifyUser(
        username: String,
        password: String,
    ): Boolean {
        val db = readableDatabase
        val details = "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD=?"
        val detailsArray = arrayOf(username, password)

        val cursor = db.query(TABLE_NAME, null, details, detailsArray, null, null, null)

        val allowLogin = (cursor.count > 0)
        cursor.close()

        return allowLogin
    }

    fun updateTotalHours(
        username: String,
        hours: Int,
    ) {
        val db = writableDatabase
        val query = "UPDATE $TABLE_NAME SET total_hours = total_hours + ? WHERE $COLUMN_USERNAME = ?"

        db.execSQL(query, arrayOf(hours, username))

        db.close()
    }

    fun getLeaderboard(): List<Pair<String, Int>> {
        val leaderboard = mutableListOf<Pair<String, Int>>()
        val db = readableDatabase
        val query = "SELECT $COLUMN_USERNAME, total_hours FROM $TABLE_NAME ORDER BY total_hours DESC"

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
            val hours = cursor.getInt(cursor.getColumnIndexOrThrow("total_hours"))
            leaderboard.add(Pair(username, hours))
        }

        cursor.close()
        db.close()
        return leaderboard
    }
}
