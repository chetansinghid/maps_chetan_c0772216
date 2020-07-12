package com.example.maps_chetan_c0772216;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int REQUEST_CODE = 1;

    private ArrayList<String> addressResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        if(!hasLocationPermission()) {
            requestLocationPermission();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        setupLocations();
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(toronto));

    }

    //  request the location permission
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    //    returns the boolean result weather location acess granted or not
    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setupLocations() {
        LatLng toronto = new LatLng(43.6532, -79.3832);
        addLocationMarker(toronto);
        LatLng vancouver = new LatLng(49.246292, -123.116226);
        addLocationMarker(vancouver);
        LatLng calgary = new LatLng(51.0447, -114.0719);
        addLocationMarker(calgary);
        LatLng ottawa = new LatLng(45.421532, -75.697189);
        addLocationMarker(ottawa);
    }

    private void addLocationMarker(LatLng location) {
        Location address = new Location("location");
        address.setLatitude(location.latitude);
        address.setLongitude(location.longitude);
//        String title = addressResult.get(0);
        String title = "";
        String snippet = "";
        MarkerOptions locationMarker = makeMarker(location, title, snippet);
        mMap.addMarker(locationMarker);
    }



    private MarkerOptions makeMarker(LatLng location, String title, String snippet) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(location);
        markerOptions.title(title);
        markerOptions.snippet(snippet);
        return markerOptions;
    }


}