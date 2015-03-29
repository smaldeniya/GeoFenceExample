package com.test.maps;


import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceTransitionsIntentService extends IntentService {

	public GeofenceTransitionsIntentService() {
		super(GeofenceTransitionsIntentService.class.getSimpleName());
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		Log.i("GeoFenceService", "recieved");

		GeofencingEvent gfEvent = GeofencingEvent.fromIntent(intent);

		if (gfEvent.hasError()) {
			Log.e(GeofenceTransitionsIntentService.class.getName(), "Error "
					+ gfEvent.getErrorCode());
			Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
			return;
		}

		int gfTransition = gfEvent.getGeofenceTransition();

		if (gfTransition == Geofence.GEOFENCE_TRANSITION_ENTER
				|| gfTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

			String geofenceTransitionDetails = getGeofenceTransitionDetails(
					getApplicationContext(), gfTransition, gfEvent.getTriggeringGeofences());
			
			NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
									.setContentTitle("Move detected!")
									.setSmallIcon(R.drawable.ic_launcher)
									.setContentText("We have detected a movement " + geofenceTransitionDetails);
			
			builder.setAutoCancel(true);
			
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(0, builder.build());

		}

	}

	private String getGeofenceTransitionDetails(Context context,
			int geofenceTransition, List<Geofence> triggeringGeofences) {

		String geofenceTransitionString = getTransitionString(geofenceTransition);
		
		ArrayList triggeringGeofencesIdsList = new ArrayList();
		
		for (Geofence geofence : triggeringGeofences) {
			triggeringGeofencesIdsList.add(geofence.getRequestId());
		}
		String triggeringGeofencesIdsString = TextUtils.join(", ",
				triggeringGeofencesIdsList);
		return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
	}

	private String getTransitionString(int transitionType) {
		
		switch (transitionType) {
			case Geofence.GEOFENCE_TRANSITION_ENTER:
				return "Entered to geofence";
			case Geofence.GEOFENCE_TRANSITION_EXIT:
				return "Exit from geofence";
			default:
				return "Can not identify";
		}
		
	}

}
