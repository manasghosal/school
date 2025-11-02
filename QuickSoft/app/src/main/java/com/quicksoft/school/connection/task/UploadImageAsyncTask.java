package com.quicksoft.school.connection.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.quicksoft.school.connection.callback.TaskCompleteCallback;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadImageAsyncTask extends AsyncTask<Void, Void, Integer>{


	private String  personID, fileExt;
	private int mTask;
	private int failResult;
	private Uri imageUri;
	private JSONArray jArray;
	public TaskCompleteCallback mTaskCompleteCallback;
	Context mContext;
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	JSONObject jObjectResult;

	public UploadImageAsyncTask(Context context, int task, String personID, String fileExt, Uri uri) {
		mContext = context;
		mTask = task;
		this.personID = personID;
		this.fileExt = fileExt;
		this.imageUri = uri;
	}

	@Override
	protected Integer doInBackground(Void... params) {

		String base64Image =  getBae64Data();
		JSONObject jsonLoginInfo = new JSONObject();
		try {
			jsonLoginInfo.put("PersonId", personID);
			jsonLoginInfo.put("FileExt", fileExt);
			jsonLoginInfo.put("ImageData", base64Image);
		} catch (Exception e) {
			e.printStackTrace();
		}

		OkHttpClient client = new OkHttpClient();
		RequestBody body = RequestBody.create(JSON, jsonLoginInfo.toString());
		Request request = new Request.Builder()
				.url(Constant.SERVER_BASE_ADDRESS+"api/quicksoftuser/personimage")
				.post(body)
				.build();

		LogUtils.i(jsonLoginInfo.toString());
		try {
			Response response = client.newCall(request).execute();
//			LogUtils.i(response.body().string());
//			jObjectResult = new JSONObject(response.body().string());
//			int respCode = jObjectResult.getInt("RespCode");
//			uniqueID = jObjectResult.getString("UniqKey");
//			if (respCode == 200) {
//				GlobalPreferenceManager.saveUniqueId(uniqueID);
//				return  Constant.SUCCESS;
//			} else {
//				failResult = respCode;
//				return  Constant.FAIL;
//			}
			return  Constant.SUCCESS;
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

	public String getBae64Data(){
		InputStream imageStream;
		try {
			imageStream = mContext.getContentResolver().openInputStream(imageUri);
			final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
			String encodedImage = encodeImage(selectedImage);
			return encodedImage;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "";
		}
	}

	private String encodeImage(Bitmap bm)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
		byte[] b = baos.toByteArray();
		String encImage = Base64.encodeToString(b, Base64.DEFAULT);

		return encImage;
	}

}