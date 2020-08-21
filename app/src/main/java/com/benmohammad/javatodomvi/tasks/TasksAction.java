package com.benmohammad.javatodomvi.tasks;

import androidx.annotation.Nullable;

import com.benmohammad.javatodomvi.data.Task;
import com.benmohammad.javatodomvi.mvibase.MviAction;
import com.google.auto.value.AutoValue;

public interface TasksAction extends MviAction {

    @AutoValue
    abstract class LoadTasks implements TasksAction {
        public abstract boolean forceUpdate();

        @Nullable
        public abstract TasksFilterType filterType();

        public static LoadTasks loadAndFilter(boolean forceUpdate, TasksFilterType filterType) {
            return new AutoValue_TasksAction_LoadTasks(forceUpdate, filterType);
        }

        public static LoadTasks load(boolean forceUpdate) {
            return new AutoValue_TasksAction_LoadTasks(forceUpdate, null);
        }
    }

    @AutoValue
    abstract class ActivateTaskAction implements TasksAction {
        abstract Task task();

        public static ActivateTaskAction create(Task task) {
            return new AutoValue_TasksAction_ActivateTaskAction(task);
        }
    }

    @AutoValue
    abstract class CompleteTaskAction implements TasksAction {
        abstract Task task();

        public static CompleteTaskAction create(Task task) {
            return new AutoValue_TasksAction_CompleteTaskAction(task);
        }
    }

    @AutoValue
    abstract class ClearCompletedTasksAction implements TasksAction {
        public static ClearCompletedTasksAction create() {
            return new AutoValue_TasksAction_ClearCompletedTasksAction();
        }
    }
}
