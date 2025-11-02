package com.quicksoft.school.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.quicksoft.school.activity.login.LoginActivity;
import com.quicksoft.school.activity.login.OTPActivity;
import com.quicksoft.school.connection.SyncManager;
import com.quicksoft.school.connection.callback.SyncCompleteCallback;
import com.quicksoft.school.preferences.GlobalPreferenceManager;
import com.quicksoft.school.util.Constant;

import org.json.JSONObject;

import es.dmoral.toasty.Toasty;
import android.os.Build;

public class LocationService extends Service implements SyncCompleteCallback {
    private final LocationServiceBinder binder = new LocationServiceBinder();
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private NotificationManager notificationManager;

    private final int LOCATION_INTERVAL = 500;
    private final int LOCATION_DISTANCE = 10;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onSyncComplete(int syncPage, int response, Object data) {
        if (syncPage == Constant.SYNC_DRIVER_LOCATION) {
            if (response == Constant.SUCCESS) {
                LogUtils.i(((JSONObject) data).toString());
            } else if (response == Constant.FAIL) {
                int respCode = (int) data;
            }
        }
    }

    private class LocationListener implements android.location.LocationListener
    {
        private Location mLastLocation;
        private SyncManager mSyncManager;

        public LocationListener(String provider)
        {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            mLastLocation = location;
            LogUtils.i( "Current Location is - Lat: " + mLastLocation.getLatitude() + "Long: " + mLastLocation.getLongitude());
            if(NetworkUtils.isConnected()) {
                mSyncManager = new SyncManager(LocationService.this, LocationService.this);
                String email = GlobalPreferenceManager.getUserEmail();
                String uniqueID = GlobalPreferenceManager.getUniqueId();
                mSyncManager.sendDriverLocation(email, uniqueID, "" + mLastLocation.getLatitude(), "" + mLastLocation.getLongitude());
                //Toast.makeText(getApplicationContext(), "location " + "Lat:" + mLastLocation.getLatitude() + " Lng:" + mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            LogUtils.i( "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            LogUtils.i("onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            LogUtils.i("onStatusChanged: " + status);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate()
    {
        LogUtils.i("onCreate");
        Notification noti = getNotification();
        if(noti != null)
            startForeground(12345678, getNotification());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (Exception ex) {
                LogUtils.i("fail to remove location listners, ignore", ex);
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void startTracking() {
        initializeLocationManager();
        mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER);

        try {
            mLocationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener);
        } catch (java.lang.SecurityException ex) {
            // Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            // Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    public void stopTracking() {
        this.onDestroy();
    }

    private Notification getNotification() {

        NotificationManager mgr=
                (NotificationManager)getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.O && mgr.getNotificationChannel("channel_01")==null) {
            NotificationChannel channel = new NotificationChannel("channel_01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);

            //NotificationManager notificationManager = getSystemService(NotificationManager.class);
            //notificationManager.createNotificationChannel(channel);
            mgr.createNotificationChannel(channel);
            Notification.Builder builder = new Notification.Builder(getApplicationContext(), "channel_01").setAutoCancel(true);
            return builder.build();
        }
        else return null;

    }


    public class LocationServiceBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

}