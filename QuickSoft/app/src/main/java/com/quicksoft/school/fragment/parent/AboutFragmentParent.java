package com.quicksoft.school.fragment.parent;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.quicksoft.school.R;
import com.quicksoft.school.util.Constant;
import com.quicksoft.school.view.fab.FloatingActionButton;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import br.liveo.interfaces.OnItemClickListener;
import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;


public class AboutFragmentParent extends Fragment {

    private TextView tvAbout, tvMission;
    private ImageView imgView;

	private OnItemClickListener mListener;

	public static AboutFragmentParent newInstance(){
		AboutFragmentParent mFragment = new AboutFragmentParent();
		return mFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_parent_about, container, false);
		imgView = rootView.findViewById(R.id.imgView);
		tvAbout = rootView.findViewById(R.id.tvAbout);
		tvMission = rootView.findViewById(R.id.tvMission);

		setHasOptionsMenu(true);
		setupStudentInfo();



		setupFAB(rootView);
		return rootView;		
	}

	public void setupStudentInfo(){
		Glide.with(getActivity()).load(R.drawable.school).into(imgView);
		tvAbout.setText(R.string.about_us);
		tvMission.setText(R.string.mission);
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
