package lk.macna.nawwa_mc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import lk.macna.nawwa_mc.databinding.ActivityLoginBinding;
import lk.macna.nawwa_mc.network.ApiConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * LoginActivity handles user authentication.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "MyPrefs";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get(ApiConfig.JSON_MEDIA_TYPE);

    private ActivityLoginBinding binding;
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
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
        binding.buttonLogin.setOnClickListener(v -> handleLogin());
        binding.buttonGoToRegister.setOnClickListener(v -> navigateToRegister());
        binding.buttonGoToForgotPassword.setOnClickListener(v -> navigateToForgotPassword());
    }

    private void handleLogin() {
        String email = binding.Email.getText().toString().trim();
        String password = binding.Password.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        performLoginRequest(email, password);
    }

    private void performLoginRequest(String email, String password) {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", email);
            jsonBody.put("password", password);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(ApiConfig.LOGIN_URL)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Login request failed", e);
                    showToastOnMainThread("Login failed. Check your connection.");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        handleLoginResponse(response.body().string());
                    } else {
                        Log.e(TAG, "Login failed with code: " + response.code());
                        showToastOnMainThread("Invalid email or password");
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "JSON error", e);
        }
    }

    private void handleLoginResponse(String responseData) {
        try {
            JSONObject json = new JSONObject(responseData);
            String message = json.optString("message");

            if ("Authentication Passed".equals(message)) {
                saveUserData(json);
                navigateToHome();
            } else {
                showToastOnMainThread("Invalid credentials");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Parsing error", e);
            showToastOnMainThread("Login failed. Unexpected response.");
        }
    }

    private void saveUserData(JSONObject json) {
        JSONObject userObj = json.optJSONObject("user");
        if (userObj == null) return;

        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("exp", json.optString("exp"));
        editor.putString("token", json.optString("token"));
        editor.putString("apiKey", userObj.optString("apiKey"));
        editor.putString("userId", userObj.optString("id"));
        editor.putString("phone", userObj.optString("phone"));
        editor.putString("name", userObj.optString("name"));
        editor.putString("email", userObj.optString("email"));
        
        JSONObject profileImage = userObj.optJSONObject("profileImage");
        if (profileImage != null) {
            editor.putString("profileImageUrl", profileImage.optString("url"));
        }
        
        editor.apply();
    }

    private void navigateToHome() {
        runOnUiThread(() -> {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToForgotPassword() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    private void showToastOnMainThread(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}