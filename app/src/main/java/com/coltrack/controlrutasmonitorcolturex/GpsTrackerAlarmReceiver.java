package com.coltrack.controlrutasmonitorcolturex;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by Nelson Rodriguez on 30/03/2017.
 */

public class GpsTrackerAlarmReceiver extends WakefulBroadcastReceiver {
    private static final String TAG = "CONTROLRUTAS";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"en GpsTrackerAlarmReceiver");
        context.startService(new Intent(context, LocationService.class));
    }
}
