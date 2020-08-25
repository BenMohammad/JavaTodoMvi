package com.benmohammad.javatodomvi.addedittask;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.benmohammad.javatodomvi.R;
import com.benmohammad.javatodomvi.util.ActivityUtils;

public class AddEditTaskActivity extends AppCompatActivity {
    public static final int REQUEST_ADD_TASK = 1;

    private ActionBar actionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addtask_act);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        AddEditTaskFragment addEditTaskFragment = (AddEditTaskFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        String taskId = getIntent().getStringExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID);
        setToolbarTitle(taskId);

        if(addEditTaskFragment == null) {
            addEditTaskFragment = AddEditTaskFragment.newInstance();

            if(taskId != null) {
                Bundle args = new Bundle();
                args.putString(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID, taskId);
                addEditTaskFragment.setArguments(args);
            }

            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), addEditTaskFragment, R.id.contentFrame);
        }
    }

    private void setToolbarTitle(@Nullable String taskId) {
        if(taskId == null) {
            actionBar.setTitle(R.string.add_task);
        } else {
            actionBar.setTitle(R.string.edit_task);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
