package com.benmohammad.javatodomvi.taskdetail;

import androidx.lifecycle.ViewModel;

import com.benmohammad.javatodomvi.data.Task;
import com.benmohammad.javatodomvi.mvibase.MviIntent;
import com.benmohammad.javatodomvi.mvibase.MviViewModel;
import com.benmohammad.javatodomvi.util.UiNotificationStatus;

import javax.annotation.Nonnull;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.subjects.PublishSubject;

import static com.google.common.base.Preconditions.checkNotNull;

public class TaskDetailViewModel extends ViewModel implements MviViewModel<TaskDetailIntent, TaskDetailViewState> {

    @Nonnull
    private PublishSubject<TaskDetailIntent> intentSubject;

    @Nonnull
    private Observable<TaskDetailViewState> statesObservable;

    @Nonnull
    private CompositeDisposable disposables = new CompositeDisposable();

    @Nonnull
    private TaskDetailActionProcessorHolder actionProcessorHolder;

    public TaskDetailViewModel(@Nonnull TaskDetailActionProcessorHolder actionProcessorHolder) {
        this.actionProcessorHolder = checkNotNull(actionProcessorHolder);
        intentSubject = PublishSubject.create();
        statesObservable = compose();
    }



    @Override
    public void processIntents(Observable<TaskDetailIntent> intents) {
        disposables.add(intents.subscribe(intentSubject::onNext));
    }

    @Override
    public Observable<TaskDetailViewState> states() {
        return statesObservable;
    }

    private Observable<TaskDetailViewState> compose() {
        return intentSubject
                .compose(intentFilter)
                .map(this::actionFromIntent)
                .compose(actionProcessorHolder.actionProcessor)
                .scan(TaskDetailViewState.idle(), reducer)
                .distinctUntilChanged()
                .replay(1)
                .autoConnect(0);
    }

    private ObservableTransformer<TaskDetailIntent, TaskDetailIntent> intentFilter =
            intents -> intents.publish(shared ->
                    Observable.merge(
                            shared.ofType(TaskDetailIntent.InitialIntent.class).take(1),
                            shared.filter(intent -> !(intent instanceof TaskDetailIntent.InitialIntent))
                    )
            );

    private TaskDetailAction actionFromIntent(MviIntent intent) {
        if(intent instanceof TaskDetailIntent.InitialIntent) {
            String taskId = ((TaskDetailIntent.InitialIntent) intent).taskId();
            checkNotNull(taskId);
            return TaskDetailAction.PopulateTask.create(taskId);
        }
        if(intent instanceof TaskDetailIntent.DeleteTask) {
            TaskDetailIntent.DeleteTask deleteTaskIntent = (TaskDetailIntent.DeleteTask) intent;
            final String taskId = deleteTaskIntent.taskId();
            return TaskDetailAction.DeleteTask.create(taskId);
        }
        if(intent instanceof TaskDetailIntent.CompleteTaskIntent) {
            TaskDetailIntent.CompleteTaskIntent completeTaskIntent = (TaskDetailIntent.CompleteTaskIntent) intent;
            final String taskId = completeTaskIntent.taskId();
            return TaskDetailAction.CompleteTask.create(taskId);
        }
        if(intent instanceof TaskDetailIntent.ActivateTaskIntent) {
            TaskDetailIntent.ActivateTaskIntent activateTaskIntent = (TaskDetailIntent.ActivateTaskIntent) intent;
            final String taskId = activateTaskIntent.taskId();
            return TaskDetailAction.ActivateTask.create(taskId);
        }
        throw new IllegalArgumentException("Do not know why..........");
    }

    @Override
    protected void onCleared() {
        disposables.dispose();
    }

    private static BiFunction<TaskDetailViewState, TaskDetailResult, TaskDetailViewState> reducer =
            (previousState, result) -> {
        TaskDetailViewState.Builder stateBuilder = previousState.buildWith();
        if(result instanceof TaskDetailResult.PopulateTask) {
            TaskDetailResult.PopulateTask populateTaskResult =
                    (TaskDetailResult.PopulateTask) result;
            switch(populateTaskResult.status()) {
                case SUCCESS:
                    Task task = checkNotNull(populateTaskResult.task());
                    stateBuilder.title(task.getTitle());
                    stateBuilder.description(task.getDescription());
                    stateBuilder.active(task.isActive());
                    stateBuilder.loading(false);
                    return stateBuilder.build();
                case FAILURE:
                    Throwable error = checkNotNull(populateTaskResult.error());
                    stateBuilder.loading(false);
                    return stateBuilder.error(error).build();
                case IN_FLIGHT:
                    stateBuilder.loading(true);
                    return stateBuilder.build();
            }
        }
        if(result instanceof TaskDetailResult.DeleteTaskResult) {
            TaskDetailResult.DeleteTaskResult deleteTaskResult =
                    (TaskDetailResult.DeleteTaskResult) result;
            switch(deleteTaskResult.status()) {
                case SUCCESS:
                    return stateBuilder.taskDeleted(true).build();
                case FAILURE:
                    return stateBuilder.error(deleteTaskResult.error()).build();
                case IN_FLIGHT:
                    return stateBuilder.build();
            }
        } else if (result instanceof TaskDetailResult.ActivateTaskResult) {
            TaskDetailResult.ActivateTaskResult activateTaskResult =
                    (TaskDetailResult.ActivateTaskResult) result;
            switch(activateTaskResult.status()) {
                case SUCCESS:
                return stateBuilder
                        .taskActivated(activateTaskResult.uiNotificationStatus() == UiNotificationStatus.SHOW)
                        .active(true)
                        .build();
                case FAILURE:
                    return stateBuilder.error(activateTaskResult.error()).build();
                case IN_FLIGHT:
                    return stateBuilder.build();
            }
        } else if(result instanceof TaskDetailResult.CompleteTaskResult) {
            TaskDetailResult.CompleteTaskResult completeTaskResult =
                    (TaskDetailResult.CompleteTaskResult) result;
            switch(completeTaskResult.status()) {
                case SUCCESS:
                    return stateBuilder
                            .taskComplete(completeTaskResult.uiNotificationStatus() == UiNotificationStatus.SHOW)
                            .active(false)
                            .build();
                case FAILURE:
                    return stateBuilder.error(completeTaskResult.error()).build();
                case IN_FLIGHT:
                    return stateBuilder.build();
            }
        }
        throw new IllegalStateException("Mishandled result : " + result);
            };
}
