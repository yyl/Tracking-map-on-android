package com.yyl.myrmex.tracking.database

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils

class LocContentProvider : ContentProvider() {

    private lateinit var database: LocDatabaseHelper

    override fun onCreate(): Boolean {
        database = LocDatabaseHelper(context)
        return false
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val queryBuilder = SQLiteQueryBuilder()
        checkColumns(projection)
        queryBuilder.tables = LocTable.TABLE_TODO

        val uriType = sURIMatcher.match(uri)
        when (uriType) {
            TODOS -> {
            }
            TODO_ID -> queryBuilder.appendWhere(LocTable.COLUMN_ID + "=" + uri.lastPathSegment)
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        val db = database.writableDatabase
        val cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        cursor.setNotificationUri(context!!.contentResolver, uri)

        return cursor
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val uriType = sURIMatcher.match(uri)
        val sqlDB = database.writableDatabase
        val id = when (uriType) {
            TODOS -> sqlDB.insert(LocTable.TABLE_TODO, null, values)
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return Uri.parse(BASE_PATH + "/" + id)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val uriType = sURIMatcher.match(uri)
        val sqlDB = database.writableDatabase
        val rowsDeleted = when (uriType) {
            TODOS -> sqlDB.delete(LocTable.TABLE_TODO, selection, selectionArgs)
            TODO_ID -> {
                val id = uri.lastPathSegment
                if (TextUtils.isEmpty(selection)) {
                    sqlDB.delete(LocTable.TABLE_TODO, LocTable.COLUMN_ID + "=" + id, null)
                } else {
                    sqlDB.delete(LocTable.TABLE_TODO, LocTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs)
                }
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return rowsDeleted
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        val uriType = sURIMatcher.match(uri)
        val sqlDB = database.writableDatabase
        val rowsUpdated = when (uriType) {
            TODOS -> sqlDB.update(LocTable.TABLE_TODO, values, selection, selectionArgs)
            TODO_ID -> {
                val id = uri.lastPathSegment
                if (TextUtils.isEmpty(selection)) {
                    sqlDB.update(LocTable.TABLE_TODO, values, LocTable.COLUMN_ID + "=" + id, null)
                } else {
                    sqlDB.update(LocTable.TABLE_TODO, values, LocTable.COLUMN_ID + "=" + id + " and " + selection, selectionArgs)
                }
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return rowsUpdated
    }

    private fun checkColumns(projection: Array<String>?) {
        val available = arrayOf(LocTable.COLUMN_TIME, LocTable.COLUMN_LONGITUDE, LocTable.COLUMN_LATITUDE, LocTable.COLUMN_ID, LocTable.COLUMN_EXTRA)
        if (projection != null) {
            val requestedColumns = HashSet(listOf(*projection))
            val availableColumns = HashSet(listOf(*available))
            if (!availableColumns.containsAll(requestedColumns)) {
                throw IllegalArgumentException("Unknown columns in projection")
            }
        }
    }

    companion object {
        private const val TODOS = 10
        private const val TODO_ID = 20
        private const val AUTHORITY = "com.yyl.myrmex.tracking.database"
        private const val BASE_PATH = "todos"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/$BASE_PATH")
        const val CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/todos"
        const val CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/todo"
        private val sURIMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            sURIMatcher.addURI(AUTHORITY, BASE_PATH, TODOS)
            sURIMatcher.addURI(AUTHORITY, "$BASE_PATH/#", TODO_ID)
        }
    }
}
