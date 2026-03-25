package lk.macna.nawwa_mc;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
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

public class MainActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, phoneEditText, passwordEditText;
    private final OkHttpClient client = new OkHttpClient();
    private static final String REGISTER_URL = "https://ecom-api.macna.app/api/users";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Use the decor view to avoid null pointer if root ID is missing
        ViewCompat.setOnApplyWindowInsetsListener(getWindow().getDecorView(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Simplified: Find the inputs directly by their unique IDs
        nameEditText = findViewById(R.id.RegName);
        emailEditText = findViewById(R.id.RegMail);
        phoneEditText = findViewById(R.id.Regphone);
        passwordEditText = findViewById(R.id.RegPassword);
        
        Button registerButton = findViewById(R.id.buttonRegister);

        registerButton.setOnClickListener(v -> {
            if (validateInputFields()) {
                registerUser();
            }
        });

        findViewById(R.id.buttonGoToLogin).setOnClickListener(v -> goToLogin());
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean validateInputFields() {
        boolean isValid = true;

        if (TextUtils.isEmpty(nameEditText.getText())) {
            nameEditText.setError("Name is required");
            isValid = false;
        } else {
            nameEditText.setError(null);
        }

        if (TextUtils.isEmpty(emailEditText.getText())) {
            emailEditText.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText()).matches()) {
            emailEditText.setError("Invalid email format");
            isValid = false;
        } else {
            emailEditText.setError(null);
        }

        if (TextUtils.isEmpty(phoneEditText.getText())) {
            phoneEditText.setError("Phone is required");
            isValid = false;
        } else {
            phoneEditText.setError(null);
        }

        if (TextUtils.isEmpty(passwordEditText.getText())) {
            passwordEditText.setError("Password is required");
            isValid = false;
        } else if (passwordEditText.getText().length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            isValid = false;
        } else {
            passwordEditText.setError(null);
        }

        return isValid;
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("email", email);
            jsonObject.put("phone", phone);
            jsonObject.put("password", password);

            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(REGISTER_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Registration failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Registration successful", Toast.LENGTH_SHORT).show());
                    } else {
                        String responseData = response.body() != null ? response.body().string() : null;
                        try {
                            assert responseData != null;
                            JSONObject json = new JSONObject(responseData);
                            JSONArray errors = json.getJSONArray("errors");
                            JSONObject error = errors.getJSONObject(0);
                            JSONObject data = error.getJSONObject("data");
                            JSONArray fieldErrors = data.getJSONArray("errors");
                            String errorMessage = fieldErrors.getJSONObject(0).getString("message");
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show());
                        } catch (JSONException e) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Registration failed", Toast.LENGTH_SHORT).show());
                        }
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }
}