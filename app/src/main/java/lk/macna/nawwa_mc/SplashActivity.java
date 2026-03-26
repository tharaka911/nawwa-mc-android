package lk.macna.nawwa_mc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import lk.macna.nawwa_mc.databinding.ActivitySplashBinding;

/**
 * SplashActivity handles the initial application startup, displays a logo animation,
 * and determines the initial destination based on user authentication status.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 1000;
    private static final int ANIMATION_DURATION_MS = 700;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String TOKEN_KEY = "token";

    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        startLogoAnimation();
        navigateToNextScreenDelayed();
    }

    private void startLogoAnimation() {
        binding.splashLogo.setAlpha(0f);
        binding.splashLogo.animate()
                .alpha(1f)
                .setDuration(ANIMATION_DURATION_MS)
                .start();
    }

    private void navigateToNextScreenDelayed() {
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAuthAndNavigate, SPLASH_DELAY_MS);
    }

    private void checkAuthAndNavigate() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String token = prefs.getString(TOKEN_KEY, null);

        Intent intent;
        if (token != null) {
            // User is authenticated
            intent = new Intent(this, HomeActivity.class);
        } else {
            // User needs to authenticate or verify
            intent = new Intent(this, VerificationActivity.class);
        }

        startActivity(intent);
        finish();
    }
}