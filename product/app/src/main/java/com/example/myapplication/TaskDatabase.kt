package com.example.myapplication

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.ContactsContract.CommonDataKinds.Note

//Database helper to hold tasks
class TaskDatabase(private val context: Context) :SQLiteOpenHelper(context, DB_NAME,null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "tasks_holder.db"
        private const val DB_VERSION = 5
        private const val TABLE_NAME = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_TAGS = "tags"
        private const val COLUMN_HOURS = "hours_taken"

        private const val TABLE_NAME_COMPLETED_TASKS = "completed_tasks"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable =
            "CREATE TABLE $TABLE_NAME($COLUMN_ID integer  primary key autoincrement," +
                    "$COLUMN_TITLE text,$COLUMN_TAGS text, $COLUMN_HOURS integer)"
        db?.execSQL(createTable)

        //DB for tasks ticked off
        val createCompletedTable =
            "CREATE TABLE $TABLE_NAME_COMPLETED_TASKS($COLUMN_ID integer  primary key autoincrement," +
                    "$COLUMN_TITLE text,$COLUMN_TAGS text, $COLUMN_HOURS integer)"
        db?.execSQL(createCompletedTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME"
        val dropCompletedTableQuery = "DROP TABLE IF EXISTS $TABLE_NAME_COMPLETED_TASKS"
        db?.execSQL(dropTableQuery)
        db?.execSQL(dropCompletedTableQuery)
        onCreate(db)
    }

    fun addTask(task: Task) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_TAGS,task.tag)
            put(COLUMN_HOURS,task.hoursTaken)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()

    }

    fun getTasks(): List<Task> {
        val tasksList = mutableListOf<Task>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
            val tag = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAGS))
            val hours = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HOURS))

            val task= Task(title,tag,hours)
            tasksList.add(task)


        }
        cursor.close()
        db.close()
        return tasksList
    }

    fun deleteTask(task:Task,username:String){
        val db = writableDatabase

        //add task to completed database
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_TAGS, task.tag)
            put(COLUMN_HOURS, task.hoursTaken)
        }
        db.insert(TABLE_NAME_COMPLETED_TASKS, null, values)

        val loginDB = LoginDetailsDatabase(context)
        loginDB.updateTotalHours(username,task.hoursTaken)

        //delete task
        val whereClause = "$COLUMN_TITLE = ? AND $COLUMN_TAGS = ? AND $COLUMN_HOURS = ?"
        val whereArgs = arrayOf(task.title, task.tag, task.hoursTaken.toString())
        db.delete(TABLE_NAME, whereClause, whereArgs)

        db.close()
    }

    fun getTotalHoursForTag(tag: String): Int {
        val db = readableDatabase
        val query = "SELECT SUM($COLUMN_HOURS) AS total_hours FROM $TABLE_NAME_COMPLETED_TASKS WHERE $COLUMN_TAGS = ?"
        val cursor = db.rawQuery(query, arrayOf(tag))
        var totalHours = 0

        if (cursor.moveToFirst()) {
            totalHours = cursor.getInt(cursor.getColumnIndexOrThrow("total_hours"))
        }

        cursor.close()
        db.close()
        return totalHours
    }




}