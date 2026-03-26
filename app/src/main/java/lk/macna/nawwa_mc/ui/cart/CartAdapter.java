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
import lk.macna.nawwa_mc.network.ApiConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * CartAdapter manages the list of items in the user's shopping cart,
 * allowing for quantity adjustments and conversion of cart items to orders.
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private static final String TAG = "CartAdapter";
    private static final String PREFS_NAME = "MyPrefs";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get(ApiConfig.JSON_MEDIA_TYPE);

    private final List<Cart> cartList;
    private final OkHttpClient httpClient = new OkHttpClient();

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
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() {
        return cartList != null ? cartList.size() : 0;
    }

    /**
     * ViewHolder for individual cart items.
     */
    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImageView;
        private final TextView productNameTextView;
        private final TextView priceTextView;
        private final TextView quantityTextView;
        private final SeekBar quantitySeekBar;
        private final Button orderNowButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.imageViewProduct);
            productNameTextView = itemView.findViewById(R.id.textViewProductName);
            priceTextView = itemView.findViewById(R.id.textViewTotalPrice);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            quantitySeekBar = itemView.findViewById(R.id.quantitySeekBar);
            orderNowButton = itemView.findViewById(R.id.button2);
        }

        public void bind(Cart cartItem) {
            productNameTextView.setText(cartItem.getName());
            priceTextView.setText(String.format("$%.2f", cartItem.getPrice()));
            updateQuantityDisplay(cartItem.getQuantity());
            
            quantitySeekBar.setProgress(cartItem.getQuantity());

            Glide.with(itemView.getContext())
                    .load(cartItem.getImageUrl())
                    .placeholder(R.drawable.new_product)
                    .centerCrop()
                    .into(productImageView);

            setupSeekBar(cartItem);
            
            orderNowButton.setOnClickListener(v -> {
                Toast.makeText(itemView.getContext(), "Converting " + cartItem.getName() + " to order...", Toast.LENGTH_SHORT).show();
                placeOrderFromCart(itemView.getContext(), cartItem);
            });
        }

        private void setupSeekBar(Cart cartItem) {
            quantitySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // Ensure minimum quantity is 1
                    int finalProgress = Math.max(1, progress);
                    updateQuantityDisplay(finalProgress);
                    cartItem.setQuantity(finalProgress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }

        private void updateQuantityDisplay(int quantity) {
            quantityTextView.setText(String.format("Quantity: %d", quantity));
        }
    }

    /**
     * Patch request to update cart status to 'make a order' and update final quantity.
     */
    private void placeOrderFromCart(Context context, Cart cartItem) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = sharedPreferences.getString("apiKey", "");

        if (apiKey.isEmpty()) {
            Log.e(TAG, "API Key missing in CartAdapter");
            return;
        }

        try {
            JSONObject patchBody = createOrderPatchJson(cartItem);
            RequestBody body = RequestBody.create(patchBody.toString(), JSON_MEDIA_TYPE);
            
            Request request = new Request.Builder()
                    .url(ApiConfig.CARTS_URL + "/" + cartItem.getCartItemId())
                    .patch(body)
                    .addHeader("Authorization", "users API-Key " + apiKey)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Order conversion failed: " + e.getMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Successfully converted cart item to order");
                    } else {
                        Log.e(TAG, "Error response from server: " + response.code());
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Failed to build JSON for cart update", e);
        }
    }

    private JSONObject createOrderPatchJson(Cart item) throws JSONException {
        JSONObject productEntry = new JSONObject();
        productEntry.put("product", item.getId());
        productEntry.put("quantity", item.getQuantity());

        JSONArray productsList = new JSONArray();
        productsList.put(productEntry);

        JSONObject root = new JSONObject();
        root.put("products", productsList);
        root.put("status", "make a order");
        return root;
    }
}