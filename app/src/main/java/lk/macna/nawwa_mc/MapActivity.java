package lk.macna.nawwa_mc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
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

import lk.macna.nawwa_mc.databinding.ActivityMapBinding;

/**
 * MapActivity allows users to select a location on a map or use their current live location.
 * Improved to show a confirmation button before returning the coordinates.
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float DEFAULT_ZOOM = 15f;

    private GoogleMap mMap;
    private ActivityMapBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng selectedLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupMap();
        setupClickListeners();
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupClickListeners() {
        binding.buttonLiveLocation.setOnClickListener(v -> checkLocationPermissionAndGetLocation());
        binding.buttonMapType.setOnClickListener(v -> cycleMapType());
        
        binding.buttonConfirmLocation.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                returnLocationResult(selectedLatLng);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        configureMapSettings();
        setupMapInteractions();
        checkLocationPermissionAndGetLocation();
    }

    private void configureMapSettings() {
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    private void setupMapInteractions() {
        mMap.setOnMapClickListener(latLng -> {
            selectedLatLng = latLng;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location"));
            
            // Show confirmation button once a location is picked
            binding.buttonConfirmLocation.setVisibility(View.VISIBLE);
        });
    }

    private void returnLocationResult(LatLng latLng) {
        Intent resultIntent = new Intent();
        // Return format: "latitude,longitude"
        String locationString = latLng.latitude + "," + latLng.longitude;
        resultIntent.putExtra("location", locationString);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void checkLocationPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocationOnMap();
            fetchLastKnownLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocationOnMap() {
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    @SuppressLint("MissingPermission")
    private void fetchLastKnownLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM));
                
                // Automatically set current location as selected until user clicks elsewhere
                selectedLatLng = currentLatLng;
                binding.buttonConfirmLocation.setVisibility(View.VISIBLE);
            }
        });
    }

    private void cycleMapType() {
        if (mMap == null) return;
        int currentType = mMap.getMapType();
        mMap.setMapType(currentType == GoogleMap.MAP_TYPE_NORMAL ? GoogleMap.MAP_TYPE_HYBRID : GoogleMap.MAP_TYPE_NORMAL);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationPermissionAndGetLocation();
        }
    }
}
