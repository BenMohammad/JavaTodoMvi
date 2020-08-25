package com.benmohammad.javatodomvi.stats;

import com.benmohammad.javatodomvi.mvibase.MviIntent;
import com.google.auto.value.AutoValue;

public interface StatisticsIntent extends MviIntent {

    @AutoValue
    abstract class InitialIntent implements StatisticsIntent {
        public static InitialIntent create() {
            return new AutoValue_StatisticsIntent_InitialIntent();
        }
    }
}
