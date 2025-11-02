package com.quicksoft.school.activity.teacher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;
import com.leo.simplearcloader.SimpleArcLoader;
import com.quicksoft.school.R;
import com.quicksoft.school.activity.ProfileActivity;
import com.quicksoft.school.activity.login.LoginActivity;
import com.quicksoft.school.activity.parent.MainParentActivity;
import com.quicksoft.school.connection.SyncManager;
import com.quicksoft.school.connection.callback.SyncCompleteCallback;
import com.quicksoft.school.fragment.parent.DashboardFragmentParent;
import com.quicksoft.school.fragment.teacher.AttendanceFragmentTeacher;
import com.quicksoft.school.fragment.teacher.DashboardFragmentTeacher;
import com.quicksoft.school.fragment.teacher.NoticeFragmentTeacher;
import com.quicksoft.school.fragment.teacher.TaskFragmentTeacher;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;

import org.json.JSONObject;

import br.liveo.interfaces.OnItemClickListener;
import br.liveo.interfaces.OnPrepareOptionsMenuLiveo;
import br.liveo.model.HelpLiveo;
import br.liveo.navigationliveo.NavigationLiveo;
import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;
import es.dmoral.toasty.Toasty;

public class MainTeacherActivity extends NavigationLiveo implements OnItemClickListener, SyncCompleteCallback {

    private HelpLiveo mHelpLiveo;
    public TextView mToolbarTitle;

    private SimpleArcDialog pDialog;
    private SyncManager mSyncManager;
    private Fragment mFragment;

    @Override
    public void onInt(Bundle savedInstanceState) {

        View mCustomHeader = getLayoutInflater().inflate(R.layout.drawer_header, this.getListView(), false);
        ShapedImageView imgProfile = mCustomHeader.findViewById(R.id.imgProfile);
        TextView tvName =  mCustomHeader.findViewById(R.id.tvName);
        imgProfile.setImageResource(R.drawable.ico_user_placeholder);

        mHelpLiveo = new HelpLiveo();
        mHelpLiveo.add(getString(R.string.dashboard), R.drawable.ic_dashboard_teal_700_24dp);
        mHelpLiveo.add(getString(R.string.attendance), R.drawable.ic_assignment_teal_700_24dp);
        mHelpLiveo.add(getString(R.string.task), R.drawable.ic_book_teal_700_24dp);
        mHelpLiveo.add(getString(R.string.notice), R.drawable.ic_assignment_late_teal_700_24dp);
        //mHelpLiveo.add(getString(R.string.jobs), R.drawable.ic_work_teal_700_24dp);


        with(this).startingPosition(0)
                .addAllHelpItem(mHelpLiveo.getHelp())
                .colorItemSelected(R.color.nliveo_blue_colorPrimary)
                .footerItem(R.string.logout, R.drawable.ic_power_settings_new_teal_700_24dp)
                .customHeader(mCustomHeader)
                .footerBackground(R.color.almostBlack38)
                .setOnPrepareOptionsMenu(onPrepare)
                .setOnClickFooter(onClickFooter)
                .build();

        int position = this.getCurrentPosition();
        this.setElevationToolBar(position != 2 ? 15 : 0);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        View toolBar = getLayoutInflater().inflate(R.layout.drawer_toolbar, null);
        mToolbarTitle = (TextView) toolBar.findViewById(R.id.toolbarTitle);
        getToolbar().addView(toolBar);
        setupProfileImage(imgProfile, tvName);

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
            mSyncManager.teacherDashBoard(email, uniqueID);
        }else{
            Toasty.error(this,"Check your internet connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSyncComplete(int syncPage, int response, Object data) {
        if(syncPage==Constant.SYNC_TEACHER_DASHBOARD){
            if(response == Constant.SUCCESS) {
                GlobalPreferenceManager.saveTeacherDashBoardInfo(((JSONObject)data).toString());
                LogUtils.i(GlobalPreferenceManager.getTeacherDashBoardInfo());
                mSyncManager.teacherGetClasses(GlobalPreferenceManager.getUserEmail(), GlobalPreferenceManager.getUniqueId());
            }else if(response == Constant.FAIL){
                int respCode =  (int)data;
                if(respCode ==401 || respCode ==403){
                    GlobalPreferenceManager.setUserLoggedIn(false);
                    GlobalPreferenceManager.setUserType(-1);
                    GlobalPreferenceManager.setLoginType(-1);
                    Toasty.error(this,"Please login again..", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MainTeacherActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }else if(syncPage==Constant.SYNC_TEACHER_CLASSES){
            if(response == Constant.SUCCESS) {
                GlobalPreferenceManager.saveTeacherClasses(((JSONObject)data).toString());
                LogUtils.i(GlobalPreferenceManager.getTeacherClasses());

                if(mFragment instanceof DashboardFragmentTeacher) {
                    ((DashboardFragmentTeacher)mFragment).getDataFromJSON();
                }

                pDialog.dismiss();
            }
        }

        if(response == Constant.FAIL || response == Constant.NETWORK_FAIL){
            pDialog.dismiss();
        }

    }


    public void setupProfileImage(ShapedImageView imgProfile, TextView tvName){
        if(GlobalPreferenceManager.getLoginType()== Constant.LOGIN_NORMAL) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.ico_user_placeholder);
            requestOptions.signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));
            Glide.with(this).setDefaultRequestOptions(requestOptions).load(GlobalPreferenceManager.getProfilePic()).into(imgProfile);

            tvName.setText(GlobalPreferenceManager.getUserFullName());
            imgProfile.setOnClickListener(onClickPhoto);
        }else if(GlobalPreferenceManager.getLoginType()== Constant.LOGIN_WITH_FACEBOOK) {
            Glide.with(this).load(GlobalPreferenceManager.getFBProfileURL()).into(imgProfile);
            tvName.setText(GlobalPreferenceManager.getFBName());
            imgProfile.setOnClickListener(null);
        }else if(GlobalPreferenceManager.getLoginType()== Constant.LOGIN_WITH_GMAIL) {
            Glide.with(this).load(GlobalPreferenceManager.getGoogleProfileURL()).into(imgProfile);
            tvName.setText(GlobalPreferenceManager.getGoogelName());
            imgProfile.setOnClickListener(null);
        }
    }


    @Override
    public void onItemClick(int position) {
        mFragment = null;
        FragmentManager mFragmentManager = getSupportFragmentManager();
        switch (position){
            case Constant.TEACHER_DASHBOARD:
                mToolbarTitle.setText(getString(R.string.dashboard));
                mFragment = DashboardFragmentTeacher.newInstance(mHelpLiveo.get(position).getName());
                break;
            case Constant.TEACHER_ATTENDANCE:
                mToolbarTitle.setText(getString(R.string.attendance));
                mFragment = AttendanceFragmentTeacher.newInstance(mHelpLiveo.get(position).getName());
                break;
            case Constant.TEACHER_TASK:
                mToolbarTitle.setText(getString(R.string.task));
                mFragment = TaskFragmentTeacher.newInstance(mHelpLiveo.get(position).getName());
                break;
            case Constant.TEACHER_NOTICE:
                mToolbarTitle.setText(getString(R.string.notice));
                mFragment = NoticeFragmentTeacher.newInstance(mHelpLiveo.get(position).getName());
                break;
            case Constant.TEACHER_JOBS:
                mToolbarTitle.setText(getString(R.string.jobs));
                break;
            default:
                mFragment = DashboardFragmentTeacher.newInstance(mHelpLiveo.get(position).getName());
                break;
        }
        if (mFragment != null){
            mFragmentManager.beginTransaction().replace(R.id.container, mFragment).commit();
        }
        setElevationToolBar(position != 2 ? 15 : 0);
    }

    private OnPrepareOptionsMenuLiveo onPrepare = new OnPrepareOptionsMenuLiveo() {
        @Override
        public void onPrepareOptionsMenu(Menu menu, int position, boolean visible) {
        }
    };

    private View.OnClickListener onClickPhoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(GlobalPreferenceManager.getLoginType()==0) {
                startActivity(new Intent(MainTeacherActivity.this, ProfileActivity.class));
                closeDrawer();
            }
        }
    };

    private View.OnClickListener onClickFooter;{
        onClickFooter = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalPreferenceManager.setUserLoggedIn(false);
                GlobalPreferenceManager.setUserType(-1);
                GlobalPreferenceManager.setLoginType(-1);
                startActivity(new Intent(MainTeacherActivity.this, LoginActivity.class));
                finish();
            }
        };
    }
}
