package lk.macna.nawwa_mc.ui.my_orders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import lk.macna.nawwa_mc.MapActivity;
import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.databinding.FragmentAddressFormBinding;
import lk.macna.nawwa_mc.network.ApiConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * AddressFormDialogFragment provides a pop-up interface for users to enter 
 * or update their delivery address and location for a specific order.
 */
public class AddressFormDialogFragment extends DialogFragment {

    private static final String TAG = "AddressFormDialog";
    private static final String PREFS_NAME = "MyPrefs";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get(ApiConfig.JSON_MEDIA_TYPE);
    private static final int REQUEST_CODE_MAP = 1001;

    private FragmentAddressFormBinding binding;
    private String orderId;
    private final OkHttpClient httpClient = new OkHttpClient();

    public AddressFormDialogFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddressFormBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        extractArguments();
        loadSavedAddress();
        setupActionButtons();
    }

    private void extractArguments() {
        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
        }
    }

    private void setupActionButtons() {
        binding.buttonSaveAddress.setOnClickListener(v -> handleAddressSubmission());
        binding.buttonOpenMap.setOnClickListener(v -> launchMapPicker());
    }

    /**
     * Loads default address details from SharedPreferences if available.
     */
    private void loadSavedAddress() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        binding.editTextAddressLine1.setText(prefs.getString("addressLine1", ""));
        binding.editTextAddressLine2.setText(prefs.getString("addressLine2", ""));
        binding.editTextCity.setText(prefs.getString("city", ""));
        binding.editTextPhone.setText(prefs.getString("phone", ""));
        binding.editTextLocation.setText(prefs.getString("location", ""));
    }

    private void handleAddressSubmission() {
        String addr1 = binding.editTextAddressLine1.getText().toString().trim();
        String addr2 = binding.editTextAddressLine2.getText().toString().trim();
        String city = binding.editTextCity.getText().toString().trim();
        String phone = binding.editTextPhone.getText().toString().trim();
        String loc = binding.editTextLocation.getText().toString().trim();

        if (addr1.isEmpty() || city.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getContext(), "Address, City and Phone are required", Toast.LENGTH_SHORT).show();
            return;
        }

        submitAddressToServer(addr1, addr2, city, phone, loc);
    }

    private void submitAddressToServer(String addr1, String addr2, String city, String phone, String loc) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = prefs.getString("apiKey", "");

        if (apiKey.isEmpty() || orderId == null) {
            Log.e(TAG, "Cannot submit: API Key or OrderID missing");
            return;
        }

        try {
            JSONObject json = createAddressJson(addr1, addr2, city, phone, loc);
            RequestBody body = RequestBody.create(json.toString(), JSON_MEDIA_TYPE);
            
            Request request = new Request.Builder()
                    .url(ApiConfig.ORDERS_URL + "/" + orderId)
                    .patch(body)
                    .addHeader("Authorization", "users API-Key " + apiKey)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Address update network error", e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        saveAddressToPreferences(addr1, addr2, city, phone, loc);
                        onUpdateSuccessful();
                    } else {
                        Log.e(TAG, "Server error during address update: " + response.code());
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "JSON serialization failed", e);
        }
    }

    private JSONObject createAddressJson(String a1, String a2, String c, String p, String l) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("addressLine1", a1);
        json.put("addressLine2", a2);
        json.put("city", c);
        json.put("phone", p);
        json.put("location", l);
        return json;
    }

    private void saveAddressToPreferences(String a1, String a2, String c, String p, String l) {
        requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString("addressLine1", a1)
                .putString("addressLine2", a2)
                .putString("city", c)
                .putString("phone", p)
                .putString("location", l)
                .apply();
    }

    private void onUpdateSuccessful() {
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Delivery details updated", Toast.LENGTH_SHORT).show();
                dismiss();
            });
        }
    }

    private void launchMapPicker() {
        Intent intent = new Intent(requireActivity(), MapActivity.class);
        startActivityForResult(intent, REQUEST_CODE_MAP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MAP && resultCode == Activity.RESULT_OK && data != null) {
            String location = data.getStringExtra("location");
            binding.editTextLocation.setText(location);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}