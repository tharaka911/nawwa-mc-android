package lk.macna.nawwa_mc.ui.my_profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;

import lk.macna.nawwa_mc.databinding.FragmentMyProfileBinding;
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

public class MyProfileFragment extends Fragment {

    private static final String TAG = "MyProfileFragmentLog";
    private static final String PREFS_NAME = "MyPrefs";
    private static final String UPDATE_PROFILE_URL = "https://ecom-api.macna.app/api/users/";
    private FragmentMyProfileBinding binding;
    private EditText editTextName, editTextUsername, editTextEmail, editTextAddressLine1, editTextAddressLine2, editTextCity, editTextPhone;
    private ImageView imageViewProfile;
    private Button buttonUpdateProfile, buttonOpenLink;
    private String userId, apiKey;
    private OkHttpClient client = new OkHttpClient();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MyProfileViewModel myProfileViewModel =
                new ViewModelProvider(this).get(MyProfileViewModel.class);

        binding = FragmentMyProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize views
        editTextName = binding.editTextName;
        editTextEmail = binding.editTextEmail;
        editTextAddressLine1 = binding.editTextAddressLine1;
        editTextAddressLine2 = binding.editTextAddressLine2;
        editTextCity = binding.editTextCity;
        editTextPhone = binding.editTextPhone;
        imageViewProfile = binding.imageViewProfile;
        buttonUpdateProfile = binding.buttonUpdateProfile;
        buttonOpenLink = binding.buttonOpenLink;

        // Load user data from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId", "");
        apiKey = sharedPreferences.getString("apiKey", "");
        String name = sharedPreferences.getString("name", "");
        String email = sharedPreferences.getString("email", "");
        String addressLine1 = sharedPreferences.getString("addressLine1", "");
        String addressLine2 = sharedPreferences.getString("addressLine2", "");
        String city = sharedPreferences.getString("city", "");
        String phone = sharedPreferences.getString("phone", "");
        String profileImageUrl = sharedPreferences.getString("profileImageUrl", "");

        // Set user data to views
        editTextName.setText(name);
        editTextEmail.setText(email);
        editTextAddressLine1.setText(addressLine1);
        editTextAddressLine2.setText(addressLine2);
        editTextCity.setText(city);
        editTextPhone.setText(phone);
        Glide.with(this).load("https://ecom-api.macna.app" + profileImageUrl).into(imageViewProfile);

        buttonUpdateProfile.setOnClickListener(v -> updateProfile());
        buttonOpenLink.setOnClickListener(v -> openLink());

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void updateProfile() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String addressLine1 = editTextAddressLine1.getText().toString().trim();
        String addressLine2 = editTextAddressLine2.getText().toString().trim();
        String city = editTextCity.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", name);
            jsonObject.put("email", email);
            jsonObject.put("addressLine1", addressLine1);
            jsonObject.put("addressLine2", addressLine2);
            jsonObject.put("city", city);
            jsonObject.put("phone", phone);

            RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(UPDATE_PROFILE_URL + "/" + userId)
                    .patch(body)
                    .addHeader("Authorization", "users API-Key " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Error updating profile", e);
                    getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Profile update failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Profile updated successfully");
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            // Update SharedPreferences
                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("name", name);
                            editor.putString("email", email);
                            editor.putString("addressLine1", addressLine1);
                            editor.putString("addressLine2", addressLine2);
                            editor.putString("city", city);
                            editor.putString("phone", phone);
                            editor.apply();

                            // Show logout dialog
                            showLogoutDialog();
                        });
                    } else {
                        Log.e(TAG, "Unsuccessful response: " + response.code());
                        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), "Profile update failed", Toast.LENGTH_SHORT).show());
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON object for profile update", e);
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Profile Updated")
                .setMessage("Please log out and log in again to see the updates.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    // User pressed OK
                    dialog.dismiss();
                })
                .show();
    }

    private void openLink() {
        String url = "https://ecom-api.macna.app/admin/collections/users";
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
    }
}