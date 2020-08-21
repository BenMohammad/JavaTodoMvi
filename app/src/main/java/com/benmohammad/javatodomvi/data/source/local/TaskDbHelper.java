package com.benmohammad.javatodomvi.data.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.benmohammad.javatodomvi.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED;
import static com.benmohammad.javatodomvi.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION;
import static com.benmohammad.javatodomvi.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID;
import static com.benmohammad.javatodomvi.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE;
import static com.benmohammad.javatodomvi.data.source.local.TasksPersistenceContract.TaskEntry.TABLE_NAME;

public class TaskDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Tasks.db";
    public static final String TEXT_TYPE = " TEXT";
    public static final String BOOLEAN_TYPE = " INTEGER";
    public static final String COMMA_SEP = ",";
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ENTRY_ID + TEXT_TYPE + " PRIMARY KEY, " +
                    COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_DESCRIPTION + TEXT_TYPE + COMMA_SEP +
                    COLUMN_NAME_COMPLETED + BOOLEAN_TYPE + " )";

    public TaskDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
