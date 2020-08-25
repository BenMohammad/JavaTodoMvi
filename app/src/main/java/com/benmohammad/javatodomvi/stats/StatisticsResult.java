package com.benmohammad.javatodomvi.stats;

import com.benmohammad.javatodomvi.mvibase.MviResult;
import com.benmohammad.javatodomvi.util.LceStatus;
import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.benmohammad.javatodomvi.util.LceStatus.FAILURE;
import static com.benmohammad.javatodomvi.util.LceStatus.IN_FLIGHT;
import static com.benmohammad.javatodomvi.util.LceStatus.SUCCESS;

public interface StatisticsResult extends MviResult {

    @AutoValue
    abstract class LoadStatistics implements StatisticsResult {
        @Nonnull
        abstract LceStatus status();

        abstract int activeCount();

        abstract int completedCount();

        @Nullable
        abstract Throwable error();

        @Nonnull
        public static LoadStatistics success(int activeCount, int completedCount) {
            return new AutoValue_StatisticsResult_LoadStatistics(SUCCESS, activeCount, completedCount, null);
        }

        @Nonnull
        public static LoadStatistics failure(Throwable error) {
            return new AutoValue_StatisticsResult_LoadStatistics(FAILURE, 0, 0, error);
        }

        @Nonnull
        public static LoadStatistics inFlight() {
            return new AutoValue_StatisticsResult_LoadStatistics(IN_FLIGHT, 0, 0, null);
        }
    }
}
