package com.quicksoft.school.fragment.teacher;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.jeevandeshmukh.fancybottomsheetdialoglib.FancyBottomSheetDialog;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.quicksoft.school.R;
import com.quicksoft.school.activity.teacher.GroupAttendanceTeacherActivity;
import com.quicksoft.school.activity.teacher.IndividualAttendanceTeacherActivity;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.quicksoft.school.util.TimeUtil;
import com.quicksoft.school.view.fab.FloatingActionButton;
import com.shagi.materialdatepicker.date.DatePickerFragmentDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import br.liveo.interfaces.OnItemClickListener;
import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;
import mehdi.sakout.fancybuttons.FancyButton;


public class AttendanceFragmentTeacher extends Fragment implements View.OnClickListener {
	private OnItemClickListener mListener;
	private FancyButton btnClass, btnSection, btnDate;
	private CardView cardGroup, cardIndividual;

	public static AttendanceFragmentTeacher newInstance(String text){
		AttendanceFragmentTeacher mFragment = new AttendanceFragmentTeacher();
		Bundle mBundle = new Bundle();
		mFragment.setArguments(mBundle);
		return mFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_teacher_attendance, container, false);

		cardGroup = rootView.findViewById(R.id.cardGroup);
		cardIndividual = rootView.findViewById(R.id.cardIndividual);
        cardGroup.setOnClickListener(this);
        cardIndividual.setOnClickListener(this);

		setHasOptionsMenu(true);
		setupFAB(rootView);
		setupClass(rootView);
		setupSection(rootView);
		setupDate(rootView);

//		btnClass.setText("2");
//		btnSection.setText("A");
//		btnDate.setText("11-10-2018");

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


	public void setupClass(final View rootView){
		final String [] classArr = getClassDataFromJSON();
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
			if (view == cardIndividual) {
				Intent intent = new Intent(getActivity(), IndividualAttendanceTeacherActivity.class);
				intent.putExtra("CLASS", btnClass.getText().toString());
				intent.putExtra("SECTION", btnSection.getText().toString());
				intent.putExtra("DATE", btnDate.getText().toString());
				startActivity(intent);
			} else if (view == cardGroup) {
				Intent intent = new Intent(getActivity(), GroupAttendanceTeacherActivity.class);
				intent.putExtra("CLASS", btnClass.getText().toString());
				intent.putExtra("SECTION", btnSection.getText().toString());
				intent.putExtra("DATE", btnDate.getText().toString());
				startActivity(intent);
			}
		}
    }
}
