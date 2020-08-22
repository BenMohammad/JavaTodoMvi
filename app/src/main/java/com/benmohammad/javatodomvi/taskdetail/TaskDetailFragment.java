package com.benmohammad.javatodomvi.taskdetail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.benmohammad.javatodomvi.R;
import com.benmohammad.javatodomvi.addedittask.AddEditTaskActivity;
import com.benmohammad.javatodomvi.addedittask.AddEditTaskFragment;
import com.benmohammad.javatodomvi.data.Task;
import com.benmohammad.javatodomvi.mvibase.MviView;
import com.benmohammad.javatodomvi.util.ToDoViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.subjects.PublishSubject;

public class TaskDetailFragment extends Fragment implements MviView<TaskDetailIntent, TaskDetailViewState> {

    @Nonnull
    private static final String ARGUMENT_TASK_ID = "TASK_ID";

    @Nonnull
    private static final int REQUEST_EDIT_TASK = 1;

    private TextView detailTitle;
    private TextView detailDescription;
    private CheckBox detailCompleteStatus;
    private FloatingActionButton fab;

    private TaskDetailViewModel viewModel;

    private CompositeDisposable disposables = new CompositeDisposable();
    private PublishSubject<TaskDetailIntent.DeleteTask> deleteTaskPublisher = PublishSubject.create();

    public static TaskDetailFragment newInstance(@Nullable String taskId) {
        Bundle bundle = new Bundle();
        bundle.putString(ARGUMENT_TASK_ID, taskId);
        TaskDetailFragment fragment = new TaskDetailFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @androidx.annotation.Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container, @androidx.annotation.Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.taskdetail_frag, container, false);
        setHasOptionsMenu(true);
        detailTitle = root.findViewById(R.id.task_detail_title);
        detailDescription = root.findViewById(R.id.task_detail_description);
        detailCompleteStatus = root.findViewById(R.id.task_detail_complete);

        fab = getActivity().findViewById(R.id.fab_edit_task);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, ToDoViewModelFactory.getInstance(getContext())).get(TaskDetailViewModel.class);
        disposables = new CompositeDisposable();
        bind();
    }

    private void bind() {
        disposables.add(viewModel.states().subscribe(this::render));
        viewModel.processIntents(intents());
        RxView.clicks(fab).debounce(200, TimeUnit.MILLISECONDS)
                .subscribe(view -> showEditTask(getArgumentTaskId()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.dispose();
    }



    @Override
    public Observable<TaskDetailIntent> intents() {
        return Observable.merge(initialIntent(), checkBoxIntents(), deleteIntent());
    }

    private Observable<TaskDetailIntent.InitialIntent> initialIntent() {
        return Observable.just(TaskDetailIntent.InitialIntent.create(getArgumentTaskId()));
    }

    private Observable<TaskDetailIntent> checkBoxIntents() {
        return RxView.clicks(detailCompleteStatus).map(
                activated -> {
                    if(detailCompleteStatus.isChecked()) {
                        return TaskDetailIntent.CompleteTaskIntent.create(getArgumentTaskId());
                    } else {
                        return TaskDetailIntent.ActivateTaskIntent.create(getArgumentTaskId());
                    }
                }
        );
    }

    private Observable<TaskDetailIntent.DeleteTask> deleteIntent() {
        return deleteTaskPublisher;
    }


    @Nullable
    private String getArgumentTaskId() {
        Bundle args = getArguments();
        if(args == null) return null;
        return args.getString(ARGUMENT_TASK_ID);
    }

    @Override
    public void render(TaskDetailViewState state) {
        setLoadingIndicator(state.loading());
        if(!state.title().isEmpty()) {
            showTitle(state.title());
        } else {
            hideTitle();
        }

        if(!state.description().isEmpty()) {
            showDescription(state.description());
        } else {
            hideDescription();
        }

        showActive(state.active());

        if(state.taskComplete()) {
            showTaskMarkedComplete();
        }

        if(state.taskActivated()) {
            showTaskMarkedActive();
        }

        if(state.taskDeleted()) {
            getActivity().finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_delete:
                deleteTaskPublisher.onNext(TaskDetailIntent.DeleteTask.create(getArgumentTaskId()));
                return true;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.taskdetail_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        if(requestCode == REQUEST_EDIT_TASK) {
            if(resultCode == Activity.RESULT_OK) {
                getActivity().finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setLoadingIndicator(boolean active) {
        if(active) {
            detailTitle.setText("");
            detailDescription.setText(getString(R.string.loading));
        }
    }

    public void hideTitle() {
        detailTitle.setVisibility(View.GONE);
    }

    public void hideDescription() {
        detailDescription.setVisibility(View.GONE);
    }

    public void showActive(boolean isActive) {
        detailCompleteStatus.setChecked(!isActive);
    }

    public void showDescription(@Nonnull String description) {
        detailDescription.setVisibility(View.VISIBLE);
        detailDescription.setText(description);
    }

    public void showTitle(@Nonnull String title) {
        detailTitle.setVisibility(View.VISIBLE);
        detailTitle.setText(title);
    }


    public void showTaskMarkedComplete() {
        Snackbar.make(getView(), getString(R.string.task_marked_complete), Snackbar.LENGTH_SHORT).show();
    }


    public void showTaskMarkedActive() {
        Snackbar.make(getView(), getString(R.string.task_marked_active), Snackbar.LENGTH_SHORT).show();
    }


    public void showMissingTask() {
        detailTitle.setText("");
        detailDescription.setText(getString(R.string.no_data));
    }

    private void showEditTask(@Nonnull String taskId) {
        Intent intent = new Intent(getContext(), AddEditTaskActivity.class);
        intent.putExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId);
        startActivityForResult(intent, REQUEST_EDIT_TASK);
    }
}
