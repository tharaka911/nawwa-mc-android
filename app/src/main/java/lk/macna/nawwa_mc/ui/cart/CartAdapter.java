package lk.macna.nawwa_mc.ui.cart;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.model.Cart;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Cart> cartList;
    private static final String TAG = "CartAdapterLog";
    private static final String PREFS_NAME = "MyPrefs";
    private static final String BASE_URL = "https://ecom-api.macna.app/api/carts/";

    public CartAdapter(List<Cart> cartList) {
        this.cartList = cartList;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Cart cartItem = cartList.get(position);
        holder.textViewProductName.setText(cartItem.getName());
        holder.textViewProductPrice.setText(String.format("$%.2f", cartItem.getPrice()));
        holder.quantityTextView.setText(String.format("Quantity: %d", cartItem.getQuantity()));
        holder.quantitySeekBar.setProgress(cartItem.getQuantity());

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(cartItem.getImageUrl())
                .placeholder(R.drawable.new_product) // default image
                .into(holder.imageViewProduct);

        // Set up listener to track changes in quantity
        holder.quantitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int stepSize = 1;
                int discreteProgress = Math.round(progress / (float) stepSize) * stepSize;
                holder.quantityTextView.setText("Quantity: " + discreteProgress);
                seekBar.setProgress(discreteProgress);
                cartItem.setQuantity(discreteProgress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do something when touch starts, if needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do something when touch stops, if needed
            }
        });

        // Handle order now button click
        holder.orderNowButton.setOnClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(), "You're Ordered, " + cartItem.getName(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, String.format("Order Now pressed for cart item ID: %s", cartItem.getCartItemId()));
            Log.d(TAG, String.format("Quantity: %d", cartItem.getQuantity()));
            updateCartItem(holder.itemView.getContext(), cartItem);
        });
    }

    private void updateCartItem(Context context, Cart cartItem) {
        OkHttpClient client = new OkHttpClient();

        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = sharedPreferences.getString("apiKey", "");

        if (apiKey.isEmpty()) {
            Log.e(TAG, "API key is missing");
            return;
        }

        try {
            JSONObject productObject = new JSONObject();
            productObject.put("product", cartItem.getId());
            productObject.put("quantity", cartItem.getQuantity());

            JSONArray productsArray = new JSONArray();
            productsArray.put(productObject);

            JSONObject cartObject = new JSONObject();
            cartObject.put("products", productsArray);
            cartObject.put("status", "make a order");

            RequestBody body = RequestBody.create(cartObject.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(BASE_URL + cartItem.getCartItemId())
                    .patch(body)
                    .addHeader("Authorization", "users API-Key " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Error updating cart item", e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Cart item updated successfully");
                    } else {
                        Log.e(TAG, "Unsuccessful response: " + response.code());
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON object for cart", e);
        }
    }

    @Override
    public int getItemCount() {
        return cartList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewProductName;
        TextView textViewProductPrice;
        TextView quantityTextView;
        SeekBar quantitySeekBar;
        Button orderNowButton;

        public CartViewHolder(View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductPrice = itemView.findViewById(R.id.textViewTotalPrice);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            quantitySeekBar = itemView.findViewById(R.id.quantitySeekBar);
            orderNowButton = itemView.findViewById(R.id.button2);
        }
    }
}