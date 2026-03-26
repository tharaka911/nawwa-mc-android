package lk.macna.nawwa_mc;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import lk.macna.nawwa_mc.databinding.ActivityForgotPasswordBinding;
import lk.macna.nawwa_mc.network.ApiConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ForgotPasswordActivity allows users to request a password reset link 
 * via their registered email address.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get(ApiConfig.JSON_MEDIA_TYPE);

    private ActivityForgotPasswordBinding binding;
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupEdgeToEdge();
        setupClickListeners();
    }

    private void setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupClickListeners() {
        binding.button.setOnClickListener(v -> handleResetRequest());
    }

    private void handleResetRequest() {
        String email = binding.email.getText().toString().trim();

        if (email.isEmpty()) {
            binding.email.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.email.setError("Please enter a valid email");
            return;
        }

        sendResetPasswordRequest(email);
    }

    private void sendResetPasswordRequest(String email) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(ApiConfig.FORGOT_PASSWORD_URL)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Request failed: " + e.getMessage());
                    showToastOnMainThread("Failed to connect to server");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        showToastOnMainThread("Password reset link sent to your email");
                        finishOnMainThread();
                    } else {
                        Log.e(TAG, "Server error: " + response.code());
                        showToastOnMainThread("User not found or server error");
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "JSON Build error", e);
        }
    }

    private void showToastOnMainThread(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private void finishOnMainThread() {
        runOnUiThread(this::finish);
    }
}