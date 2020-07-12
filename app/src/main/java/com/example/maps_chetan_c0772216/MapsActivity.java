package com.example.maps_chetan_c0772216;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int REQUEST_CODE = 1;
    private LatLng cityA, cityB, cityC, cityD;

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

        if(!hasLocationPermission()) {
            requestLocationPermission();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        setupLocations();


    }

    //  request the location permission
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }
    //    returns the boolean result weather location acess granted or not
    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
//    sets up the initial location for cities
    private void setupLocations() {
        cityA = new LatLng(43.6532, -79.3832);
        addLocationMarker(cityA);
        cityB = new LatLng(49.246292, -123.116226);
        addLocationMarker(cityB);
        cityC = new LatLng(51.0447, -114.0719);
        addLocationMarker(cityC);
        cityD = new LatLng(45.421532, -75.697189);
        addLocationMarker(cityD);
    }
//  adds the location marker
    private void addLocationMarker(LatLng location) {
        ArrayList<String> locationDetails = getLocationAddressDetails(location);
        String title, snippet;
        if(locationDetails.isEmpty()) {
            title = "Couldn't fetch details";
            snippet = "Couldn't fetch details";
        }
        else {
            title = locationDetails.get(0);
            snippet = locationDetails.get(1);
        }

        MarkerOptions locationMarker = makeMarker(location, title, snippet);
        mMap.addMarker(locationMarker);
    }
//  fetches the geocoder details of latlangs
    private ArrayList<String> getLocationAddressDetails(LatLng location) {
        ArrayList<String> addressDetails = new ArrayList<>();
        try {
            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            Address address = addressList.get(0);
            String title = address.getThoroughfare() + "," + address.getSubThoroughfare();
            String snippet = address.getLocality() + "," + address.getAdminArea();
            addressDetails.add(title);
            addressDetails.add(snippet);
        } catch (Exception exception) {
            Log.i("Geolocation data fetch error", exception.getMessage());
        }
        return addressDetails;
    }
//  sets up the marker
    private MarkerOptions makeMarker(LatLng location, String title, String snippet) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(location);
        markerOptions.title(title);
        markerOptions.snippet(snippet);
        markerOptions.draggable(true);
        return markerOptions;
    }


}