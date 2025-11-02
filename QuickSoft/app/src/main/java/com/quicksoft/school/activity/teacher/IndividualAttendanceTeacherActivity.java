package com.quicksoft.school.activity.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.chaos.view.PinView;
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;
import com.leo.simplearcloader.SimpleArcLoader;
import com.libRG.CustomTextView;
import com.quicksoft.school.Application;
import com.quicksoft.school.R;
import com.quicksoft.school.activity.driver.MainDriverActivity;
import com.quicksoft.school.activity.login.LoginActivity;
import com.quicksoft.school.activity.parent.MainParentActivity;
import com.quicksoft.school.adapter.GroupAttendanceAdapter;
import com.quicksoft.school.adapter.IndiAttendanceAdapter;
import com.quicksoft.school.connection.SyncManager;
import com.quicksoft.school.connection.callback.AttendanceCallback;
import com.quicksoft.school.connection.callback.SyncCompleteCallback;
import com.quicksoft.school.model.AttendanceStudent;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.quicksoft.school.util.TimeUtil;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.InfiniteScrollAdapter;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

public class IndividualAttendanceTeacherActivity extends AppCompatActivity implements View.OnClickListener, AttendanceCallback, SyncCompleteCallback, DiscreteScrollView.OnItemChangedListener<RecyclerView.ViewHolder> {

    private Toolbar mToolbar;
//    private FancyButton btnLate, btnPresent;
    private TextView tvName, tvLate, tvPresent, tvDate, tvClass;
    private DiscreteScrollView discreteScrollView;
    private CustomTextView tvRollNo;
    private FancyButton btnSubmit;

    private ArrayList<AttendanceStudent> attendanceStudentArrayList;
    private InfiniteScrollAdapter infiniteAdapter;
    private IndiAttendanceAdapter indiAttendanceAdapter;

    private SimpleArcDialog pDialog;
    private SyncManager mSyncManager;

    private String classs, section;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_indi_attendance);
        setupToolbar();

        tvName = findViewById(R.id.tvName);
        tvLate = findViewById(R.id.tvLate);
        tvPresent = findViewById(R.id.tvPresent);
        tvDate = findViewById(R.id.tvDate);
        tvClass = findViewById(R.id.tvClass);
        discreteScrollView = findViewById(R.id.discreteScrollView);
        tvRollNo = findViewById(R.id.tvRollNo);
        btnSubmit = findViewById(R.id.btnSubmit);
//        btnPresent = findViewById(R.id.btnPresent);
//        btnLate = findViewById(R.id.btnLate);
//
//        btnPresent.setOnClickListener(this);
//        btnLate.setOnClickListener(this);

        classs = getIntent().getStringExtra("CLASS");
        section = getIntent().getStringExtra("SECTION");
        tvClass.setText(classs+" - "+ section);
        tvDate.setText(getIntent().getStringExtra("DATE"));

        btnSubmit.setOnClickListener(this);
        getTeacherDataFromServer();
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
                    startActivity(new Intent(IndividualAttendanceTeacherActivity.this, LoginActivity.class));
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
        setupViewPager();
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
                    ArrayList<AttendanceStudent> data = indiAttendanceAdapter.getStudentArray();
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

    public void setupViewPager(){
        discreteScrollView.addOnItemChangedListener(this);
        indiAttendanceAdapter = new IndiAttendanceAdapter(this, attendanceStudentArrayList, this);
        infiniteAdapter = InfiniteScrollAdapter.wrap(indiAttendanceAdapter);
        discreteScrollView.setAdapter(infiniteAdapter);
        //discreteScrollView.setItemTransitionTimeMillis(DiscreteScrollViewOptions.getTransitionTime());
        discreteScrollView.setItemTransformer(new ScaleTransformer.Builder()
                .setMinScale(0.8f)
                .build());
//        onItemChanged(attendanceStudentArrayList.get(0));
    }

    @Override
    public void onCurrentItemChanged(@Nullable RecyclerView.ViewHolder viewHolder, int position) {
        int positionInDataSet = infiniteAdapter.getRealPosition(position);
        onItemChanged(attendanceStudentArrayList.get(positionInDataSet));
    }

    private void onItemChanged(AttendanceStudent item) {
        tvName.setText(item.getName());
        tvRollNo.setText(""+item.getRollNo());
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
            ArrayList<AttendanceStudent> data = indiAttendanceAdapter.getStudentArray();
            ((Application)getApplication()).setStudentAttendanceData(data);
            Intent intent = new Intent(IndividualAttendanceTeacherActivity.this, SummeryAttendanceTeacherActivity.class);
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

            indiAttendanceAdapter = new IndiAttendanceAdapter(this, attendanceStudentArrayList, this);
            infiniteAdapter = InfiniteScrollAdapter.wrap(indiAttendanceAdapter);
            discreteScrollView.setAdapter(infiniteAdapter);
        }
    }
}
