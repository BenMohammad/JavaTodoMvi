package com.benmohammad.javatodomvi.tasks;

import android.app.TaskInfo;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.benmohammad.javatodomvi.data.Task;
import com.benmohammad.javatodomvi.mvibase.MviIntent;
import com.benmohammad.javatodomvi.mvibase.MviViewModel;
import com.benmohammad.javatodomvi.util.UiNotificationStatus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.subjects.PublishSubject;

import static com.benmohammad.javatodomvi.util.UiNotificationStatus.SHOW;
import static com.google.common.base.Preconditions.checkNotNull;

public class TasksViewModel extends ViewModel implements MviViewModel<TasksIntent, TasksViewState> {

    @NonNull
    private PublishSubject<TasksIntent> intentsSubject;

    @NonNull
    private Observable<TasksViewState> statesObservable;

    @NonNull
    private CompositeDisposable disposables = new CompositeDisposable();

    @NonNull
    private TasksActionProcessorHolder actionProcessorHolder;

    public TasksViewModel(@NonNull TasksActionProcessorHolder actionProcessorHolder) {
        this.actionProcessorHolder = checkNotNull(actionProcessorHolder, "tasksActionProcessorHolder cannot be null");
        intentsSubject = PublishSubject.create();
        statesObservable = compose();
    }


    @Override
    public void processIntents(Observable<TasksIntent> intents) {
        disposables.add(intents.subscribe(intentsSubject::onNext));
    }

    @Override
    public Observable<TasksViewState> states() {
        return statesObservable;
    }

    private Observable<TasksViewState> compose() {
        return intentsSubject
                .compose(intentFilter)
                .map(this::actionFromIntent)
                .compose(actionProcessorHolder.actionProcessor)
                .scan(TasksViewState.idle(), reducer)
                .distinctUntilChanged()
                .replay(1)
                .autoConnect(0);
    }

    private ObservableTransformer<TasksIntent, TasksIntent> intentFilter =
            intents -> intents.publish(shared ->
                    Observable.merge(shared.ofType(TasksIntent.InitialIntent.class).take(1),
                                    shared.filter(intent -> !(intent instanceof TasksIntent.InitialIntent))));

    private TasksAction actionFromIntent(MviIntent intent) {
        if(intent instanceof TasksIntent.InitialIntent) {
            return TasksAction.LoadTasks.loadAndFilter(true, TasksFilterType.ALL_TASKS);
        }
        if(intent instanceof TasksIntent.ChangeFilterIntent) {
            return TasksAction.LoadTasks.loadAndFilter(false, ((TasksIntent.ChangeFilterIntent) intent).filterType());
        }
        if(intent instanceof TasksIntent.RefreshIntent) {
            return TasksAction.LoadTasks.load(((TasksIntent.RefreshIntent) intent).forceUpdate());
        }
        if(intent instanceof TasksIntent.ActivateTaskIntent) {
            return TasksAction.ActivateTaskAction.create(
                    ((TasksIntent.ActivateTaskIntent)intent).task());
        }
        if(intent instanceof TasksIntent.CompleteTaskIntent){
            return TasksAction.CompleteTaskAction.create(
                    ((TasksIntent.CompleteTaskIntent)intent).task());
        }
        if(intent instanceof TasksIntent.ClearCompletedTasksIntent) {
            return TasksAction.ClearCompletedTasksAction.create();
        }
        throw new IllegalArgumentException("do not know how to handle this intent " + intent);
    }

    @Override
    protected void onCleared() {
        disposables.dispose();
    }

    private static BiFunction<TasksViewState, TasksResult, TasksViewState> reducer =
            (previousState, result) -> {
                TasksViewState.Builder stateBuilder = previousState.buildWith();
                if(result instanceof TasksResult.LoadTasks) {
                    TasksResult.LoadTasks loadResult = (TasksResult.LoadTasks) result;
                    switch(loadResult.status()) {
                        case SUCCESS:
                            TasksFilterType filterType = loadResult.filterType();
                            if(filterType == null) {
                                filterType = previousState.tasksFilterType();
                            }
                            List<Task> tasks = filteredTasks(checkNotNull(loadResult.tasks()), filterType);
                            return stateBuilder.isLoading(false).tasks(tasks).tasksFilterType(filterType).build();
                        case FAILURE:
                            return stateBuilder.isLoading(false).error(loadResult.error()).build();
                        case IN_FLIGHT:
                            return stateBuilder.isLoading(true).build();
                    }
                } else if (result instanceof  TasksResult.CompleteTaskResult) {
                    TasksResult.CompleteTaskResult completeTaskResult =
                            (TasksResult.CompleteTaskResult) result;
                    switch(completeTaskResult.status()) {
                        case SUCCESS:
                            stateBuilder.taskComplete(completeTaskResult.uiNotificationStatus() == SHOW);
                            if(completeTaskResult.tasks() != null) {
                                List<Task> tasks = filteredTasks(checkNotNull(completeTaskResult.tasks()),previousState.tasksFilterType());
                                stateBuilder.tasks(tasks);
                            }
                            return stateBuilder.build();
                        case FAILURE:
                            return stateBuilder.error(completeTaskResult.error()).build();
                        case IN_FLIGHT:
                            return stateBuilder.build();
                    }
                } else if (result instanceof TasksResult.ActivateTaskResult) {
                    TasksResult.ActivateTaskResult activateTaskResult =
                            (TasksResult.ActivateTaskResult) result;
                    switch(activateTaskResult.status()) {
                        case SUCCESS:
                            stateBuilder.taskActivated(activateTaskResult.uiNotificationStatus() == SHOW);
                            if(activateTaskResult.tasks() != null) {
                                List<Task> tasks = filteredTasks(checkNotNull(activateTaskResult.tasks()), previousState.tasksFilterType());
                                stateBuilder.tasks(tasks);
                            }
                            return stateBuilder.build();
                        case FAILURE:
                            return stateBuilder.error(activateTaskResult.error()).build();
                        case IN_FLIGHT:
                            return stateBuilder.build();
                    }
                } else if (result instanceof TasksResult.ClearCompletedTaskResult) {
                    TasksResult.ClearCompletedTaskResult clearCompletedTasks =
                            (TasksResult.ClearCompletedTaskResult) result;
                    switch(clearCompletedTasks.status()) {
                        case SUCCESS:
                            stateBuilder.completedTasksCleared(clearCompletedTasks.uiNotificationStatus() == SHOW);
                            if(clearCompletedTasks.tasks() != null) {
                                List<Task> tasks =
                                        filteredTasks(checkNotNull(clearCompletedTasks.tasks()), previousState.tasksFilterType());
                                stateBuilder.tasks(tasks);
                            }
                            return stateBuilder.build();
                        case FAILURE:
                            return stateBuilder.error(clearCompletedTasks.error()).build();
                        case IN_FLIGHT:
                            return stateBuilder.build();
                    }
                } else {
                    throw new IllegalArgumentException("Don't know this result: " + result);
                }
                throw new IllegalStateException("Mishandled result? Should life be so real!!");
            };


            private static List<Task> filteredTasks(@NonNull List<Task> tasks,
                                                    @NonNull TasksFilterType filterType) {
                List<Task> filteredTasks = new ArrayList<>(tasks.size());
                switch(filterType) {
                    case ALL_TASKS:
                        filteredTasks.addAll(tasks);
                        break;
                    case ACTIVE_TASKS:
                        for(Task task : tasks) {
                            if(task.isActive()) filteredTasks.add(task);
                        }
                        break;
                    case COMPLETED_TASKS:
                        for(Task task : tasks) {
                            if(task.isCompleted()) filteredTasks.add(task);
                        }
                        break;
                }
                return filteredTasks;
            }

}
