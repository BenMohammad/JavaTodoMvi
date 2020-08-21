package com.benmohammad.javatodomvi.data.source.local;

import android.provider.BaseColumns;

public final class TasksPersistenceContract {

    private TasksPersistenceContract(){}

    public static abstract class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "tasks";
        public static final String COLUMN_NAME_ENTRY_ID = "entryId";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_COMPLETED = "completed";
    }
}
