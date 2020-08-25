package com.benmohammad.javatodomvi.stats;

import com.benmohammad.javatodomvi.data.Task;
import com.benmohammad.javatodomvi.data.source.TasksRepository;
import com.benmohammad.javatodomvi.util.Pair;
import com.benmohammad.javatodomvi.util.schedulers.BaseSchedulerProvider;

import javax.annotation.Nonnull;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;

import static com.google.common.base.Preconditions.checkNotNull;

public class StatisticsActionProcessorHolder {

    @Nonnull
    private TasksRepository tasksRepository;

    @Nonnull
    private BaseSchedulerProvider schedulerProvider;

    public StatisticsActionProcessorHolder(@Nonnull TasksRepository tasksRepository,
                                           @Nonnull BaseSchedulerProvider schedulerProvider) {
        this.tasksRepository = checkNotNull(tasksRepository, "tasksRepository cannot be null");
        this.schedulerProvider = checkNotNull(schedulerProvider, "scheduler Provider cannot be null");
    }

    private ObservableTransformer<StatisticsAction.LoadStatistics, StatisticsResult.LoadStatistics>
    loadStatisticsProcessor = actions -> actions.flatMap(
            action ->
                    tasksRepository.getTasks()
                    .toObservable()
                    .flatMap(Observable::fromIterable)
                    .publish(shared ->
                            Single.zip(shared.filter(Task::isActive).count(),
                                       shared.filter(Task::isCompleted).count(),
                                    Pair::create).toObservable())
            .map(pair ->
                    StatisticsResult.LoadStatistics.success(
                            pair.first().intValue(), pair.second().intValue()))
            .onErrorReturn(StatisticsResult.LoadStatistics::failure)
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .startWith(StatisticsResult.LoadStatistics.inFlight()));

    ObservableTransformer<StatisticsAction, StatisticsResult> actionProcessor =
            actions -> actions.publish(shared ->
                    shared.ofType(StatisticsAction.LoadStatistics.class).compose(loadStatisticsProcessor).cast(StatisticsResult.class)

                    .mergeWith(shared.filter(v -> !(v instanceof StatisticsAction.LoadStatistics))
                    .flatMap(w -> Observable.error(
                            new IllegalArgumentException("Unknown Action type: " + w)))));
}
