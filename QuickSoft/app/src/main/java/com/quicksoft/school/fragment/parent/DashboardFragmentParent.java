package com.quicksoft.school.fragment.parent;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.quicksoft.school.R;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.quicksoft.school.view.MovableFloatingActionButton;
import com.quicksoft.school.view.fab.FloatingActionButton;
import com.quicksoft.school.view.fab.FloatingActionMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import br.liveo.interfaces.OnItemClickListener;
import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;


public class DashboardFragmentParent extends Fragment {
    private static final String TEXT_FRAGMENT = "TEXT_FRAGMENT";

    private TextView tvName, tvClass, tvSection, tvRollNo;
    private TextView tvPayment;
    private ShapedImageView imgStudentProfile;
    private CompactCalendarView calendarView;

	private OnItemClickListener mListener;

	private int[] monthArray;

	public static DashboardFragmentParent newInstance(String text){
		DashboardFragmentParent mFragment = new DashboardFragmentParent();
		Bundle mBundle = new Bundle();
		mBundle.putString(TEXT_FRAGMENT, text);
		mFragment.setArguments(mBundle);

		return mFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_parent_dashboard, container, false);
		imgStudentProfile = rootView.findViewById(R.id.imgStudentProfile);
		tvName = rootView.findViewById(R.id.tvName);
		tvClass = rootView.findViewById(R.id.tvClass);
		tvSection = rootView.findViewById(R.id.tvSection);
		tvRollNo = rootView.findViewById(R.id.tvRollNo);
		tvPayment = rootView.findViewById(R.id.tvPayment);
		calendarView = rootView.findViewById(R.id.calendarView);

		setHasOptionsMenu(true);
		setupStudentInfo();

		setupFAB(rootView);

		return rootView;		
	}

	public void setupStudentInfo(){
		String data = GlobalPreferenceManager.getParentDashBoardInfo();
		try {
			JSONObject jsonObject = new JSONObject(data);
			String personId = jsonObject.getString("ChildPersonid");
			String totalFeeDue = jsonObject.getString("TotalFeeDue");
			String dueDate = jsonObject.getString("DueDate");
			String remarks = jsonObject.getString("FeeDueNote");
			String Classs = jsonObject.getString("Classs");
			String Section = jsonObject.getString("Section");
			String Roll = jsonObject.getString("Roll");
			String Lname = jsonObject.getString("Lname");
			String Mname = jsonObject.getString("Mname");
			String Fname = jsonObject.getString("Fname");
			String routeId = jsonObject.getString("RouteId");
			JSONArray array = jsonObject.getJSONArray("Attendance");

			String name = "";
			if(Mname.compareTo("")==0)
				name = Fname + " " + Lname;
			else
				name = Fname + " " + Mname + " " + Lname;

			SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd\'T\'HH:mm:ss");
			SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yyyy");
			Date ddate = dt.parse(dueDate);
			dueDate = dt1.format(ddate);

			String imageUrl = Constant.SERVER_BASE_ADDRESS + "api/quicksoftuser/personimage?personId=" + personId +"&ext=png";

			monthArray = new int[array.length()];
			for (int i = 0; i < array.length(); ++i) {
				monthArray[i] = array.getInt(i);
			}

			RequestOptions requestOptions = new RequestOptions();
			requestOptions.placeholder(R.drawable.ico_user_placeholder);
			requestOptions.signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));
			Glide.with(getActivity()).setDefaultRequestOptions(requestOptions).load(imageUrl).into(imgStudentProfile);
			tvPayment.setText(remarks + " " + totalFeeDue + " and due date is "+ dueDate);
			tvName.setText(name);
			tvClass.setText(Classs);
			tvSection.setText(Section);
			tvRollNo.setText(Roll);
			GlobalPreferenceManager.saveParentRouteId(routeId);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		setupAttendanceCalendar();
	}


	public void setupAttendanceCalendar(){
		calendarView.setFirstDayOfWeek(Calendar.SUNDAY);
		calendarView.displayOtherMonthDays(false);
		calendarView.shouldScrollMonth(false);
		calendarView.setEventIndicatorStyle(CompactCalendarView.FILL_LARGE_INDICATOR);

		Calendar currentCalender = Calendar.getInstance(Locale.getDefault());
		currentCalender.setTime(new Date());
		int curMonth = currentCalender.get(Calendar.MONTH);
		int curYear = currentCalender.get(Calendar.YEAR);
		int daysInMonth = currentCalender.getActualMaximum(Calendar.DAY_OF_MONTH);
		//LogUtils.i("Days "+daysInMonth);
		if(monthArray!=null) {
			try {
				SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
				for (int i = 0; i < daysInMonth; i++) {
					//LogUtils.i(monthArray[i]);
					if (monthArray[i] == 3) {
						Date d = f.parse((i + 1) + "-" + (curMonth + 1) + "-" + curYear);
						Event ev = new Event(Color.GREEN, d.getTime());
						calendarView.addEvent(ev);
					} else if (monthArray[i] == 2) {
						Date d = f.parse((i + 1) + "-" + (curMonth + 1) + "-" + curYear);
						Event ev = new Event(Color.CYAN, d.getTime());
						calendarView.addEvent(ev);
					} else if (monthArray[i] == 1) {
						Date d = f.parse((i + 1) + "-" + (curMonth + 1) + "-" + curYear);
						Event ev = new Event(Color.RED, d.getTime());
						calendarView.addEvent(ev);
					}
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
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

			}
		});
		fabPayment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

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
}
