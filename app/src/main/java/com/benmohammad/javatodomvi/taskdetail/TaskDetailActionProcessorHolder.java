package com.benmohammad.javatodomvi.taskdetail;

import com.benmohammad.javatodomvi.data.source.TasksRepository;
import com.benmohammad.javatodomvi.data.source.remote.TasksRemoteDataSource;
import com.benmohammad.javatodomvi.util.schedulers.BaseSchedulerProvider;

import javax.annotation.Nonnull;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

import static com.benmohammad.javatodomvi.util.ObservableUtils.pairWithDelay;
import static com.google.common.base.Preconditions.checkNotNull;

public class TaskDetailActionProcessorHolder {

    @Nonnull
    private TasksRepository tasksRepository;

    @Nonnull
    private BaseSchedulerProvider schedulerProvider;

    public TaskDetailActionProcessorHolder(@Nonnull TasksRepository tasksRepository,
                                           @Nonnull BaseSchedulerProvider schedulerProvider) {
        this.tasksRepository = checkNotNull(tasksRepository, "repository cannot be null");
        this.schedulerProvider = checkNotNull(schedulerProvider, "scheduler cannot be null");
    }

    private ObservableTransformer<TaskDetailAction.PopulateTask, TaskDetailResult.PopulateTask>
    populateTaskProcessor =
            actions -> actions.flatMap(action ->
                    tasksRepository.getTask(action.taskId())
                        .toObservable()
                        .map(TaskDetailResult.PopulateTask::success)
                        .onErrorReturn(TaskDetailResult.PopulateTask::failure)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .startWith(TaskDetailResult.PopulateTask.inFlight()));

    private ObservableTransformer<TaskDetailAction.CompleteTask, TaskDetailResult.CompleteTaskResult>
    completeTasKProcessor = actions -> actions.flatMap(action ->
            tasksRepository.completeTask(action.taskId())
                .andThen(tasksRepository.getTask(action.taskId())
                        .toObservable()
                        .flatMap(task ->
                                pairWithDelay(
                                        TaskDetailResult.CompleteTaskResult.success(task),
                                        TaskDetailResult.CompleteTaskResult.hideUiNotification()
                                ))
                        .onErrorReturn(TaskDetailResult.CompleteTaskResult::failure)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .startWith(TaskDetailResult.CompleteTaskResult.inFlight())));

    private ObservableTransformer<TaskDetailAction.ActivateTask, TaskDetailResult.ActivateTaskResult>
    activateTaskProcessor = actions -> actions.flatMap(action ->
            tasksRepository.activateTask(action.taskId())
                .andThen(tasksRepository.getTask(action.taskId())
                .toObservable()
                .flatMap(task ->
                        pairWithDelay(
                                TaskDetailResult.ActivateTaskResult.success(task),
                                TaskDetailResult.ActivateTaskResult.hideUiNotification()
                        ))
                        .onErrorReturn(TaskDetailResult.ActivateTaskResult::failure)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .startWith(TaskDetailResult.ActivateTaskResult.inFlight())));

    private ObservableTransformer<TaskDetailAction.DeleteTask, TaskDetailResult.DeleteTaskResult>
    deleteTasKProcessor = actions -> actions.flatMap(action ->
            tasksRepository.deleteTask(action.taskId())
                .andThen(Observable.just(TaskDetailResult.DeleteTaskResult.success()))
                .onErrorReturn(TaskDetailResult.DeleteTaskResult::failure)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .startWith(TaskDetailResult.DeleteTaskResult.inFLight()));

    ObservableTransformer<TaskDetailAction, TaskDetailResult> actionProcessor =
            actions -> actions.publish(shared ->
                    Observable.merge(shared.ofType(TaskDetailAction.PopulateTask.class).compose(populateTaskProcessor),
                                    shared.ofType(TaskDetailAction.CompleteTask.class).compose(completeTasKProcessor),
                                    shared.ofType(TaskDetailAction.ActivateTask.class).compose(activateTaskProcessor),
                                    shared.ofType(TaskDetailAction.DeleteTask.class).compose(deleteTasKProcessor)

                        .mergeWith(
                                shared.filter(v -> !(v instanceof TaskDetailAction.PopulateTask)  &&
                                        !(v instanceof TaskDetailAction.CompleteTask)  &&
                                        !(v instanceof TaskDetailAction.ActivateTask)  &&
                                        !(v instanceof TaskDetailAction.DeleteTask))
                                .flatMap(w -> Observable.error(
                                        new IllegalArgumentException("Unknown action type: " + w))))));
}
