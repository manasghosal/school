package com.quicksoft.school.activity.parent;

import android.app.Activity;
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
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;
import com.leo.simplearcloader.SimpleArcLoader;
import com.quicksoft.school.R;
import com.quicksoft.school.activity.PasswordActivity;
import com.quicksoft.school.activity.login.LoginActivity;
import com.quicksoft.school.connection.SyncManager;
import com.quicksoft.school.connection.callback.SyncCompleteCallback;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;

import org.json.JSONObject;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;

public class NoticeDetailsParentActivity extends AppCompatActivity implements View.OnClickListener, SyncCompleteCallback {

    private Toolbar mToolbar;
    private TextView tvTitle, tvDesc;
    private FancyButton btnReply;
    private String position;
    private String noticeId;
    private String noticeType;

    private SimpleArcDialog pDialog;
    private SyncManager mSyncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_task_details);
        setupToolbar();

        tvTitle = findViewById(R.id.tvTitle);
        tvDesc = findViewById(R.id.tvDesc);
        btnReply = findViewById(R.id.btnReply);
        btnReply.setOnClickListener(this);

        getIntent().getStringExtra("TEACHER");
        getIntent().getStringExtra("DATE");
        tvTitle.setText(getIntent().getStringExtra("TITLE"));
        tvDesc.setText(getIntent().getStringExtra("DESC"));

        noticeId = getIntent().getStringExtra("ID");
        noticeType = getIntent().getStringExtra("TYPE");

        String str = getIntent().getStringExtra("SUBMITTED");
        String isSubmiited = str.substring(0, 3);
        LogUtils.i(isSubmiited);

        if(noticeType.compareTo("2")==0) {
            if (isSubmiited.compareTo("YES") == 0) {
                btnReply.setText("Submitted Already");
                btnReply.setEnabled(false);
                position = str.substring(3, str.length());
            } else {
                btnReply.setText("Submit");
                position = str.substring(2, str.length());
//            LogUtils.i("Position:" + position);
            }
        }else{
            btnReply.setVisibility(View.GONE);
        }

    }

    public void setupToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mToolbarTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mToolbarTitle.setText("Notice Details");

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
    public void onClick(View view) {
        if(view == btnReply){

            if(NetworkUtils.isConnected()) {
                mSyncManager = new SyncManager(NoticeDetailsParentActivity.this, NoticeDetailsParentActivity.this);

                pDialog = new SimpleArcDialog(NoticeDetailsParentActivity.this);
                ArcConfiguration configuration = new ArcConfiguration(NoticeDetailsParentActivity.this);
                configuration.setLoaderStyle(SimpleArcLoader.STYLE.COMPLETE_ARC);
                configuration.setText("Please wait..");
                pDialog.setConfiguration(configuration);
                pDialog.setCancelable(false);
                pDialog.show();
                String email = GlobalPreferenceManager.getUserEmail();
                String uniqueID = GlobalPreferenceManager.getUniqueId();
                mSyncManager.parentSubmitNotice(email, uniqueID, noticeId);
            }else{
                Toasty.error(NoticeDetailsParentActivity.this,"Check your internet connection", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    public void onSyncComplete(int syncPage, int response, Object data) {
        if(syncPage==Constant.SYNC_PARENT_NOTICE_SUBMIT){
            if(response == Constant.SUCCESS) {
                LogUtils.i(((JSONObject)data).toString());

                Toasty.info(this,"Submitted successfully", Toast.LENGTH_LONG).show();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("POSITION", position);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

                pDialog.dismiss();
            }else if(response == Constant.FAIL){
                int respCode =  (int)data;
                if(respCode ==401 || respCode ==403){
                    GlobalPreferenceManager.setUserLoggedIn(false);
                    GlobalPreferenceManager.setUserType(-1);
                    GlobalPreferenceManager.setLoginType(-1);
                    Toasty.error(this,"Please login again..", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(NoticeDetailsParentActivity.this, LoginActivity.class));
                    finish();
                }
            }
        }

        if(response == Constant.FAIL || response == Constant.NETWORK_FAIL){
            pDialog.dismiss();
        }
    }
}
