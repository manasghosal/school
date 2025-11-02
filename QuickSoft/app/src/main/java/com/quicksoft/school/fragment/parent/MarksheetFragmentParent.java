package com.quicksoft.school.fragment.parent;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;
import com.jeevandeshmukh.fancybottomsheetdialoglib.FancyBottomSheetDialog;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.quicksoft.school.R;
import com.quicksoft.school.adapter.MarksheetAdapter;
import com.quicksoft.school.model.MarksheetParent;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.quicksoft.school.view.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import br.liveo.interfaces.OnItemClickListener;
import mehdi.sakout.fancybuttons.FancyButton;


public class MarksheetFragmentParent extends Fragment implements View.OnClickListener {
	private OnItemClickListener mListener;
	private FancyButton btnClass, btnTerms;
	private TextView tvRank, tvStatus;
	private RecyclerView recyclerView;

	private ArrayList<MarksheetParent> marksheetParentArrayList;
	private MarksheetAdapter marksheetAdapter;

	public static MarksheetFragmentParent newInstance(){
		MarksheetFragmentParent mFragment = new MarksheetFragmentParent();
		return mFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_parent_marksheet, container, false);

		setHasOptionsMenu(true);
		setupFAB(rootView);

		getDataFromJSON();
		//getClassDataFromJSON();

		setupClass(rootView);
		setupTerms(rootView);

		tvRank = rootView.findViewById(R.id.tvRank);
		tvStatus = rootView.findViewById(R.id.tvStatus);
		recyclerView = rootView.findViewById(R.id.recyclerView);

		return rootView;		
	}

	public void getDataFromJSON(){
		String data = GlobalPreferenceManager.getParentMarksheet();
		LogUtils.i(data);
		marksheetParentArrayList = new ArrayList<>();
		try {
			JSONObject jsonObject = new JSONObject(data);
			JSONArray array = jsonObject.getJSONArray("ExamMarksList");
			for (int i=0; i<array.length(); i++){
				JSONObject object = array.getJSONObject(i);
				String ExamName = object.getString("ExamName");
				String Class = object.getString("Class");
				String Remark = object.getString("Remark");
				String Rank = object.getString("Rank");
				JSONArray subjectMarksArray = object.getJSONArray("SubjectMarksList");

				MarksheetParent marksheetParent = new MarksheetParent(Rank, Remark, Class, ExamName);
				ArrayList<MarksheetParent.MarksSubject> marksSubjectArrayList = new ArrayList<>();
				for(int j=0; j<subjectMarksArray.length(); j++){
					JSONObject object1 = subjectMarksArray.getJSONObject(j);
					String Subject = object1.getString("Subject");
					int Marks = object1.getInt("Marks");
					int TotalMarks = object1.getInt("TotalMarks");
					int HighestMarks = object1.getInt("HighestMarks");

					MarksheetParent.MarksSubject marksSubject = marksheetParent.new MarksSubject(Subject, Marks, HighestMarks, TotalMarks);
					marksSubjectArrayList.add(marksSubject);
				}
				marksheetParent.setMarksSubjectArrayList(marksSubjectArrayList);
				marksheetParentArrayList.add(marksheetParent);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String[] getSectionDataFromJSON(String classs){
		String data = GlobalPreferenceManager.getParentClasses();
		try {
			JSONObject jsonObject = new JSONObject(data);
			JSONArray array = jsonObject.getJSONArray("ClassList");
			ArrayList<String> examArrayList = new ArrayList<>();
			for (int i=0; i<array.length(); i++){
				JSONObject object = array.getJSONObject(i);
				String Class = object.getString("Class");
				if(classs.compareTo(Class)==0){
					JSONArray examArray = object.getJSONArray("ExamList");
					for (int j=0; j<examArray.length(); j++){
						JSONObject object1 = examArray.getJSONObject(j);
						String examName = object1.getString("ExamName");
						if(examName.compareTo("")!=0)
							examArrayList.add(examName);
					}
				}
			}
			String[] termsArray = examArrayList.toArray(new String[examArrayList.size()]);
			return termsArray;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return new String[0];
	}

	public String[] getClassDataFromJSON(){
		String data = GlobalPreferenceManager.getParentClasses();
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

	public void setupClass(final View rootView){
		final String [] classArr = getClassDataFromJSON();
		btnClass = rootView.findViewById(R.id.btnClass);
        btnClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle("Class");
                dialog.setSingleChoiceItems(classArr, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position) {
                        btnClass.setText(classArr[position]);
						setupTerms(rootView);
						btnTerms.setText("Select Terms");
                        dialog.dismiss();
//						if(btnTerms.getText().toString().compareTo("Select Terms")!=0){
//							fetchMarkSheet();
//						}
                    }

                });
                AlertDialog alert = dialog.create();
                alert.show();
            }
        });

	}

	public void setupTerms(View rootView){
//		final String [] secArr = {"1st Terms", "2nd Terms"};
		String classs = btnClass.getText().toString();
		final String [] secArr = getSectionDataFromJSON(classs);
		btnTerms = rootView.findViewById(R.id.btnTerms);
		if(secArr.length!=0) {
			btnTerms.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
					dialog.setTitle("Section");
					dialog.setSingleChoiceItems(secArr, -1, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int position) {
							btnTerms.setText(secArr[position]);
							dialog.dismiss();
							if (btnClass.getText().toString().compareTo("Select Class") != 0) {
								fetchMarkSheet();
							}
						}

					});
					AlertDialog alert = dialog.create();
					alert.show();
				}
			});
		}
	}

	public void fetchMarkSheet(){
		String classs = btnClass.getText().toString();
		String terms = btnTerms.getText().toString();

		tvRank.setText("");
		tvStatus.setText("");
		recyclerView.setVisibility(View.INVISIBLE);

		for(int i=0; i<marksheetParentArrayList.size();i++){
			if(marksheetParentArrayList.get(i).getClasss().compareTo(classs)==0){
				if(marksheetParentArrayList.get(i).getTerms().compareTo(terms)==0){
					tvRank.setText(marksheetParentArrayList.get(i).getRank());
					tvStatus.setText(marksheetParentArrayList.get(i).getStatus());

					recyclerView.setVisibility(View.VISIBLE);
					recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
					marksheetAdapter = new MarksheetAdapter(getActivity(), marksheetParentArrayList.get(i).getSubjctMarksArray());
					recyclerView.setAdapter(marksheetAdapter);
					break;
				}
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
    public void onClick(View view) {
		if(btnClass.getText().equals("Select Class") || btnTerms.getText().equals("Select Terms")){
			new FancyBottomSheetDialog.Builder(getActivity())
					.setMessage("Please select Class, Section and Date")
					.setBackgroundColor(getResources().getColor(R.color.colorPrimary))
					.setIcon(R.drawable.ic_assignment_late_white_24dp,true)
					.setPositiveBtnText("Ok")
					.setNegativeBtnText("")
					.setPositiveBtnBackground(getResources().getColor(R.color.okButtonColor))
					.build();
		}
    }
}
