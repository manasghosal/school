package com.quicksoft.school.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.florent37.viewanimator.AnimationListener;
import com.github.florent37.viewanimator.ViewAnimator;
import com.quicksoft.school.R;
import com.quicksoft.school.activity.driver.MainDriverActivity;
import com.quicksoft.school.activity.login.LoginActivity;
import com.quicksoft.school.activity.login.OTPActivity;
import com.quicksoft.school.activity.parent.MainParentActivity;
import com.quicksoft.school.activity.teacher.MainTeacherActivity;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;

import es.dmoral.toasty.Toasty;
import me.tankery.permission.PermissionRequestActivity;

public class SplashActivity extends AppCompatActivity {

    private ImageView imgCoLogo, imgIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (GlobalPreferenceManager.isUserLoggedIn()) {
            if (GlobalPreferenceManager.getUserType() == Constant.USER_PARENT) {
                startActivity(new Intent(SplashActivity.this, MainParentActivity.class));
            } else if (GlobalPreferenceManager.getUserType() == Constant.USER_TEACHER) {
                startActivity(new Intent(SplashActivity.this, MainTeacherActivity.class));
            }else if (GlobalPreferenceManager.getUserType() == Constant.USER_DRIVER) {
                startActivity(new Intent(SplashActivity.this, MainDriverActivity.class));
            }
            finish();
        }else {
            imgCoLogo = findViewById(R.id.imgCoLogo);
            imgIcon = findViewById(R.id.imgIcon);

            ViewAnimator
                    .animate(imgCoLogo).translationY(800, 0).alpha(0, 1).duration(1000)
                    .andAnimate(imgIcon).translationX(-800,0).pivotX(0).duration(1500)
                    .andAnimate(imgIcon).translationX(-400,0).pivotX(0).duration(1500)
                    .onStop(new AnimationListener.Stop() {
                        @Override
                        public void onStop() {
                            if (!GlobalPreferenceManager.isAllPermissionGranted()) {
                                checkPermission();
                            } else {
                                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                finish();
                            }
                        }
                    }).start();
        }

    }

    // ---------------------------   Permissions Handler -------------------------//
    private void checkPermission(){
        String[] MUST_PERMISSIONS = new String[] {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.CALL_PHONE
        };

        String message = "We need SD Card and location permissions for better experience.";
        PermissionRequestActivity.start(this, Constant.PERMISSION_REQUEST_CODE, MUST_PERMISSIONS, message, message);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.PERMISSION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                GlobalPreferenceManager.setAllPermissionGranted(true);

                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();

            } else {
                Toasty.error(this, "Permission denied", Toast.LENGTH_SHORT, true).show();
            }
        }
    }
}
