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
 * MyOrdersFragment displays a history of the user's orders, including product details,
 * order status, and total pricing.
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

    /**
     * Executes the network request to retrieve user orders.
     */
    private void fetchOrdersFromApi() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = prefs.getString("apiKey", "");

        if (apiKey.isEmpty()) {
            Log.e(TAG, "Authentication token missing");
            return;
        }

        Request request = new Request.Builder()
                .url(ApiConfig.ORDERS_URL)
                .addHeader("Authorization", "users API-Key " + apiKey)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Network request failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Unexpected server response: " + response.code());
                    return;
                }

                try {
                    String jsonResponse = response.body().string();
                    parseOrdersJson(jsonResponse);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON parsing error: " + e.getMessage());
                }
            }
        });
    }

    private void parseOrdersJson(String json) throws JSONException {
        JSONObject root = new JSONObject(json);
        JSONArray ordersArray = root.getJSONArray("docs");

        List<Order> tempOrders = new ArrayList<>();
        for (int i = 0; i < ordersArray.length(); i++) {
            JSONObject orderJson = ordersArray.getJSONObject(i);
            Order order = mapJsonToOrder(orderJson);
            tempOrders.add(order);
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
        order.setAddressLine1(orderJson.optString("addressLine1"));
        order.setAddressLine2(orderJson.optString("addressLine2"));
        order.setCity(orderJson.optString("city"));
        order.setPhone(orderJson.optString("phone"));
        order.setLocation(orderJson.optString("location"));
        order.setOrderStatus(orderJson.optString("orderStatus"));
        order.setTotalPrice(orderJson.getDouble("totalPrice"));

        JSONArray products = orderJson.getJSONArray("products");
        if (products.length() > 0) {
            JSONObject firstProduct = products.getJSONObject(0);
            JSONObject details = firstProduct.getJSONObject("product");
            JSONObject image = details.getJSONObject("image");

            order.setProductName(details.getString("name"));
            order.setPrice(details.getDouble("price"));
            order.setQuantity(firstProduct.getInt("quantity"));
            order.setImageUrl(ApiConfig.BASE_URL + image.getString("url"));
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
