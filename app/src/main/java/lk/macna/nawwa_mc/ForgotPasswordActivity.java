package lk.macna.nawwa_mc;

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

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivityLog";
    private static final String RESET_PASSWORD_URL = "https://ecom-api.macna.app/api/users/forgot-password";

    private EditText emailEditText;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailEditText = findViewById(R.id.email);
        Button resetPasswordButton = findViewById(R.id.button);

        client = new OkHttpClient();

        resetPasswordButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(ForgotPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                sendResetPasswordRequest(email);
            }
        });
    }

    private void sendResetPasswordRequest(String email) {
        try {
            JSONObject json = new JSONObject();
            json.put("email", email);

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(RESET_PASSWORD_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Failed to send request", e);
                    runOnUiThread(() -> Toast.makeText(ForgotPasswordActivity.this, "Failed to send request", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(ForgotPasswordActivity.this, "Password reset link sent to your email", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(ForgotPasswordActivity.this, "Failed to send reset link. Please try again.", Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to build request", e);
            Toast.makeText(this, "Failed to build request", Toast.LENGTH_SHORT).show();
        }
    }
}