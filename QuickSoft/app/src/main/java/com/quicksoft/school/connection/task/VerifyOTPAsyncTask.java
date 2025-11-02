package com.quicksoft.school.connection.task;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.quicksoft.school.connection.callback.TaskCompleteCallback;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
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

public class VerifyOTPAsyncTask extends AsyncTask<Void, Void, Integer>{


	private String  mEmail, uniqueID;
	private int mTask;
	private int failResult;
	private String otp;
	private JSONArray jArray;
	public TaskCompleteCallback mTaskCompleteCallback;
	Context mContext;
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	JSONObject jObjectResult;

	public VerifyOTPAsyncTask(Context context, int task, String email, String uniqueID, String otp) {
		mContext = context;
		mTask = task;
		mEmail = email;
		this.uniqueID = uniqueID;
		this.otp = otp;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		JSONObject jsonLoginInfo = new JSONObject();
		try {
			jsonLoginInfo.put("UserID", mEmail);
			jsonLoginInfo.put("UniqKey", uniqueID);
			jsonLoginInfo.put("OTP", otp);
		} catch (Exception e) {
			e.printStackTrace();
		}

		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(JSON, jsonLoginInfo.toString());
		Request request = new Request.Builder()
				.url(Constant.SERVER_BASE_ADDRESS+"api/quicksoftuser/verifyotp")
				.post(body)
				.build();

//		LogUtils.i(request.toString());
		try {
			Response response = client.newCall(request).execute();
//			LogUtils.i(response.body().string());
			jObjectResult = new JSONObject(response.body().string());
			int respCode = jObjectResult.getInt("RespCode");
			uniqueID = jObjectResult.getString("UniqKey");
			if (respCode == 200) {
				GlobalPreferenceManager.saveUniqueId(uniqueID);
				return  Constant.SUCCESS;
			} else {
				failResult = respCode;
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
			Toast.makeText(mContext, "Server connection error", Toast.LENGTH_SHORT).show();
			mTaskCompleteCallback.onTaskCompleteCallback(mTask, Constant.FAIL, failResult);
		} else if (result == Constant.NETWORK_FAIL) {
			Toast.makeText(mContext, "Server connection error", Toast.LENGTH_SHORT).show();
			mTaskCompleteCallback.onTaskCompleteCallback(mTask, Constant.FAIL, Constant.FAIL);
		}
	}

}