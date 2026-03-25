package lk.macna.nawwa_mc.ui.my_orders;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.databinding.FragmentMyOrdersBinding;
import lk.macna.nawwa_mc.model.Order;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyOrdersFragment extends Fragment implements MyOrdersAdapter.OnOrderItemClickListener {

    private FragmentMyOrdersBinding binding;
    private RecyclerView recyclerView;
    private MyOrdersAdapter myOrdersAdapter;
    private List<Order> orderList = new ArrayList<>();

    private static final String ORDERS_API_URL = "https://ecom-api.macna.app/api/orders";
    private static final String PREFS_NAME = "MyPrefs";
    private static final String TAG = "MyOrdersFragmentLog";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MyOrdersViewModel myOrdersViewModel =
                new ViewModelProvider(this).get(MyOrdersViewModel.class);

        binding = FragmentMyOrdersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up RecyclerView
        recyclerView = root.findViewById(R.id.RecyclerViewMyOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up adapter
        myOrdersAdapter = new MyOrdersAdapter(orderList, this);
        recyclerView.setAdapter(myOrdersAdapter);

        // Fetch orders from API
        fetchOrders();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void fetchOrders() {
        OkHttpClient client = new OkHttpClient();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = sharedPreferences.getString("apiKey", "");

        if (apiKey.isEmpty()) {
            Log.e(TAG, "API key is missing");
            return;
        }

        Request request = new Request.Builder()
                .url(ORDERS_API_URL)
                .addHeader("Authorization", "users API-Key " + apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Error fetching orders", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray ordersJsonArray = jsonResponse.getJSONArray("docs");  // Get array of orders
                        orderList.clear();  // Clear any existing data

                        // Parse orders
                        for (int i = 0; i < ordersJsonArray.length(); i++) {
                            JSONObject orderJson = ordersJsonArray.getJSONObject(i);

                            Order order = new Order();
                            order.setOrderID(orderJson.getString("id"));
                            order.setAddressLine1(orderJson.optString("addressLine1"));
                            order.setAddressLine2(orderJson.optString("addressLine2"));
                            order.setCity(orderJson.optString("city"));
                            order.setPhone(orderJson.optString("phone"));
                            order.setLocation(orderJson.optString("location"));
                            order.setOrderStatus(orderJson.optString("orderStatus"));
                            order.setTotalPrice(orderJson.getDouble("totalPrice"));

                            JSONArray productsJsonArray = orderJson.getJSONArray("products");
                            if (productsJsonArray.length() > 0) {
                                JSONObject productJson = productsJsonArray.getJSONObject(0);
                                JSONObject productDetailsJson = productJson.getJSONObject("product");
                                JSONObject imageJson = productDetailsJson.getJSONObject("image");

                                order.setProductName(productDetailsJson.getString("name"));
                                order.setPrice(productDetailsJson.getDouble("price"));
                                order.setQuantity(productJson.getInt("quantity"));
                                order.setImageUrl("https://ecom-api.macna.app" + imageJson.getString("url"));
                            }

                            orderList.add(order);
                        }

                        // Notify adapter on the main thread
                        getActivity().runOnUiThread(() -> myOrdersAdapter.notifyDataSetChanged());

                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing orders", e);
                    }
                } else {
                    Log.e(TAG, "Unsuccessful response: " + response.code());
                }
            }
        });
    }

    @Override
    public void onAddToCartClicked(String orderId) {
        Log.d(TAG, "onAddToCartClicked: " + orderId);

        // Show the dialog fragment
        AddressFormDialogFragment dialogFragment = new AddressFormDialogFragment();
        Bundle args = new Bundle();
        args.putString("orderId", orderId);
        dialogFragment.setArguments(args);
        dialogFragment.show(getChildFragmentManager(), "AddressFormDialogFragment");
    }
}