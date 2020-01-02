package com.example.sportfinds;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;

import java.util.Random;


public class BadmintonMapsActivity extends FragmentActivity implements OnMapReadyCallback,
GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener,
LocationListener{

    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 7192;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 300193;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mlastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference ref;
    GeoFire geofire;
    VerticalSeekBar mSeekbar;

    Marker mCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badminton_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ref = FirebaseDatabase.getInstance().getReference("MyLocation");
        geofire = new GeoFire(ref);

        mSeekbar = (VerticalSeekBar)findViewById(R.id.verticalSeekbar);
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                mMap.animateCamera(CameraUpdateFactory.zoomTo(i), 2000, null);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setUpLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if (checkPlayServices())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }
                }
                break;
        }

    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //Request runtime permission
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }
        else
        {
            if (checkPlayServices())
            {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        mlastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mlastLocation != null)
        {
            final double latitude = mlastLocation.getLatitude();
            final double longitude = mlastLocation.getLongitude();

            //update to firebase
            geofire.setLocation("You", new GeoLocation(latitude, longitude),
                    new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            //add marker
                            if (mCurrent != null)
                            {
                                mCurrent.remove();//remove old marker
                            }
                            mCurrent = mMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(latitude, longitude))
                                    .title("You"));

                            //move camera to our position
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude),14.0f));
                        }
                    });



            Log.d("EDMTDEV", String.format("Your location was changed : %f/ %f", latitude, longitude));
        }
        else
        {
            Log.d("EDMTDEV","Can not get your location");
        }
    }

    private void createLocationRequest()  {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            else
            {
                Toast.makeText(this, "This device is not supported! ", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;

        }
        return true;
    }

    //MENDAPAT
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //create court badminton area
        //MERLIMAU PASIR
        LatLng merlimau_area = new LatLng(2.1472028,102.4285198);
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(2.1472028,102.4285198))
                .title("Dewan Datuk Hj Ahmad Ithnin Badminton court")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
        mMap.addCircle(new CircleOptions()
                .center(merlimau_area)
                .radius(1000) //in meters
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f)

        );
        //MERLIMAU
        LatLng dangAnum_area = new LatLng(2.1572249,102.4275062);
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(2.1572249,102.4275062))
                .title("Dewan Seri Anum SMK Dang Anum Badminton court")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
        mMap.addCircle(new CircleOptions()
                .center(dangAnum_area)
                .radius(1000) //in meters
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f)
        );
        //LIPAT KAJANG
        LatLng lipatKajang_area = new LatLng(2.2733989,102.4415642);
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(2.2733989,102.4415642))
                .title("Dewan MPJ Lipat Kajang Badminton Court")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
        mMap.addCircle(new CircleOptions()
                .center(lipatKajang_area)
                .radius(1000) //in meters
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f)
        );

        //SAWIT MERLIMAU
//        LatLng sawit_area = new LatLng(2.160004,102.436082);
//        mMap.addMarker(new MarkerOptions()
//                .position(new LatLng(2.160004,102.436082))
//                .title("Sawit futsal court"));
//        mMap.addCircle(new CircleOptions()
//                .center(sawit_area)
//                .radius(1000) //in meters
//                .strokeColor(Color.BLUE)
//                .fillColor(0x220000FF)
//                .strokeWidth(5.0f)
//        );
        //Add geoQuery here
        //0.5f = 0.5km = 500m

        //MERLIMAU PASIR
        GeoQuery mendapatQuery = geofire.queryAtLocation(new GeoLocation(merlimau_area.latitude, merlimau_area.longitude),1.0f);
        mendapatQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendNotification("SPORTFINDER", String.format("%s entered the Dewan Datuk Hj Ahmad Ithnin Badminton court area", key));
            }

            @Override
            public void onKeyExited(String key) {
                sendNotification("SPORTFINDER", String.format("%s is no longer in the Dewan Datuk Hj Ahmad Ithnin Badminton court area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d("MOVE", String.format("%s moved within the badminton court area[%f/%f]",key,location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e("ERROR", " " + error);
            }
        });

        //DANG ANUM
        GeoQuery frescoQuery = geofire.queryAtLocation(new GeoLocation(dangAnum_area.latitude, dangAnum_area.longitude),1.0f);
        frescoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendNotification("SPORTFINDER", String.format("%s entered the Dewan Seri Anum SMK Dang Anum Badminton court area", key));
            }

            @Override
            public void onKeyExited(String key) {
                sendNotification("SPORTFINDER", String.format("%s is no longer in the Dewan Seri Anum SMK Dang Anum Badminton court area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d("MOVE", String.format("%s moved within the Badminton court area[%f/%f]",key,location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e("ERROR", " " + error);
            }
        });

        //LIPAT KAJANG
        GeoQuery merlimauPQuery = geofire.queryAtLocation(new GeoLocation(lipatKajang_area.latitude, lipatKajang_area.longitude),1.0f);
        merlimauPQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendNotification("SPORTFINDER", String.format("%s entered the Dewan MPJ Lipat Kajang Badminton Court area", key));
            }

            @Override
            public void onKeyExited(String key) {
                sendNotification("SPORTFINDER", String.format("%s is no longer in the Dewan MPJ Lipat Kajang Badminton Court area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d("MOVE", String.format("%s moved within the badminton court area[%f/%f]",key,location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e("ERROR", " " + error);
            }
        });

        //SAWIT
//        GeoQuery sawitQuery = geofire.queryAtLocation(new GeoLocation(sawit_area.latitude, sawit_area.longitude),1.0f);
//        sawitQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
//            @Override
//            public void onKeyEntered(String key, GeoLocation location) {
//                sendNotification("GEOFENCING", String.format("%s entered the Sawit futsal court area", key));
//            }
//
//            @Override
//            public void onKeyExited(String key) {
//                sendNotification("GEOFENCING", String.format("%s is no longer in the Sawit futsal court area", key));
//            }
//
//            @Override
//            public void onKeyMoved(String key, GeoLocation location) {
//                Log.d("MOVE", String.format("%s moved within the futsal court area[%f/%f]",key,location.latitude, location.longitude));
//            }
//
//            @Override
//            public void onGeoQueryReady() {
//
//            }
//
//            @Override
//            public void onGeoQueryError(DatabaseError error) {
//                Log.e("ERROR", " " + error);
//            }
//        });


    }

    private void sendNotification(String title, String content) {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(content);

        NotificationManager manager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, BadmintonMapsActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;

        manager.notify(new Random().nextInt(), notification);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mlastLocation = location;
        displayLocation();
    }
}
