package com.benmohammad.javatodomvi.stats;

import androidx.lifecycle.ViewModel;

import com.benmohammad.javatodomvi.mvibase.MviIntent;
import com.benmohammad.javatodomvi.mvibase.MviViewModel;

import javax.annotation.Nonnull;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.subjects.PublishSubject;

import static autovalue.shaded.com.google$.common.base.$Preconditions.checkNotNull;

public class StatisticsViewModel extends ViewModel implements MviViewModel<StatisticsIntent, StatisticsViewState> {

    @Nonnull
    private PublishSubject<StatisticsIntent> intentsSubject;

    @Nonnull
    private Observable<StatisticsViewState> stateObservable;

    @Nonnull
    private CompositeDisposable disposables = new CompositeDisposable();

    @Nonnull
    private StatisticsActionProcessorHolder actionProcessorHolder;

    public StatisticsViewModel(@Nonnull StatisticsActionProcessorHolder actionProcessorHolder) {
        this.actionProcessorHolder = checkNotNull(actionProcessorHolder, "actionProcessorHolder cannot be null");
        intentsSubject = PublishSubject.create();
        stateObservable = compose();
    }



    @Override
    public void processIntents(Observable<StatisticsIntent> intents) {
        disposables.add(intents.subscribe(intentsSubject::onNext));
    }

    @Override
    public Observable<StatisticsViewState> states() {
        return stateObservable;
    }

    private Observable<StatisticsViewState> compose() {
        return intentsSubject
                .compose(intentFilter)
                .map(this::actionFromIntent)
                .compose(actionProcessorHolder.actionProcessor)
                .scan(StatisticsViewState.idle(), reducer)
                .distinctUntilChanged()
                .replay(1)
                .autoConnect(0);
    }

    private ObservableTransformer<StatisticsIntent, StatisticsIntent> intentFilter =
            intents -> intents.publish(shared ->
                    Observable.merge(
                            shared.ofType(StatisticsIntent.InitialIntent.class).take(1),
                            shared.filter(intent -> !(intent instanceof StatisticsIntent.InitialIntent))));


    private StatisticsAction actionFromIntent(MviIntent intent) {
        if(intent instanceof StatisticsIntent.InitialIntent) {
            return StatisticsAction.LoadStatistics.create();
        }
        throw new IllegalArgumentException("do not know how to handle");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.dispose();
    }

    private static BiFunction<StatisticsViewState, StatisticsResult, StatisticsViewState> reducer =
            (previousState, result) -> {
        StatisticsViewState.Builder stateBuilder = previousState.buildWith();
        if(result instanceof StatisticsResult.LoadStatistics) {
            StatisticsResult.LoadStatistics loadStatisticsResult = (StatisticsResult.LoadStatistics) result;
            switch(loadStatisticsResult.status()) {
                case SUCCESS:
                    return stateBuilder.isLoading(false)
                            .activeCount(loadStatisticsResult.activeCount())
                            .completedCount(loadStatisticsResult.completedCount())
                            .build();
                case FAILURE:
                    return stateBuilder.isLoading(false).error(loadStatisticsResult.error()).build();
                case IN_FLIGHT:
                    return stateBuilder.isLoading(true).build();
            }
        } else {
            throw new IllegalArgumentException("Don't know this result");
        }
        throw new IllegalStateException("MisHandled......................" + result);
    };
}
