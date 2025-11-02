package com.quicksoft.school.fragment.parent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.LogUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.quicksoft.school.R;
import com.quicksoft.school.adapter.NoticeParentAdapter;
import com.quicksoft.school.adapter.TaskParentAdapter;
import com.quicksoft.school.model.NoticeParent;
import com.quicksoft.school.model.TaskParent;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.quicksoft.school.view.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import br.liveo.interfaces.OnItemClickListener;


public class NoticeFragmentParent extends Fragment {

	private OnItemClickListener mListener;
	private ArrayList<NoticeParent> noticeParentArrayList;
	private RecyclerView recyclerView;

	public static NoticeFragmentParent newInstance(){
		NoticeFragmentParent mFragment = new NoticeFragmentParent();
		return mFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_parent_task, container, false);
		setHasOptionsMenu(true);

		getDataFromJSON();
		setupFAB(rootView);
		initListView(rootView);

		return rootView;		
	}

	public void getDataFromJSON(){
		noticeParentArrayList = new ArrayList<>();
		String data = GlobalPreferenceManager.getParentNotice();
        LogUtils.i(data);
		try {
			JSONObject jsonObject = new JSONObject(data);
			JSONArray array = jsonObject.getJSONArray("NoticeList");
			for (int i=0; i<array.length(); i++){
				JSONObject object = array.getJSONObject(i);
				String NoiticeId = object.getString("NoiticeId");
				String NoticeDate = object.getString("NoticeDate");
				String NoticeTxt = object.getString("NoticeTxt");
				int noticeType = object.getInt("NoticeType");

				NoticeParent noticeParent = new NoticeParent(NoiticeId, NoticeDate, NoticeTxt,noticeType);
				noticeParentArrayList.add(noticeParent);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void initListView(View rootView){
		recyclerView = rootView.findViewById(R.id.recyclerView);
		recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL));
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

		NoticeParentAdapter adapter = new NoticeParentAdapter(getActivity(), noticeParentArrayList);
		recyclerView.setAdapter(adapter);

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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == Constant.PARENT_NOTICE_DETAILS_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			String posVal = data.getStringExtra("POSITION");
			LogUtils.i(" i m here " + posVal);

			int pos = Integer.valueOf(posVal);
			noticeParentArrayList.get(pos).setSubmit(true);
			NoticeParentAdapter adapter = new NoticeParentAdapter(getActivity(), noticeParentArrayList);
			recyclerView.setAdapter(adapter);
			recyclerView.invalidate();
		}
	}
}
