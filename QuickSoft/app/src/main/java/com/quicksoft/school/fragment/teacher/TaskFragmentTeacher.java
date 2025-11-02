package com.quicksoft.school.fragment.teacher;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.jeevandeshmukh.fancybottomsheetdialoglib.FancyBottomSheetDialog;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.leo.simplearcloader.ArcConfiguration;
import com.leo.simplearcloader.SimpleArcDialog;
import com.leo.simplearcloader.SimpleArcLoader;
import com.leon.lfilepickerlibrary.LFilePicker;
import com.quicksoft.school.R;
import com.quicksoft.school.activity.login.LoginActivity;
import com.quicksoft.school.activity.teacher.GroupAttendanceTeacherActivity;
import com.quicksoft.school.activity.teacher.IndividualAttendanceTeacherActivity;
import com.quicksoft.school.adapter.GroupAttendanceAdapter;
import com.quicksoft.school.adapter.SelectStudentAdapter;
import com.quicksoft.school.connection.SyncManager;
import com.quicksoft.school.connection.callback.SyncCompleteCallback;
import com.quicksoft.school.model.AttendanceStudent;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.quicksoft.school.util.TimeUtil;
import com.quicksoft.school.view.fab.FloatingActionButton;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.shagi.materialdatepicker.date.DatePickerFragmentDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.liveo.interfaces.OnItemClickListener;
import es.dmoral.toasty.Toasty;
import mehdi.sakout.fancybuttons.FancyButton;


public class TaskFragmentTeacher extends Fragment implements View.OnClickListener, SyncCompleteCallback {
	private OnItemClickListener mListener;
	private FancyButton btnClass, btnSection, btnDate, btnStudent;
	private FancyButton btnAttachment, btnPostTask;
	private MaterialEditText editTopic, editDesc;

	private ArrayList<AttendanceStudent> attendanceStudentArrayList;
	ArrayList<String> studentIdList = new ArrayList<String>();
	boolean isAllStudent=false;

	private SimpleArcDialog pDialog;
	private SyncManager mSyncManager;

	public static TaskFragmentTeacher newInstance(String text){
		TaskFragmentTeacher mFragment = new TaskFragmentTeacher();
		Bundle mBundle = new Bundle();
		mFragment.setArguments(mBundle);
		return mFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_teacher_task, container, false);

		setHasOptionsMenu(true);
		setupFAB(rootView);
		setupClass(rootView);
		setupSection(rootView);
		setupDate(rootView);
		setupStudent(rootView);

		editTopic = rootView.findViewById(R.id.editTopic);
		editDesc = rootView.findViewById(R.id.editDesc);
		btnAttachment = rootView.findViewById(R.id.btnAttachment);
		btnPostTask = rootView.findViewById(R.id.btnPostTask);
		btnAttachment.setOnClickListener(this);
		btnPostTask.setOnClickListener(this);

		return rootView;		
	}

	public String[] getClassDataFromJSON(){
		String data = GlobalPreferenceManager.getTeacherClasses();
		LogUtils.i(data);
		try {
			JSONObject jsonObject = new JSONObject(data);
			JSONArray array = jsonObject.getJSONArray("ClassArray");
			String[] classArray = new String[array.length()];
			for (int i = 0; i < array.length(); ++i) {
				classArray[i] = array.getString(i);
			}
			return classArray;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new String[0];
	}

	public String[] getSectionDataFromJSON(String classs){
		String data = GlobalPreferenceManager.getTeacherClasses();
		String[] sectionArray = null;
		try {
			JSONObject jsonObject = new JSONObject(data);
			JSONArray array = jsonObject.getJSONArray("ClassList");
			ArrayList<String> examArrayList = new ArrayList<>();
			for (int i=0; i<array.length(); i++){
				JSONObject object = array.getJSONObject(i);
				String Class = object.getString("Class");
				if(classs.compareTo(Class)==0){
					JSONArray sectionJSONArray = object.getJSONArray("SectionList");
					sectionArray = new String[sectionJSONArray.length()];
					for (int j = 0; j < sectionJSONArray.length(); ++j) {
						sectionArray[j] = sectionJSONArray.getString(j);
					}
				}
			}

			return sectionArray;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new String[0];
	}

	public void setupStudent(View rootView){

		btnStudent = rootView.findViewById(R.id.btnStudent);
		btnStudent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				String classs = btnClass.getText().toString();
				String section = btnSection.getText().toString();

				if(btnClass.getText().equals("Select Class") || btnSection.getText().equals("Select Section") || btnDate.getText().equals("Select Date")){
					new FancyBottomSheetDialog.Builder(getActivity())
							.setMessage("Please select Class, Section and Date")
							.setBackgroundColor(getResources().getColor(R.color.colorPrimary))
							.setIcon(R.drawable.ic_assignment_late_white_24dp,true)
							.setPositiveBtnText("Ok")
							.setNegativeBtnText("")
							.setPositiveBtnBackground(getResources().getColor(R.color.okButtonColor))
							.build();
				}else {
					if(NetworkUtils.isConnected()) {
						mSyncManager = new SyncManager(getActivity(), TaskFragmentTeacher.this);

						pDialog = new SimpleArcDialog(getActivity());
						ArcConfiguration configuration = new ArcConfiguration(getActivity());
						configuration.setLoaderStyle(SimpleArcLoader.STYLE.COMPLETE_ARC);
						configuration.setText("Please wait..");
						pDialog.setConfiguration(configuration);
						pDialog.setCancelable(false);
						pDialog.show();
						String email = GlobalPreferenceManager.getUserEmail();
						String uniqueID = GlobalPreferenceManager.getUniqueId();
						LogUtils.i(email + " " + uniqueID);
						mSyncManager.teacherGetStudent(email, uniqueID, classs, section);
					}else{
						Toasty.error(getActivity(),"Check your internet connection", Toast.LENGTH_LONG).show();
					}
				}
			}
		});

	}

	@Override
	public void onSyncComplete(int syncPage, int response, Object data) {
		if(syncPage==Constant.SYNC_TEACHER_STUDENT_LIST){
			if(response == Constant.SUCCESS) {
				LogUtils.i(((JSONObject)data).toString());
				attendanceStudentArrayList = new ArrayList<>();
				attendanceStudentArrayList.clear();
				attendanceStudentArrayList.add(new AttendanceStudent(-1, "Select All", "", ""));
		 		try {
					JSONObject jsonObject = (JSONObject)data;
					JSONArray studentRollArray = jsonObject.getJSONArray("StudentRollList");

					JSONArray array = jsonObject.getJSONArray("StudentInfoList");
					for (int j=0; j<studentRollArray.length(); j++) {
						for (int i = 0; i < array.length(); i++) {
							JSONObject object = array.getJSONObject(i);
							int RollNo = object.getInt("RollNo");
							if(studentRollArray.getInt(j)==RollNo) {
								String personId = object.getString("PersonId");
								String studentID = object.getString("StudentID");
								String Lname = object.getString("StudentLName");
								String Mname = object.getString("StudentMName");
								String Fname = object.getString("StudentFName");


								String name = "";
								if (Mname.compareTo("") == 0)
									name = Fname + " " + Lname;
								else
									name = Fname + " " + Mname + " " + Lname;

								String imageUrl = Constant.SERVER_BASE_ADDRESS + "api/quicksoftuser/personimage?personId=" + personId + "&ext=png";

								AttendanceStudent attendanceStudent = new AttendanceStudent(RollNo, name, imageUrl, studentID);
								attendanceStudentArrayList.add(attendanceStudent);
							}
						}
					}
					setupListView();
				} catch (JSONException e) {
					e.printStackTrace();
				}

				pDialog.dismiss();
			}else if(response == Constant.FAIL){
				int respCode =  (int)data;
				if(respCode ==401 || respCode ==403){
					GlobalPreferenceManager.setUserLoggedIn(false);
					GlobalPreferenceManager.setUserType(-1);
					GlobalPreferenceManager.setLoginType(-1);
					Toasty.error(getActivity(),"Please login again..", Toast.LENGTH_LONG).show();
					startActivity(new Intent(getActivity(), LoginActivity.class));
					getActivity().finish();
				}
			}
		}else if(syncPage==Constant.SYNC_TEACHER_TASK){
			if(response == Constant.SUCCESS) {
				LogUtils.i(((JSONObject) data).toString());
//				btnClass.setText("Select Class");
//				btnDate.setText("Select Date");
//				btnSection.setText("Select Section");
				editDesc.setText("");
				editTopic.setText("");
				pDialog.dismiss();
				Toasty.info(getActivity(),"Task submitted", Toast.LENGTH_LONG).show();
			}
		}

		if(response == Constant.FAIL || response == Constant.NETWORK_FAIL){
			pDialog.dismiss();
		}
	}

	public void setupListView(){
		RecyclerView recyclerView = new RecyclerView(getActivity());
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		SelectStudentAdapter groupAttendanceAdapter = new SelectStudentAdapter(getActivity(), attendanceStudentArrayList);
		recyclerView.setAdapter(groupAttendanceAdapter);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(recyclerView);
		builder.setTitle("Select Student");
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int position) {

				studentIdList.clear();
				if(attendanceStudentArrayList.get(0).getChecked())
					isAllStudent=true;
				else
					isAllStudent=false;

				for(int i=1; i<attendanceStudentArrayList.size(); i++){
					if(attendanceStudentArrayList.get(i).getChecked()){
						studentIdList.add(attendanceStudentArrayList.get(i).getStudentId());
					}
				}
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {

			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}

	public void setupClass(final View rootView){
		final String [] classArr =  getClassDataFromJSON();
		btnClass = rootView.findViewById(R.id.btnClass);
		btnClass.setText(GlobalPreferenceManager.getTeacherClass());
        btnClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("Class");
                dialog.setSingleChoiceItems(classArr, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        btnClass.setText(classArr[position]);
						setupSection(rootView);
						btnSection.setText("Select Section");
                        dialog.dismiss();
                    }

                });
                AlertDialog alert = dialog.create();
                alert.show();
            }
        });

	}

	public void setupSection(View rootView){
		String classs = btnClass.getText().toString();
		final String [] secArr = getSectionDataFromJSON(classs);
		btnSection = rootView.findViewById(R.id.btnSection);
		btnSection.setText(GlobalPreferenceManager.getTeacherSection());
		btnSection.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
				dialog.setTitle("Section");
				dialog.setSingleChoiceItems(secArr, -1, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int position) {
						btnSection.setText(secArr[position]);
						dialog.dismiss();
					}

				});
				AlertDialog alert = dialog.create();
				alert.show();
			}
		});
	}

	public void setupDate(View rootView){

		btnDate = rootView.findViewById(R.id.btnDate);
		btnDate.setText(TimeUtil.getTodaysDate());
		btnDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Calendar now = Calendar.getInstance();
				DatePickerFragmentDialog dpd = DatePickerFragmentDialog.newInstance(
						new DatePickerFragmentDialog.OnDateSetListener() {
							@Override
							public void onDateSet(DatePickerFragmentDialog view, int year, int monthOfYear, int dayOfMonth) {
								btnDate.setText(dayOfMonth+"-"+(monthOfYear+1)+"-"+year);
							}
						},
						now.get(Calendar.YEAR),
						now.get(Calendar.MONTH),
						now.get(Calendar.DAY_OF_MONTH)
				);
				dpd.show(getFragmentManager(),"asd");
			}
		});

	}

	public void setupFAB(View rootView){
		FloatingActionButton fabDashboard = rootView.findViewById(R.id.fabDashboard);
		FloatingActionButton fabAttendance = rootView.findViewById(R.id.fabAttendance);
		FloatingActionButton fabProject = rootView.findViewById(R.id.fabProject);
		FloatingActionButton fabTask = rootView.findViewById(R.id.fabTask);
		FloatingActionButton fabNotice = rootView.findViewById(R.id.fabNotice);
		FloatingActionButton fabWallet = rootView.findViewById(R.id.fabWallet);
		fabDashboard.setImageDrawable(new IconDrawable(getActivity(), MaterialIcons.md_dashboard).colorRes(R.color.colorWhite));
		fabAttendance.setImageDrawable(new IconDrawable(getActivity(), MaterialIcons.md_assessment).colorRes(R.color.colorWhite));
		fabProject.setImageDrawable(new IconDrawable(getActivity(), MaterialIcons.md_business).colorRes(R.color.colorWhite));
		fabTask.setImageDrawable(new IconDrawable(getActivity(), MaterialIcons.md_book).colorRes(R.color.colorWhite));
		fabNotice.setImageDrawable(new IconDrawable(getActivity(), MaterialIcons.md_assignment_late).colorRes(R.color.colorWhite));
		fabWallet.setImageDrawable(new IconDrawable(getActivity(), MaterialIcons.md_credit_card).colorRes(R.color.colorWhite));

		fabDashboard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mListener.onItemClick(Constant.TEACHER_DASHBOARD);
			}
		});
		fabAttendance.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mListener.onItemClick(Constant.TEACHER_ATTENDANCE);
			}
		});
		fabProject.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

			}
		});
		fabTask.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

			}
		});
		fabNotice.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mListener.onItemClick(Constant.TEACHER_NOTICE);
			}
		});
		fabWallet.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

			}
		});
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnItemClickListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;

	}

    @Override
    public void onClick(View view) {

		if(view == btnPostTask){
			if(btnClass.getText().equals("Select Class") || btnSection.getText().equals("Select Section") || btnDate.getText().equals("Select Date")){
				new FancyBottomSheetDialog.Builder(getActivity())
						.setMessage("Please select Class, Section and Date")
						.setBackgroundColor(getResources().getColor(R.color.colorPrimary))
						.setIcon(R.drawable.ic_assignment_late_white_24dp,true)
						.setPositiveBtnText("Ok")
						.setNegativeBtnText("")
						.setPositiveBtnBackground(getResources().getColor(R.color.okButtonColor))
						.build();
			}else if(editTopic.getText().toString().compareTo("")==0){
				new FancyBottomSheetDialog.Builder(getActivity())
						.setMessage("Did you forget to add Topic/Task?")
						.setBackgroundColor(getResources().getColor(R.color.colorPrimary))
						.setIcon(R.drawable.ic_assignment_late_white_24dp,true)
						.setPositiveBtnText("Ok")
						.setNegativeBtnText("")
						.setPositiveBtnBackground(getResources().getColor(R.color.okButtonColor))
						.build();
			}else {

				pDialog = new SimpleArcDialog(getActivity());
				ArcConfiguration configuration = new ArcConfiguration(getActivity());
				configuration.setLoaderStyle(SimpleArcLoader.STYLE.COMPLETE_ARC);
				configuration.setText("Please wait..");
				pDialog.setConfiguration(configuration);
				pDialog.setCancelable(false);
				pDialog.show();

				String email = GlobalPreferenceManager.getUserEmail();
				String uniqueID = GlobalPreferenceManager.getUniqueId();
				String classs = btnClass.getText().toString();
				String section = btnSection.getText().toString();
				String dates = btnDate.getText().toString();
				String dueDate = TimeUtil.getReverseFormattedDate(dates);

				mSyncManager.teacherPostTask(email, uniqueID, classs, section, dueDate, editTopic.getText().toString(), editDesc.getText().toString(), isAllStudent, studentIdList);
			}

		}else if(view == btnAttachment){
			new LFilePicker()
					.withActivity(getActivity())
					.withRequestCode(Constant.EX_FILE_PICKER_RESULT)
					.withStartPath("/storage/emulated/0/Download")
					.withIsGreater(false)
					.withFileSize(500 * 1024)
					.start();
		}
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constant.EX_FILE_PICKER_RESULT) {
			List<String> list = data.getStringArrayListExtra("paths");
			btnAttachment.setText(list.size() + " file");
		}
	}
}
