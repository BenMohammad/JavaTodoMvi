package com.benmohammad.javatodomvi.stats;

import com.benmohammad.javatodomvi.mvibase.MviAction;
import com.google.auto.value.AutoValue;

public interface StatisticsAction extends MviAction {

    @AutoValue
    abstract class LoadStatistics implements StatisticsAction {
        public static LoadStatistics create() {
            return new AutoValue_StatisticsAction_LoadStatistics();
        }
    }
}
