package com.test.maps.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.test.maps.GeofenceTransitionsIntentService;
import com.test.maps.R;

public class MapViewFragment extends Fragment implements OnMapLongClickListener, ConnectionCallbacks, OnConnectionFailedListener{

	private SupportMapFragment fragment;
	private GoogleMap googleMap;
	private Circle geofenceCircle;
	private GoogleApiClient mGoogleApiClient;
	
	private static boolean isGoogleApiConnected = false;
	
	private Context context;
	
	public static final List<Geofence> GEOFENCE_LIST = new ArrayList<Geofence>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.map_view, container, false);
		context = getActivity();
		mGoogleApiClient = new GoogleApiClient.Builder(context)
									.addApi(LocationServices.API)
									.addConnectionCallbacks(this)
									.addOnConnectionFailedListener(this)
									.build();
		mGoogleApiClient.connect();
							
		return view;

	}

	// adding new map fragment if map fragment is not exists
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentManager fm = getChildFragmentManager();
		fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
		if (fragment == null) {
			fragment = SupportMapFragment.newInstance();
			fm.beginTransaction().replace(R.id.map, fragment).commit();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (googleMap == null) {

			googleMap = fragment.getMap();

			googleMap.setMyLocationEnabled(true);

			LocationManager locationManager = (LocationManager) getActivity()
					.getSystemService(Context.LOCATION_SERVICE);

			List<String> providers = locationManager.getProviders(true);
			Location bestLocation = null;

			do {
				
				bestLocation = getBestLocation(locationManager, providers);
				
			} while (bestLocation == null);

			double lat = bestLocation.getLatitude();
			double lng = bestLocation.getLongitude();
			LatLng coordinate = new LatLng(lat, lng);

			CameraUpdate center = CameraUpdateFactory.newLatLng(coordinate);
			CameraUpdate zoom = CameraUpdateFactory.zoomTo(18);
			googleMap.moveCamera(zoom);
			googleMap.moveCamera(center);
			
			googleMap.setOnMapLongClickListener(this);

		}

	}

	@Override
	public void onMapLongClick(LatLng arg0) {
		
		if(isGoogleApiConnected) {
			Geofence geofence = new Geofence.Builder().setRequestId(arg0.toString())
					.setCircularRegion(arg0.latitude, arg0.longitude, 30.0f)
					.setExpirationDuration(50000)
					.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
					.build();
			
				GEOFENCE_LIST.add(geofence);
				
				CircleOptions circleArea = new CircleOptions();
				circleArea.center(arg0)
					.radius(30.0f)
					.fillColor(0x40ff0000)
					.strokeColor(Color.TRANSPARENT).strokeWidth(2.0f);
				geofenceCircle = googleMap.addCircle(circleArea);
				
				Intent intent = new Intent(context, GeofenceTransitionsIntentService.class);
				PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				
				GeofencingRequest.Builder gfrBuilder = new GeofencingRequest.Builder();
				gfrBuilder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
				gfrBuilder.addGeofences(GEOFENCE_LIST);
				GeofencingRequest gfRequest = gfrBuilder.build();
				
						
				LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, gfRequest, pendingIntent);
		} else {
			Toast.makeText(context, "App is not connected to google apis yet.", Toast.LENGTH_LONG).show();
			mGoogleApiClient.connect();
		}
		
	}
	
	@Override
	public void onConnected(Bundle connectionHint) {
		isGoogleApiConnected = true;
		Log.i(MapViewFragment.class.getName(), "Connected to Service- Location");
	}

	@Override
	public void onConnectionSuspended(int cause) {
		Log.i(MapViewFragment.class.getName(), "Connection suspended");
		mGoogleApiClient.connect();
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		isGoogleApiConnected = false;
		Log.e(MapViewFragment.class.getName(), "error: " + arg0.getErrorCode());
	}

	
	private Location getBestLocation(LocationManager locationManager, List<String> providers) {
		
		Location bestLocation = null;
		
		for (String provider : providers) {
			Location l = locationManager.getLastKnownLocation(provider);
			if (l == null) {
				continue;
			}
			if (bestLocation == null
					|| l.getAccuracy() < bestLocation.getAccuracy()) {
				// Found best last known location: %s", l);
				bestLocation = l;
			}
			
		}
		
		return bestLocation;
	}
	
}
