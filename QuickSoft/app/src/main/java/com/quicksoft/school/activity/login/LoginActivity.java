package com.quicksoft.school.activity.login;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
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

import java.util.Arrays;

import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;
import android.content.SharedPreferences;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, SyncCompleteCallback, GoogleApiClient.OnConnectionFailedListener {

    private MaterialEditText editStudentId, editEmail, editPassword;
    private FancyButton btnLogin, btnGoogle, btnFB, btnUserType;
    private TextView tvRegister, tvForget;
    private SimpleArcDialog pDialog;
    private SyncManager mSyncManager;
    //Facebook
    private CallbackManager callbackManager;
    //Google
    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editStudentId = findViewById(R.id.editStudentId);
        editEmail = findViewById(R.id.editUserName);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFB = findViewById(R.id.btnFB);
        btnUserType = findViewById(R.id.btnUserType);
        tvRegister = findViewById(R.id.tvRegister);
        tvForget  = findViewById(R.id.tvForget);

        btnLogin.setOnClickListener(this);
        btnGoogle.setOnClickListener(this);
        btnFB.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        btnUserType.setOnClickListener(this);

        mSyncManager = new SyncManager(this, this);
        initViewForUserType(-1);
        setupFBLogin();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        mGoogleApiClient = new GoogleApiClient.Builder(LoginActivity.this).enableAutoManage(LoginActivity.this , this /* OnConnectionFailedListener */).addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();

    }

    @Override
    public void onClick(View v) {
        if(v==btnLogin) {
            if (NetworkUtils.isConnected()) {
                login();
//                GlobalPreferenceManager.setLoginType(Constant.LOGIN_NORMAL);
//                Intent intent = new Intent(LoginActivity.this, OTPActivity.class);
//                startActivity(intent);
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
        }else if(v==btnUserType) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("User Type");
            dialog.setSingleChoiceItems(R.array.userType, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int position) {
                    btnUserType.setText(getResources().getStringArray(R.array.userType)[position]);
                    initViewForUserType(position);
                    dialog.dismiss();
                    GlobalPreferenceManager.setUserType(position);
                }

            });
            AlertDialog alert = dialog.create();
            alert.show();
        } else if(v == btnFB){
            LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "email"));
        }else if(v == btnGoogle){
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, Constant.GOOGLE_SIGNIN_RESULT);
        }
    }

    public void initViewForUserType(int position){
        SharedPreferences sp = getSharedPreferences("LoginTeacher", MODE_PRIVATE);
        String teacher_unm = sp.getString("Unm","");
        String teacher_psw = sp.getString("Psw","");
        String teacher_tchrid = sp.getString("Tchrid","");

        sp = getSharedPreferences("LoginParent", MODE_PRIVATE);
        String parent_unm = sp.getString("Unm","");
        String parent_psw = sp.getString("Psw","");
        String parent_stdid = sp.getString("Stdid","");

        sp = getSharedPreferences("LoginDriver", MODE_PRIVATE);
        String driver_unm = sp.getString("Unm","");
        String driver_psw = sp.getString("Psw","");
        String driver_drvid = sp.getString("Drvid", "");

        if(position == -1){
            editStudentId.setVisibility(View.GONE);
            editEmail.setVisibility(View.GONE);
            editPassword.setVisibility(View.GONE);
            btnLogin.setVisibility(View.GONE);
            btnGoogle.setVisibility(View.GONE);
            btnFB.setVisibility(View.GONE);
            tvForget.setVisibility(View.GONE);
            editStudentId.setText("");
            editEmail.setText("");
            editPassword.setText("");
        }else if(position == Constant.USER_PARENT){
            editStudentId.setVisibility(View.VISIBLE);
            editEmail.setVisibility(View.VISIBLE);
            editPassword.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
            btnGoogle.setVisibility(View.VISIBLE);
            btnFB.setVisibility(View.VISIBLE);
            tvForget.setVisibility(View.VISIBLE);
            editStudentId.setFloatingLabelText(getString(R.string.hintParent));
            editStudentId.setHint(getString(R.string.hintParent));
            editStudentId.setText("");
            editEmail.setText("");
            editPassword.setText("");

            //editStudentId.setText("2013020001000003");
            //editEmail.setText("gonesh.lal@gmail.com");
            //editPassword.setText("043019");
            editStudentId.setText(parent_stdid);
            editEmail.setText(parent_unm);
            editPassword.setText(parent_psw);

        }else if(position == Constant.USER_TEACHER){
            editStudentId.setVisibility(View.VISIBLE);
            editEmail.setVisibility(View.VISIBLE);
            editPassword.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
            btnGoogle.setVisibility(View.VISIBLE);
            btnFB.setVisibility(View.VISIBLE);
            tvForget.setVisibility(View.VISIBLE);
            editStudentId.setFloatingLabelText(getString(R.string.hintTeacher));
            editStudentId.setHint(getString(R.string.hintTeacher));
            editStudentId.setText("");
            editEmail.setText("");
            editPassword.setText("");

            //editStudentId.setText("0001");
            //editEmail.setText("sushmita.sen@gmail.com");
            //editPassword.setText("043019");
            editStudentId.setText(teacher_tchrid);
            editEmail.setText(teacher_unm);
            editPassword.setText(teacher_psw);

        }else if(position == Constant.USER_DRIVER){
            editStudentId.setVisibility(View.VISIBLE);
            editEmail.setVisibility(View.VISIBLE);
            editPassword.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.VISIBLE);
            btnGoogle.setVisibility(View.VISIBLE);
            btnFB.setVisibility(View.VISIBLE);
            tvForget.setVisibility(View.VISIBLE);
            editStudentId.setFloatingLabelText(getString(R.string.hintDriver));
            editStudentId.setHint(getString(R.string.hintDriver));
            editStudentId.setText("");
            editEmail.setText("");
            editPassword.setText("");

            //editStudentId.setText("0001");
            //editEmail.setText("sujoy.dutta@gmail.com");
            //editPassword.setText("043019");
            editStudentId.setText(driver_drvid);
            editEmail.setText(driver_unm);
            editPassword.setText(driver_psw);
        }
    }

    public void login() {


        Boolean valid = true;
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();
        String studentId = editStudentId.getText().toString();
        LogUtils.i("Login"+ " email:"+email + " password:"+password+" studentId:"+studentId);
        int uType = GlobalPreferenceManager.getUserType();
        String userType = "";
        if(uType==0)
            userType = "PA";
        else if(uType==1)
            userType = "TE";
        else if(uType==2)
            userType = "DR";


        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editEmail.setError("enter a valid email address");
            valid = false;
        } else {
            editEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 ) {
            editPassword.setError("Password must be more than 4 characters");
            valid = false;
        } else {
            editPassword.setError(null);
        }

        if (studentId.isEmpty() || studentId.length() < 3 ) {
            editStudentId.setError("Please enter valid Student ID");
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
            if(uType == 0)
            {
                SharedPreferences sp = getSharedPreferences("LoginParent", MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                ed.putString("Unm",editEmail.getText().toString());
                ed.putString("Psw",editPassword.getText().toString());
                ed.putString("Stdid",editStudentId.getText().toString());
                ed.commit();
            }
            else if(uType == 1)
            {
                SharedPreferences sp = getSharedPreferences("LoginTeacher", MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                ed.putString("Unm",editEmail.getText().toString());
                ed.putString("Psw",editPassword.getText().toString());
                ed.putString("Tchrid",editStudentId.getText().toString());
                ed.commit();
            }
            else if(uType == 2)
            {
                SharedPreferences sp = getSharedPreferences("LoginDriver", MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                ed.putString("Unm",editEmail.getText().toString());
                ed.putString("Psw",editPassword.getText().toString());
                ed.putString("Drvid",editStudentId.getText().toString());
                ed.commit();
            }
            mSyncManager.userLogin(email, password,userType, "","",studentId);
        }

    }

    public void setupFBLogin(){
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        //getFbInfoWithProfileTracker();
                        getUserProfileWithGraphRequest(loginResult.getAccessToken());
                    }
                    @Override
                    public void onCancel() {
                        // Toast.makeText(MainActivity.this, "login canceled by user", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toasty.error(LoginActivity.this, "Error " + exception.getLocalizedMessage(), Toast.LENGTH_SHORT, true).show();
                    }
                });
    }

    private void getUserProfileWithGraphRequest(AccessToken accessToken){
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String firstName="";
                    String email_id = object.getString("email");
                    String profile_name = object.getString("name");
                    if(object.has("first_name"))
                        firstName = object.getString("first_name");
                    long fb_id = object.getLong("id");
                    String profileImageURL = "https://graph.facebook.com/"+fb_id+"/picture?type=large";
                    GlobalPreferenceManager.setFacebookProfileInfo(email_id, profile_name , ""+fb_id, profileImageURL);
                    //LogUtils.i("url: "+ profileImageURL);
                    startActivity(new Intent(LoginActivity.this, LoginFacebookActivity.class));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toasty.error(LoginActivity.this, "Error " + e.getLocalizedMessage(), Toast.LENGTH_SHORT, true).show();
                }

            }

        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, name, email, gender");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }else if (requestCode == Constant.GOOGLE_SIGNIN_RESULT) {

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            LogUtils.i("handleSignInResult:" + result.isSuccess());
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                LogUtils.i( acct.getDisplayName());
                String email = acct.getEmail();
                String profile_name = acct.getDisplayName();
                String id =  acct.getId();
                String profileImageURL = acct.getPhotoUrl().toString();
                GlobalPreferenceManager.setGoogleProfileInfo(email, profile_name , id, profileImageURL);
                //LogUtils.i("url: "+ profileImageURL);
                startActivity(new Intent(LoginActivity.this, LoginGoogleActivity.class));
            }

        }
    }


    @Override
    public void onSyncComplete(int syncPage, int response, Object data) {
        if(syncPage==Constant.SYNC_LOGIN){
            if(response == Constant.SUCCESS) {

                try {
                    JSONObject jsonObject = (JSONObject)data;
                    String UniqKey = jsonObject.getString("UniqKey");
                    String personId = jsonObject.getString("PersonId");
                    String Lname = jsonObject.getString("Lname");
                    String Mname = jsonObject.getString("Mname");
                    String Fname = jsonObject.getString("Fname");
                    String phone = jsonObject.getString("Phone");

                    String name = "";
                    if(Mname.compareTo("")==0)
                        name = Fname + " " + Lname;
                    else
                        name = Fname + " " + Mname + " " + Lname;

                    String imageUrl = Constant.SERVER_BASE_ADDRESS + "api/quicksoftuser/personimage?personId=" + personId +"&ext=png";
                    GlobalPreferenceManager.saveUniqueId(UniqKey);
                    GlobalPreferenceManager.saveProfilePic(imageUrl);
                    GlobalPreferenceManager.savePersonID(personId);
                    GlobalPreferenceManager.saveUserFullName(name);
                    GlobalPreferenceManager.saveUserPhone(phone);
                    GlobalPreferenceManager.saveUserEmail(editEmail.getText().toString());
                    GlobalPreferenceManager.setLoginType(Constant.LOGIN_NORMAL);
                    startActivity(new Intent(LoginActivity.this, OTPActivity.class));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            pDialog.dismiss();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}