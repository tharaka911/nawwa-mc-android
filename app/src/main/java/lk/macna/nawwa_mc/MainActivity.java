package lk.macna.nawwa_mc;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import lk.macna.nawwa_mc.databinding.ActivityMainBinding;
import lk.macna.nawwa_mc.network.ApiConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * MainActivity handles user registration.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get(ApiConfig.JSON_MEDIA_TYPE);

    private ActivityMainBinding binding;
    private final OkHttpClient httpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
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
        binding.buttonRegister.setOnClickListener(v -> {
            if (validateInputs()) {
                performRegistration();
            }
        });

        binding.buttonGoToLogin.setOnClickListener(v -> navigateToLogin());
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String name = binding.RegName.getText().toString().trim();
        String email = binding.RegMail.getText().toString().trim();
        String phone = binding.Regphone.getText().toString().trim();
        String password = binding.RegPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.RegName.setError("Name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            binding.RegMail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.RegMail.setError("Invalid email format");
            isValid = false;
        }

        if (TextUtils.isEmpty(phone)) {
            binding.Regphone.setError("Phone is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            binding.RegPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            binding.RegPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        return isValid;
    }

    private void performRegistration() {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("name", binding.RegName.getText().toString().trim());
            jsonBody.put("email", binding.RegMail.getText().toString().trim());
            jsonBody.put("phone", binding.Regphone.getText().toString().trim());
            jsonBody.put("password", binding.RegPassword.getText().toString().trim());

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(ApiConfig.REGISTER_URL)
                    .post(body)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Registration failed", e);
                    showToastOnMainThread("Registration failed. Check connection.");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        showToastOnMainThread("Registration successful!");
                        navigateToLogin();
                    } else {
                        handleErrorResponse(response);
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "JSON error", e);
        }
    }

    private void handleErrorResponse(Response response) throws IOException {
        String responseData = response.body() != null ? response.body().string() : "";
        Log.e(TAG, "Server error: " + responseData);
        
        try {
            JSONObject json = new JSONObject(responseData);
            JSONArray errors = json.getJSONArray("errors");
            String message = errors.getJSONObject(0)
                    .getJSONObject("data")
                    .getJSONArray("errors")
                    .getJSONObject(0)
                    .getString("message");
            showToastOnMainThread(message);
        } catch (Exception e) {
            showToastOnMainThread("Registration failed. Please try again.");
        }
    }

    private void navigateToLogin() {
        runOnUiThread(() -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void showToastOnMainThread(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}