package com.quicksoft.school.connection.task;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.quicksoft.school.connection.callback.TaskCompleteCallback;
import com.quicksoft.school.util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginAsyncTask extends AsyncTask<Void, Void, Integer>{

	
	private String  mEmail;
	private String mPassword, mUserType, mFBId, mGoogleId, mId;
	private int mTask;
	private JSONArray jArray;
	private JSONObject jObjectResult;
	public TaskCompleteCallback mTaskCompleteCallback;
	Context mContext;
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	public LoginAsyncTask(Context context, int task, String email, String password, String userType, String fbId, String googleId, String id) {
		mContext = context;
		mEmail = email;
		mPassword = password;
		mTask = task;
		mUserType = userType;
		mFBId = fbId;
		mGoogleId = googleId;
		mId = id;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		JSONObject jsonLoginInfo = new JSONObject();
		try {
			jsonLoginInfo.put("UserType", mUserType);
			jsonLoginInfo.put("FacebookId", mFBId);
			jsonLoginInfo.put("GoogleId", mGoogleId);
			jsonLoginInfo.put("UserId", mEmail);
			jsonLoginInfo.put("Password", mPassword);
			jsonLoginInfo.put("Id", mId);
		} catch (Exception e) {
			e.printStackTrace();
		}

		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(JSON, jsonLoginInfo.toString());
		Request request = new Request.Builder()
				.url(Constant.SERVER_BASE_ADDRESS+"api/quicksoftuser/loginuser")
				.post(body)
				.build();
		try {
			Response response = client.newCall(request).execute();
			//LogUtils.i(response.body().string());
			jObjectResult = new JSONObject(response.body().string());
			LogUtils.i(jObjectResult.toString());
			int respCode = jObjectResult.getInt("RespCode");
			//String uniqueID = jObjectResult.getString("UniqKey");
			if (respCode == 200) {
				return  Constant.SUCCESS;
			} else {
				return  Constant.FAIL;
			}
		} catch (JSONException e) {
			LogUtils.i("Error: "+e.toString());
			return  Constant.NETWORK_FAIL;
		} catch (IOException e) {
			LogUtils.i("Error: "+e.toString());
			return  Constant.NETWORK_FAIL;
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		if (result == Constant.SUCCESS) {
			mTaskCompleteCallback.onTaskCompleteCallback(mTask, Constant.SUCCESS, jObjectResult);
		} else if (result == Constant.FAIL) {
			Toast.makeText(mContext, "Wrong username or password", Toast.LENGTH_SHORT).show();
			mTaskCompleteCallback.onTaskCompleteCallback(mTask, Constant.FAIL, Constant.FAIL);
		} else if (result == Constant.NETWORK_FAIL) {
			Toast.makeText(mContext, "Server connection error", Toast.LENGTH_SHORT).show();
			mTaskCompleteCallback.onTaskCompleteCallback(mTask, Constant.FAIL, Constant.FAIL);
		}
	}

}