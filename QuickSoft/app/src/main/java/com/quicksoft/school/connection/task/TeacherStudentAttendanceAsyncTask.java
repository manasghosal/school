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

public class TeacherStudentAttendanceAsyncTask extends AsyncTask<Void, Void, Integer>{


	private String  mEmail, uniqueID, classs, section, date;
	private int mTask;
	private JSONArray jArray;
	public TaskCompleteCallback mTaskCompleteCallback;
	Context mContext;
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	JSONObject jObjectResult;

	public TeacherStudentAttendanceAsyncTask(Context context, int task, String email, String uniqueID, String classs, String section, String date) {
		mContext = context;
		mTask = task;
		mEmail = email;
		this.classs = classs;
		this.section = section;
		this.uniqueID = uniqueID;
		this.date = date;
	}

	@Override
	protected Integer doInBackground(Void... params) {
		JSONObject jsonLoginInfo = new JSONObject();
		try {
			jsonLoginInfo.put("UserID", mEmail);
			jsonLoginInfo.put("UniqKey", uniqueID);
			jsonLoginInfo.put("Class", classs);
			jsonLoginInfo.put("Section", section);
			jsonLoginInfo.put("AttendanceDate", date);
		} catch (Exception e) {
			e.printStackTrace();
		}

		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(JSON, jsonLoginInfo.toString());
		Request request = new Request.Builder()
				.url(Constant.SERVER_BASE_ADDRESS+"api/quicksoftuser/loadattendanceteacher")
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
			mTaskCompleteCallback.onTaskCompleteCallback(mTask, Constant.FAIL, Constant.FAIL);
		} else if (result == Constant.NETWORK_FAIL) {
			Toast.makeText(mContext, "Server connection error", Toast.LENGTH_SHORT).show();
			mTaskCompleteCallback.onTaskCompleteCallback(mTask, Constant.FAIL, Constant.FAIL);
		}
	}

}