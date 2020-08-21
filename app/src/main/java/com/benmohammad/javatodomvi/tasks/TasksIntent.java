package com.benmohammad.javatodomvi.tasks;

import com.benmohammad.javatodomvi.data.Task;
import com.benmohammad.javatodomvi.mvibase.MviIntent;
import com.google.auto.value.AutoValue;

public interface TasksIntent extends MviIntent {

    @AutoValue
    abstract class InitialIntent implements TasksIntent {
        public static InitialIntent create() {
            return new AutoValue_TasksIntent_InitialIntent();
        }
    }

    @AutoValue
    abstract class RefreshIntent implements TasksIntent {
        abstract boolean forceUpdate();

        public static RefreshIntent create(boolean forceUpdate) {
            return new AutoValue_TasksIntent_RefreshIntent(forceUpdate);
        }
    }

    @AutoValue
    abstract class ActivateTaskIntent implements TasksIntent {
        abstract Task task();

        public static ActivateTaskIntent create(Task task) {
            return new AutoValue_TasksIntent_ActivateTaskIntent(task);
        }
    }

    @AutoValue
    abstract class CompleteTaskIntent implements TasksIntent {
        abstract Task task();

        public static CompleteTaskIntent create(Task task) {
            return new AutoValue_TasksIntent_CompleteTaskIntent(task);
        }
    }

    @AutoValue
    abstract class ClearCompletedTasksIntent implements TasksIntent {
        public static ClearCompletedTasksIntent create() {
            return new AutoValue_TasksIntent_ClearCompletedTasksIntent();
        }
    }

    @AutoValue
    abstract class ChangeFilterIntent implements TasksIntent {
        abstract TasksFilterType filterType();

        public static ChangeFilterIntent create(TasksFilterType filterType) {
            return new AutoValue_TasksIntent_ChangeFilterIntent(filterType);
        }
    }
}
