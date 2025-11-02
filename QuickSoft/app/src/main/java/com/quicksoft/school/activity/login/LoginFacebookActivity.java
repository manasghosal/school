package com.quicksoft.school.activity.login;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.jeevandeshmukh.fancybottomsheetdialoglib.FancyBottomSheetDialog;
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;
import com.leo.simplearcloader.SimpleArcLoader;
import com.quicksoft.school.R;
import com.quicksoft.school.connection.SyncManager;
import com.quicksoft.school.connection.callback.SyncCompleteCallback;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;
import mehdi.sakout.fancybuttons.FancyButton;


public class LoginFacebookActivity extends AppCompatActivity implements View.OnClickListener, SyncCompleteCallback{

    private MaterialEditText editStudentId;
    private FancyButton btnLogin, btnUserType;
    private SimpleArcDialog pDialog;
    private SyncManager mSyncManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_facebook);

        editStudentId = findViewById(R.id.editStudentId);
        btnLogin = findViewById(R.id.btnLogin);
        btnUserType = findViewById(R.id.btnUserType);

        btnLogin.setOnClickListener(this);

        mSyncManager = new SyncManager(this, this);
        LogUtils.i("UserType: "+GlobalPreferenceManager.getUserType());
        initViewForUserType(GlobalPreferenceManager.getUserType());
    }

    @Override
    public void onClick(View v) {
        if(v==btnLogin) {
            if (NetworkUtils.isConnected()) {
                login();
            }else {
                new FancyBottomSheetDialog.Builder(this)
                        .setMessage(getString(R.string.dialog_no_network))
                        .setBackgroundColor(getResources().getColor(R.color.colorPrimary))
                        .setIcon(R.drawable.ic_power_settings_new_teal_700_24dp,true)
                        .setPositiveBtnText("Ok")
                        .setNegativeBtnText("")
                        .setPositiveBtnBackground(getResources().getColor(R.color.okButtonColor))
                        .build();
            }
        }
    }

    public void initViewForUserType(int position){
        if(position == -1){
            editStudentId.setVisibility(View.GONE);
            btnLogin.setVisibility(View.GONE);
        }else if(position == Constant.USER_PARENT){
            editStudentId.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
            editStudentId.setHint("Student ID");
            btnUserType.setText(getResources().getStringArray(R.array.userType)[position]);
        }else if(position == Constant.USER_TEACHER){
            editStudentId.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
            editStudentId.setHint("Teacher ID");
            btnUserType.setText(getResources().getStringArray(R.array.userType)[position]);
        }else if(position == Constant.USER_DRIVER){
            editStudentId.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
            editStudentId.setHint("Driver ID");
            btnUserType.setText(getResources().getStringArray(R.array.userType)[position]);
        }
    }

    public void login() {
        LogUtils.i("Login");

        Boolean valid = true;
        String studentID = editStudentId.getText().toString();

        int uType = GlobalPreferenceManager.getUserType();
        String userType = "";
        if(uType==0)
            userType = "PA";
        else if(uType==1)
            userType = "TE";
        else if(uType==2)
            userType = "DA";

        String fbID =  GlobalPreferenceManager.getFBId();

        if (studentID.isEmpty()) {
            editStudentId.setError("Enter a valid ID");
            valid = false;
        } else {
            editStudentId.setError(null);
        }

        if(valid)
        {
            pDialog = new SimpleArcDialog(this);
            ArcConfiguration configuration = new ArcConfiguration(this);
            configuration.setLoaderStyle(SimpleArcLoader.STYLE.COMPLETE_ARC);
            configuration.setText("Please wait..");
            pDialog.setConfiguration(configuration);
            pDialog.setCancelable(false);
            pDialog.show();
            mSyncManager.userLogin("", "",userType, fbID,"",studentID);
        }

    }

    @Override
    public void onSyncComplete(int syncPage, int response, Object data) {
        if(syncPage==Constant.SYNC_LOGIN){
            if(response == Constant.SUCCESS) {
                try {
                    JSONObject jsonObject = (JSONObject)data;
                    String UniqKey = jsonObject.getString("UniqKey");
                    String phone = jsonObject.getString("Phone");

                    GlobalPreferenceManager.saveUniqueId(UniqKey);
                    GlobalPreferenceManager.saveUserPhone(phone);

                    pDialog.dismiss();
                    GlobalPreferenceManager.setLoginType(Constant.LOGIN_WITH_FACEBOOK);
                    startActivity(new Intent(LoginFacebookActivity.this, OTPActivity.class));
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                pDialog.dismiss();
            }
        }
    }
}