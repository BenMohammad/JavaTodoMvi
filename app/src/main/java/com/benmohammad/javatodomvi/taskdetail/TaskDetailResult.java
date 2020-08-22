package com.benmohammad.javatodomvi.taskdetail;

import com.benmohammad.javatodomvi.data.Task;
import com.benmohammad.javatodomvi.mvibase.MviResult;
import com.benmohammad.javatodomvi.tasks.TasksResult;
import com.benmohammad.javatodomvi.util.LceStatus;
import com.benmohammad.javatodomvi.util.UiNotificationStatus;
import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.benmohammad.javatodomvi.util.LceStatus.FAILURE;
import static com.benmohammad.javatodomvi.util.LceStatus.IN_FLIGHT;
import static com.benmohammad.javatodomvi.util.LceStatus.SUCCESS;
import static com.benmohammad.javatodomvi.util.UiNotificationStatus.HIDE;
import static com.benmohammad.javatodomvi.util.UiNotificationStatus.SHOW;

public interface TaskDetailResult extends MviResult {

    @AutoValue
    abstract class PopulateTask implements TaskDetailResult {
        @Nonnull
        abstract LceStatus status();

        @Nullable
        abstract Task task();

        @Nullable
        abstract Throwable error();

        @Nonnull
        static PopulateTask success(@Nonnull Task task) {
            return new AutoValue_TaskDetailResult_PopulateTask(SUCCESS, task, null);
        }

        @Nonnull
        static PopulateTask failure(Throwable error) {
            return new AutoValue_TaskDetailResult_PopulateTask(FAILURE, null, error);
        }

        @Nonnull
        static PopulateTask inFlight() {
            return new AutoValue_TaskDetailResult_PopulateTask(IN_FLIGHT, null, null);
        }
    }

    @AutoValue
    abstract class ActivateTaskResult implements TaskDetailResult {
        @Nonnull
        abstract LceStatus status();

        @Nullable
        abstract UiNotificationStatus uiNotificationStatus();

        @Nullable
        abstract Task task();

        @Nullable
        abstract Throwable error();

        @Nonnull
        static ActivateTaskResult hideUiNotification() {
            return new AutoValue_TaskDetailResult_ActivateTaskResult(SUCCESS, HIDE, null, null);
        }

        @Nonnull
        static ActivateTaskResult success(@Nonnull Task task) {
            return new AutoValue_TaskDetailResult_ActivateTaskResult(SUCCESS, SHOW, task, null);
        }

        @Nonnull
        static ActivateTaskResult failure(Throwable error) {
            return new AutoValue_TaskDetailResult_ActivateTaskResult(FAILURE, null, null, error);
        }

        @Nonnull
        static ActivateTaskResult inFlight() {
            return new AutoValue_TaskDetailResult_ActivateTaskResult(IN_FLIGHT, null, null, null);
        }
    }

    @AutoValue
    abstract class CompleteTaskResult implements TaskDetailResult {
        @Nonnull
        abstract LceStatus status();

        @Nullable
        abstract UiNotificationStatus uiNotificationStatus();

        @Nullable
        abstract Task task();

        @Nullable
        abstract Throwable error();

        @Nonnull
        static CompleteTaskResult hideUiNotification() {
            return new AutoValue_TaskDetailResult_CompleteTaskResult(SUCCESS, HIDE, null, null);
        }

        @Nonnull
        static CompleteTaskResult success(@Nonnull Task task) {
            return new AutoValue_TaskDetailResult_CompleteTaskResult(SUCCESS, SHOW, task, null);
        }

        @Nonnull
        static CompleteTaskResult failure(Throwable error) {
            return new AutoValue_TaskDetailResult_CompleteTaskResult(FAILURE, null, null, error);
        }

        @Nonnull
        static CompleteTaskResult inFlight() {
            return new AutoValue_TaskDetailResult_CompleteTaskResult(IN_FLIGHT, null, null, null);
        }
    }

    @AutoValue
    abstract class DeleteTaskResult implements TaskDetailResult {
        @Nonnull
        abstract LceStatus status();

        @Nullable
        abstract Throwable error();

        @Nonnull
        static DeleteTaskResult success() {
            return new AutoValue_TaskDetailResult_DeleteTaskResult(SUCCESS, null);
        }

        @Nonnull
        static DeleteTaskResult failure(Throwable error) {
            return new AutoValue_TaskDetailResult_DeleteTaskResult(FAILURE, error);
        }


        @Nonnull
        static DeleteTaskResult inFLight() {
            return new AutoValue_TaskDetailResult_DeleteTaskResult(IN_FLIGHT, null);
        }
    }
}
