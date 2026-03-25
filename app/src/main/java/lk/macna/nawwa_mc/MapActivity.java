package lk.macna.nawwa_mc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker currentLocationMarker;
    private Button buttonLiveLocation;
    private Button buttonMapType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        buttonLiveLocation = findViewById(R.id.buttonLiveLocation);
        buttonLiveLocation.setOnClickListener(v -> enableMyLocation());

        buttonMapType = findViewById(R.id.buttonMapType);
        buttonMapType.setOnClickListener(v -> changeMapType());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Show zoom controls on the map
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Set a map click listener to get the clicked location
        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            Marker selectedMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            Intent resultIntent = new Intent();
            resultIntent.putExtra("location", latLng.latitude + "," + latLng.longitude);
            setResult(RESULT_OK, resultIntent);

            // Show toast when marker is selected
            if (selectedMarker != null) {
                selectedMarker.showInfoWindow();
                Toast.makeText(this, "Marker Selected: " + latLng.latitude + ", " + latLng.longitude, Toast.LENGTH_SHORT).show();
            }

            finish();
        });

        // Enable current location and add marker
        enableMyLocation();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        if (currentLocationMarker != null) {
                            currentLocationMarker.remove();
                        }
                        currentLocationMarker = mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15), 2000, null);

                        mMap.setOnMarkerClickListener(marker -> {
                            if (marker.equals(currentLocationMarker)) {
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18), 2000, null);
                                return true;
                            }
                            return false;
                        });
                    }
                });
    }

    private void changeMapType() {
        if (mMap != null) {
            if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_TERRAIN) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            } else {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission is required to use this feature.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}