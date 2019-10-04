package com.project.ways;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.content.Context.LOCATION_SERVICE;

public class LocationTracker implements LocationListener {
    private final Activity mContext;
    private static final int PERMISSIONS_REQUEST_LOCATION=99;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES=10;
    private static final long MIN_TIME_BW_UPDATES=1000*60;

    private boolean mlsGPSEnabled;
    private boolean mlsNetworkEnabled;

    private Location location;
    private double latitude;
    private double longitude;
    private LocationManager locationManager;

    public LocationTracker(Activity context){
        this.mContext = context;
    }
    /**
     * This function returns the location of current location. Either from GPS or from Network.
     * GPS will be picked up with higher priority.
     * @return current location, on emulator, default is 1600 Amphetheater Way, Montain *View
     */
    public Location getLocation(){
        try{
            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);
            // getting GPS status
            mlsGPSEnabled=locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            mlsNetworkEnabled=locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if(!mlsGPSEnabled && !mlsNetworkEnabled){
                return null;
            } else {
                // First get location from Network Provider
                checkLocationPermission();
                if(mlsNetworkEnabled){
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if(locationManager!=null){
                        location=locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if(location!=null){
                            latitude=location.getLatitude();
                            longitude=location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS services
                if(mlsGPSEnabled){
                    if(location == null){
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        if(locationManager!=null){
                            location=locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if(location!=null){
                                latitude=location.getLatitude();
                                longitude=location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return location;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    /**
     * Get latitude
     * @return latitude
     */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }
        return latitude;
    }

    /**
     * Get Longitude
     * @return longitude
     */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        return longitude;
    }
    /**
     * Run time permission check
     * @return if the permission is set
     */
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            // No explanation needed, we can requestion the permission
            ActivityCompat.requestPermissions(mContext,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }
        return true;
    }
    }

