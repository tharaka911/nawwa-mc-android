package lk.macna.nawwa_mc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 1000; // Duration of wait

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Start the animation
        ImageView logo = findViewById(R.id.splash_logo);
        logo.setAlpha(0f);
        logo.animate().alpha(1f).setDuration(700);

        new Handler().postDelayed(() -> {
            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            String token = sharedPreferences.getString("token", null);

            if (token != null) {
                // User is logged in, navigate to HomeActivity
                Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                startActivity(intent);
            } else {
                // User is not logged in, navigate to LoginActivity
                Intent intent = new Intent(SplashActivity.this, VerificationActivity.class);
                startActivity(intent);
            }

            finish();
        }, SPLASH_DISPLAY_LENGTH);
    }
}