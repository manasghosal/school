package com.quicksoft.school.fragment.driver;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blankj.utilcode.util.LogUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.quicksoft.school.R;
import com.quicksoft.school.activity.driver.TripAttendanceDriverActivity;
import com.quicksoft.school.model.AttendanceStudent;
import com.quicksoft.school.model.Passanger;
import com.quicksoft.school.model.PassengerPickDriver;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.service.LocationService;
import com.quicksoft.school.util.Constant;
import com.quicksoft.school.view.drawroute.DrawMarker;
import com.quicksoft.school.view.drawroute.DrawRouteMaps;
import com.shagi.materialdatepicker.date.DatePickerFragmentDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.MarkerOptions;
import mehdi.sakout.fancybuttons.FancyButton;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import android.widget.Toast;
public class DashboardFragmentDriver extends Fragment implements OnMapReadyCallback, OnMarkerClickListener
{

	private FancyButton btnVehicle, btnDate;
	private static float MAP_ZOOM_MAX = 3;
	private static float MAP_ZOOM_MIN = 21;
	private GoogleMap mMap = null;
	private static final int INITIAL_REQUEST=1337;
	private static final int LOCATION_REQUEST=INITIAL_REQUEST+3;
	private ArrayList<String> vehicleList;
	private ArrayList<String> vehicleRouteDescriptionList;
	ArrayList<PassengerPickDriver> passengerPickDriverArrayList;
	LatLng schoolLatLng;
	public LocationService gpsService;
	public boolean mTracking = false;
	private boolean isBound = false;

	public static DashboardFragmentDriver newInstance(){
		DashboardFragmentDriver mFragment = new DashboardFragmentDriver();
		return mFragment;
	}
	public void onDataReady()
	{
		setHasOptionsMenu(true);
		setupClass();//rootView
		setupDate();//rootView
		if (ActivityCompat.checkSelfPermission(getContext(),
				android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ActivityCompat.checkSelfPermission(getContext(),
						android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			LogUtils.i("LOCATION_REQUESTLOCATION_REQUESTLOCATION_REQUESTLOCATION_REQUESTLOCATION_REQUEST","0");
			requestPermissions(
					new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION},
					LOCATION_REQUEST
			);
		} else {
			LogUtils.e("DB", "PERMISSION GRANTED");
			SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.gMap);
			mapFragment.getMapAsync(this);

			Intent intent = new Intent(getActivity(), LocationService.class);
			getActivity().startService(intent);
			isBound = getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		}

	}

	@Override
	public boolean onMarkerClick(final Marker marker)
	{
		Toast.makeText(getActivity(),"onMarkerClick clicked: " + marker.getTitle(),Toast.LENGTH_SHORT).show();
		LogUtils.i("onMarkerClick");
		int position = (int) marker.getTag();
		if(position != 99999) {
			if (passengerPickDriverArrayList.get(position).getPassengerPickDriverArrayList() != null) {
				Intent intent = new Intent(getActivity(), TripAttendanceDriverActivity.class);

				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("PASSANGERLIST", passengerPickDriverArrayList.get(position).getPassengerPickDriverArrayList());
				intent.putExtras(bundle);
				startActivity(intent);
			}
		}
		return true;
	}

	public void onInfoWindowClick(Marker marker) {
		LogUtils.i("onInfoWindowClick");
		int position = (int) marker.getTag();

		if (passengerPickDriverArrayList.get(position).getPassengerPickDriverArrayList() != null) {
			Intent intent = new Intent(getActivity(), TripAttendanceDriverActivity.class);

			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList("PASSANGERLIST", passengerPickDriverArrayList.get(position).getPassengerPickDriverArrayList());
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_driver_dashboard, container, false);
		return rootView;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		LogUtils.i("LOCATION_REQUESTLOCATION_REQUESTLOCATION_REQUESTLOCATION_REQUESTLOCATION_REQUEST","0");
		switch (requestCode) {
			case LOCATION_REQUEST: {
				LogUtils.i("LOCATION_REQUESTLOCATION_REQUESTLOCATION_REQUESTLOCATION_REQUESTLOCATION_REQUEST","0");
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
 					SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.gMap);
					mapFragment.getMapAsync(this);

					Intent intent = new Intent(getActivity(), LocationService.class);
					getActivity().startService(intent);
					isBound = getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

				} else {
				}
				return;
			}
		}
	}
	void FillDriverVehicle()
	{
		String data = GlobalPreferenceManager.getDriverVehicle();
		LogUtils.i(data);
		vehicleList = new ArrayList<>();
		vehicleRouteDescriptionList = new ArrayList<>();
		try {
			JSONObject jsonObject = new JSONObject(data);
			JSONArray array = jsonObject.getJSONArray("VehicleNo");
			JSONArray array1 = jsonObject.getJSONArray("RouteDescription");
			LogUtils.i("Vehicle List length: "+ array.length());
			for (int i=0; i<array.length(); i++){
				String object = array.getString(i);
				vehicleList.add(object);
				object = array1.getString(i);
				vehicleRouteDescriptionList.add(object);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void setupClass(){//View rootView
		View rootView = getView();
		FillDriverVehicle();
		final String [] vehicleNo = new String[vehicleList.size()];

		for(int i = 0; i < vehicleList.size(); i++)
			vehicleNo[i]= vehicleList.get(i);
		btnVehicle = rootView.findViewById(R.id.btnVehicle);
		btnVehicle.setText("Vehicle: "+ vehicleNo[0]);
		getDataFromJSON(vehicleNo[0]);
		btnVehicle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
				dialog.setTitle("Vehicle No");
				dialog.setSingleChoiceItems(vehicleNo, -1, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int position) {
                        btnVehicle.setText(vehicleNo[position]);
                        getDataFromJSON(vehicleNo[position]);
                        if (mMap != null)
                        {
                            mMap.clear();
                        }
						dialog.dismiss();
					}

				});
				AlertDialog alert = dialog.create();
				alert.show();
			}
		});

	}

	public void setupDate(){//View rootView
		View rootView = getView();
		final Calendar now = Calendar.getInstance();
		btnDate = rootView.findViewById(R.id.btnDate);
		btnDate.setText(now.get(Calendar.DAY_OF_MONTH)+"-"+(now.get(Calendar.MONTH)+1) + "-"+now.get(Calendar.YEAR));
		btnDate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

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

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		MAP_ZOOM_MAX = mMap.getMaxZoomLevel();
		MAP_ZOOM_MIN = mMap.getMinZoomLevel();
		LogUtils.i(passengerPickDriverArrayList.size());
		try {
			mMap.setMyLocationEnabled(true);
			mMap.getUiSettings().setMyLocationButtonEnabled(true);
			mMap.getUiSettings().setZoomControlsEnabled(true);
			if(passengerPickDriverArrayList.size() > 0) {
				int dest = passengerPickDriverArrayList.size()-1;
				LatLng origin = new LatLng(passengerPickDriverArrayList.get(dest).getLat(), passengerPickDriverArrayList.get(dest).getLon());
				LatLng destination = schoolLatLng;//new LatLng(passengerPickDriverArrayList.get(1).getLat(), passengerPickDriverArrayList.get(1).getLon());
				DrawRouteMaps.getInstance(getActivity()).draw(origin, destination, mMap);
				//DrawMarker.getInstance(getActivity()).draw(mMap, origin, R.drawable.ic_room_teal_700_24dp, passengerPickDriverArrayList.get(0).getPlaceName(), passengerPickDriverArrayList.get(0).getSequence());
				//DrawMarker.getInstance(getActivity()).draw(mMap, destination, R.drawable.ic_home_teal_700_24dp, passengerPickDriverArrayList.get(1).getPlaceName(), passengerPickDriverArrayList.get(1).getSequence());
				DrawMarker.getInstance(getActivity()).draw(mMap, schoolLatLng, R.drawable.ic_home_teal_700_24dp, "School",99999);
				for (int i = 0; i < passengerPickDriverArrayList.size(); i++) {
					LatLng latlng = new LatLng(passengerPickDriverArrayList.get(i).getLat(), passengerPickDriverArrayList.get(i).getLon());
//					if(i == 0)//passengerPickDriverArrayList.size() - 1)
//						DrawMarker.getInstance(getActivity()).draw(mMap, latlng, R.drawable.ic_home_teal_700_24dp, passengerPickDriverArrayList.get(i).getPlaceName(), passengerPickDriverArrayList.get(i).getSequence());
//					else
						DrawMarker.getInstance(getActivity()).draw(mMap, latlng, R.drawable.ic_room_teal_700_24dp, passengerPickDriverArrayList.get(i).getPlaceName(), passengerPickDriverArrayList.get(i).getSequence());
				}
				LogUtils.i("OnMapReady 1");
				/*mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
					@Override
					public void onInfoWindowClick(Marker marker) {
                        LogUtils.i("onInfoWindowClick");
						int position = (int) marker.getTag();

						if (passengerPickDriverArrayList.get(position).getPassengerPickDriverArrayList() != null) {
							Intent intent = new Intent(getActivity(), TripAttendanceDriverActivity.class);

							Bundle bundle = new Bundle();
							bundle.putParcelableArrayList("PASSANGERLIST", passengerPickDriverArrayList.get(position).getPassengerPickDriverArrayList());
							intent.putExtras(bundle);
							startActivity(intent);
						}
					}
				});*/

				LogUtils.i("OnMapReady 2");
				LatLngBounds bounds = new LatLngBounds.Builder().include(origin).include(destination).build();
				Point displaySize = new Point();
				getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
				mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 800, 30));
				mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
				LogUtils.i("OnMapReady 3");
				mMap.setOnMarkerClickListener(this);
				//mMap.setOnInfoWindowClickListener(this);
				/*// Unclustered marker - instead of adding to the map directly, use the MarkerManager
				MarkerManager.Collection markerCollection = markerManager.newCollection();
				markerCollection.addMarker(new MarkerOptions()
						.position(new LatLng(51.150000, -0.150032))
						.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
						.title("Unclustered marker"));
				markerCollection.setOnMarkerClickListener(marker -> {
					Toast.makeText(MultiLayerDemoActivity.this,
							"Marker clicked: " + marker.getTitle(),
							Toast.LENGTH_SHORT).show();
					return false;
				});*/
			}
		}
		catch(SecurityException e)
		{
			LogUtils.i("Error: "+ e.getMessage());
		}
	}


	/*public void testData(){

		ArrayList<AttendanceStudent> attendanceStudentsArrayList1 = new ArrayList<>();
		AttendanceStudent as1 = new AttendanceStudent(1, "Atanu Ghosh", null);
		AttendanceStudent as2 = new AttendanceStudent(2, "Akash Das", null);
		attendanceStudentsArrayList1.add(as1);
		attendanceStudentsArrayList1.add(as2);

		ArrayList<AttendanceStudent> attendanceStudentsArrayList2 = new ArrayList<>();
		AttendanceStudent as3 = new AttendanceStudent(3, "Tanmoy Banerjee", null);
		attendanceStudentsArrayList2.add(as3);

		ArrayList<AttendanceStudent> attendanceStudentsArrayList3 = new ArrayList<>();
		AttendanceStudent as4 = new AttendanceStudent(4, "Monojit Roy", null);
		AttendanceStudent as5 = new AttendanceStudent(5, "Suchitra Sen", null);
		attendanceStudentsArrayList3.add(as4);
		attendanceStudentsArrayList3.add(as5);

		passengerPickDriverArrayList = new ArrayList<>();
		passengerPickDriverArrayList.add(new PassengerPickDriver(22.4945112, 88.3582335, "Start", attendanceStudentsArrayList1,0));
		passengerPickDriverArrayList.add(new PassengerPickDriver(22.540953, 88.3603394, "Destination", null, 1));
		passengerPickDriverArrayList.add(new PassengerPickDriver(22.4945112, 88.352335, "Salt Lake", attendanceStudentsArrayList3, 2));

	}*/
	public void getDataFromJSON(String vehicle){
		String data = GlobalPreferenceManager.getDriverDashBoardInfo(vehicle);
		LogUtils.i(data);
		passengerPickDriverArrayList = new ArrayList<>();
		try {
			JSONObject jsonObject = new JSONObject(data);
			String SchoolLatitude = jsonObject.getString("SchoolLatitude");
			String SchoolLongitude = jsonObject.getString("SchoolLongitude");
			schoolLatLng = new LatLng(Double.parseDouble(SchoolLatitude), Double.parseDouble(SchoolLongitude));
			JSONArray array = jsonObject.getJSONArray("RouteDetailsLst");
			LogUtils.i("RouteDetailsLst length: "+ array.length());
			for (int i=0; i<array.length(); i++){
				JSONObject object = array.getJSONObject(i);
				Double lat = Double.parseDouble(object.getString("PickupLatitude"));
				Double lng = Double.parseDouble(object.getString("PickupLongitude"));
				ArrayList<Passanger> passangersArrayList = new ArrayList<>();
				JSONArray array1 = object.getJSONArray("passengerDetailsLst");
				for (int j=0; j<array1.length(); j++) {
					JSONObject object1 = array1.getJSONObject(j);
					String personId = object1.getString("PersonId");
					String fname = object1.getString("FName");
					String mname = object1.getString("MName");
					String lname = object1.getString("LName");
					String remark = object1.getString("Remark");
					String imageUrl = Constant.SERVER_BASE_ADDRESS + "api/quicksoftuser/personimage?personId=" + personId +"&ext=png";
					Passanger p = new Passanger(personId, fname, mname, lname, imageUrl, remark);
					passangersArrayList.add(p);
				}
				passengerPickDriverArrayList.add(new PassengerPickDriver(lat, lng, "", passangersArrayList,i));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void onDetach() {
		super.onDetach();
		try {
			if (isBound) {
				getActivity().unbindService(serviceConnection);
			}
		}catch (Exception e){
			LogUtils.i("Error: "+ e.getMessage());
		}
	}
	@Override
	public void onResume(){
		super.onResume();
		LogUtils.i("On resume called");
		if(mMap != null){ //prevent crashing if the map doesn't exist yet (eg. on starting activity)
			mMap.clear();
			try {
				if(passengerPickDriverArrayList.size() > 0) {

					int dest = passengerPickDriverArrayList.size()-1;
					LatLng origin = new LatLng(passengerPickDriverArrayList.get(dest).getLat(), passengerPickDriverArrayList.get(dest).getLon());
					LatLng destination = schoolLatLng;//new LatLng(passengerPickDriverArrayList.get(1).getLat(), passengerPickDriverArrayList.get(1).getLon());
					DrawRouteMaps.getInstance(getActivity()).draw(origin, destination, mMap);
					DrawMarker.getInstance(getActivity()).draw(mMap, schoolLatLng, R.drawable.ic_home_teal_700_24dp, "School",99999);
					for (int i = 0; i < passengerPickDriverArrayList.size(); i++) {
						LatLng latlng = new LatLng(passengerPickDriverArrayList.get(i).getLat(), passengerPickDriverArrayList.get(i).getLon());
						DrawMarker.getInstance(getActivity()).draw(mMap, latlng, R.drawable.ic_room_teal_700_24dp, passengerPickDriverArrayList.get(i).getPlaceName(), passengerPickDriverArrayList.get(i).getSequence());
					}
					LatLngBounds bounds = new LatLngBounds.Builder().include(origin).include(destination).build();
					Point displaySize = new Point();
					getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
					mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 800, 30));
					mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
				}
			}
			catch(SecurityException e)
			{
				LogUtils.i("Error: "+ e.getMessage());
			}
			// add markers from database to the map
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}
	private ServiceConnection serviceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			String name = className.getClassName();
			if (name.endsWith("LocationService")) {
				gpsService = ((LocationService.LocationServiceBinder) service).getService();
				LogUtils.i("GPS is ready");
				gpsService.startTracking();
				mTracking = true;
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			if (className.getClassName().equals("LocationService")) {
				gpsService = null;
				LogUtils.i("GPS is disconnected");
				gpsService.stopTracking();
				mTracking = false;
			}
		}
	};
}
