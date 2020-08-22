package com.benmohammad.javatodomvi.taskdetail;

import androidx.annotation.NonNull;

import com.benmohammad.javatodomvi.mvibase.MviAction;
import com.google.auto.value.AutoValue;

import javax.annotation.Nonnull;

public interface TaskDetailAction extends MviAction {

    @AutoValue
    abstract class PopulateTask implements TaskDetailAction {
        abstract String taskId();

        public static PopulateTask create(@NonNull String taskId) {
            return new AutoValue_TaskDetailAction_PopulateTask(taskId);
        }
    }

    @AutoValue
    abstract class DeleteTask implements TaskDetailAction {
        abstract String taskId();

        public static DeleteTask create(@NonNull String taskId) {
            return new AutoValue_TaskDetailAction_DeleteTask(taskId);
        }
    }

    @AutoValue
    abstract class ActivateTask implements TaskDetailAction {
        abstract String taskId();

        public static ActivateTask create(@Nonnull String taskId) {
            return new AutoValue_TaskDetailAction_ActivateTask(taskId);
        }
    }

    @AutoValue
    abstract class CompleteTask implements TaskDetailAction {
        abstract String taskId();

        public static CompleteTask create(@Nonnull String taskId) {
            return new AutoValue_TaskDetailAction_CompleteTask(taskId);
        }
    }
}
