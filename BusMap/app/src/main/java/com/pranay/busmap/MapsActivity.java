package com.pranay.busmap;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.transit.realtime.GtfsRealtime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, LocationListener {

    private GoogleMap mMap;

    ArrayList<Float> lat_points = new ArrayList<>();
    ArrayList<Float> long_points = new ArrayList<>();
    ArrayList<String> bus_ids = new ArrayList<>();
    ArrayList<String> selectedBus = new ArrayList<>();
    String type = "";
    Handler handler = new Handler();
    LocationRequest mLocationRequest;
    GoogleApiClient googleApiClient;
    Location mLocation;
    Marker mMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        type = getIntent().getStringExtra("type");
        selectedBus = getIntent().getStringArrayListExtra("list");

        Log.e("xyz", "onCreate: " + type );
        Log.e("xyz", "onCreate: " + selectedBus );
        handler.postDelayed(new Runnable() {
            public void run() {

                new busInformation().execute();
                handler.postDelayed(this, 15000);
            }
        }, 15000);

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    Activity#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for Activity#requestPermissions for more details.
                return;
            }
        }
        mMap.setMyLocationEnabled(true);
        if (googleApiClient == null) {
            init();
        }
        new busInformation().execute();
    }

    protected synchronized void init() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(7000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        mLocation = location;
        if (mMarker != null) {
            mMarker.remove();
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Me");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        mMarker = mMap.addMarker(markerOptions);
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,14));
    }

    class busInformation extends AsyncTask<Void,Void,Void> {
        GtfsRealtime.FeedMessage feed = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MapsActivity.this, "Refresh Bus location", Toast.LENGTH_SHORT).show();
            lat_points.clear();
            long_points.clear();
            bus_ids.clear();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            URL url = null;
            try {
                url = new URL("http://gtfs.halifax.ca/realtime/Vehicle/VehiclePositions.pb");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(MapsActivity.this, "Updated", Toast.LENGTH_SHORT).show();
            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (entity.hasVehicle()) {
                    lat_points.add(entity.getVehicle().getPosition().getLatitude());
                    long_points.add(entity.getVehicle().getPosition().getLongitude());
                    bus_ids.add(entity.getVehicle().getVehicle().getId());
                }
            }

            mMap.clear();


            for (int i = 0; i < lat_points.size(); i++) {

                if(type != null &&type.equals("select")){

                    if(checkINselectData(bus_ids.get(i))){
                        LatLng bus = new LatLng(lat_points.get(i), long_points.get(i));
                        mMap.addMarker(new MarkerOptions()
                                .position(bus)
                                .title("Bus ID:-" + bus_ids.get(i))
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_indicator)));
                    }
                }else {
                    LatLng bus = new LatLng(lat_points.get(i), long_points.get(i));
                    mMap.addMarker(new MarkerOptions()
                            .position(bus)
                            .title("Bus ID:-" + bus_ids.get(i))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_indicator)));
                }
            }
        }

    }

    private boolean checkINselectData(String s) {
        for (int i = 0; i < selectedBus.size(); i++) {
            if(selectedBus.get(i).equals(s)){
                return true;
            }
        }
        return false;
    }
}
