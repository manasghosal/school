package com.quicksoft.school.activity.parent;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
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
import com.quicksoft.school.activity.login.OTPActivity;
import com.quicksoft.school.activity.teacher.MainTeacherActivity;
import com.quicksoft.school.connection.SyncManager;
import com.quicksoft.school.connection.callback.SyncCompleteCallback;
import com.quicksoft.school.fragment.parent.AboutFragmentParent;
import com.quicksoft.school.fragment.parent.AttendanceFragmentParent;
import com.quicksoft.school.fragment.parent.DashboardFragmentParent;
import com.quicksoft.school.fragment.parent.FacultyFragmentParent;
import com.quicksoft.school.fragment.parent.GvtBodyFragmentParent;
import com.quicksoft.school.fragment.parent.MarksheetFragmentParent;
import com.quicksoft.school.fragment.parent.NoticeFragmentParent;
import com.quicksoft.school.fragment.parent.PaymentFragmentParent;
import com.quicksoft.school.fragment.parent.TaskFragmentParent;
import com.quicksoft.school.fragment.parent.TrackDriverFragmentParent;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;

import org.json.JSONObject;

import br.liveo.interfaces.OnItemClickListener;
import br.liveo.interfaces.OnPrepareOptionsMenuLiveo;
import br.liveo.model.HelpLiveo;
import br.liveo.navigationliveo.NavigationLiveo;
import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;
import es.dmoral.toasty.Toasty;

public class MainParentActivity extends NavigationLiveo implements OnItemClickListener, SyncCompleteCallback {

    private HelpLiveo mHelpLiveo;
    public TextView mToolbarTitle;

    private SimpleArcDialog pDialog;
    private SyncManager mSyncManager;

    private Fragment mFragment = null;

    @Override
    public void onInt(Bundle savedInstanceState) {

        View mCustomHeader = getLayoutInflater().inflate(R.layout.drawer_header, this.getListView(), false);
        ShapedImageView imgProfile = mCustomHeader.findViewById(R.id.imgProfile);
        TextView tvName =  mCustomHeader.findViewById(R.id.tvName);
        imgProfile.setImageResource(R.drawable.ico_user_placeholder);

        mHelpLiveo = new HelpLiveo();
        mHelpLiveo.add(getString(R.string.dashboard), R.drawable.ic_dashboard_teal_700_24dp);
        mHelpLiveo.add(getString(R.string.attendance), R.drawable.ic_assignment_teal_700_24dp); //Item subHeader
        mHelpLiveo.add(getString(R.string.payment), R.drawable.ic_payment_teal_700_24dp);
        mHelpLiveo.add(getString(R.string.track), R.drawable.ic_explore_teal_700_24dp);
        mHelpLiveo.add(getString(R.string.task), R.drawable.ic_book_teal_700_24dp);
        mHelpLiveo.add(getString(R.string.notice), R.drawable.ic_description_teal_700_24dp);
        mHelpLiveo.add(getString(R.string.marksheet), R.drawable.icmarksheet_teal_700_24dp);
        mHelpLiveo.add(getString(R.string.govbody), R.drawable.ic_perm_contact_calendar_teal_700_24dp);
        mHelpLiveo.add(getString(R.string.faculty), R.drawable.ic_supervisor_account_teal_700_24dp);
        mHelpLiveo.add(getString(R.string.about), R.drawable.ic_turned_in_teal_700_24dp);

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
        mToolbarTitle = toolBar.findViewById(R.id.toolbarTitle);
        getToolbar().addView(toolBar);

        setupProfileImage(imgProfile, tvName);

        getParentDataFromServer();
    }

    public void getParentDataFromServer(){

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
            mSyncManager.parentDashBoard(email, uniqueID);
        }else{
            Toasty.error(this,"Check your internet connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSyncComplete(int syncPage, int response, Object data) {
        if(syncPage==Constant.SYNC_PARENT_DASHBOARD){
            if(response == Constant.SUCCESS) {
                GlobalPreferenceManager.saveParentDashBoardInfo(((JSONObject)data).toString());
                LogUtils.i(GlobalPreferenceManager.getParentDashBoardInfo());
                mSyncManager.parentAttendance(GlobalPreferenceManager.getUserEmail(), GlobalPreferenceManager.getUniqueId());
            }else if(response == Constant.FAIL){
                int respCode =  (int)data;
                if(respCode ==401 || respCode ==403){
                    GlobalPreferenceManager.setUserLoggedIn(false);
                    GlobalPreferenceManager.setUserType(-1);
                    GlobalPreferenceManager.setLoginType(-1);
                    Toasty.error(this,"Please login again..", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MainParentActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }else if(syncPage==Constant.SYNC_PARENT_ATTENDANCE){
            if(response == Constant.SUCCESS) {
                GlobalPreferenceManager.saveParentAttendance(((JSONObject)data).toString());
                LogUtils.i(GlobalPreferenceManager.getParentAttendance());
                mSyncManager.parentTasks(GlobalPreferenceManager.getUserEmail(), GlobalPreferenceManager.getUniqueId());
            }
        }
        else if(syncPage==Constant.SYNC_PARENT_TASK){
            if(response == Constant.SUCCESS) {
                GlobalPreferenceManager.saveParentTasks(((JSONObject)data).toString());
                LogUtils.i(GlobalPreferenceManager.getParentTasks());
                mSyncManager.parentNotice(GlobalPreferenceManager.getUserEmail(), GlobalPreferenceManager.getUniqueId());
            }
        }
        else if(syncPage==Constant.SYNC_PARENT_NOTICE){
            if(response == Constant.SUCCESS) {
                GlobalPreferenceManager.saveParentNotice(((JSONObject)data).toString());
                LogUtils.i(GlobalPreferenceManager.getParentNotice());
                mSyncManager.parentFee(GlobalPreferenceManager.getUserEmail(), GlobalPreferenceManager.getUniqueId());
            }
        }
        else if(syncPage==Constant.SYNC_PARENT_FEE){
            if(response == Constant.SUCCESS) {
                GlobalPreferenceManager.saveParentFee(((JSONObject)data).toString());
                LogUtils.i(GlobalPreferenceManager.getParentFee());
                mSyncManager.parentGovBody(GlobalPreferenceManager.getUserEmail(), GlobalPreferenceManager.getUniqueId());
            }
        }
        else if(syncPage==Constant.SYNC_PARENT_GOVBODY){
            if(response == Constant.SUCCESS) {
                GlobalPreferenceManager.saveParentGovBody(((JSONObject)data).toString());
                LogUtils.i(GlobalPreferenceManager.getParentGovBody());
                mSyncManager.parentFaculty(GlobalPreferenceManager.getUserEmail(), GlobalPreferenceManager.getUniqueId());
            }
        }
        else if(syncPage==Constant.SYNC_PARENT_FACULTY){
            if(response == Constant.SUCCESS) {
                GlobalPreferenceManager.saveParentFaculty(((JSONObject)data).toString());
                LogUtils.i(GlobalPreferenceManager.getParentFaculty());
                mSyncManager.parentMarksheet(GlobalPreferenceManager.getUserEmail(), GlobalPreferenceManager.getUniqueId());
            }
        }
        else if(syncPage==Constant.SYNC_PARENT_MARKSHEET){
            if(response == Constant.SUCCESS) {
                GlobalPreferenceManager.saveParentMarksheet(((JSONObject)data).toString());
                LogUtils.i(GlobalPreferenceManager.getParentMarksheet());
                mSyncManager.parentGetClasses(GlobalPreferenceManager.getUserEmail(), GlobalPreferenceManager.getUniqueId());
            }
        }
        else if(syncPage==Constant.SYNC_PARENT_CLASSES){
            if(response == Constant.SUCCESS) {
                GlobalPreferenceManager.saveParentClasses(((JSONObject)data).toString());
                LogUtils.i(GlobalPreferenceManager.getParentClasses());
            }
            if(mFragment instanceof DashboardFragmentParent) {
                ((DashboardFragmentParent)mFragment).setupStudentInfo();
            }
            pDialog.dismiss();
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
        FragmentManager mFragmentManager = getSupportFragmentManager();
        switch (position){
            case Constant.PARENT_DASHBOARD:
                mToolbarTitle.setText(getString(R.string.dashboard));
                mFragment = DashboardFragmentParent.newInstance(mHelpLiveo.get(position).getName());
                break;
            case Constant.PARENT_ATTENDANCE:
                mToolbarTitle.setText(getString(R.string.attendance));
                mFragment = AttendanceFragmentParent.newInstance();
                break;
            case Constant.PARENT_PAYMENTS:
                mToolbarTitle.setText(getString(R.string.payment));
                mFragment = PaymentFragmentParent.newInstance();
                break;
            case Constant.PARENT_TRACK:
                mToolbarTitle.setText(getString(R.string.track));
                mFragment = TrackDriverFragmentParent.newInstance();
                break;
            case Constant.PARENT_TASK:
                mFragment = TaskFragmentParent.newInstance();
                mToolbarTitle.setText(getString(R.string.task));
                break;
            case Constant.PARENT_NOTICE:
                mFragment = NoticeFragmentParent.newInstance();
                mToolbarTitle.setText(getString(R.string.notice));
                break;
            case Constant.PARENT_MARKSHEET:
                mFragment = MarksheetFragmentParent.newInstance();
                mToolbarTitle.setText(getString(R.string.marksheet));
                break;
            case Constant.PARENT_GOVT_BODY:
                mToolbarTitle.setText(getString(R.string.govbody));
                mFragment = GvtBodyFragmentParent.newInstance();
                break;
            case Constant.PARENT_FACULTY:
                mToolbarTitle.setText(getString(R.string.faculty));
                mFragment = FacultyFragmentParent.newInstance();
                break;
            case Constant.PARENT_ABOUT:
                mToolbarTitle.setText(getString(R.string.about));
                mFragment = AboutFragmentParent.newInstance();
                break;
            default:
                mFragment = DashboardFragmentParent.newInstance(mHelpLiveo.get(position).getName());
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
                startActivity(new Intent(MainParentActivity.this, ProfileActivity.class));
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
                startActivity(new Intent(MainParentActivity.this, LoginActivity.class));
                finish();
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getParentDataFromServer();
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }
}
