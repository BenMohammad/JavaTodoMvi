package com.benmohammad.javatodomvi.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.benmohammad.javatodomvi.data.Task;
import com.benmohammad.javatodomvi.data.source.TasksDataSource;
import com.benmohammad.javatodomvi.util.schedulers.BaseSchedulerProvider;
import com.squareup.sqlbrite2.BriteDatabase;
import com.squareup.sqlbrite2.SqlBrite;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;
import static com.benmohammad.javatodomvi.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_COMPLETED;
import static com.benmohammad.javatodomvi.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_DESCRIPTION;
import static com.benmohammad.javatodomvi.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_ENTRY_ID;
import static com.benmohammad.javatodomvi.data.source.local.TasksPersistenceContract.TaskEntry.COLUMN_NAME_TITLE;
import static com.benmohammad.javatodomvi.data.source.local.TasksPersistenceContract.TaskEntry.TABLE_NAME;


public class TasksLocalDataSource implements TasksDataSource {

    @Nullable static TasksLocalDataSource INSTANCE;
    @NonNull
    private final BriteDatabase databaseHelper;

    @NonNull
    private Function<Cursor, Task> taskMapperFunction;


    private TasksLocalDataSource(@NonNull Context context,
                                 @NonNull BaseSchedulerProvider schedulerProvider) {
        checkNotNull(context, "Context cannot be null");
        checkNotNull(schedulerProvider, "Scheduler cannot be null");
        TaskDbHelper dbHelper = new TaskDbHelper(context);
        SqlBrite sqlBrite = new SqlBrite.Builder().build();
        databaseHelper = sqlBrite.wrapDatabaseHelper(dbHelper, schedulerProvider.io());
        taskMapperFunction = this::getTask;
    }

    @NonNull
    private Task getTask(@NonNull Cursor c) {
        String itemId = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_ENTRY_ID));
        String title = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_TITLE));
        String description = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_DESCRIPTION));
        boolean isCompleted = c.getInt(c.getColumnIndexOrThrow(COLUMN_NAME_COMPLETED)) == 1;
        return new Task(title, description, itemId, isCompleted);
    }

    public TasksLocalDataSource getInstance(@NonNull Context context,
                                            @NonNull BaseSchedulerProvider schedulerProvider){
        if(INSTANCE == null) {
            INSTANCE = new TasksLocalDataSource(context, schedulerProvider);
        }
        return INSTANCE;
    }

    @Override
    public Single<List<Task>> getTasks() {
        String[] projection = {
                COLUMN_NAME_ENTRY_ID, COLUMN_NAME_TITLE, COLUMN_NAME_DESCRIPTION,
                COLUMN_NAME_COMPLETED
        };

        String sql = String.format("SELECT %s FROM %s", TextUtils.join(",", projection), TasksPersistenceContract.TaskEntry.TABLE_NAME);
        return databaseHelper.createQuery(TasksPersistenceContract.TaskEntry.TABLE_NAME, sql)
                .mapToList(taskMapperFunction)
                .firstOrError();
    }

    @Override
    public Single<Task> getTask(@NonNull String taskId) {
        String[] projection = {
                COLUMN_NAME_ENTRY_ID, COLUMN_NAME_TITLE, COLUMN_NAME_DESCRIPTION,
                COLUMN_NAME_COMPLETED
        };

        String sql = String.format("SELECT %s FROM %s WHERE %s LIKE ?", TextUtils.join(",", projection), TasksPersistenceContract.TaskEntry.TABLE_NAME, COLUMN_NAME_ENTRY_ID);
        return databaseHelper.createQuery(TABLE_NAME, sql, taskId)
                .mapToOne(taskMapperFunction)
                .firstOrError();
    }

    @Override
    public Completable saveTask(@NonNull Task task) {
        checkNotNull(task);
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_ENTRY_ID, task.getId());
        values.put(COLUMN_NAME_TITLE, task.getTitle());
        values.put(COLUMN_NAME_DESCRIPTION, task.getDescription());
        values.put(COLUMN_NAME_COMPLETED, task.isCompleted());
        databaseHelper.insert(TABLE_NAME, values, SQLiteDatabase.CONFLICT_REPLACE);
        return Completable.complete();
    }

    @Override
    public Completable completeTask(@NonNull Task task) {
        completeTask(task);
        return null;
    }

    @Override
    public Completable completeTask(@NonNull String taskId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_COMPLETED, true);
        String selection = COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs = {taskId};
        databaseHelper.update(TABLE_NAME, values, selection, selectionArgs);
        return Completable.complete();
    }

    @Override
    public Completable activateTask(@NonNull Task task) {
        activateTask(task.getId());
        return Completable.complete();
    }

    @Override
    public Completable activateTask(@NonNull String taskId) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_COMPLETED, false);
        String selection = COLUMN_NAME_ENTRY_ID + " LIKE ?";
        String[] selectionArgs =  {taskId};
        databaseHelper.update(TABLE_NAME, values, selection, selectionArgs);
        return Completable.complete();
    }

    @Override
    public Completable clearCompletedTasks() {
        String selection = COLUMN_NAME_COMPLETED + " LIKE ?";
        String[] selectionArgs = {"1"};
        databaseHelper.delete(TABLE_NAME, selection, selectionArgs);
        return null;
    }

    @Override
    public void refreshTask() {

    }

    @Override
    public void deleteAllTasks() {
        databaseHelper.delete(TABLE_NAME, null);
    }

    @Override
    public Completable deleteTask(@NonNull String taskId) {
        String selection = COLUMN_NAME_ENTRY_ID  + " LIKE ?";
        String[] selectionArgs = {taskId};
        databaseHelper.delete(TABLE_NAME, selection, selectionArgs);
        return Completable.complete();
    }
}
