package com.benmohammad.javatodomvi.stats;

import com.benmohammad.javatodomvi.mvibase.MviViewState;
import com.google.auto.value.AutoValue;


import javax.annotation.Nullable;

@AutoValue
public abstract class StatisticsViewState implements MviViewState {

    abstract boolean isLoading();
    abstract int activeCount();
    abstract int completedCount();
    @Nullable
    abstract Throwable error();
    public abstract Builder buildWith();

    static StatisticsViewState idle() {
        return new AutoValue_StatisticsViewState.Builder().isLoading(false)
                .activeCount(0)
                .completedCount(0)
                .error(null)
                .build();
    }

    @AutoValue.Builder
    static abstract class Builder {
        abstract Builder isLoading(boolean isLoading);
        abstract Builder activeCount(int activeCount);
        abstract Builder completedCount(int completedCount);
        abstract Builder error(@Nullable Throwable error);
        abstract StatisticsViewState build();
    }
}
