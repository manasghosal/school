package com.quicksoft.school.activity.driver;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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
import com.quicksoft.school.activity.teacher.MainTeacherActivity;
import com.quicksoft.school.connection.SyncManager;
import com.quicksoft.school.connection.callback.SyncCompleteCallback;
import com.quicksoft.school.fragment.driver.DashboardFragmentDriver;
import com.quicksoft.school.fragment.parent.DashboardFragmentParent;
import com.quicksoft.school.fragment.teacher.DashboardFragmentTeacher;
import com.quicksoft.school.model.Passanger;
import com.quicksoft.school.model.PassengerPickDriver;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import br.liveo.interfaces.OnItemClickListener;
import br.liveo.interfaces.OnPrepareOptionsMenuLiveo;
import br.liveo.model.HelpLiveo;
import br.liveo.navigationliveo.NavigationLiveo;
import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;
import es.dmoral.toasty.Toasty;
import com.google.android.gms.maps.MapFragment;
public class MainDriverActivity extends NavigationLiveo implements OnItemClickListener , SyncCompleteCallback {

    private HelpLiveo mHelpLiveo;
    public TextView mToolbarTitle;
    private static final int INITIAL_REQUEST=1337;
    private static final int LOCATION_REQUEST=INITIAL_REQUEST+3;
    private SimpleArcDialog pDialog;
    private SyncManager mSyncManager;
    private ArrayList<String> vehicleList;
    private ArrayList<String> vehicleRouteDescriptionList;
    private int fetchIndex = 0;

    Fragment mFragment = null;
    @Override
    public void onInt(Bundle savedInstanceState) {

        View mCustomHeader = getLayoutInflater().inflate(R.layout.drawer_header, this.getListView(), false);
        ShapedImageView imgProfile = mCustomHeader.findViewById(R.id.imgProfile);
        TextView tvName =  mCustomHeader.findViewById(R.id.tvName);
        imgProfile.setImageResource(R.drawable.ico_user_placeholder);

        mHelpLiveo = new HelpLiveo();
        mHelpLiveo.add(getString(R.string.dashboard), R.drawable.ic_dashboard_teal_700_24dp);

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
        getDriverVehicleFromServer();
    }
    private void getDriverVehicleFromServer()
    {
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
            mSyncManager.driverVehicles(email, uniqueID);
        }else{
            Toasty.error(this,"Check your internet connection", Toast.LENGTH_LONG).show();
        }
    }
    private void getDriverDataFromServer(String vehicle){

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
            LogUtils.i(email + " " + uniqueID + " " + vehicle);
            mSyncManager.driverDashboard(email, uniqueID, vehicle);
        }else{
            Toasty.error(this,"Check your internet connection", Toast.LENGTH_LONG).show();
        }
    }
    void FillDriverVehicle()
    {
        String data = GlobalPreferenceManager.getDriverVehicle();
        LogUtils.i(data);
        vehicleList = new ArrayList<>();
        vehicleRouteDescriptionList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray array = jsonObject.getJSONArray("VehicleNo");
            JSONArray array1 = jsonObject.getJSONArray("RouteDescription");
            LogUtils.i("Vehicle List length: "+ array.length());
            for (int i=0; i<array.length(); i++){
                String object = array.getString(i);
                vehicleList.add(object);
                object = array1.getString(i);
                vehicleRouteDescriptionList.add(object);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onSyncComplete(int syncPage, int response, Object data) {
        LogUtils.i(response);
        if(syncPage == Constant.SYNC_DRIVER_VEHICLE)
        {
            if(response == Constant.SUCCESS) {
                pDialog.dismiss();
                GlobalPreferenceManager.saveDriverVehicle(((JSONObject)data).toString());
                LogUtils.i(GlobalPreferenceManager.getDriverVehicle());
                FillDriverVehicle();
                fetchIndex = 0;
                if(fetchIndex < vehicleList.size()) {
                    getDriverDataFromServer(vehicleList.get(fetchIndex));
                }
            }else if(response == Constant.FAIL){
                int respCode =  (int)data;
                if(respCode ==401 || respCode ==403){
                    GlobalPreferenceManager.setUserLoggedIn(false);
                    GlobalPreferenceManager.setUserType(-1);
                    GlobalPreferenceManager.setLoginType(-1);
                    Toasty.error(this,"Please login again..", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MainDriverActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }
        if(syncPage==Constant.SYNC_DRIVER_DASHBOARD){
            if(response == Constant.SUCCESS) {
                //pDialog.dismiss();
                GlobalPreferenceManager.saveDriverDashBoardInfo(vehicleList.get(fetchIndex), ((JSONObject)data).toString());
                LogUtils.i(GlobalPreferenceManager.getDriverDashBoardInfo(vehicleList.get(fetchIndex)));
                fetchIndex++;
                if(fetchIndex < vehicleList.size()) {
                    getDriverDataFromServer(vehicleList.get(fetchIndex));
                    fetchIndex++;
                }
                else {
                    fetchIndex = 0;
                    if(mFragment == null)
                        mFragment = DashboardFragmentDriver.newInstance();
                    ((DashboardFragmentDriver)mFragment).onDataReady();

                }
            }else if(response == Constant.FAIL){
                int respCode =  (int)data;
                if(respCode ==401 || respCode ==403){
                    GlobalPreferenceManager.setUserLoggedIn(false);
                    GlobalPreferenceManager.setUserType(-1);
                    GlobalPreferenceManager.setLoginType(-1);
                    Toasty.error(this,"Please login again..", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(MainDriverActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }
        if(response == Constant.FAIL || response == Constant.NETWORK_FAIL){
            pDialog.dismiss();
        }
        pDialog.dismiss();
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
    public void onResume() {
        super.onResume();
        if(mFragment != null)
            mFragment.onResume();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        LogUtils.i("salle kutte LOCATION_REQUESTLOCATION_REQUESTLOCATION_REQUESTLOCATION_REQUESTLOCATION_REQUEST","0");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST: {
                LogUtils.i("LOCATION_REQUESTLOCATION_REQUESTLOCATION_REQUESTLOCATION_REQUESTLOCATION_REQUEST","0");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    //yes

                    LogUtils.e("test","1");

                    //Intent intent = new Intent(getActivity(), MapsActivity.class);
                    //intent.putExtra("latitude", 35.694828);
                    //intent.putExtra("longitude", 51.378129);
                    //startActivity(intent);

                } else {
                    //utilityFunctions.showSweetAlertWarning(getActivity(),r.getString(R.string.str_warning_title_empty),
                    //		r.getString(R.string.str_you_must_allow_this_permission_toast),
                    //		r.getString(R.string.str_warning_btn_login));

                    LogUtils.e("test","2");
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
/*
 if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(
                    new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST
            );
        } else {
            LogUtils.e("DB", "PERMISSION GRANTED");
        }*/
    @Override
    public void onItemClick(int position) {

        FragmentManager mFragmentManager = getSupportFragmentManager();

        switch (position){
            case Constant.DRIVER_DASHBOARD:
                mToolbarTitle.setText(getString(R.string.dashboard));
                mFragment = DashboardFragmentDriver.newInstance();

                break;
            default:
                //mFragment = DashboardFragmentDriver.newInstance();
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
                startActivity(new Intent(MainDriverActivity.this, ProfileActivity.class));
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
                startActivity(new Intent(MainDriverActivity.this, LoginActivity.class));
                finish();
            }
        };
    }
}
