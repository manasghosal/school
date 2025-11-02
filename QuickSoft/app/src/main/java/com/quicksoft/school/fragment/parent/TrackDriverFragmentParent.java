package com.quicksoft.school.fragment.parent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.quicksoft.school.R;
import com.quicksoft.school.connection.SyncManager;
import com.quicksoft.school.connection.callback.SyncCompleteCallback;
import com.quicksoft.school.fragment.teacher.NoticeFragmentTeacher;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;
import com.quicksoft.school.view.drawroute.DrawMarker;
import com.quicksoft.school.view.drawroute.DrawRouteMaps;
import com.quicksoft.school.view.fab.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import br.liveo.interfaces.OnItemClickListener;
import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;

import static com.blankj.utilcode.util.ActivityUtils.startActivity;

public class TrackDriverFragmentParent extends Fragment implements OnMapReadyCallback, SyncCompleteCallback {

    private TextView tvName, tvPhone;
    private ShapedImageView imgDriverProfile;

	private static float MAP_ZOOM_MAX = 3;
	private static float MAP_ZOOM_MIN = 21;
	private GoogleMap mMap;
	private static final int INITIAL_REQUEST=1337;
	private static final int LOCATION_REQUEST=INITIAL_REQUEST+3;
	private OnItemClickListener mListener;
	private CountDownTimer updateLLocationDataTimer;
	private SyncManager mSyncManager;
	private Marker mCurrentMarker = null;

	public static TrackDriverFragmentParent newInstance(){
		TrackDriverFragmentParent mFragment = new TrackDriverFragmentParent();
		return mFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_parent_track_driver, container, false);
		SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.gMap);
		mapFragment.getMapAsync(this);

		imgDriverProfile = rootView.findViewById(R.id.imgDriverProfile);
		tvName = rootView.findViewById(R.id.tvName);
		tvPhone = rootView.findViewById(R.id.tvPhone);

		setupDriverProfile();
		setHasOptionsMenu(true);
		setupFAB(rootView);

		getLatestLocation();
		updateLLocationDataTimer = new CountDownTimer(Constant.PARENT_LOCATION_UPDATE_INTERVAL, 1000) {
			public void onTick(long millisUntilFinished) {
			}

			public void onFinish() {
				getLatestLocation();
				LogUtils.i("Checking server for new location updates");
			}
		};

		return rootView;		
	}

	public void setupDriverProfile(){
		//Glide.with(this).load(R.drawable.dummy_driver).into(imgDriverProfile);
		//tvName.setText(R.string.dummy_driver_name);
		//tvPhone.setText("+91-9836953872");
		try{
			String data = GlobalPreferenceManager.getParentDashBoardInfo();
			JSONObject jsonObject = new JSONObject(data);
			String drivername = jsonObject.getString("DriverFname") + " "
					+ jsonObject.getString("DriverMname") + " "
					+ jsonObject.getString("DriverLname");
			String driverphone = jsonObject.getString("DriverPhone");
			String driverPersonId = jsonObject.getString("DriverPersonId");
			String imageUrl = Constant.SERVER_BASE_ADDRESS + "api/quicksoftuser/personimage?personId=" + driverPersonId +"&ext=png";
			RequestOptions requestOptions = new RequestOptions();
			requestOptions.placeholder(R.drawable.ico_user_placeholder);
			requestOptions.signature(new ObjectKey(String.valueOf(System.currentTimeMillis())));
			Glide.with(this).setDefaultRequestOptions(requestOptions).load(imageUrl).into(imgDriverProfile);

			tvName.setText(drivername);
			tvPhone.setText(driverphone);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		imgDriverProfile.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_DIAL);
				intent.setData(Uri.parse("tel:" + tvPhone.getText()));
				//startActivity(intent);
				//Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + govtBodyParentArrayList.get(position).getPhone()));
				//startActivity(intent);
				if (Build.VERSION.SDK_INT > 23) {
					startActivity(intent);
				} else {

					if (ActivityCompat.checkSelfPermission(getActivity(),
							Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
						Toast.makeText(getActivity(), "Permission Not Granted ", Toast.LENGTH_SHORT).show();
					} else {
						final String[] PERMISSIONS_STORAGE = {Manifest.permission.CALL_PHONE};
						ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, 9);
						startActivity(intent);
					}
				}
			}
		});

	}

	private LatLng getLocation() {
		if (ActivityCompat.checkSelfPermission(
				getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
				getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			requestPermissions(
					new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,android.Manifest.permission.ACCESS_FINE_LOCATION},
					LOCATION_REQUEST
			);
		} else {
			LocationManager locationManager = (LocationManager) getContext().getSystemService(getContext().LOCATION_SERVICE);
			Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (locationGPS != null) {
				double lat = locationGPS.getLatitude();
				double longi = locationGPS.getLongitude();
				LatLng latLng = new LatLng(lat, longi);
				return latLng;
			} else {
				return null;
			}
		}
		return null;
	}
	@SuppressLint("MissingPermission")
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		MAP_ZOOM_MAX = mMap.getMaxZoomLevel();
		MAP_ZOOM_MIN = mMap.getMinZoomLevel();
//		mMap.setMyLocationEnabled(true);

//		LatLng origin = new LatLng(22.4945112, 88.3582335);
//		LatLng destination = new LatLng(22.540953, 88.3603394);
//		DrawRouteMaps.getInstance(getActivity()).draw(origin, destination, mMap);
//		DrawMarker.getInstance(getActivity()).draw(mMap, origin, R.drawable.ic_room_teal_700_24dp, "Origin Location");
//		DrawMarker.getInstance(getActivity()).draw(mMap, destination, R.drawable.ic_home_teal_700_24dp, "Destination Location");

//		LatLngBounds bounds = new LatLngBounds.Builder().include(origin).include(destination).build();
//		Point displaySize = new Point();
//		getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
//		mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 800, 30));
		LatLng latLng = getLocation();
		if(latLng == null)
			latLng = new LatLng(22.4945112, 88.3582335);

		MarkerOptions markerOption = new MarkerOptions();
		markerOption.position(latLng);
		markerOption.title("Driver");
		markerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
		mCurrentMarker = mMap.addMarker(markerOption);
		CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(13).build();
		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

//		CameraPosition googlePlex = CameraPosition.builder()
//				.target(new LatLng(31.527653,74.455632))
//				.zoom(7)
//				.bearing(0)
//				.tilt(45)
//				.build();
//		mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 5000, null);

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
		if(updateLLocationDataTimer!=null)
			updateLLocationDataTimer.cancel();
	}

	public void getLatestLocation(){
		if(NetworkUtils.isConnected()) {
			mSyncManager = new SyncManager(getActivity(), this);
			String email = GlobalPreferenceManager.getUserEmail();
			String uniqueID = GlobalPreferenceManager.getUniqueId();
			String routeID = GlobalPreferenceManager.getParentRouteID();
			mSyncManager.parentTrackBus(email, uniqueID, routeID);
		}
	}

	@Override
	public void onSyncComplete(int syncPage, int response, Object data) {
		if (syncPage == Constant.SYNC_PARENT_TRACKBUS) {
			if (response == Constant.SUCCESS) {
				LogUtils.i(((JSONObject)data).toString());
				try {
					JSONObject jsonObject = (JSONObject)data;
					String latitude = jsonObject.getString("Latitude");
					String longitude = jsonObject.getString("Longitude");
					LatLng latLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
					updateMapwithMarker(latLng);
					updateLLocationDataTimer.start();

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void animateMarker(final Marker marker, final LatLng toPosition, final boolean hideMarker) {
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		Projection proj = mMap.getProjection();
		Point startPoint = proj.toScreenLocation(marker.getPosition());
		final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		final long duration = 500;

		final Interpolator interpolator = new LinearInterpolator();

		handler.post(new Runnable() {
			@Override
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float) elapsed
						/ duration);
				double lng = t * toPosition.longitude + (1 - t)
						* startLatLng.longitude;
				double lat = t * toPosition.latitude + (1 - t)
						* startLatLng.latitude;
				marker.setPosition(new LatLng(lat, lng));

				if (t < 1.0) {
					// Post again 16ms later.
					handler.postDelayed(this, 16);
				} else {
					if (hideMarker) {
						marker.setVisible(false);
					} else {
						marker.setVisible(true);
					}
				}
			}
		});
	}
	public void updateMapwithMarker(LatLng latLng){
		//if(mCurrentMarker != null){
		//	mCurrentMarker.remove();
		//}
		Toast.makeText(getActivity(), "Driver New location Lat:" + latLng.latitude + " Lng:" + latLng.longitude, Toast.LENGTH_SHORT).show();
		//MarkerOptions markerOption = new MarkerOptions();
		//markerOption.position(latLng);
		//markerOption.title("Driver");
		//markerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
		//mCurrentMarker = mMap.addMarker(markerOption);
		//CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).zoom(13).build();
		//mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
		//LatLng currentLatLng = new LatLng(location.getLatitude(),location.getLongitude());
		//CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentLatLng,15);
		//mMap.moveCamera(update);
		animateMarker(mCurrentMarker, latLng, false);
	}
}
