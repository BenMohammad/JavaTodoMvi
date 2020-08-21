package com.benmohammad.javatodomvi.data.source.remote;

import androidx.annotation.NonNull;

import com.benmohammad.javatodomvi.data.Task;
import com.benmohammad.javatodomvi.data.source.TasksDataSource;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

public class TasksRemoteDataSource implements TasksDataSource {

    private static TasksRemoteDataSource INSTANCE;

    private static final int SERVICE_LATENCY_IN_MILLIS = 5000;

    private final static Map<String, Task> TASKS_SERVICE_DATA;

    static {
        TASKS_SERVICE_DATA = new LinkedHashMap<>(2);
        addTask("Build Tower of Pisa", "Make sure it straight");
        addTask("Kill Bill Gates", "he is the evil genius");
    }

    public static TasksRemoteDataSource getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new TasksRemoteDataSource();
        }
        return INSTANCE;
    }

    private TasksRemoteDataSource(){}

    private static void addTask(String title, String description) {
        Task newTask = new Task(title, description);
        TASKS_SERVICE_DATA.put(newTask.getId(), newTask);
    }

    @Override
    public Single<List<Task>> getTasks() {
        return Observable.fromIterable(TASKS_SERVICE_DATA.values())
                .delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS)
                .toList();
    }

    @Override
    public Single<Task> getTask(@NonNull String taskId) {
        return Single.just(TASKS_SERVICE_DATA.get(taskId))
                .delay(SERVICE_LATENCY_IN_MILLIS, TimeUnit.MILLISECONDS);
    }

    @Override
    public Completable saveTask(@NonNull Task task) {
        TASKS_SERVICE_DATA.put(task.getId(), task);
        return Completable.complete();
    }

    @Override
    public Completable completeTask(@NonNull Task task) {
        Task completedTask = new Task(task.getTitle(), task.getDescription(), task.getId(), true);
        TASKS_SERVICE_DATA.put(task.getId(), completedTask);
        return null;
    }

    @Override
    public Completable completeTask(@NonNull String taskId) {
        return Completable.complete();
    }

    @Override
    public Completable activateTask(@NonNull Task task) {
        Task activeTask = new Task(task.getTitle(), task.getDescription(), task.getId());
        TASKS_SERVICE_DATA.put(task.getId(), activeTask);
        return Completable.complete();
    }

    @Override
    public Completable activateTask(@NonNull String taskId) {
        return Completable.complete();
    }

    @Override
    public Completable clearCompletedTasks() {
        Iterator<Map.Entry<String, Task>> it = TASKS_SERVICE_DATA.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, Task> entry = it.next();
            if(entry.getValue().isCompleted()) {
                it.remove();
            }
        }
        return null;
    }

    @Override
    public void refreshTask() {

    }

    @Override
    public void deleteAllTasks() {
        TASKS_SERVICE_DATA.clear();
    }

    @Override
    public Completable deleteTask(@NonNull String taskId) {
        TASKS_SERVICE_DATA.remove(taskId);
        return Completable.complete();
    }
}
