package com.quicksoft.school.fragment.teacher;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.quicksoft.school.R;
import com.quicksoft.school.model.FacultyParent;
import com.quicksoft.school.model.ScheduleTeacher;
import com.quicksoft.school.model.TaskTeacher;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.quicksoft.school.util.TimeUtil;
import com.quicksoft.school.view.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import br.liveo.interfaces.OnItemClickListener;
import android.content.Intent;
import android.graphics.Bitmap;

public class DashboardFragmentTeacher extends Fragment {
	private OnItemClickListener mListener;
	private ArrayList<ScheduleTeacher> scheduleTeacherArrayList;
	private ArrayList<TaskTeacher> taskTeacherArrayList;

    LinearLayout scheduleLayout, taskLayout;

	public static DashboardFragmentTeacher newInstance(String text){
		DashboardFragmentTeacher mFragment = new DashboardFragmentTeacher();
		Bundle mBundle = new Bundle();
		mFragment.setArguments(mBundle);
		return mFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_teacher_dashboard, container, false);
        scheduleLayout = rootView.findViewById(R.id.llayout);
		taskLayout = rootView.findViewById(R.id.taskllayout);

		setHasOptionsMenu(true);
		setupFAB(rootView);

		testData();

		getDataFromJSON();
		return rootView;		
	}

    public void getDataFromJSON(){
        String data = GlobalPreferenceManager.getTeacherDashBoardInfo();
        LogUtils.i(data);
        scheduleTeacherArrayList = new ArrayList<>();
		taskTeacherArrayList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(data);
            String teacherClass = jsonObject.getString("Class");
			String teacherSection = jsonObject.getString("Section");
			GlobalPreferenceManager.saveTeacherClass(teacherClass);
			GlobalPreferenceManager.saveTeacherSection(teacherSection);

            JSONArray array = jsonObject.getJSONArray("TimeTableToday");
            for (int i=0; i<array.length(); i++){
                JSONObject object = array.getJSONObject(i);
                String From = object.getString("From");
                String To = object.getString("To");
               // String Day = object.getString("Day");
                String Subject = object.getString("Subject");
                String Class_Section = object.getString("Class_Section");
               // String Description = object.getString("Description");


				String startTime = From.substring(0, From.length() - 3);
				String endTime = To.substring(0, To.length() - 3);

                ScheduleTeacher scheduleTeacher = new ScheduleTeacher(Subject,startTime +" - "+ endTime, Class_Section);
                scheduleTeacherArrayList.add(scheduleTeacher);
            }

			JSONArray incompleteTasksArray = jsonObject.getJSONArray("IncompleteTasks");
			for (int i=0; i<incompleteTasksArray.length(); i++) {
				JSONObject object = incompleteTasksArray.getJSONObject(i);
				String Class = object.getString("Class");
				String Section = object.getString("Section");
				String Heading = object.getString("Heading");
				String TaskDetail = object.getString("TaskDetail");
				String StudentFName = object.getString("StudentFName");
				String StudentMName = "";//object.getString("StudentMName");
				String StudentLName = object.getString("StudentLName");
				String DueDate = object.getString("DueDate");
				String Subject = object.getString("Subject");

				String name = "";
				if(StudentMName.compareTo("")==0)
					name = StudentFName + " " + StudentLName;
				else
					name = StudentFName + " " + StudentMName + " " + StudentLName;

				String ddate = TimeUtil.getFormattedDate(DueDate);

				TaskTeacher taskTeacher = new TaskTeacher(name, Heading, ddate, Class, Section, Subject,TaskDetail);
				taskTeacherArrayList.add(taskTeacher);
			}

        } catch (JSONException e) {
            e.printStackTrace();
        }

        setupSchedule();
		setupTask();
    }

	public void setupSchedule(){
		LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		scheduleLayout.removeAllViews();
		for(int i=0; i<scheduleTeacherArrayList.size(); i++){
			View v = vi.inflate(R.layout.schedule_item_teacher, null);
			((TextView)v.findViewById(R.id.tvTitle)).setText(scheduleTeacherArrayList.get(i).getScheduleTitle());
			((TextView)v.findViewById(R.id.tvTime)).setText(scheduleTeacherArrayList.get(i).getTime());
			((TextView)v.findViewById(R.id.tvClass)).setText(scheduleTeacherArrayList.get(i).getClassRoom());
			scheduleLayout.addView(v);
		}
	}

	public void setupTask(){

		LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		taskLayout.removeAllViews();

		for(int i=0; i<taskTeacherArrayList.size(); i++){
			View v = vi.inflate(R.layout.task_item_teacher, null);
			((TextView)v.findViewById(R.id.tvTitle)).setText(taskTeacherArrayList.get(i).getTaskTile());
			((TextView)v.findViewById(R.id.tvClass)).setText(taskTeacherArrayList.get(i).getTaskClass()+" - "+ taskTeacherArrayList.get(i).getTaskSection() +" ("+ taskTeacherArrayList.get(i).getTaskStudentName()+")");
			((TextView)v.findViewById(R.id.tvDate)).setText(taskTeacherArrayList.get(i).getTaskDate());
			if(taskTeacherArrayList.get(i).getTaskSubject().compareTo("null")!=0) {
                ((TextView) v.findViewById(R.id.tvSubject)).setText(taskTeacherArrayList.get(i).getTaskSubject());
            }
			((TextView)v.findViewById(R.id.tvDescription)).setText(taskTeacherArrayList.get(i).getTaskDescription());
			taskLayout.addView(v);
		}
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
				mListener.onItemClick(Constant.TEACHER_TASK);
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
				Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(cameraIntent, Constant.TEACHER_CAMERA_PIC_REQUEST);
			}
			protected void onActivityResult(int requestCode, int resultCode, Intent data) {
				if (requestCode == Constant.TEACHER_CAMERA_PIC_REQUEST) {
					Bitmap image = (Bitmap) data.getExtras().get("data");
					//ImageView imageview = (ImageView) findViewById(R.id.ImageView01); //sets imageview as the bitmap
					//imageview.setImageBitmap(image);
				}
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


	public void testData(){
//		scheduleTeacherArrayList = new ArrayList<>();
//		ScheduleTeacher st1 = new ScheduleTeacher(0, "Geography","09:00 - 10:00", "IV - A");
//		ScheduleTeacher st2 = new ScheduleTeacher(1, "History","11:00 - 12:00", "IV - B");
//		ScheduleTeacher st3 = new ScheduleTeacher(2, "Physical Science","13:00 - 14:00", "V - C");
//		scheduleTeacherArrayList.add(st1);
//		scheduleTeacherArrayList.add(st2);
//		scheduleTeacherArrayList.add(st3);

		taskTeacherArrayList = new ArrayList<>();
		TaskTeacher tt1 = new TaskTeacher(0, "Physics homework", "12-08-2018", "V", "A", "Physics","Only first  two chapter from the book.");
		TaskTeacher tt2 = new TaskTeacher(0, "Math homework & Class test", "14-08-2018", "VII", "A", "Maths","Open book test");
		taskTeacherArrayList.add(tt1);
		taskTeacherArrayList.add(tt2);
	}
}
