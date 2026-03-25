package lk.macna.nawwa_mc.ui.product;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.databinding.FragmentProductBinding;
import lk.macna.nawwa_mc.model.Product;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProductFragment extends Fragment {

    private FragmentProductBinding binding;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();

    private static final String API_URL = "https://ecom-api.macna.app/api/products";
    private static final String BASE_URL = "https://ecom-api.macna.app";
    private static final String PREFS_NAME = "MyPrefs";
    private static final String TAG = "ProductFragmentLog";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentProductBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up RecyclerView
        recyclerView = root.findViewById(R.id.recyclerViewProduct);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up adapter
        productAdapter = new ProductAdapter(productList, getContext());
        recyclerView.setAdapter(productAdapter);

        // Fetch data from API
        fetchProducts();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void fetchProducts() {
        OkHttpClient client = new OkHttpClient();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = sharedPreferences.getString("apiKey", "");

        if (apiKey.isEmpty()) {
            Log.e(TAG, "API key is missing");
            return;
        }

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "users API-Key " + apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error fetching products", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray productsJsonArray = jsonResponse.getJSONArray("docs");  // Get products from "docs" array
                        productList.clear();  // Clear any existing data

                        // Parse products
                        for (int i = 0; i < productsJsonArray.length(); i++) {
                            JSONObject productJson = productsJsonArray.getJSONObject(i);
                            Product product = new Product();
                            product.setId(productJson.getString("id"));
                            product.setName(productJson.getString("name"));
                            product.setCategory(productJson.getJSONObject("category").getString("name"));
                            product.setPrice(productJson.getDouble("price"));
                            product.setImageUrl(BASE_URL + productJson.getJSONObject("image").getString("url"));
                            productList.add(product);
                        }

                        // Notify adapter on the main thread
                        getActivity().runOnUiThread(() -> productAdapter.notifyDataSetChanged());

                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing products", e);
                    }
                } else {
                    Log.e(TAG, "Unsuccessful response: " + response.code());
                }
            }
        });
    }
}