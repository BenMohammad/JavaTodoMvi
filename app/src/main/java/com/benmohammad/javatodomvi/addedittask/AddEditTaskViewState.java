package com.benmohammad.javatodomvi.addedittask;

import com.benmohammad.javatodomvi.mvibase.MviViewState;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class AddEditTaskViewState implements MviViewState {

    abstract String title();
    abstract String description();
    abstract boolean isEmpty();
    abstract boolean isSaved();

    @Nullable
    abstract Throwable error();

    public abstract Builder buildWith();

    static AddEditTaskViewState idle() {
        return new AutoValue_AddEditTaskViewState.Builder()
                .title("")
                .description("")
                .error(null)
                .isEmpty(false)
                .isSaved(false)
                .build();
    }

    @AutoValue.Builder
    static abstract class Builder {
        abstract Builder title(String title);
        abstract Builder description(String description);
        abstract Builder isEmpty(boolean isEmpty);
        abstract Builder isSaved(boolean isSaved);
        abstract Builder error(Throwable error);
        abstract AddEditTaskViewState build();
    }
}
