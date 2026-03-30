package lk.macna.nawwa_mc.ui.cart;

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

import lk.macna.nawwa_mc.databinding.FragmentCartBinding;
import lk.macna.nawwa_mc.model.Cart;
import lk.macna.nawwa_mc.network.ApiConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * CartFragment displays the user's current pending shopping cart items.
 */
public class CartFragment extends Fragment {

    private static final String TAG = "CartFragment";
    private static final String PREFS_NAME = "MyPrefs";

    private FragmentCartBinding binding;
    private CartAdapter cartAdapter;
    private final List<Cart> cartList = new ArrayList<>();
    private final OkHttpClient httpClient = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupCartRecyclerView();
        fetchCartItemsFromApi();
    }

    private void setupCartRecyclerView() {
        cartAdapter = new CartAdapter(cartList);
        binding.RecyclerViewCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.RecyclerViewCart.setAdapter(cartAdapter);
    }

    /**
     * Fetches the user's cart from the remote API.
     */
    private void fetchCartItemsFromApi() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = prefs.getString("apiKey", "");

        if (apiKey.isEmpty()) {
            Log.e(TAG, "Authentication token missing in SharedPreferences");
            return;
        }

        Request request = new Request.Builder()
                .url(ApiConfig.CARTS_URL)
                .addHeader("Authorization", "users API-Key " + apiKey)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Cart items fetch failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Server error during cart fetch: " + response.code());
                    return;
                }

                try {
                    String jsonResponse = response.body().string();
                    parseCartJson(jsonResponse);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse cart JSON response", e);
                }
            }
        });
    }

    private void parseCartJson(String json) throws JSONException {
        JSONObject rootObj = new JSONObject(json);
        JSONArray cartsArray = rootObj.getJSONArray("docs");

        List<Cart> tempCartList = new ArrayList<>();
        for (int i = 0; i < cartsArray.length(); i++) {
            JSONObject cartJson = cartsArray.getJSONObject(i);
            
            // Only process pending carts
            if ("pending".equals(cartJson.optString("status"))) {
                extractProductsFromCart(cartJson, tempCartList);
            }
        }

        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                cartList.clear();
                cartList.addAll(tempCartList);
                cartAdapter.notifyDataSetChanged();
            });
        }
    }

    private void extractProductsFromCart(JSONObject cartJson, List<Cart> targetList) throws JSONException {
        String cartId = cartJson.getString("id");
        JSONArray productsArray = cartJson.getJSONArray("products");

        for (int j = 0; j < productsArray.length(); j++) {
            JSONObject productEntry = productsArray.getJSONObject(j);
            JSONObject details = productEntry.getJSONObject("product");
            
            Cart cartItem = new Cart();
            cartItem.setCartItemId(cartId);
            cartItem.setId(details.getString("id"));
            cartItem.setName(details.getString("name"));
            cartItem.setPrice(details.getDouble("price"));
            cartItem.setQuantity(productEntry.getInt("quantity"));
            
            // Handle image logic: check product entry level first, then details
            JSONObject image = productEntry.optJSONObject("image");
            if (image == null) {
                image = details.optJSONObject("image");
            }

            if (image != null) {
                String url = image.optString("url");
                if (!url.isEmpty()) {
                    // Check if URL is absolute or relative
                    if (url.startsWith("http")) {
                        cartItem.setImageUrl(url);
                    } else {
                        cartItem.setImageUrl(ApiConfig.BASE_URL + url);
                    }
                }
            }
            
            targetList.add(cartItem);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
