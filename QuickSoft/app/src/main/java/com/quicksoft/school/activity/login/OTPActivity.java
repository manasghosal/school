package com.quicksoft.school.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.chaos.view.PinView;
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;
import com.leo.simplearcloader.SimpleArcLoader;
import com.quicksoft.school.R;
import com.quicksoft.school.activity.driver.MainDriverActivity;
import com.quicksoft.school.activity.parent.MainParentActivity;
import com.quicksoft.school.activity.teacher.MainTeacherActivity;
import com.quicksoft.school.connection.SyncManager;
import com.quicksoft.school.connection.callback.SyncCompleteCallback;
import com.quicksoft.school.fragment.parent.DashboardFragmentParent;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;

import org.json.JSONObject;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

public class OTPActivity extends AppCompatActivity implements View.OnClickListener, SyncCompleteCallback {

    private FancyButton btnResend, btnVerify;
    private PinView customOtpView;
    private TextView tvText;

    private SimpleArcDialog pDialog;
    private SyncManager mSyncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        btnResend = findViewById(R.id.btnResend);
        btnVerify = findViewById(R.id.btnVerify);
        customOtpView = findViewById(R.id.customOtpView);
        tvText = findViewById(R.id.tvTitle2);

        btnResend.setOnClickListener(this);
        btnVerify.setOnClickListener(this);
        customOtpView.setAnimationEnable(true);

        tvText.setText("Please type the verification code\nsend to " + GlobalPreferenceManager.getUserPhone());
    }

    @Override
    public void onClick(View view) {
        if(view == btnResend){
            if(NetworkUtils.isConnected()) {
                mSyncManager = new SyncManager(OTPActivity.this, OTPActivity.this);

                pDialog = new SimpleArcDialog(OTPActivity.this);
                ArcConfiguration configuration = new ArcConfiguration(OTPActivity.this);
                configuration.setLoaderStyle(SimpleArcLoader.STYLE.COMPLETE_ARC);
                configuration.setText("Please wait..");
                pDialog.setConfiguration(configuration);
                pDialog.setCancelable(false);
                pDialog.show();
                String email = GlobalPreferenceManager.getUserEmail();
                String uniqueID = GlobalPreferenceManager.getUniqueId();
                mSyncManager.resendOTP(email, uniqueID);
            }else{
                Toasty.error(OTPActivity.this,"Check your internet connection", Toast.LENGTH_LONG).show();
            }
        }else if(view == btnVerify){


            if(customOtpView.getText().toString().compareTo("0000")!=0){
                Toasty.error(OTPActivity.this, "OTP did not match. Please try again.", Toast.LENGTH_SHORT, true).show();
            }else{
                GlobalPreferenceManager.setUserLoggedIn(true);
                finishAffinity();
                if (GlobalPreferenceManager.getUserType() == Constant.USER_PARENT) {
                    startActivity(new Intent(OTPActivity.this, MainParentActivity.class));
                } else if (GlobalPreferenceManager.getUserType() == Constant.USER_TEACHER) {
                    startActivity(new Intent(OTPActivity.this, MainTeacherActivity.class));
                }else if (GlobalPreferenceManager.getUserType() == Constant.USER_DRIVER) {
                    startActivity(new Intent(OTPActivity.this, MainDriverActivity.class));
                }

                finish();
            }

//            String otp = customOtpView.getText().toString();
//            if(otp.length()==4) {
//                if(NetworkUtils.isConnected()) {
//                    mSyncManager = new SyncManager(OTPActivity.this, OTPActivity.this);
//                    pDialog = new SimpleArcDialog(OTPActivity.this);
//                    ArcConfiguration configuration = new ArcConfiguration(OTPActivity.this);
//                    configuration.setLoaderStyle(SimpleArcLoader.STYLE.COMPLETE_ARC);
//                    configuration.setText("Please wait..");
//                    pDialog.setConfiguration(configuration);
//                    pDialog.setCancelable(false);
//                    pDialog.show();
//                    String email = GlobalPreferenceManager.getUserEmail();
//                    String uniqueID = GlobalPreferenceManager.getUniqueId();
//                    mSyncManager.verifyOTP(email, uniqueID, otp);
//                }else{
//                    Toasty.error(OTPActivity.this,"Check your internet connection", Toast.LENGTH_LONG).show();
//                }
//            }else{
//                Toasty.error(OTPActivity.this,"Enter 4 digit OTP", Toast.LENGTH_LONG).show();
//            }
        }
    }

    @Override
    public void onSyncComplete(int syncPage, int response, Object data) {
        if (syncPage == Constant.SYNC_RESEND_OTP) {
            if (response == Constant.SUCCESS) {
                LogUtils.i(((JSONObject) data).toString());
            } else if (response == Constant.FAIL) {
                int respCode = (int) data;
                if (respCode == 401 || respCode == 403) {
                    GlobalPreferenceManager.setUserLoggedIn(false);
                    GlobalPreferenceManager.setUserType(-1);
                    GlobalPreferenceManager.setLoginType(-1);
                    Toasty.error(this, "Please login again..", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(OTPActivity.this, LoginActivity.class));
                    finish();
                }
            }
            pDialog.dismiss();
        }else if (syncPage == Constant.SYNC_VERIFYOTP) {
            if (response == Constant.SUCCESS) {
                LogUtils.i(((JSONObject) data).toString());

                pDialog.dismiss();
                GlobalPreferenceManager.setUserLoggedIn(true);
                finishAffinity();
                if (GlobalPreferenceManager.getUserType() == Constant.USER_PARENT) {
                    startActivity(new Intent(OTPActivity.this, MainParentActivity.class));
                } else if (GlobalPreferenceManager.getUserType() == Constant.USER_TEACHER) {
                    startActivity(new Intent(OTPActivity.this, MainTeacherActivity.class));
                }else if (GlobalPreferenceManager.getUserType() == Constant.USER_DRIVER) {
                    startActivity(new Intent(OTPActivity.this, MainDriverActivity.class));
                }
                finish();

            } else if (response == Constant.FAIL) {
                int respCode = (int) data;
                if (respCode == 403) {
                    GlobalPreferenceManager.setUserLoggedIn(false);
                    GlobalPreferenceManager.setUserType(-1);
                    GlobalPreferenceManager.setLoginType(-1);
                    Toasty.error(this, "Please login again..", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(OTPActivity.this, LoginActivity.class));
                    finish();
                }else if(respCode == 401){
                    Toasty.error(this, "OTP did not match", Toast.LENGTH_LONG).show();
                }
            }
        }

        if(response == Constant.FAIL || response == Constant.NETWORK_FAIL){
            pDialog.dismiss();
        }
    }
}
