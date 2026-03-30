package lk.macna.nawwa_mc.ui.my_orders;

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

import lk.macna.nawwa_mc.databinding.FragmentMyOrdersBinding;
import lk.macna.nawwa_mc.model.Order;
import lk.macna.nawwa_mc.network.ApiConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * MyOrdersFragment displays a history of the user's orders.
 * Improved to handle image URLs correctly for better clarity.
 */
public class MyOrdersFragment extends Fragment implements MyOrdersAdapter.OnOrderItemClickListener {

    private static final String TAG = "MyOrdersFragment";
    private static final String PREFS_NAME = "MyPrefs";

    private FragmentMyOrdersBinding binding;
    private MyOrdersAdapter myOrdersAdapter;
    private final List<Order> orderList = new ArrayList<>();
    private final OkHttpClient httpClient = new OkHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupOrdersRecyclerView();
        fetchOrdersFromApi();
    }

    private void setupOrdersRecyclerView() {
        myOrdersAdapter = new MyOrdersAdapter(orderList, this);
        binding.RecyclerViewMyOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.RecyclerViewMyOrders.setAdapter(myOrdersAdapter);
    }

    private void fetchOrdersFromApi() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = prefs.getString("apiKey", "");

        if (apiKey.isEmpty()) return;

        Request request = new Request.Builder()
                .url(ApiConfig.ORDERS_URL)
                .addHeader("Authorization", "users API-Key " + apiKey)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Orders fetch failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        parseOrdersJson(response.body().string());
                    } catch (JSONException e) {
                        Log.e(TAG, "Parsing error", e);
                    }
                }
            }
        });
    }

    private void parseOrdersJson(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONArray ordersArray = root.getJSONArray("docs");

        List<Order> tempOrders = new ArrayList<>();
        for (int i = 0; i < ordersArray.length(); i++) {
            tempOrders.add(mapJsonToOrder(ordersArray.getJSONObject(i)));
        }

        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                orderList.clear();
                orderList.addAll(tempOrders);
                myOrdersAdapter.notifyDataSetChanged();
            });
        }
    }

    private Order mapJsonToOrder(JSONObject orderJson) throws JSONException {
        Order order = new Order();
        order.setOrderID(orderJson.getString("id"));
        order.setOrderStatus(orderJson.optString("orderStatus"));
        order.setTotalPrice(orderJson.optDouble("totalPrice", 0.0));

        JSONArray products = orderJson.optJSONArray("products");
        if (products != null && products.length() > 0) {
            JSONObject firstProduct = products.getJSONObject(0);
            JSONObject details = firstProduct.optJSONObject("product");
            if (details != null) {
                order.setProductName(details.optString("name"));
                
                JSONObject imageObj = details.optJSONObject("image");
                if (imageObj != null) {
                    String imageUrl = imageObj.optString("url");
                    if (imageUrl.startsWith("http")) {
                        order.setImageUrl(imageUrl);
                    } else {
                        order.setImageUrl(ApiConfig.BASE_URL + imageUrl);
                    }
                }
            }
        }
        return order;
    }

    @Override
    public void onAddToCartClicked(String orderId) {
        AddressFormDialogFragment dialogFragment = new AddressFormDialogFragment();
        Bundle args = new Bundle();
        args.putString("orderId", orderId);
        dialogFragment.setArguments(args);
        dialogFragment.show(getChildFragmentManager(), "AddressFormDialogFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
