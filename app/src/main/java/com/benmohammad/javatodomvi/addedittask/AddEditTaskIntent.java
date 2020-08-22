package com.benmohammad.javatodomvi.addedittask;

import com.benmohammad.javatodomvi.mvibase.MviIntent;
import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface AddEditTaskIntent extends MviIntent {

    @AutoValue
    abstract class InitialIntent implements AddEditTaskIntent {
        @Nullable
        abstract String taskId();

        public static InitialIntent create(@Nullable String taskId) {
            return new AutoValue_AddEditTaskIntent_InitialIntent(taskId);
        }
    }

    @AutoValue
    abstract class SaveTask implements AddEditTaskIntent {
        @Nullable
        abstract String taskId();
        abstract String title();
        abstract String description();

        public static SaveTask create(@Nullable String taskId, @Nonnull String title, @Nonnull String description) {
            return new AutoValue_AddEditTaskIntent_SaveTask(taskId, title, description);
        }
    }
}
