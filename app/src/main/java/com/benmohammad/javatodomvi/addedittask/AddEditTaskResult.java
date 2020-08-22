package com.benmohammad.javatodomvi.addedittask;

import com.benmohammad.javatodomvi.data.Task;
import com.benmohammad.javatodomvi.mvibase.MviResult;
import com.benmohammad.javatodomvi.util.LceStatus;
import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.benmohammad.javatodomvi.util.LceStatus.FAILURE;
import static com.benmohammad.javatodomvi.util.LceStatus.IN_FLIGHT;
import static com.benmohammad.javatodomvi.util.LceStatus.SUCCESS;

public interface AddEditTaskResult extends MviResult {

    @AutoValue
    abstract class PopulateTask implements AddEditTaskResult {
        @Nonnull
        abstract LceStatus status();

        @Nullable
        abstract Task task();

        @Nullable
        abstract Throwable error();

        @Nonnull
        static PopulateTask success(@Nonnull Task task) {
            return new AutoValue_AddEditTaskResult_PopulateTask(SUCCESS, task, null);
        }

        @Nonnull
        static PopulateTask failure(Throwable error) {
            return new AutoValue_AddEditTaskResult_PopulateTask(FAILURE, null, error);
        }

        @Nonnull
        static PopulateTask inFlight() {
            return new AutoValue_AddEditTaskResult_PopulateTask(IN_FLIGHT, null, null);
        }
    }
    @AutoValue
    abstract class CreateTask implements AddEditTaskResult {
        abstract boolean isEmpty();

        static CreateTask success() {
            return new AutoValue_AddEditTaskResult_CreateTask(false);
        }

        static CreateTask empty() {
            return new AutoValue_AddEditTaskResult_CreateTask(true);
        }
    }

    @AutoValue
    abstract class UpdateTask implements AddEditTaskResult {
        static UpdateTask create() {
            return new AutoValue_AddEditTaskResult_UpdateTask();
        }
    }
}
