package com.coltrack.controlrutasmonitorcolturex;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{
    String LOGTAG = "CONTROLRUTAS";
    private boolean currentlyProcessingLocation = false;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;


    public LocationService() {
        Log.d(LOGTAG,"en LocationService...constructor");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // if we are currently trying to get a location and the alarm manager has called this again,
        // no need to start processing a new location.
        if (!currentlyProcessingLocation) {
            currentlyProcessingLocation = true;
            startTracking();
        }

        return START_NOT_STICKY;
    }

    private void startTracking() {
        Log.d(LOGTAG,"startTracking...");
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            Log.e(LOGTAG, "unable to connect to google play services.");
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOGTAG, "onConnected");

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); // milliseconds
        locationRequest.setFastestInterval(1000); // the fastest rate in milliseconds at which your app can handle location updates
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOGTAG, "GoogleApiClient connection has been suspend");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOGTAG, "onConnectionFailed");

        stopLocationUpdates();
        stopSelf();

    }

    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOGTAG,"onLocationChanged");
        Log.d(LOGTAG, "position: " + location.getLatitude() + ", " + location.getLongitude() + " accuracy: " + location.getAccuracy());
        if (location.getAccuracy() < 500.0f) {
            stopLocationUpdates();
            sendLocationDataToWebsite(location);
        }
    }

    private void sendLocationDataToWebsite(Location location) {
        String provider=location.getProvider();
        float bearing=location.getBearing();
        float accuary=location.getAccuracy();
        double altitude=location.getAltitude();
        double latitude=location.getLatitude();
        double longitude=location.getLongitude();
        float velocidadmps=location.getSpeed();
        float velocidadkph=velocidadmps*3.6f;


        Log.i(LOGTAG,"provider: "+provider);
        Log.i(LOGTAG,"bearing: "+bearing);
        Log.i(LOGTAG,"accuary: "+accuary);
        Log.i(LOGTAG,"altitude: "+altitude);
        Log.i(LOGTAG,"latitude: "+latitude);
        Log.i(LOGTAG,"longitude: "+longitude);
        Log.i(LOGTAG,"velocidadmps: "+velocidadmps);
        Log.i(LOGTAG,"velocidadkph: "+velocidadkph);

        stopSelf();
    }
}
