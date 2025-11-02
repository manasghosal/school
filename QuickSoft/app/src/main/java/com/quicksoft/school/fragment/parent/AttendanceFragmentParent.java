package com.quicksoft.school.fragment.parent;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.quicksoft.school.R;
import com.quicksoft.school.model.MonthlyAttendanceParent;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.quicksoft.school.util.TimeUtil;
import com.quicksoft.school.view.ProgressButton;
import com.quicksoft.school.view.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import br.liveo.interfaces.OnItemClickListener;


public class AttendanceFragmentParent extends Fragment implements View.OnClickListener {

    private ProgressButton btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn10, btn11, btn12;
    private TextView tvYear, tvMonth;
    private ArrayList<ProgressButton> arrayListButton = new ArrayList<>();
	private ArrayList<MonthlyAttendanceParent> monthlyAttendanceArrayList = new ArrayList<>();
	private CompactCalendarView calendarView;

	private OnItemClickListener mListener;

	public static AttendanceFragmentParent newInstance(){
		AttendanceFragmentParent mFragment = new AttendanceFragmentParent();
		return mFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_parent_attendance, container, false);
		tvYear = rootView.findViewById(R.id.tvYear);
		tvMonth = rootView.findViewById(R.id.tvMonth);
		calendarView = rootView.findViewById(R.id.calendarView);

		setHasOptionsMenu(true);
		initCalendarButtons(rootView);
		setupFAB(rootView);

		getDataFromJSON();
		setupAttendancePercentage();
		setupAttendanceCalendar();

		return rootView;		
	}

	public void getDataFromJSON(){
		String data = GlobalPreferenceManager.getParentAttendance();
		try {
			JSONObject jsonObject = new JSONObject(data);
			JSONArray array = jsonObject.getJSONArray("MonthAttendanceList");
			for (int i=0; i<array.length(); i++){
				JSONObject object = array.getJSONObject(i);
				int month = object.getInt("Month");
				int parcent = object.getInt("AttendancePercent");
				JSONArray jArray = object.getJSONArray("Attendance");

				int [] monthArray = new int[jArray.length()];
				for (int k = 0; k < jArray.length(); k++) {
					monthArray[k] = jArray.getInt(k);
				}

				MonthlyAttendanceParent monthlyAttendanceParent = new MonthlyAttendanceParent(parcent,monthArray,month);
				monthlyAttendanceArrayList.add(monthlyAttendanceParent);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void setupAttendancePercentage(){
		for(int i=0; i<12; i++){
			for(int j=0; j<monthlyAttendanceArrayList.size(); j++){
				if(monthlyAttendanceArrayList.get(j).getMonth()==i+1){
					arrayListButton.get(i).setMaximumPercentage(monthlyAttendanceArrayList.get(j).getPresentPercentage()/100.0f);
				}
			}
		}
	}

	public void initCalendarButtons(View rootView){

		Calendar currentCalender = Calendar.getInstance(Locale.getDefault());
		currentCalender.setTime(new Date());
		int curYear = currentCalender.get(Calendar.YEAR);
		tvYear.setText(""+curYear);

		btn1 = rootView.findViewById(R.id.btn1);
		btn2 = rootView.findViewById(R.id.btn2);
		btn3 = rootView.findViewById(R.id.btn3);
		btn4 = rootView.findViewById(R.id.btn4);
		btn5 = rootView.findViewById(R.id.btn5);
		btn6 = rootView.findViewById(R.id.btn6);
		btn7 = rootView.findViewById(R.id.btn7);
		btn8 = rootView.findViewById(R.id.btn8);
		btn9 = rootView.findViewById(R.id.btn9);
		btn10 = rootView.findViewById(R.id.btn10);
		btn11 = rootView.findViewById(R.id.btn11);
		btn12 = rootView.findViewById(R.id.btn12);

		arrayListButton.add(btn1);
		arrayListButton.add(btn2);
		arrayListButton.add(btn3);
		arrayListButton.add(btn4);
		arrayListButton.add(btn5);
		arrayListButton.add(btn6);
		arrayListButton.add(btn7);
		arrayListButton.add(btn8);
		arrayListButton.add(btn9);
		arrayListButton.add(btn10);
		arrayListButton.add(btn11);
		arrayListButton.add(btn12);

		btn1.setText("Jan");
		btn2.setText("Feb");
		btn3.setText("Mar");
		btn4.setText("Apr");
		btn5.setText("May");
		btn6.setText("Jun");
		btn7.setText("Jul");
		btn8.setText("Aug");
		btn9.setText("Sep");
		btn10.setText("Oct");
		btn11.setText("Nov");
		btn12.setText("Dec");

		for(int i=0; i< arrayListButton.size(); i++){
			arrayListButton.get(i).setTextColor(getResources().getColor(R.color.colorWhite));
			arrayListButton.get(i).setProgressColor(getResources().getColor(R.color.white54));
			arrayListButton.get(i).useRoundedRectangleShape(20.0f);
			arrayListButton.get(i).setGravity(Gravity.CENTER);
			arrayListButton.get(i).setProgressBackgroundColor(getResources().getColor(R.color.darkSlateBlue));

			arrayListButton.get(i).setOnClickListener(this);
		}
	}


	public void setupAttendanceCalendar(){

		tvMonth.setText(TimeUtil.getMonthInString(TimeUtil.getCurrentMonth()));

		calendarView.setFirstDayOfWeek(Calendar.SUNDAY);
		calendarView.displayOtherMonthDays(false);
		calendarView.shouldScrollMonth(true);
		calendarView.setEventIndicatorStyle(CompactCalendarView.FILL_LARGE_INDICATOR);


		SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
		Calendar currentCalender = Calendar.getInstance(Locale.getDefault());
		currentCalender.setTime(new Date());
		int curYear = currentCalender.get(Calendar.YEAR);

		for(int i=0; i<12; i++){
			for(int j=0; j<monthlyAttendanceArrayList.size(); j++){
				if(monthlyAttendanceArrayList.get(j).getMonth()==i+1){
					int daysInMonth = TimeUtil.getNoOfDaysInMonth(i,curYear);
					try {
						int [] monthArray = monthlyAttendanceArrayList.get(j).getAttendanceForMonth();
						for (int count=0; count<daysInMonth; count++){
							//LogUtils.i(monthArray[i]);
							if(monthArray[count]==3){
								Date d = f.parse((count+1)+"-"+(i+1)+"-"+curYear);
								Event ev = new Event(Color.GREEN, d.getTime());
								calendarView.addEvent(ev);
							}else if(monthArray[count]==2){
								Date d = f.parse((count+1)+"-"+(i+1)+"-"+curYear);
								Event ev = new Event(Color.CYAN, d.getTime());
								calendarView.addEvent(ev);
							}else if(monthArray[count]==1){
								Date d = f.parse((count+1)+"-"+(i+1)+"-"+curYear);
								Event ev = new Event(Color.RED, d.getTime());
								calendarView.addEvent(ev);
							}
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}


				}
			}
		}

		calendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
			@Override
			public void onDayClick(Date dateClicked) {

			}

			@Override
			public void onMonthScroll(Date firstDayOfNewMonth) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(firstDayOfNewMonth);
				tvMonth.setText(TimeUtil.getMonthInString(calendar.get(Calendar.MONTH)));
			}
		});
	}


	@Override
	public void onClick(View view) {
		for(int i=0; i< arrayListButton.size(); i++){
			if(arrayListButton.get(i)==view){
				tvMonth.setText(TimeUtil.getMonthInString(i));

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(calendarView.getFirstDayOfCurrentMonth());
                int curMonth = calendar.get(Calendar.MONTH);
                if(i<curMonth) {
                    int roll = curMonth -i;
                    for (int j = 0; j < roll; j++) {
                        calendarView.scrollLeft();
                    }
                }else if(i>curMonth) {
                    int roll = i - curMonth;
                    for (int j = 0; j < roll; j++) {
                        calendarView.scrollRight();
                    }
                }
				break;
			}
		}
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

	public void setupFAB(View rootView){
		FloatingActionButton fabDashboard = rootView.findViewById(R.id.fabDashboard);
		FloatingActionButton fabPayment = rootView.findViewById(R.id.fabPayment);
		FloatingActionButton fabTrack = rootView.findViewById(R.id.fabTrack);
		FloatingActionButton fabTask = rootView.findViewById(R.id.fabTask);
		//FloatingActionButton fabWallet = rootView.findViewById(R.id.fabWallet);
		fabDashboard.setImageDrawable(new IconDrawable(getActivity(), MaterialIcons.md_dashboard).colorRes(R.color.colorWhite));
		fabPayment.setImageDrawable(new IconDrawable(getActivity(), MaterialIcons.md_payment).colorRes(R.color.colorWhite));
		fabTrack.setImageDrawable(new IconDrawable(getActivity(), MaterialIcons.md_explore).colorRes(R.color.colorWhite));
		fabTask.setImageDrawable(new IconDrawable(getActivity(), MaterialIcons.md_book).colorRes(R.color.colorWhite));
		//fabWallet.setImageDrawable(new IconDrawable(getActivity(), MaterialIcons.md_credit_card).colorRes(R.color.colorWhite));

		fabDashboard.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mListener.onItemClick(Constant.PARENT_DASHBOARD);
			}
		});
		fabPayment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mListener.onItemClick(Constant.PARENT_PAYMENTS);
			}
		});
		fabTrack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mListener.onItemClick(Constant.PARENT_TRACK);
			}
		});
		fabTask.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mListener.onItemClick(Constant.PARENT_TASK);
			}
		});
//		fabWallet.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//
//			}
//		});
	}
}
