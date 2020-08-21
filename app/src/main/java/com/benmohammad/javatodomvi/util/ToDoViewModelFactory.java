package com.benmohammad.javatodomvi.util;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.benmohammad.javatodomvi.injection.Injection;
import com.benmohammad.javatodomvi.tasks.TasksActionProcessorHolder;
import com.benmohammad.javatodomvi.tasks.TasksViewModel;


public class ToDoViewModelFactory implements ViewModelProvider.Factory {

    @SuppressLint("StaticFieldLeak")
    private static ToDoViewModelFactory INSTANCE;

    private final Context applicationContext;

    private ToDoViewModelFactory(Context applicationContext) {
        this.applicationContext= applicationContext;
    }

    public static ToDoViewModelFactory getInstance(Context context) {
        if(INSTANCE == null) {
            INSTANCE = new ToDoViewModelFactory(context.getApplicationContext());
        }
        return INSTANCE;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass == TasksViewModel.class) {
            return (T) new TasksViewModel(
                    new TasksActionProcessorHolder(
                            Injection.provideTasksRepository(applicationContext),
                            Injection.provideSchedulerProvider()));
        }

        throw new IllegalArgumentException("unknown model class: " + modelClass);
    }
}
