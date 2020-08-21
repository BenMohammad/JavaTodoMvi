package com.benmohammad.javatodomvi.tasks;

import androidx.annotation.NonNull;

import com.benmohammad.javatodomvi.data.source.TasksRepository;
import com.benmohammad.javatodomvi.util.schedulers.BaseSchedulerProvider;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

import static com.benmohammad.javatodomvi.util.ObservableUtils.pairWithDelay;
import static com.google.common.base.Preconditions.checkNotNull;

public class TasksActionProcessorHolder {

    @NonNull
    private TasksRepository tasksRepository;

    @NonNull
    private BaseSchedulerProvider schedulerProvider;

    public TasksActionProcessorHolder(@NonNull TasksRepository tasksRepository,
                                      @NonNull BaseSchedulerProvider schedulerProvider) {
        this.tasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null");
        this.schedulerProvider = checkNotNull(schedulerProvider, "schedulerProvider cannot be null");
    }

    private ObservableTransformer<TasksAction.LoadTasks, TasksResult.LoadTasks> loadTaskProcessor =
            actions -> actions.flatMap(action ->
                    tasksRepository.getTasks(action.forceUpdate())
                .toObservable()
                .map(tasks -> TasksResult.LoadTasks.success(tasks, action.filterType()))
                .onErrorReturn(TasksResult.LoadTasks::failure)
                .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(TasksResult.LoadTasks.inFlight()));

    private ObservableTransformer<TasksAction.ActivateTaskAction, TasksResult.ActivateTaskResult> activateTaskProcessor =
            actions -> actions.flatMap(action ->
                    tasksRepository.activateTask(action.task())
                    .andThen(tasksRepository.getTasks())
                    .toObservable()
                    .flatMap(tasks -> pairWithDelay(
                            TasksResult.ActivateTaskResult.success(tasks),
                            TasksResult.ActivateTaskResult.hideUiNotification()
                    )))
            .onErrorReturn(TasksResult.ActivateTaskResult::failure)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .startWith(TasksResult.ActivateTaskResult.inFlight());

    private ObservableTransformer<TasksAction.CompleteTaskAction, TasksResult.CompleteTaskResult> completeTaskProcessor =
            actions -> actions.flatMap(action ->
                    tasksRepository.completeTask(action.task())
                    .andThen(tasksRepository.getTasks())
                    .toObservable()
                    .flatMap(tasks -> pairWithDelay(
                            TasksResult.CompleteTaskResult.success(tasks),
                            TasksResult.CompleteTaskResult.hideUiNotification()
                    )))
            .onErrorReturn(TasksResult.CompleteTaskResult::failure)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .startWith(TasksResult.CompleteTaskResult.inFlight());

    private ObservableTransformer<TasksAction.ClearCompletedTasksAction, TasksResult.ClearCompletedTaskResult> clearCompletedTaskProcessor =
            actions -> actions.flatMap(
                    action -> tasksRepository.clearCompletedTasks()
                    .andThen(tasksRepository.getTasks())
                    .toObservable()
                    .flatMap(tasks ->
                            pairWithDelay(
                                    TasksResult.ClearCompletedTaskResult.success(tasks),
                                    TasksResult.ClearCompletedTaskResult.hideUiNotification()
                            ))
                    .onErrorReturn(TasksResult.ClearCompletedTaskResult::failure)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .startWith(TasksResult.ClearCompletedTaskResult.inFlight()));

    ObservableTransformer<TasksAction, TasksResult> actionProcessor =
            actions -> actions.publish(shared -> Observable.merge(
                    shared.ofType(TasksAction.LoadTasks.class).compose(loadTaskProcessor),
                    shared.ofType(TasksAction.ActivateTaskAction.class).compose(activateTaskProcessor),
                    shared.ofType(TasksAction.CompleteTaskAction.class).compose(completeTaskProcessor),
                    shared.ofType(TasksAction.ClearCompletedTasksAction.class).compose(clearCompletedTaskProcessor))
                .mergeWith(
                        shared.filter(v -> !(v instanceof TasksAction.LoadTasks)
                                && !(v instanceof TasksAction.ActivateTaskAction)
                                && !(v instanceof TasksAction.CompleteTaskAction)
                                && !(v instanceof TasksAction.ClearCompletedTasksAction))
                        .flatMap(w -> Observable.error(
                                new IllegalArgumentException("Unknown Action type: " + w)
                        ))));
}
