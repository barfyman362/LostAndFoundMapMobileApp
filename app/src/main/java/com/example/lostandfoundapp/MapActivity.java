package com.example.lostandfoundapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    GoogleMap googleMap;
    EditText editRadius;
    Button btnApplyRadius;

    DatabaseHelper dbHelper;
    ArrayList<Advert> adverts;

    double userLatitude = 0;
    double userLongitude = 0;
    boolean hasUserLocation = false;

    private static final int LOCATION_PERMISSION_CODE = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        editRadius = findViewById(R.id.editRadius);
        btnApplyRadius = findViewById(R.id.btnApplyRadius);

        dbHelper = new DatabaseHelper(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnApplyRadius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMarkers();
            }
        });
    }

    // called when the map is ready
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        getUserLocation();
        showMarkers();

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Object tag = marker.getTag();

                if (tag != null) {
                    Intent intent = new Intent(MapActivity.this, AdvertDetailActivity.class);
                    intent.putExtra("advertId", (int) tag);
                    startActivity(intent);
                }
            }
        });
    }

    // gets the user's current location for the radius search
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE
            );

            return;
        }

        googleMap.setMyLocationEnabled(true);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Location currentLocation = null;

        try {
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (currentLocation == null) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Could not get current location", Toast.LENGTH_SHORT).show();
        }

        if (currentLocation != null) {
            userLatitude = currentLocation.getLatitude();
            userLongitude = currentLocation.getLongitude();
            hasUserLocation = true;

            LatLng userPosition = new LatLng(userLatitude, userLongitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 12));
        } else {
            // default spot, mainly useful for emulator testing
            LatLng melbourne = new LatLng(-37.8136, 144.9631);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(melbourne, 10));
        }
    }

    // adds the saved adverts to the map
    private void showMarkers() {
        if (googleMap == null) {
            return;
        }

        googleMap.clear();

        adverts = dbHelper.getAllAdverts();

        double radius = getRadius();
        int count = 0;

        for (int i = 0; i < adverts.size(); i++) {
            Advert advert = adverts.get(i);

            if (advert.latitude == 0 && advert.longitude == 0) {
                continue;
            }

            if (hasUserLocation && radius > 0) {
                double distance = getDistance(
                        userLatitude,
                        userLongitude,
                        advert.latitude,
                        advert.longitude
                );

                if (distance > radius) {
                    continue;
                }
            }

            LatLng advertSpot = new LatLng(advert.latitude, advert.longitude);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(advertSpot)
                    .title(advert.type + ": " + advert.description)
                    .snippet("Tap for details");

            if (advert.type.equals("Lost")) {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            } else {
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }

            Marker marker = googleMap.addMarker(markerOptions);

            if (marker != null) {
                marker.setTag(advert.id);
            }

            if (count == 0) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(advertSpot, 12));
            }

            count++;
        }

        if (count == 0) {
            Toast.makeText(this, "No adverts found", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, count + " advert(s) shown", Toast.LENGTH_SHORT).show();
        }
    }

    // gets the radius from the text box in kilometres
    private double getRadius() {
        String radiusText = editRadius.getText().toString().trim();

        if (radiusText.isEmpty()) {
            return 0;
        }

        try {
            return Double.parseDouble(radiusText);
        } catch (Exception e) {
            Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show();
            return 0;
        }
    }

    // works out the distance between the user and an advert in kilometres
    private double getDistance(double startLatitude, double startLongitude,
                               double endLatitude, double endLongitude) {
        float[] result = new float[1];

        Location.distanceBetween(
                startLatitude,
                startLongitude,
                endLatitude,
                endLongitude,
                result
        );

        return result[0] / 1000.0;
    }

    // runs again after the user accepts location permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
                showMarkers();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                showMarkers();
            }
        }
    }
}