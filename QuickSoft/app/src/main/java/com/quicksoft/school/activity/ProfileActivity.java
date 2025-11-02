package com.quicksoft.school.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;
import com.leo.simplearcloader.SimpleArcLoader;
import com.quicksoft.school.R;
import com.quicksoft.school.activity.login.LoginActivity;
import com.quicksoft.school.connection.SyncManager;
import com.quicksoft.school.connection.callback.SyncCompleteCallback;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.theartofdev.edmodo.cropper.CropImage;
import com.bumptech.glide.signature.ObjectKey;
import org.json.JSONObject;

import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;
import es.dmoral.toasty.Toasty;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener, SyncCompleteCallback {
    private Toolbar mToolbar;
    private ShapedImageView imgProfileEdit, imgProfile;
    private TextView tvName, tvPassword;
    private Uri imageUri;

    private SimpleArcDialog pDialog;
    private SyncManager mSyncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupToolbar();

        imgProfileEdit = findViewById(R.id.imgProfileEdit);
        imgProfile = findViewById(R.id.imgProfile);
        tvName = findViewById(R.id.tvName);
        tvPassword = findViewById(R.id.tvPassword);

        imgProfileEdit.setOnClickListener(this);
        tvPassword.setOnClickListener(this);

        setupProfileImage(imgProfile, tvName);
    }

    @Override
    public void onClick(View view) {
        if(view == tvPassword){
            startActivity(new Intent(ProfileActivity.this, PasswordActivity.class));
        }else if(view == imgProfileEdit){
            CropImage.startPickImageActivity(this);
        }
    }

    public void setupToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mToolbarTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mToolbarTitle.setText("Profile");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);
            CropImage.activity(imageUri).start(this);
        }else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                uploadImage();
                imgProfile.setImageURI(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                LogUtils.i("Error", error.toString());
            }
        }
    }


    public void uploadImage(){
        if(NetworkUtils.isConnected()) {
            mSyncManager = new SyncManager(ProfileActivity.this, ProfileActivity.this);

            pDialog = new SimpleArcDialog(ProfileActivity.this);
            ArcConfiguration configuration = new ArcConfiguration(ProfileActivity.this);
            configuration.setLoaderStyle(SimpleArcLoader.STYLE.COMPLETE_ARC);
            configuration.setText("Please wait..");
            pDialog.setConfiguration(configuration);
            pDialog.setCancelable(false);
            pDialog.show();
            String fileExt = "png";
            String personID = GlobalPreferenceManager.getPersonID();
            mSyncManager.uploadImage(personID, fileExt, imageUri);
        }else{
            Toasty.error(ProfileActivity.this,"Check your internet connection", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onSyncComplete(int syncPage, int response, Object data) {
        if (syncPage == Constant.SYNC_UPLOAD_IMAGE) {
            if (response == Constant.SUCCESS) {
               // LogUtils.i(((JSONObject) data).toString());
                imgProfile.setImageURI(imageUri);
                Toasty.info(ProfileActivity.this,"Profile picture is updated", Toast.LENGTH_LONG).show();
                pDialog.dismiss();
               // setupProfileImage(imgProfile, tvName);
            }else if(response == Constant.FAIL){
                int respCode =  (int)data;
                if(respCode ==401 || respCode ==403){
                    GlobalPreferenceManager.setUserLoggedIn(false);
                    GlobalPreferenceManager.setUserType(-1);
                    GlobalPreferenceManager.setLoginType(-1);
                    Toasty.error(ProfileActivity.this,"Please login again..", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                    ProfileActivity.this.finish();
                }
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
            Glide.with(this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(GlobalPreferenceManager.getProfilePic())
                    .into(imgProfile);

            tvName.setText(GlobalPreferenceManager.getUserFullName());
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
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
