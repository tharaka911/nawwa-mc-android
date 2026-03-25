package lk.macna.nawwa_mc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivityLog";
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonGoToRegister;
    private Button buttonGoToForgotPassword;

    private OkHttpClient client = new OkHttpClient();
    private static final String LOGIN_URL = "https://ecom-api.macna.app/api/users/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        
        // Find the root view (from NestedScrollView or the ConstraintLayout inside)
        // Since NestedScrollView is the root in the new layout, we use that ID if it had one, 
        // or just apply it to the content view.
        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Simplified: find the inputs directly by their unique IDs
        editTextEmail = findViewById(R.id.Email);
        editTextPassword = findViewById(R.id.Password);
        
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonGoToRegister = findViewById(R.id.buttonGoToRegister);
        buttonGoToForgotPassword = findViewById(R.id.buttonGoToForgotPassword);

        buttonLogin.setOnClickListener(v -> login());
        buttonGoToRegister.setOnClickListener(v -> goToRegister());
        buttonGoToForgotPassword.setOnClickListener(v -> goToForgotPassword());
    }

    private void login() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "login: Email or password is empty");
            return;
        }

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", email);
            jsonObject.put("password", password);

            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(LOGIN_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Log.d(TAG, "login: Sending login request");

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "onFailure: Login request failed", e);
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d(TAG, "onResponse: Received login response");
                    if (response.isSuccessful()) {
                        String responseData = response.body() != null ? response.body().string() : null;
                        Log.d(TAG, "onResponse: Response data: " + responseData);
                        try {
                            JSONObject json = new JSONObject(responseData);
                            if (json.has("message")) {
                                String message = json.getString("message");
                                if (message.equals("Authentication Passed")) {
                                    Log.d(TAG, "onResponse: Login successful");

                                    String exp = json.optString("exp");
                                    String token = json.optString("token");
                                    JSONObject userObject = json.optJSONObject("user");
                                    if (userObject != null) {
                                        String apiKey = userObject.optString("apiKey");
                                        String userId = userObject.optString("id");
                                        String phone = userObject.optString("phone");
                                        String name = userObject.optString("name");
                                        String email = userObject.optString("email");
                                        String profileImageUrl = userObject.optJSONObject("profileImage") != null
                                                ? userObject.optJSONObject("profileImage").optString("url")
                                                : "";

                                        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("exp", exp);
                                        editor.putString("token", token);
                                        editor.putString("apiKey", apiKey);
                                        editor.putString("userId", userId);
                                        editor.putString("phone", phone);
                                        editor.putString("name", name);
                                        editor.putString("email", email);
                                        editor.putString("profileImageUrl", profileImageUrl);
                                        editor.apply();

                                        Log.d(TAG, "onResponse: User details stored in SharedPreferences");
                                    }

                                    runOnUiThread(() -> {
                                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    });
                                } else {
                                    Log.d(TAG, "onResponse: Login failed, invalid email or password");
                                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show());
                                }
                            } else {
                                Log.e(TAG, "onResponse: JSON response does not contain 'message' key");
                                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show());
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "onResponse: JSON parsing error", e);
                            runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show());
                        }
                    } else {
                        Log.d(TAG, "onResponse: Login failed, response not successful");
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (JSONException e) {
            Log.d(TAG, "login: JSON creation error", e);
        }
    }

    private void goToRegister() {
        Log.d(TAG, "goToRegister: Navigating to register activity");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToForgotPassword() {
        Log.d(TAG, "goToForgotPassword: Navigating to forgot_password activity");
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }
}