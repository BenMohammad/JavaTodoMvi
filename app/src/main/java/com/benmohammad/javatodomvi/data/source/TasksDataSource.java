package com.benmohammad.javatodomvi.data.source;

import androidx.annotation.NonNull;

import com.benmohammad.javatodomvi.data.Task;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface TasksDataSource {

    default Single<List<Task>> getTasks(boolean forceUpdate) {
        if(forceUpdate) refreshTask();
        return getTasks();
    }

    Single<List<Task>> getTasks();
    Single<Task> getTask(@NonNull String taskId);
    Completable saveTask(@NonNull Task task);
    Completable completeTask(@NonNull Task task);
    Completable completeTask(@NonNull String taskId);
    Completable activateTask(@NonNull Task task);
    Completable activateTask(@NonNull String taskId);
    Completable clearCompletedTask();
    void refreshTask();
    void deleteAllTasks();
    Completable deleteTask(@NonNull String taskId);
}
