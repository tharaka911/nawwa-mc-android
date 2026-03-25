package lk.macna.nawwa_mc.ui.my_orders;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import lk.macna.nawwa_mc.MapActivity;
import lk.macna.nawwa_mc.R;
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

public class AddressFormDialogFragment extends DialogFragment {

    private static final String TAG = "AddressFormDialogLog";
    private static final String PREFS_NAME = "MyPrefs";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final int REQUEST_CODE_MAP = 1001;

    private EditText editTextAddressLine1;
    private EditText editTextAddressLine2;
    private EditText editTextCity;
    private EditText editTextPhone;
    private EditText editTextLocation;
    private String orderId;

    public AddressFormDialogFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_address_form, container, false);

        editTextAddressLine1 = view.findViewById(R.id.editTextAddressLine1);
        editTextAddressLine2 = view.findViewById(R.id.editTextAddressLine2);
        editTextCity = view.findViewById(R.id.editTextCity);
        editTextPhone = view.findViewById(R.id.editTextPhone);
        editTextLocation = view.findViewById(R.id.editTextLocation);
        Button buttonSaveAddress = view.findViewById(R.id.buttonSaveAddress);
        Button buttonOpenMap = view.findViewById(R.id.buttonOpenMap);

        if (getArguments() != null) {
            orderId = getArguments().getString("orderId");
        }

        // Load data from SharedPreferences
        loadAddressFromPreferences();

        buttonSaveAddress.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Address Saved", Toast.LENGTH_SHORT).show();
            saveAddress();
        });

        buttonOpenMap.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Select the Location", Toast.LENGTH_SHORT).show();
            openMap();
        });

        return view;
    }

    private void loadAddressFromPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String addressLine1 = sharedPreferences.getString("addressLine1", "");
        String addressLine2 = sharedPreferences.getString("addressLine2", "");
        String city = sharedPreferences.getString("city", "");
        String phone = sharedPreferences.getString("phone", "");
        String location = sharedPreferences.getString("location", "");

        editTextAddressLine1.setText(addressLine1);
        editTextAddressLine2.setText(addressLine2);
        editTextCity.setText(city);
        editTextPhone.setText(phone);
        editTextLocation.setText(location);
    }

    private void saveAddress() {
        String addressLine1 = editTextAddressLine1.getText().toString();
        String addressLine2 = editTextAddressLine2.getText().toString();
        String city = editTextCity.getText().toString();
        String phone = editTextPhone.getText().toString();
        String location = editTextLocation.getText().toString();

        OkHttpClient client = new OkHttpClient();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = sharedPreferences.getString("apiKey", "");

        if (apiKey.isEmpty()) {
            Log.e(TAG, "API key is missing");
            return;
        }

        JSONObject addressJson = new JSONObject();
        try {
            addressJson.put("addressLine1", addressLine1);
            addressJson.put("addressLine2", addressLine2);
            addressJson.put("city", city);
            addressJson.put("phone", phone);
            addressJson.put("location", location);

            RequestBody body = RequestBody.create(addressJson.toString(), JSON);
            Request request = new Request.Builder()
                    .url("https://ecom-api.macna.app/api/orders/" + orderId)
                    .patch(body)
                    .addHeader("Authorization", "users API-Key " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Error updating address", e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Address updated successfully");
                        getActivity().runOnUiThread(() -> {
                            saveAddressToPreferences(addressLine1, addressLine2, city, phone, location);
                            dismiss();
                        });
                    } else {
                        Log.e(TAG, "Unsuccessful response: " + response.code());
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON object for address", e);
        }
    }

    private void saveAddressToPreferences(String addressLine1, String addressLine2, String city, String phone, String location) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("addressLine1", addressLine1);
        editor.putString("addressLine2", addressLine2);
        editor.putString("city", city);
        editor.putString("phone", phone);
        editor.putString("location", location);
        editor.apply();
    }

    private void openMap() {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        startActivityForResult(intent, REQUEST_CODE_MAP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_MAP && resultCode == getActivity().RESULT_OK) {
            String location = data.getStringExtra("location");
            editTextLocation.setText(location);
        }
    }
}