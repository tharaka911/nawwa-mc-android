package lk.macna.nawwa_mc.ui.product;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lk.macna.nawwa_mc.databinding.FragmentProductBinding;
import lk.macna.nawwa_mc.model.Product;
import lk.macna.nawwa_mc.network.ApiConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * ProductFragment handles the display of product listings, fetching data from
 * a remote API and presenting it in a list format.
 */
public class ProductFragment extends Fragment {

    private static final String TAG = "ProductFragment";
    private static final String PREFS_NAME = "MyPrefs";

    private FragmentProductBinding binding;
    private ProductAdapter productAdapter;
    private final List<Product> productList = new ArrayList<>();
    private final OkHttpClient httpClient = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupProductList();
        fetchProducts();
    }

    private void setupProductList() {
        productAdapter = new ProductAdapter(productList, requireContext());
        binding.recyclerViewProduct.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewProduct.setAdapter(productAdapter);
    }

    /**
     * Initiates an asynchronous request to fetch product data.
     */
    private void fetchProducts() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = prefs.getString("apiKey", "");

        if (apiKey.isEmpty()) {
            Log.e(TAG, "Missing API Key in SharedPreferences");
            return;
        }

        Request request = new Request.Builder()
                .url(ApiConfig.PRODUCTS_URL)
                .addHeader("Authorization", "users API-Key " + apiKey)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Product fetch failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Unexpected response code: " + response.code());
                    return;
                }

                try {
                    String jsonResponse = response.body().string();
                    parseProductJson(jsonResponse);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse product JSON", e);
                }
            }
        });
    }

    private void parseProductJson(String json) throws JSONException {
        JSONObject rootObj = new JSONObject(json);
        JSONArray docsArray = rootObj.getJSONArray("docs");

        List<Product> tempProducts = new ArrayList<>();
        for (int i = 0; i < docsArray.length(); i++) {
            JSONObject prodJson = docsArray.getJSONObject(i);
            Product product = new Product();
            
            product.setId(prodJson.getString("id"));
            product.setName(prodJson.getString("name"));
            product.setCategory(prodJson.getJSONObject("category").getString("name"));
            product.setPrice(prodJson.getDouble("price"));
            product.setImageUrl(ApiConfig.BASE_URL + prodJson.getJSONObject("image").getString("url"));
            
            tempProducts.add(product);
        }

        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                productList.clear();
                productList.addAll(tempProducts);
                productAdapter.notifyDataSetChanged();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}