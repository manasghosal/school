package com.quicksoft.school.fragment.parent;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.quicksoft.school.R;
import com.quicksoft.school.model.MonthlyAttendanceParent;
import com.quicksoft.school.model.MonthlyPaymentParent;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.quicksoft.school.util.TimeUtil;
import com.quicksoft.school.view.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import br.liveo.interfaces.OnItemClickListener;
import mehdi.sakout.fancybuttons.FancyButton;


public class PaymentFragmentParent extends Fragment implements View.OnClickListener {

	private OnItemClickListener mListener;

    private FancyButton btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9, btn10, btn11, btn12;
    private TextView tvYear, tvDueFee;
    private LinearLayout linearLayout;
    private ArrayList<FancyButton> arrayListButton = new ArrayList<>();
	private MonthlyPaymentParent monthlyPaymentParent;

	public static PaymentFragmentParent newInstance(){
		PaymentFragmentParent mFragment = new PaymentFragmentParent();
		return mFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_parent_payment, container, false);
		tvYear = rootView.findViewById(R.id.tvYear);
		linearLayout = rootView.findViewById(R.id.llayout);

		setHasOptionsMenu(true);
		initCalendarButtons(rootView);
		setupFAB(rootView);

		getDataFromJSON();
		setupDueFees(rootView);
		setupDetailPaymentView();

		return rootView;		
	}

	public void getDataFromJSON() {
		String data = GlobalPreferenceManager.getParentFee();
		LogUtils.i(data);
		int year = TimeUtil.getCurrentYear();

		monthlyPaymentParent = new MonthlyPaymentParent();
		ArrayList<MonthlyPaymentParent.Payment> paymentArrayList = new ArrayList<>();

		try {
			JSONObject jsonObject = new JSONObject(data);
			double globalFeeDue = jsonObject.getDouble("GlobalFeeDue");
			JSONArray array = jsonObject.getJSONArray("FeeForMonthList");
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				int month = object.getInt("Month");
				JSONArray jArray = object.getJSONArray("FeeDetailList");

				for (int j = 0; j < jArray.length(); j++) {
					JSONObject jobject = jArray.getJSONObject(j);
					double amount = jobject.getDouble("Amount");
					String FeeDetail = jobject.getString("FeeDetail");
					String FeeDueDate = jobject.getString("FeeDueDate");
					MonthlyPaymentParent.Payment payment = monthlyPaymentParent.new Payment(FeeDetail, amount, 0, FeeDueDate, month - 1, year);
					paymentArrayList.add(payment);
				}
			}

			monthlyPaymentParent.setPaymentArrayList(paymentArrayList);
			monthlyPaymentParent.setDueFees(globalFeeDue);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void setupDueFees(View rootView){
		tvDueFee = rootView.findViewById(R.id.tvDueFee);
		tvDueFee.setText("Rs: " + monthlyPaymentParent.getDueFees());

	}

	public void setupDetailPaymentView(){
		linearLayout.removeAllViews();
		LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		int currentMonth = TimeUtil.getCurrentMonth();
		double total = 0;
		for(int j=0; j<monthlyPaymentParent.getPaymentArrayList().size(); j++){
			if( currentMonth ==monthlyPaymentParent.getPaymentArrayList().get(j).getMonth()){
				View v = vi.inflate(R.layout.payment_item_parent, null);
				((TextView)v.findViewById(R.id.tvTitle)).setText(monthlyPaymentParent.getPaymentArrayList().get(j).getPaymentType());
				double fees  = monthlyPaymentParent.getPaymentArrayList().get(j).getFees();
				String dueDate = monthlyPaymentParent.getPaymentArrayList().get(j).getDueDate();
				total = total + fees;
				((TextView)v.findViewById(R.id.tvFee)).setText("Rs. "+ fees);
				((TextView)v.findViewById(R.id.tvDueDate)).setText("Due Date: "+ dueDate);
				linearLayout.addView(v);
			}
		}

		View v = vi.inflate(R.layout.payment_item_parent, null);
		((TextView)v.findViewById(R.id.tvTitle)).setText("TOTAL FEE");
		TextView tvFee = v.findViewById(R.id.tvFee);
		tvFee.setText("Rs. "+ total);
		tvFee.setTextColor(getResources().getColor(R.color.lipstick));
		tvFee.setTypeface(Typeface.DEFAULT_BOLD);
		linearLayout.addView(v);

	}

	public void initCalendarButtons(View rootView){

		tvYear.setText("2018");

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
			arrayListButton.get(i).setGravity(Gravity.CENTER);

			arrayListButton.get(i).setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View view) {
		linearLayout.removeAllViews();
		LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		for(int i=0; i< arrayListButton.size(); i++){
			if(arrayListButton.get(i)==view){
				double total = 0;
				for(int j=0; j<monthlyPaymentParent.getPaymentArrayList().size(); j++){
					if(i==monthlyPaymentParent.getPaymentArrayList().get(j).getMonth()){
						View v = vi.inflate(R.layout.payment_item_parent, null);
						((TextView)v.findViewById(R.id.tvTitle)).setText(monthlyPaymentParent.getPaymentArrayList().get(j).getPaymentType());
						double fees  = monthlyPaymentParent.getPaymentArrayList().get(j).getFees();
						String dueDate = monthlyPaymentParent.getPaymentArrayList().get(j).getDueDate();
						total = total + fees;
						((TextView)v.findViewById(R.id.tvFee)).setText("Rs. "+ fees);
						((TextView)v.findViewById(R.id.tvDueDate)).setText("Due Date: "+ dueDate);
						linearLayout.addView(v);
					}
				}

				View v = vi.inflate(R.layout.payment_item_parent, null);
				((TextView)v.findViewById(R.id.tvTitle)).setText("TOTAL FEE");
				TextView tvFee = v.findViewById(R.id.tvFee);
				tvFee.setText("Rs. "+ total);
				tvFee.setTextColor(getResources().getColor(R.color.lipstick));
				tvFee.setTypeface(Typeface.DEFAULT_BOLD);
				linearLayout.addView(v);

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
