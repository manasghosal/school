package com.quicksoft.school.activity.teacher;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;
import com.leo.simplearcloader.SimpleArcLoader;
import com.quicksoft.school.Application;
import com.quicksoft.school.R;
import com.quicksoft.school.adapter.GroupAttendanceAdapter;
import com.quicksoft.school.adapter.SummeryAttendanceAdapter;
import com.quicksoft.school.connection.SyncManager;
import com.quicksoft.school.connection.callback.AttendanceCallback;
import com.quicksoft.school.connection.callback.SyncCompleteCallback;
import com.quicksoft.school.model.AttendanceStudent;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.quicksoft.school.util.TimeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

public class SummeryAttendanceTeacherActivity extends AppCompatActivity implements  SyncCompleteCallback, AttendanceCallback, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Toolbar mToolbar;
    private TextView tvName, tvLate, tvPresent, tvDate, tvClass;
    private RecyclerView mRecyclerView;
    private FancyButton btnSubmit;
    private AppCompatCheckBox cBoxAllPresent;

    private ArrayList<AttendanceStudent> attendanceStudentArrayList;
    private SummeryAttendanceAdapter summeryAttendanceAdapter;

    private SimpleArcDialog pDialog;
    private SyncManager mSyncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_summery_attendance);
        setupToolbar();

        tvName = findViewById(R.id.tvName);
        tvLate = findViewById(R.id.tvLate);
        tvPresent = findViewById(R.id.tvPresent);
        tvDate = findViewById(R.id.tvDate);
        tvClass = findViewById(R.id.tvClass);
        btnSubmit = findViewById(R.id.btnSubmit);
        cBoxAllPresent = findViewById(R.id.cBoxAllPresent);

        tvClass.setText(getIntent().getStringExtra("CLASS"));
        tvDate.setText(getIntent().getStringExtra("DATE"));

        btnSubmit.setOnClickListener(this);
        cBoxAllPresent.setOnCheckedChangeListener(this);
        setupListView();

    }

    public void setupListView(){
        ArrayList<AttendanceStudent> data = ((Application)getApplication()).getStudentAttendanceData();
        ArrayList<AttendanceStudent> data1 = new ArrayList<>();
        int late=0;
        int absent =0;
        for(int i =0; i<data.size(); i++){
            String remark = data.get(i).getRemark();
            if(remark.compareTo("P")!=0){
                data1.add(data.get(i));
            }

            if(remark.compareTo("A")==0) {
                absent++;
            }else if(remark.compareTo("L")==0) {
                late++;
            }
        }

        tvPresent.setText(""+late);
        tvLate.setText(""+absent);

        attendanceStudentArrayList = data1;

        mRecyclerView = findViewById(R.id.listView);
        summeryAttendanceAdapter = new SummeryAttendanceAdapter(this, attendanceStudentArrayList, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(summeryAttendanceAdapter);

    }

    public void setupToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mToolbarTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mToolbarTitle.setText("Summery Attendance");

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

    @Override
    public void onAttandance(int present, int absent) {
        tvPresent.setText(""+present);
        tvLate.setText(""+absent);
    }

    @Override
    public void onClick(View view) {
        if(view == btnSubmit){

            if(NetworkUtils.isConnected()) {
                mSyncManager = new SyncManager(this, this);

                pDialog = new SimpleArcDialog(this);
                ArcConfiguration configuration = new ArcConfiguration(this);
                configuration.setLoaderStyle(SimpleArcLoader.STYLE.COMPLETE_ARC);
                configuration.setText("Please wait..");
                pDialog.setConfiguration(configuration);
                pDialog.setCancelable(false);
                pDialog.show();
                String email = GlobalPreferenceManager.getUserEmail();
                String uniqueID = GlobalPreferenceManager.getUniqueId();
                LogUtils.i(email + " " + uniqueID);
                String date = TimeUtil.getReverseFormattedDate(tvDate.getText().toString());

                try {
                    ArrayList<AttendanceStudent> data = summeryAttendanceAdapter.getStudentArray();
                    JSONArray jsonArray = new JSONArray();
                    for(int i = 0; i<data.size(); i++){
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("StudentId", data.get(i).getStudentId());
                        jsonObject.put("AttendanceDate", date);
                        jsonObject.put("Remark", data.get(i).getRemark());
                        jsonArray.put(jsonObject);
                    }

                    mSyncManager.teacherPostAttendance(email, uniqueID, jsonArray);

                } catch (JSONException e) {
                    e.printStackTrace();
                    pDialog.dismiss();
                }

            }else{
                Toasty.error(this,"Check your internet connection", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onSyncComplete(int syncPage, int response, Object data) {
        if(syncPage==Constant.SYNC_TEACHER_STUDENT_ATTENDANCE){
            if(response == Constant.SUCCESS) {
                LogUtils.i(((JSONObject) data).toString());
                Toasty.info(this,"Attendance submitted successfully", Toast.LENGTH_LONG).show();
                finish();
                pDialog.dismiss();
            }
        }

        if(response == Constant.FAIL || response == Constant.NETWORK_FAIL){
            pDialog.dismiss();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b){
            for(int i=0; i<attendanceStudentArrayList.size(); i++){
                attendanceStudentArrayList.get(i).setRemark("P");
            }
        }else{
            for(int i=0; i<attendanceStudentArrayList.size(); i++){
                attendanceStudentArrayList.get(i).setRemark("A");
            }
        }

        summeryAttendanceAdapter = new SummeryAttendanceAdapter(this, attendanceStudentArrayList, this);
        mRecyclerView.setAdapter(summeryAttendanceAdapter);
        mRecyclerView.invalidate();
    }

}
