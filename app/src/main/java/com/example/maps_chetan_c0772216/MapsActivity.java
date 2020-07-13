package com.example.maps_chetan_c0772216;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int REQUEST_CODE = 1;
    List<Marker> markerList = new ArrayList<>();
    private LatLngBounds latLngBounds;
    private Polygon quadilateral;
    private static final int MAX_MARKERS = 4;
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

        setupLocationTracker();
        setUpMoveMarkerLocationUpdateTracker();
        setUpLongClickGestureRecognizer();
        setupPolygonClickListener();
//        moveCameraToLocationBounds();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(56.1304, -106.3468)));

    }

    private void setupPolygonClickListener() {
        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                
            }
        });
    }

    private void setUpLongClickGestureRecognizer() {
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                LatLng l1 = new LatLng(latLng.latitude - 5, latLng.longitude - 5);
                LatLng l2 = new LatLng(latLng.latitude + 5, latLng.latitude + 5);
                LatLngBounds latLngBounds = new LatLngBounds(l1, l2);
                if(latLngBounds.contains(markerList.get(0).getPosition())) {
                    clearMarker();
                }
            }
            public void clearMarker() {
                markerList.get(0).remove();
            }

        });
    }

    //  updates the marker location on dragging
    private void setUpMoveMarkerLocationUpdateTracker() {
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                marker.hideInfoWindow();
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng newLocation = marker.getPosition();
                ArrayList<String> newAddressDetails = getLocationAddressDetails(newLocation);
                marker.setTitle(newAddressDetails.get(0));
                marker.setSnippet(newAddressDetails.get(1));
                marker.showInfoWindow();
            }
        });
    }
//  sets up adding location marker on tap
    private void setupLocationTracker() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                updateMapView(latLng);
            }

            public void updateMapView(LatLng location) {
                if (markerList.size() < MAX_MARKERS) {
                    addLocationMarker(location);
                }
//                checks if new marker addition causes quad formation
                if (markerList.size() == MAX_MARKERS)
                    drawShape();
            }

            private void drawShape() {
                PolygonOptions options = new PolygonOptions()
                        .fillColor(Color.parseColor("#5900FF00"))
                        .strokeColor(Color.RED)
                        .strokeWidth(10);

                for (int i=0; i<MAX_MARKERS; i++) {
                    options.add(markerList.get(i).getPosition());
                }

                quadilateral = mMap.addPolygon(options);
            }

        });
    }

    //  request the location permission
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }
    //    returns the boolean result weather location acess granted or not
    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
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
        markerList.add(mMap.addMarker(locationMarker));
    }
//  fetches the geocoder details of latlangs
    private ArrayList<String> getLocationAddressDetails(LatLng location) {
        ArrayList<String> addressDetails = new ArrayList<>();
        try {
            Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            Address address = addressList.get(0);
            String title = address.getThoroughfare() + "," + address.getSubThoroughfare() + address.getPostalCode();
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
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        return markerOptions;
    }
//    move the camera to the location bounds
//    private void moveCameraToLocationBounds() {
//        latLngBounds = new LatLngBounds(cityA, cityD);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 10));
//    }


}