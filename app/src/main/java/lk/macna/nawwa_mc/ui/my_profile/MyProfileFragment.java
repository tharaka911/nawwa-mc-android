package lk.macna.nawwa_mc.ui.my_profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.databinding.FragmentMyProfileBinding;
import lk.macna.nawwa_mc.network.ApiConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * MyProfileFragment allows users to view and update their personal profile information,
 * including name, email, address, and contact details.
 */
public class MyProfileFragment extends Fragment {

    private static final String TAG = "MyProfileFragment";
    private static final String PREFS_NAME = "MyPrefs";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get(ApiConfig.JSON_MEDIA_TYPE);

    private FragmentMyProfileBinding binding;
    private final OkHttpClient httpClient = new OkHttpClient();
    private String userId, apiKey;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        loadUserProfileData();
        setupClickListeners();
    }

    /**
     * Loads user information from SharedPreferences and populates the UI.
     */
    private void loadUserProfileData() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        userId = sharedPreferences.getString("userId", "");
        apiKey = sharedPreferences.getString("apiKey", "");

        binding.editTextName.setText(sharedPreferences.getString("name", ""));
        binding.editTextEmail.setText(sharedPreferences.getString("email", ""));
        binding.editTextAddressLine1.setText(sharedPreferences.getString("addressLine1", ""));
        binding.editTextAddressLine2.setText(sharedPreferences.getString("addressLine2", ""));
        binding.editTextCity.setText(sharedPreferences.getString("city", ""));
        binding.editTextPhone.setText(sharedPreferences.getString("phone", ""));

        String profileImageUrl = sharedPreferences.getString("profileImageUrl", "");
        if (!profileImageUrl.isEmpty()) {
            Glide.with(this)
                    .load(ApiConfig.BASE_URL + profileImageUrl)
                    .placeholder(R.mipmap.ic_launcher_round)
                    .into(binding.imageViewProfile);
        }
    }

    private void setupClickListeners() {
        binding.buttonUpdateProfile.setOnClickListener(v -> handleProfileUpdate());
        binding.buttonOpenLink.setOnClickListener(v -> openAdminLink());
    }

    /**
     * Validates and submits updated profile data to the server.
     */
    private void handleProfileUpdate() {
        String name = binding.editTextName.getText().toString().trim();
        String email = binding.editTextEmail.getText().toString().trim();
        String addressLine1 = binding.editTextAddressLine1.getText().toString().trim();
        String addressLine2 = binding.editTextAddressLine2.getText().toString().trim();
        String city = binding.editTextCity.getText().toString().trim();
        String phone = binding.editTextPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(getContext(), "Name and Email are required", Toast.LENGTH_SHORT).show();
            return;
        }

        updateProfileOnServer(name, email, addressLine1, addressLine2, city, phone);
    }

    private void updateProfileOnServer(String name, String email, String addr1, String addr2, String city, String phone) {
        try {
            JSONObject profileJson = new JSONObject();
            profileJson.put("name", name);
            profileJson.put("email", email);
            profileJson.put("addressLine1", addr1);
            profileJson.put("addressLine2", addr2);
            profileJson.put("city", city);
            profileJson.put("phone", phone);

            RequestBody body = RequestBody.create(profileJson.toString(), JSON_MEDIA_TYPE);
            Request request = new Request.Builder()
                    .url(ApiConfig.PROFILE_URL + userId)
                    .patch(body)
                    .addHeader("Authorization", "users API-Key " + apiKey)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Profile update network failure", e);
                    showToastOnMain("Update failed. Check your connection.");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        saveUpdatedDataLocally(name, email, addr1, addr2, city, phone);
                        showSuccessUI();
                    } else {
                        Log.e(TAG, "Profile update server error: " + response.code());
                        showToastOnMain("Update failed. Server error.");
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "JSON Build error", e);
        }
    }

    private void saveUpdatedDataLocally(String name, String email, String addr1, String addr2, String city, String phone) {
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("addressLine1", addr1);
        editor.putString("addressLine2", addr2);
        editor.putString("city", city);
        editor.putString("phone", phone);
        editor.apply();
    }

    private void showSuccessUI() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Profile Updated")
                        .setMessage("Your profile has been successfully updated. Please relogin to see full changes if necessary.")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            });
        }
    }

    private void openAdminLink() {
        String url = ApiConfig.BASE_URL + "/admin/collections/users";
        new CustomTabsIntent.Builder().build().launchUrl(requireContext(), Uri.parse(url));
    }

    private void showToastOnMain(String message) {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> 
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}