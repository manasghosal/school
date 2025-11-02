package com.quicksoft.school.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.jeevandeshmukh.fancybottomsheetdialoglib.FancyBottomSheetDialog;
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;
import com.leo.simplearcloader.SimpleArcLoader;
import com.quicksoft.school.R;
import com.quicksoft.school.activity.login.LoginActivity;
import com.quicksoft.school.connection.SyncManager;
import com.quicksoft.school.connection.callback.SyncCompleteCallback;
import com.quicksoft.school.fragment.teacher.NoticeFragmentTeacher;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONObject;

import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

public class PasswordActivity extends AppCompatActivity implements View.OnClickListener, SyncCompleteCallback {
    private Toolbar mToolbar;
    private MaterialEditText editOldPassword, editNewPassword, editRePassword;
    private FancyButton btnSubmit;

    private SimpleArcDialog pDialog;
    private SyncManager mSyncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        setupToolbar();

        editOldPassword = findViewById(R.id.editOldPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        editRePassword = findViewById(R.id.editRePassword);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == btnSubmit){
            String oloPass = editOldPassword.getText().toString();
            String newPass = editNewPassword.getText().toString();
            String rePass = editRePassword.getText().toString();

            if(isValid(newPass, rePass)){
                if(NetworkUtils.isConnected()) {
                    mSyncManager = new SyncManager(PasswordActivity.this, PasswordActivity.this);

                    pDialog = new SimpleArcDialog(PasswordActivity.this);
                    ArcConfiguration configuration = new ArcConfiguration(PasswordActivity.this);
                    configuration.setLoaderStyle(SimpleArcLoader.STYLE.COMPLETE_ARC);
                    configuration.setText("Please wait..");
                    pDialog.setConfiguration(configuration);
                    pDialog.setCancelable(false);
                    pDialog.show();
                    String email = GlobalPreferenceManager.getUserEmail();
                    String uniqueID = GlobalPreferenceManager.getUniqueId();
                    mSyncManager.changePassword(email, uniqueID, oloPass, newPass);
                }else{
                    Toasty.error(PasswordActivity.this,"Check your internet connection", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onSyncComplete(int syncPage, int response, Object data) {
        if (syncPage == Constant.SYNC_PASSWORD) {
            if (response == Constant.SUCCESS) {
                LogUtils.i(((JSONObject) data).toString());
                editOldPassword.setText("");
                editNewPassword.setText("");
                editRePassword.setText("");
                Toasty.info(PasswordActivity.this,"Password is updated", Toast.LENGTH_LONG).show();
                pDialog.dismiss();
            }else if(response == Constant.FAIL){
                int respCode =  (int)data;
                if(respCode ==401 || respCode ==403){
                    GlobalPreferenceManager.setUserLoggedIn(false);
                    GlobalPreferenceManager.setUserType(-1);
                    GlobalPreferenceManager.setLoginType(-1);
                    Toasty.error(PasswordActivity.this,"Please login again..", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(PasswordActivity.this, LoginActivity.class));
                    PasswordActivity.this.finish();
                }else {
                    Toasty.error(PasswordActivity.this,"Old password is not matching..", Toast.LENGTH_LONG).show();
                }
            }
        }

        if(response == Constant.FAIL || response == Constant.NETWORK_FAIL){
            pDialog.dismiss();
        }
    }

    public boolean isValid(String passwordhere, String confirmhere) {

        Pattern specailCharPatten = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Pattern UpperCasePatten = Pattern.compile("[A-Z ]");
        Pattern lowerCasePatten = Pattern.compile("[a-z ]");
        Pattern digitCasePatten = Pattern.compile("[0-9 ]");

        boolean flag=true;
        String str="";

        if (!passwordhere.equals(confirmhere)) {
            str = "password and confirm password does not match";
            flag=false;
        }else if (passwordhere.length() < 6) {
            str = "Password lenght must have alleast 6 character !!";
            flag=false;
        }else if (!specailCharPatten.matcher(passwordhere).find()) {
            str = "Password must have atleast one specail character !!";
            flag=false;
        }else if (!UpperCasePatten.matcher(passwordhere).find()) {
            str = "Password must have atleast one uppercase character !!";
            flag=false;
        }else if (!lowerCasePatten.matcher(passwordhere).find()) {
            str = "Password must have atleast one lowercase character !!";
            flag=false;
        }else if (!digitCasePatten.matcher(passwordhere).find()) {
            str = "Password must have atleast one digit character !!";
            flag=false;
        }

        if(!flag){
            new FancyBottomSheetDialog.Builder(this)
                    .setMessage(str)
                    .setBackgroundColor(getResources().getColor(R.color.colorPrimary))
                    .setIcon(R.drawable.ic_assignment_late_white_24dp,true)
                    .setPositiveBtnText("Ok")
                    .setNegativeBtnText("")
                    .setPositiveBtnBackground(getResources().getColor(R.color.okButtonColor))
                    .build();
        }

        return flag;

    }

    public void setupToolbar(){
        mToolbar = findViewById(R.id.toolbar);
        TextView mToolbarTitle = mToolbar.findViewById(R.id.toolbar_title);
        mToolbarTitle.setText("Change Password");

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
