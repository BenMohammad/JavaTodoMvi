package com.benmohammad.javatodomvi.data.source;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.benmohammad.javatodomvi.data.Task;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static com.google.common.base.Preconditions.checkNotNull;


public class TasksRepository implements TasksDataSource {

    @Nullable
    private static TasksRepository INSTANCE = null;

    @NonNull
    private final TasksDataSource taskRemoteDataSource;

    @NonNull
    private final TasksDataSource taskLocalDataSource;

    @VisibleForTesting
    @Nullable
    Map<String, Task> cachedTasks;

    @VisibleForTesting
    boolean cacheIsDirty = false;

    private TasksRepository(@NonNull TasksDataSource taskRemoteDataSource,
                            @NonNull TasksDataSource taskLocalDataSource) {
        this.taskLocalDataSource = checkNotNull(taskLocalDataSource);
        this.taskRemoteDataSource = checkNotNull(taskRemoteDataSource);
    }

    public static TasksRepository getInstance(@NonNull TasksDataSource taskRemoteDataSource,
                                              @NonNull TasksDataSource taskLocalDataSource) {
        if(INSTANCE == null) {
            INSTANCE = new TasksRepository(taskRemoteDataSource, taskLocalDataSource);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Single<List<Task>> getTasks() {
        if(cachedTasks != null && !cacheIsDirty) {
            return Observable.fromIterable(cachedTasks.values()).toList();
        } else if(cachedTasks == null) {
            cachedTasks = new LinkedHashMap<>();
        }

        Single<List<Task>> remoteTasks = getAndSaveRemoteTasks();

        if(cacheIsDirty) {
            return remoteTasks;
        } else {

            Single<List<Task>> localTasks = getAndCacheLocalTasks();
            return Single.concat(localTasks, remoteTasks)
                    .filter(tasks -> !tasks.isEmpty())
                    .firstOrError();
        }
    }

    private Single<List<Task>> getAndSaveRemoteTasks() {
        return taskRemoteDataSource.getTasks()
                .flatMap(tasks -> Observable.fromIterable(tasks).doOnNext(task -> {
                    taskRemoteDataSource.saveTask(task);
                    cachedTasks.put(task.getId(), task);
                }).toList())
                .doOnSuccess(ignored -> cacheIsDirty = false);

    }

    private Single<List<Task>> getAndCacheLocalTasks() {
        return taskLocalDataSource.getTasks()
                .flatMap(tasks -> Observable.fromIterable(tasks)
                        .doOnNext(task -> cachedTasks.put(task.getId(), task))
                        .toList());
    }

    @Override
    public Single<Task> getTask(@NonNull String taskId) {
        checkNotNull(taskId);

        final Task cachedTask = getTaskWithId(taskId);

        if(cachedTask != null) {
            return Single.just(cachedTask);
        }

        if(cachedTasks == null) {
            cachedTasks = new LinkedHashMap<>();
        }

        Single<Task> localTask = getTaskWithIdFromLocalRepository(taskId);
        Single<Task> remoteTask = taskRemoteDataSource.getTask(taskId).doOnSuccess(task -> {
            taskLocalDataSource.saveTask(task);
            cachedTasks.put(task.getId(), task);
        });

        return Single.concat(localTask, remoteTask).firstOrError();
    }

    @Nullable
    private Task getTaskWithId(@NonNull String taskId) {
        checkNotNull(taskId);
        if(cachedTasks == null || cachedTasks.isEmpty()) {
            return null;
        } else {
            return cachedTasks.get(taskId);
        }
    }

    @NonNull
    Single<Task> getTaskWithIdFromLocalRepository(@NonNull final String taskId) {
        return taskLocalDataSource.getTask(taskId)
                .doOnSuccess(task -> cachedTasks.put(task.getId(), task));
    }



    @Override
    public Completable saveTask(@NonNull Task task) {
        checkNotNull(task);
        taskRemoteDataSource.saveTask(task);
        taskLocalDataSource.saveTask(task);

        if(cachedTasks == null) {
            cachedTasks = new LinkedHashMap<>();
        }

        cachedTasks.put(task.getId(), task);
        return Completable.complete();
    }

    @Override
    public Completable completeTask(@NonNull Task task) {
        checkNotNull(task);
        taskRemoteDataSource.completeTask(task);
        taskLocalDataSource.completeTask(task);

        Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);

        if(cachedTasks == null) {
            cachedTasks = new LinkedHashMap<>();
        }

        cachedTasks.put(task.getId(), completedTask);
        return Completable.complete();
    }

    @Override
    public Completable completeTask(@NonNull String taskId) {
        checkNotNull(taskId);
        Task taskWithId = getTaskWithId(taskId);

        if(taskWithId != null) {
            return completeTask(taskWithId);
        } else {
            return Completable.complete();
        }
    }

    @Override
    public Completable activateTask(@NonNull Task task) {
        checkNotNull(task);
        taskRemoteDataSource.activateTask(task);
        taskLocalDataSource.activateTask(task);

        Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
        if(cachedTasks == null) {
            cachedTasks = new LinkedHashMap<>();
        }

        cachedTasks.put(task.getId(), activeTask);
        return Completable.complete();
    }

    @Override
    public Completable activateTask(@NonNull String taskId) {
        checkNotNull(taskId);
        Task taskWithId = getTaskWithId(taskId);
        if(taskWithId != null) {
            return activateTask(taskWithId);
        } else {
            return Completable.complete();
        }
    }

    @Override
    public Completable clearCompletedTasks() {
        taskRemoteDataSource.clearCompletedTasks();
        taskLocalDataSource.clearCompletedTasks();

        if(cachedTasks == null) {
            cachedTasks = new LinkedHashMap<>();
        }

        Iterator<Map.Entry<String, Task>> it = cachedTasks.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Task> entry = it.next();
            if(entry.getValue().isCompleted()) {
                it.remove();
            }
        }
        return Completable.complete();
    }

    @Override
    public void refreshTask() {
        cacheIsDirty = true;
    }

    @Override
    public void deleteAllTasks() {
        taskRemoteDataSource.deleteAllTasks();
        taskLocalDataSource.deleteAllTasks();

        if(cachedTasks == null) {
            cachedTasks = new LinkedHashMap<>();
        }

        cachedTasks.clear();
    }

    @Override
    public Completable deleteTask(@NonNull String taskId) {
        checkNotNull(taskId);
        taskRemoteDataSource.deleteTask(taskId);
        taskLocalDataSource.deleteTask(taskId);

        cachedTasks.remove(taskId);
        return Completable.complete();
    }
}
