package com.quicksoft.school.activity.teacher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;
import com.leo.simplearcloader.SimpleArcLoader;
import com.libRG.CustomTextView;
import com.quicksoft.school.Application;
import com.quicksoft.school.R;
import com.quicksoft.school.activity.login.LoginActivity;
import com.quicksoft.school.activity.parent.MainParentActivity;
import com.quicksoft.school.adapter.GroupAttendanceAdapter;
import com.quicksoft.school.adapter.SummeryAttendanceAdapter;
import com.quicksoft.school.connection.SyncManager;
import com.quicksoft.school.connection.callback.AttendanceCallback;
import com.quicksoft.school.connection.callback.SyncCompleteCallback;
import com.quicksoft.school.fragment.parent.DashboardFragmentParent;
import com.quicksoft.school.model.AttendanceStudent;
import com.quicksoft.school.model.ScheduleTeacher;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.quicksoft.school.util.TimeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

public class GroupAttendanceTeacherActivity extends AppCompatActivity  implements SyncCompleteCallback, AttendanceCallback, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Toolbar mToolbar;
    private TextView tvName, tvLate, tvPresent, tvDate, tvClass;
    private FancyButton btnSubmit;
    private RecyclerView mRecyclerView;
    private AppCompatCheckBox cBoxAllPresent;

    private ArrayList<AttendanceStudent> attendanceStudentArrayList;
    private GroupAttendanceAdapter groupAttendanceAdapter;

    private SimpleArcDialog pDialog;
    private SyncManager mSyncManager;

    private String classs, section;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_group_attendance);
        setupToolbar();

        tvName = findViewById(R.id.tvName);
        tvLate = findViewById(R.id.tvLate);
        tvPresent = findViewById(R.id.tvPresent);
        tvDate = findViewById(R.id.tvDate);
        tvClass = findViewById(R.id.tvClass);
        btnSubmit = findViewById(R.id.btnSubmit);
        cBoxAllPresent = findViewById(R.id.cBoxAllPresent);

        classs = getIntent().getStringExtra("CLASS");
        section = getIntent().getStringExtra("SECTION");
        tvClass.setText(classs+" - "+ section);
        tvDate.setText(getIntent().getStringExtra("DATE"));

        btnSubmit.setOnClickListener(this);
        cBoxAllPresent.setOnCheckedChangeListener(this);
        getTeacherDataFromServer();
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
                    ArrayList<AttendanceStudent> data = groupAttendanceAdapter.getStudentArray();
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

    public void getTeacherDataFromServer(){

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
            mSyncManager.teacherAttendanceStudentList(email, uniqueID, classs, section, date);
        }else{
            Toasty.error(this,"Check your internet connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSyncComplete(int syncPage, int response, Object data) {
        if(syncPage==Constant.SYNC_TEACHER_STUDENT_ATTENDANCE_LIST){
            if(response == Constant.SUCCESS) {
                LogUtils.i(((JSONObject)data).toString());
                attendanceStudentArrayList = new ArrayList<>();
                attendanceStudentArrayList.clear();
                int present=0;
                int absent =0;
                try {
                    JSONObject jsonObject = (JSONObject)data;
                    JSONArray studentRollArray = jsonObject.getJSONArray("StudentRollList");
                    JSONArray array = jsonObject.getJSONArray("StudentInfoList");
                    for (int j=0; j<studentRollArray.length(); j++) {
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            int RollNo = object.getInt("RollNo");
                            if (studentRollArray.getInt(j) == RollNo) {
                                String personId = object.getString("PersonId");
                                String studentID = object.getString("StudentID");
                                String Lname = object.getString("StudentLName");
                                String Mname = object.getString("StudentMName");
                                String Fname = object.getString("StudentFName");
                                String remark = object.getString("Remark");


                                String name = "";
                                if (Mname.compareTo("") == 0)
                                    name = Fname + " " + Lname;
                                else
                                    name = Fname + " " + Mname + " " + Lname;

                                if(remark.compareTo("P")==0) {
                                    present++;
                                }else if(remark.compareTo("A")==0) {
                                    absent++;
                                }

                                String imageUrl = Constant.SERVER_BASE_ADDRESS + "api/quicksoftuser/personimage?personId=" + personId + "&ext=png";

                                AttendanceStudent attendanceStudent = new AttendanceStudent(RollNo, name, imageUrl, studentID, remark);
                                attendanceStudentArrayList.add(attendanceStudent);
                            }
                        }
                    }

                    tvPresent.setText(""+present);
                    tvLate.setText(""+absent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                pDialog.dismiss();
            }else if(response == Constant.FAIL){
                int respCode =  (int)data;
                if(respCode ==401 || respCode ==403){
                    GlobalPreferenceManager.setUserLoggedIn(false);
                    GlobalPreferenceManager.setUserType(-1);
                    GlobalPreferenceManager.setLoginType(-1);
                    Toasty.error(this,"Please login again..", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(GroupAttendanceTeacherActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }else if(syncPage==Constant.SYNC_TEACHER_STUDENT_ATTENDANCE){
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
        setupListView();
    }

    public void setupListView(){
        mRecyclerView = findViewById(R.id.listView);
        groupAttendanceAdapter = new GroupAttendanceAdapter(this, attendanceStudentArrayList, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(groupAttendanceAdapter);

    }

    public void setupToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mToolbarTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mToolbarTitle.setText("Attendance");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.teacher_attendance_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }else if(menuItem.getItemId() == R.id.menuSummery){
            ArrayList<AttendanceStudent> data = groupAttendanceAdapter.getStudentArray();
            ((Application)getApplication()).setStudentAttendanceData(data);
            Intent intent = new Intent(GroupAttendanceTeacherActivity.this, SummeryAttendanceTeacherActivity.class);
            intent.putExtra("CLASS", tvClass.getText().toString());
            intent.putExtra("DATE", tvDate.getText().toString());
            startActivityForResult(intent, Constant.TEACHER_ATTENDANCE_SUMMERY_REQUEST_CODE);
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public void onAttandance(int present, int absent) {
        tvPresent.setText(""+present);
        tvLate.setText(""+absent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Constant.TEACHER_ATTENDANCE_SUMMERY_REQUEST_CODE){
            ArrayList<AttendanceStudent> data1 = ((Application)getApplication()).getStudentAttendanceData();
            attendanceStudentArrayList = data1;
            groupAttendanceAdapter = new GroupAttendanceAdapter(this, attendanceStudentArrayList, this);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setAdapter(groupAttendanceAdapter);
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

        groupAttendanceAdapter = new GroupAttendanceAdapter(this, attendanceStudentArrayList, this);
        mRecyclerView.setAdapter(groupAttendanceAdapter);
        mRecyclerView.invalidate();
    }
}
