package lk.macna.nawwa_mc;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class VerificationActivity extends AppCompatActivity {

    private static final String TAG = "VerificationActivityLog";

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private TextView verificationText;

    private boolean isHumanVerified = false;
    private boolean isDialogShowing = false; // Flag to prevent multiple dialogs
    private static final float FLIP_THRESHOLD = 9;  
    private static final int MIN_FLIP_COUNT = 2; 
    private int flipCount = 0;
    private boolean isUpsideDown = false;
    private boolean verificationStarted = false;
    private boolean flipDetected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verification);

        verificationText = findViewById(R.id.verificationText);
        Button startVerificationButton = findViewById(R.id.startVerificationButton);
        Button helpButton = findViewById(R.id.helpButton);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        startVerificationButton.setOnClickListener(v -> startVerification());

        helpButton.setOnClickListener(v -> {
            String url = "https://macna.app"; // Updated to your brand URL
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && verificationStarted && !isHumanVerified) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    private void startVerification() {
        flipCount = 0;
        isHumanVerified = false;
        isDialogShowing = false;
        verificationStarted = true;
        isUpsideDown = false;  
        flipDetected = false;   
        verificationText.setText("Verification started. Please flip your phone 2 times.");
        Toast.makeText(this, "Verification started", Toast.LENGTH_SHORT).show();

        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void showAlertDialog() {
        if (isDialogShowing) return;
        isDialogShowing = true;

        new AlertDialog.Builder(this)
                .setTitle("Verification Successful")
                .setMessage("System identified you're human.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = new Intent(VerificationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (isHumanVerified) return;

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float z = event.values[2];

                if (!flipDetected) {
                    if (!isUpsideDown && z < -FLIP_THRESHOLD) {
                        isUpsideDown = true;
                        flipCount++;
                        flipDetected = true;
                        updateStatus();
                    } else if (isUpsideDown && z > FLIP_THRESHOLD) {
                        isUpsideDown = false;
                        flipCount++;
                        flipDetected = true;
                        updateStatus();
                    }
                }

                if (flipDetected) {
                    new android.os.Handler().postDelayed(() -> flipDetected = false, 500);
                }

                if (flipCount >= MIN_FLIP_COUNT * 2) {
                    isHumanVerified = true;
                    if (sensorManager != null) {
                        sensorManager.unregisterListener(sensorEventListener);
                    }
                    showAlertDialog();
                }
            }
        }

        private void updateStatus() {
            runOnUiThread(() -> {
                int remaining = (MIN_FLIP_COUNT * 2) - flipCount;
                if (remaining > 0) {
                    verificationText.setText("Keep going! " + remaining + " more flips needed.");
                } else {
                    verificationText.setText("Verification Complete!");
                }
            });
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
}