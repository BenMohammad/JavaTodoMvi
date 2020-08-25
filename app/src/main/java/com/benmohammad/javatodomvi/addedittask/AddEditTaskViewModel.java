package com.benmohammad.javatodomvi.addedittask;

import androidx.lifecycle.ViewModel;

import com.benmohammad.javatodomvi.data.Task;
import com.benmohammad.javatodomvi.mvibase.MviIntent;
import com.benmohammad.javatodomvi.mvibase.MviViewModel;

import javax.annotation.Nonnull;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.subjects.PublishSubject;

import static com.google.common.base.Preconditions.checkNotNull;

public class AddEditTaskViewModel extends ViewModel implements MviViewModel<AddEditTaskIntent, AddEditTaskViewState> {

    @Nonnull
    private PublishSubject<AddEditTaskIntent> intentSubject;

    @Nonnull
    private Observable<AddEditTaskViewState> statesObservable;

    @Nonnull
    private CompositeDisposable disposables = new CompositeDisposable();

    @Nonnull
    private AddEditTaskActionProcessorHolder actionProcessorHolder;

    public AddEditTaskViewModel(@Nonnull AddEditTaskActionProcessorHolder actionProcessorHolder) {
        this.actionProcessorHolder = checkNotNull(actionProcessorHolder);
        intentSubject = PublishSubject.create();
        statesObservable = compose();
    }



    @Override
    public void processIntents(Observable<AddEditTaskIntent> intents) {
        disposables.add(intents.subscribe(intentSubject::onNext));
    }

    @Override
    public Observable<AddEditTaskViewState> states() {
        return statesObservable;
    }

    private Observable<AddEditTaskViewState> compose() {
        return intentSubject
                .compose(intentFilter)
                .map(this::actionFromIntent)
                .filter(action -> !(action instanceof AddEditTaskAction.SkipMe))
                .compose(actionProcessorHolder.actionProcessor)
                .scan(AddEditTaskViewState.idle(), reducer)
                .distinctUntilChanged()
                .replay(1)
                .autoConnect(0);
    }


    private ObservableTransformer<AddEditTaskIntent, AddEditTaskIntent> intentFilter =
            intents -> intents.publish(shared ->
                    Observable.merge(
                            shared.ofType(AddEditTaskIntent.InitialIntent.class).take(1),
                            shared.filter(intent -> !(intent instanceof AddEditTaskIntent.InitialIntent))
                    ));

    private AddEditTaskAction actionFromIntent(MviIntent intent) {
        if(intent instanceof AddEditTaskIntent.InitialIntent) {
            String taskId = ((AddEditTaskIntent.InitialIntent) intent).taskId();
            if(taskId == null) {
                return AddEditTaskAction.SkipMe.create();
            } else {
                return AddEditTaskAction.PopulateTask.create(taskId);
            }
        }
        if(intent instanceof AddEditTaskIntent.SaveTask) {
            AddEditTaskIntent.SaveTask saveTaskIntent = (AddEditTaskIntent.SaveTask) intent;
            final String taskId = saveTaskIntent.taskId();
            if(taskId == null) {
                return AddEditTaskAction.CreateTask.create(
                        saveTaskIntent.title(), saveTaskIntent.description());
            } else {
                return AddEditTaskAction.UpdateTask.create(
                        taskId, saveTaskIntent.title(), saveTaskIntent.description());
            }
        }
        throw new IllegalArgumentException("Do not know what to do...");
    }

    @Override
    protected void onCleared() {
        disposables.dispose();
    }

    private static BiFunction<AddEditTaskViewState, AddEditTaskResult, AddEditTaskViewState> reducer =
            (previousState, result) -> {
        AddEditTaskViewState.Builder stateBuilder = previousState.buildWith();
        if(result instanceof AddEditTaskResult.PopulateTask) {
            AddEditTaskResult.PopulateTask populateTaskResult = (AddEditTaskResult.PopulateTask) result;
            switch(populateTaskResult.status()) {
                case SUCCESS:
                    Task task = checkNotNull(populateTaskResult.task());
                    if(task.isActive()) {
                        stateBuilder.title(task.getTitle());
                        stateBuilder.description(task.getDescription());
                    }
                    return stateBuilder.build();
                case FAILURE:
                    Throwable error = checkNotNull(populateTaskResult.error());
                    return stateBuilder.error(error).build();
                case IN_FLIGHT:
                    return stateBuilder.build();
            }
        } if(result instanceof AddEditTaskResult.CreateTask) {
            AddEditTaskResult.CreateTask createTaskResult = (AddEditTaskResult.CreateTask) result;
            if(createTaskResult.isEmpty()) {
                return stateBuilder.isEmpty(true).build();
            } else {
                return stateBuilder.isEmpty(false).isSaved(true).build();
            }
        }if(result instanceof AddEditTaskResult.UpdateTask) {
            return stateBuilder.isSaved(true).build();
        }
        throw new IllegalStateException("Mishandled result");
    };
}
