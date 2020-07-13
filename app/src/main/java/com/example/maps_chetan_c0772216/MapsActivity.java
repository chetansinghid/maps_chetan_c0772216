package com.example.maps_chetan_c0772216;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int REQUEST_CODE = 1;
    List<Marker> markerList = new ArrayList<>();
    private Polygon quadilateral;
    private List<Polyline> polylineList = new ArrayList<>();
    private static final int MAX_MARKERS = 4;
    private ArrayList<Double> angleList = new ArrayList<>();
    private float distance = 0;


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
        setupPolylineClickListener();
//        moveCameraToLocationBounds();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(56.1304, -106.3468),10f));

    }

    private void setupPolygonClickListener() {
        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                Toast.makeText(MapsActivity.this, "Total Distance: " + distance + "KM", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void setupPolylineClickListener() {
        mMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                LatLng ptOne = polyline.getPoints().get(0);
                LatLng ptTwo = polyline.getPoints().get(1);

                double lat = (ptOne.latitude + ptTwo.latitude)/2;
                double lng = (ptOne.longitude + ptTwo.longitude)/2;
                float results[] = new float[10];
                Location.distanceBetween(ptOne.latitude, ptOne.longitude, ptTwo.latitude,ptTwo.longitude, results);
                float distanceLine = results[0];
                Toast.makeText(MapsActivity.this, "Distance: " + distanceLine + "KM", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setUpLongClickGestureRecognizer() {
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                for(Marker marker: markerList) {
                    if(Math.abs(marker.getPosition().latitude - latLng.latitude) < 0.05 && Math.abs(marker.getPosition().longitude - latLng.longitude) < 0.05) {
                        if(markerList.size() == MAX_MARKERS) {
                            removeOverlays();
                        }
                        marker.remove();
                        markerList.remove(marker);
                        break;
                    }
                }
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
                if(newAddressDetails.size()>0)
                {
                    marker.setTitle(newAddressDetails.get(0));
                    marker.setSnippet(newAddressDetails.get(1));
                }
                marker.showInfoWindow();
                checkQuadilateral();
            }

            private void checkQuadilateral() {
                if(markerList.size() == MAX_MARKERS) {
                    removeOverlays();
                    sortPoints();
                    drawPolygon();
                    drawPolylines();
                }
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
                if (markerList.size() == MAX_MARKERS) {
                    sortPoints();
                    drawPolygon();
                    drawPolylines();
                }

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
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        return markerOptions;
    }

    //            draws polyline
    private void drawPolylines() {
        for(int i = 0; i<4; i++) {
            if(i==3) {
                drawPolyline(markerList.get(i), markerList.get(0));
            }
            else {
                drawPolyline(markerList.get(i), markerList.get(i + 1));
            }
        }
    }

    private void drawPolyline(Marker pointA, Marker pointB) {
        Polyline polyline = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(
                        pointA.getPosition(),
                        pointB.getPosition()));
        polyline.setWidth(10);
        polyline.setColor(Color.RED);
        polylineList.add(polyline);
        float results[] = new float[10];
        Location.distanceBetween(pointA.getPosition().latitude, pointA.getPosition().longitude,
                pointB.getPosition().latitude, pointB.getPosition().longitude, results);
        distance += results[0];
    }

    private void drawPolygon() {
        PolygonOptions options = new PolygonOptions()
                .fillColor(Color.parseColor("#5900FF00"))
                .clickable(true);
        for (int i=0; i<MAX_MARKERS; i++) {
            options.add(markerList.get(i).getPosition());
        }

        quadilateral = mMap.addPolygon(options);
    }
    //          sort points for simple polygon
    private void sortPoints() {
        findAngles();
        for(int i=0;i<4;i++) {
            for(int j=0; j<3; j++) {
                if(angleList.get(j) > angleList.get(j+1)) {
                    double angle = angleList.get(j);
                    angleList.set(j, angleList.get(j+1));
                    angleList.set(j+1, angle);

                    Marker marker = markerList.get(j);
                    markerList.set(j, markerList.get(j+1));
                    markerList.set(j+1, marker);
                }
            }
        }
    }
    //            finds angles
    private void findAngles() {
        LatLng centerLocation = findCenter();
        double dx, dy;

        for (int i = 0; i < MAX_MARKERS; i++) {
            LatLng position = markerList.get(i).getPosition();
            dx = position.latitude - centerLocation.latitude;
            dy = position.longitude - centerLocation.longitude;

            angleList.add(Math.atan2(dy, dx));
        }
    }
    //          find center point
    private LatLng findCenter() {

        double x = 0, y = 0;

        for (int i = 0; i < MAX_MARKERS; i++) {
            x += markerList.get(i).getPosition().latitude;
            y += markerList.get(i).getPosition().longitude;
        }
        LatLng centerLocation = new LatLng(x / MAX_MARKERS, y / MAX_MARKERS);
        return centerLocation;
    }

    private void removeOverlays() {
        for(Polyline line: polylineList) {
            line.remove();
        }
        quadilateral.remove();
    }
//    move the camera to the location bounds
//    private void moveCameraToLocationBounds() {
//        latLngBounds = new LatLngBounds(cityA, cityD);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 10));
//    }


}