package com.quicksoft.school.activity.driver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.quicksoft.school.R;
import com.quicksoft.school.adapter.TripAttendanceAdapter;
import com.quicksoft.school.model.Passanger;

import java.util.ArrayList;

public class TripAttendanceDriverActivity extends AppCompatActivity  {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;

    private ArrayList<Passanger> passangertArrayList;
    private TripAttendanceAdapter tripAttendanceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_trip_attendance);
        setupToolbar();

        passangertArrayList = getIntent().getExtras().getParcelableArrayList("PASSANGERLIST");
        setupListView();

    }

    public void setupListView(){
        mRecyclerView = findViewById(R.id.listView);
        tripAttendanceAdapter = new TripAttendanceAdapter(this, passangertArrayList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(tripAttendanceAdapter);

    }

    public void setupToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mToolbarTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mToolbarTitle.setText("Passanger");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
