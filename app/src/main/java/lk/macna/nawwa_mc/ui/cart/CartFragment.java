package lk.macna.nawwa_mc.ui.cart;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import lk.macna.nawwa_mc.databinding.FragmentCartBinding;
import lk.macna.nawwa_mc.model.Cart;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<Cart> cartList = new ArrayList<>();

    private static final String CART_API_URL = "https://ecom-api.macna.app/api/carts";
    private static final String PREFS_NAME = "MyPrefs";
    private static final String TAG = "CartFragmentLog";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CartViewModel cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        binding = FragmentCartBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up RecyclerView
        recyclerView = root.findViewById(R.id.RecyclerViewCart);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up adapter
        cartAdapter = new CartAdapter(cartList);
        recyclerView.setAdapter(cartAdapter);

        // Fetch data from API
        fetchCartItems();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void fetchCartItems() {
        OkHttpClient client = new OkHttpClient();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = sharedPreferences.getString("apiKey", "");

        if (apiKey.isEmpty()) {
            Log.e(TAG, "API key is missing");
            return;
        }

        Request request = new Request.Builder()
                .url(CART_API_URL)
                .addHeader("Authorization", "users API-Key " + apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error fetching cart items", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray cartsJsonArray = jsonResponse.getJSONArray("docs");  // Get array of carts
                        cartList.clear();  // Clear any existing data

                        // Parse cart items
                        for (int i = 0; i < cartsJsonArray.length(); i++) {
                            JSONObject cartJson = cartsJsonArray.getJSONObject(i);
                            if ("pending".equals(cartJson.getString("status"))) {
                                String cartItemId = cartJson.getString("id");
                                JSONArray productsJsonArray = cartJson.getJSONArray("products");
                                for (int j = 0; j < productsJsonArray.length(); j++) {
                                    JSONObject productJson = productsJsonArray.getJSONObject(j);
                                    JSONObject productDetailsJson = productJson.getJSONObject("product");
                                    Cart cartItem = new Cart();
                                    cartItem.setId(productDetailsJson.getString("id"));
                                    cartItem.setCartItemId(cartItemId);
                                    cartItem.setName(productDetailsJson.getString("name"));
                                    cartItem.setPrice(productDetailsJson.getDouble("price"));
                                    cartItem.setQuantity(productJson.getInt("quantity"));
                                    cartItem.setImageUrl("https://ecom-api.macna.app" + productDetailsJson.getJSONObject("image").getString("url"));
                                    cartList.add(cartItem);
                                }
                            }
                        }

                        // Notify adapter on the main thread
                        getActivity().runOnUiThread(() -> cartAdapter.notifyDataSetChanged());

                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing cart items", e);
                    }
                } else {
                    Log.e(TAG, "Unsuccessful response: " + response.code());
                }
            }
        });
    }
}