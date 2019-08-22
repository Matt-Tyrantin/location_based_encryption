package com.example.location_basedencryption;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class CipherLocationListener implements LocationListener {
    private Location mLocation;

    @Override
    public void onLocationChanged(Location location)
    {
        this.mLocation = location;

        Log.v("Coordinates", this.getLocationString(location));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }

    public Location getCurrentLocation() throws NullLocationException
    {
        if (mLocation != null) {
            return this.mLocation;

        } else {
            throw new NullLocationException();
        }
    }

    public String getLocationString(Location location)
    {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        return String.format("%.4f", longitude)
                + ','
                + String.format("%.4f", latitude);
    }

    public String getAltitudeLocationString(Location location)
    {
        double altitude = location.getAltitude();

        return getLocationString(location)
                + ','
                + String.format("%.4f", altitude);
    }
}
