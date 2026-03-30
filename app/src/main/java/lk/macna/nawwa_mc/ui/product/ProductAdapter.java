package lk.macna.nawwa_mc.ui.product;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.model.Product;
import lk.macna.nawwa_mc.network.ApiConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * ProductAdapter manages the display of products in a list and handles
 * interactions such as adding items to the shopping cart.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private static final String TAG = "ProductAdapter";
    private static final String PREFS_NAME = "MyPrefs";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get(ApiConfig.JSON_MEDIA_TYPE);

    private final List<Product> productList;
    private final OkHttpClient httpClient = new OkHttpClient();
    private final SharedPreferences sharedPreferences;
    private final Context context;

    public ProductAdapter(List<Product> productList, @NonNull Context context) {
        this.productList = productList;
        this.context = context;
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_item, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImageView;
        private final TextView nameTextView;
        private final TextView categoryTextView;
        private final TextView priceTextView;
        private final Button addToCartButton;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.imageViewProduct);
            nameTextView = itemView.findViewById(R.id.textViewProductName);
            categoryTextView = itemView.findViewById(R.id.textViewOrderStatus2);
            priceTextView = itemView.findViewById(R.id.textViewTotalPrice);
            addToCartButton = itemView.findViewById(R.id.ButtonaddToCart);
        }

        public void bind(Product product) {
            nameTextView.setText(product.getName());
            categoryTextView.setText(product.getCategory());
            priceTextView.setText(String.format("LKR %.2f", product.getPrice()));

            Glide.with(itemView.getContext())
                    .load(product.getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.new_product)
                            .error(R.drawable.new_product)
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .centerCrop()
                    .into(productImageView);

            addToCartButton.setOnClickListener(v -> handleAddToCart(product));
        }

        private void handleAddToCart(Product product) {
            addToCartApiCall(product.getId(), product.getName());
        }
    }

    private void addToCartApiCall(String productId, String productName) {
        String apiKey = sharedPreferences.getString("apiKey", "");
        String email = sharedPreferences.getString("email", "");

        // Log the credentials being used
        Log.d(TAG, "Using API Key: " + (apiKey.isEmpty() ? "MISSING" : "PRESENT"));
        Log.d(TAG, "Using Email: " + (email.isEmpty() ? "MISSING" : email));

        if (apiKey.isEmpty() || email.isEmpty()) {
            showToast("Please login first");
            return;
        }

        try {
            JSONObject cartJson = createCartRequestJson(productId, email);
            String jsonString = cartJson.toString();
            
            Log.d(TAG, "Request URL: " + ApiConfig.CARTS_URL);
            Log.d(TAG, "Request JSON: " + jsonString);

            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, jsonString);
            
            Request request = new Request.Builder()
                    .url(ApiConfig.CARTS_URL)
                    .post(body)
                    .addHeader("Authorization", "users API-Key " + apiKey)
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Network failure: " + e.getMessage());
                    showToast("Network error");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "No response body";
                    
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Cart Response: " + responseBody);
                        showToast(productName + " added to cart!");
                    } else {
                        Log.e(TAG, "Cart Error " + response.code() + ": " + responseBody);
                        showToast("Failed to add to cart (Error " + response.code() + ")");
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "JSON error", e);
        }
    }

    private JSONObject createCartRequestJson(String productId, String email) throws JSONException {
        JSONObject productEntry = new JSONObject();
        productEntry.put("product", productId);
        productEntry.put("quantity", 1);

        JSONArray productsList = new JSONArray();
        productsList.put(productEntry);

        JSONObject root = new JSONObject();
        // The backend expects "useremail" as the key, not "email"
        root.put("useremail", email);
        root.put("products", productsList);
        root.put("status", "pending");
        return root;
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() -> 
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        );
    }
}
