package com.benmohammad.javatodomvi.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.benmohammad.javatodomvi.mvibase.MviView;
import com.benmohammad.javatodomvi.taskdetail.TaskDetailActivity;
import com.benmohammad.javatodomvi.util.ToDoViewModelFactory;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class TasksFragment extends Fragment implements MviView<TasksIntent, TasksViewState> {

    private TasksViewModel viewModel;
    private TasksAdapter listAdapter;
    private View noTasksView;
    private ImageView noTaskIcon;
    private TextView noTaskMainView;
    private TextView noTaskAddView;
    private LinearLayout tasksView;
    private TextView filteringLabelView;
    private ScrollChildSwipeRefreshLayout swipeRefreshLayout;
    private PublishSubject<TasksIntent.RefreshIntent> refreshIntentPublisher = PublishSubject.create();
    private PublishSubject<TasksIntent.ClearCompletedTasksIntent> clearCompletedTasksIntentPublisher = PublishSubject.create();
    private PublishSubject<TasksIntent.ChangeFilterIntent> changeFilterIntentPublisher = PublishSubject.create();
    private CompositeDisposable disposables = new CompositeDisposable();

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listAdapter = new TasksAdapter(new ArrayList<>(0));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, ToDoViewModelFactory.getInstance(getContext())).get(TasksViewModel.class);
        bind();
    }

    private void bind() {
        disposables.add(viewModel.states().subscribe(this::render));
        viewModel.processIntents(intents());
        disposables.add(listAdapter.getTaskClickObservable().subscribe(task -> showTaskDetailsUi(task.getId())));
    }

    private void showTaskDetailsUi(String taskId) {
        Intent intent = new Intent(getContext(), TaskDetailActivity.class);
        intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId);
        startActivity(intent);
    }

    @Override
    public Observable<TasksIntent> intents() {
        return null;
    }

    @Override
    public void render(TasksViewState state) {

    }
}
