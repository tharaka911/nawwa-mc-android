package lk.macna.nawwa_mc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import lk.macna.nawwa_mc.databinding.ActivityVerificationBinding;

/**
 * VerificationActivity implements a simple gesture-based human verification system
 * using the device's accelerometer to detect phone flips.
 */
public class VerificationActivity extends AppCompatActivity {

    private static final String TAG = "VerificationActivity";
    private static final float FLIP_THRESHOLD = 9.0f;
    private static final int REQUIRED_FLIPS = 4; // 2 full cycles (up/down x 2)
    private static final int DETECTION_COOLDOWN_MS = 500;
    private static final String BRAND_URL = "https://ecom-api.macna.app";

    private ActivityVerificationBinding binding;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    private int flipCount = 0;
    private boolean isUpsideDown = false;
    private boolean isVerificationActive = false;
    private boolean isCooldownActive = false;
    private boolean isDialogShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupEdgeToEdge();
        initializeSensors();
        setupClickListeners();
    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeSensors() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
    }

    private void setupClickListeners() {
        binding.startVerificationButton.setOnClickListener(v -> startFlipVerification());
        binding.helpButton.setOnClickListener(v -> openBrandWebsite());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isVerificationActive) {
            registerSensorListener();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterSensorListener();
    }

    @SuppressLint("SetTextI18n")
    private void startFlipVerification() {
        flipCount = 0;
        isVerificationActive = true;
        isUpsideDown = false;
        isCooldownActive = false;
        
        binding.verificationText.setText("Please flip your phone face down and up twice.");
        Toast.makeText(this, "Verification Mode Active", Toast.LENGTH_SHORT).show();
        
        registerSensorListener();
    }

    private void registerSensorListener() {
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void unregisterSensorListener() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    private void openBrandWebsite() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(BRAND_URL));
        startActivity(intent);
    }

    private void showSuccessDialog() {
        if (isDialogShowing) return;
        isDialogShowing = true;

        new AlertDialog.Builder(this)
                .setTitle("Verification Successful")
                .setMessage("You've been identified as human. Welcome!")
                .setCancelable(false)
                .setPositiveButton("Proceed", (dialog, which) -> {
                    navigateToRegister();
                })
                .show();
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!isVerificationActive || isCooldownActive) return;

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                processAccelerometerData(event.values[2]);
            }
        }

        private void processAccelerometerData(float zAxis) {
            // Detect flip to upside down
            if (!isUpsideDown && zAxis < -FLIP_THRESHOLD) {
                handleDetectedFlip(true);
            } 
            // Detect flip back to normal
            else if (isUpsideDown && zAxis > FLIP_THRESHOLD) {
                handleDetectedFlip(false);
            }
        }

        private void handleDetectedFlip(boolean currentlyUpsideDown) {
            isUpsideDown = currentlyUpsideDown;
            flipCount++;
            isCooldownActive = true;
            
            updateUI();
            
            if (flipCount >= REQUIRED_FLIPS) {
                completeVerification();
            } else {
                // Prevent jitter by adding a small cooldown
                new Handler(Looper.getMainLooper()).postDelayed(() -> isCooldownActive = false, DETECTION_COOLDOWN_MS);
            }
        }

        @SuppressLint({"DefaultLocale", "SetTextI18n"})
        private void updateUI() {
            int remaining = REQUIRED_FLIPS - flipCount;
            if (remaining > 0) {
                binding.verificationText.setText(String.format("Keep going! %d movements left.", remaining));
            } else {
                binding.verificationText.setText("Verification Complete!");
            }
        }

        private void completeVerification() {
            isVerificationActive = false;
            unregisterSensorListener();
            showSuccessDialog();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
}