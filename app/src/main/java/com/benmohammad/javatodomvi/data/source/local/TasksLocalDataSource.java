package com.benmohammad.javatodomvi.data.source.local;

import android.content.Context;
import android.database.Cursor;

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
        return null;
    }

    @Override
    public Single<Task> getTask(@NonNull String taskId) {
        return null;
    }

    @Override
    public Completable saveTask(@NonNull Task task) {
        return null;
    }

    @Override
    public Completable completeTask(@NonNull Task task) {
        return null;
    }

    @Override
    public Completable completeTask(@NonNull String taskId) {
        return null;
    }

    @Override
    public Completable activateTask(@NonNull Task task) {
        return null;
    }

    @Override
    public Completable activateTask(@NonNull String taskId) {
        return null;
    }

    @Override
    public Completable clearCompletedTask() {
        return null;
    }

    @Override
    public void refreshTask() {

    }

    @Override
    public void deleteAllTasks() {

    }

    @Override
    public Completable deleteTask(@NonNull String taskId) {
        return null;
    }
}
