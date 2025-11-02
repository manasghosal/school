package com.quicksoft.school.fragment.parent;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.quicksoft.school.R;
import com.quicksoft.school.adapter.GovtBodyAdapter;
import com.quicksoft.school.model.GovtBodyParent;
import com.quicksoft.school.model.MonthlyAttendanceParent;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.quicksoft.school.view.drawroute.DrawMarker;
import com.quicksoft.school.view.drawroute.DrawRouteMaps;
import com.quicksoft.school.view.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import br.liveo.interfaces.OnItemClickListener;
import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;

import static com.facebook.FacebookSdk.getApplicationContext;


public class GvtBodyFragmentParent extends Fragment {

	private OnItemClickListener mListener;
	private ArrayList<GovtBodyParent> govtBodyParentArrayList;

	public static GvtBodyFragmentParent newInstance(){
		GvtBodyFragmentParent mFragment = new GvtBodyFragmentParent();
		return mFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_parent_govtbody, container, false);
		setHasOptionsMenu(true);

		getDataFromJSON();
		setupFAB(rootView);
		initGridView(rootView);

		return rootView;		
	}

	public void getDataFromJSON(){
		String data = GlobalPreferenceManager.getParentGovBody();
		LogUtils.i(data);
		govtBodyParentArrayList = new ArrayList<>();
		try {
			JSONObject jsonObject = new JSONObject(data);
			JSONArray array = jsonObject.getJSONArray("GoverningBodyList");
			for (int i=0; i<array.length(); i++){
				JSONObject object = array.getJSONObject(i);
				String Designation = object.getString("Designation");
				String Qualification = object.getString("Qualification");
				String Department = object.getString("Department");
				String First_name = object.getString("First_name");
				String Middle_name = object.getString("Middle_name");
				String Last_name = object.getString("Last_name");
				String Phone = object.getString("Phone");
				String Email = object.getString("Email");
				String PersonId = object.getString("PersonId");
				String LastVisited =  object.getString("LastVisited");

				String name = "";
				if(Middle_name.compareTo("")==0)
					name = First_name + " " + Last_name;
				else
					name = First_name + " " + Middle_name + " " + Last_name;

				String imageUrl = Constant.SERVER_BASE_ADDRESS + "api/quicksoftuser/personimage?personId=" + PersonId +"&ext=png";

				GovtBodyParent govtBodyParent = new GovtBodyParent(1, name,Designation, imageUrl,Qualification, Department, Phone, LastVisited);
				govtBodyParentArrayList.add(govtBodyParent);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void initGridView(View rootView){
		RecyclerView recyclerView = rootView.findViewById(R.id.recyclerView);
		recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
		recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.HORIZONTAL));
		recyclerView.setHasFixedSize(true);
		RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),2);
		recyclerView.setLayoutManager(layoutManager);

		GovtBodyAdapter adapter = new GovtBodyAdapter(getActivity(), govtBodyParentArrayList);
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

	public void testData(){
		govtBodyParentArrayList = new ArrayList<>();
		GovtBodyParent gb1 = new GovtBodyParent(1, "Amit Mashelkar","Principal", null,"Msc in Physics", "Administration", "+91993695387", "Punched in at 10:00am");
		GovtBodyParent gb2 = new GovtBodyParent(1, "Jitendar Singh","Vice Principal", null,"Msc in Math", "Administration", "+91993694387", "Punched in at 09:30am");

		govtBodyParentArrayList.add(gb1);
		govtBodyParentArrayList.add(gb2);
	}
}
