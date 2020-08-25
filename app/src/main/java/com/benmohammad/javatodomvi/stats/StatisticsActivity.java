package com.benmohammad.javatodomvi.stats;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.benmohammad.javatodomvi.R;
import com.benmohammad.javatodomvi.util.ActivityUtils;
import com.google.android.material.navigation.NavigationView;

public class StatisticsActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistics_act);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setTitle(R.string.statistics_title);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_menu);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.setStatusBarBackground(R.color.colorPrimaryDark);
        NavigationView navigationView = findViewById(R.id.nav_view);
        if(navigationView != null) {
            setUpDrawerContent(navigationView);
        }

        StatisticsFragment statisticsFragment = (StatisticsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if(statisticsFragment == null) {
            statisticsFragment = StatisticsFragment.newInstance();
            ActivityUtils.addFragmentToActivity(getSupportFragmentManager(), statisticsFragment, R.id.contentFrame);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch(menuItem.getItemId()) {
                case R.id.list_navigation_menu_item:
                    NavUtils.navigateUpFromSameTask(StatisticsActivity.this);
                    break;
                case R.id.statistics_navigation_menu_item:
                    break;
                default:
                    break;
            }

            menuItem.setChecked(true);
            drawerLayout.closeDrawers();
            return true;
        });
    }
}
