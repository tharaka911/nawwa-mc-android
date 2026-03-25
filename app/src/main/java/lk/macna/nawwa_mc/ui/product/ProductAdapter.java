package lk.macna.nawwa_mc.ui.product;

import android.content.Context;
import android.content.SharedPreferences;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.model.Product;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private static final String TAG = "ProductAdapterLog";
    private static final String CART_API_URL = "https://ecom-api.macna.app/api/carts";
    private List<Product> productList;
    private OkHttpClient client = new OkHttpClient();
    private SharedPreferences sharedPreferences;

    public ProductAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
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
        holder.textViewProductName.setText(product.getName());
        holder.textViewCategoryType.setText(product.getCategory());
        holder.textViewProductPrice.setText(String.format("$%.2f", product.getPrice()));

        // Load image using Glide
        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.new_product) // default image
                .into(holder.imageViewProduct);

        // Handle add to cart button click
        holder.buttonAddToCart.setOnClickListener(v -> {
            Toast.makeText(holder.itemView.getContext(), "You're Added, " + product.getName()+" to cart", Toast.LENGTH_SHORT).show();
            Log.d(TAG, String.format("Add to Cart pressed for product: [ID: %s, Name: %s, Price: $%.2f]", product.getId(), product.getName(), product.getPrice()));
            addToCart(product.getId());
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    private void addToCart(String productId) {
        String apiKey = sharedPreferences.getString("apiKey", "");

        if (apiKey.isEmpty()) {
            Log.e(TAG, "API key is missing");
            return;
        }

        try {
            JSONObject productObject = new JSONObject();
            productObject.put("product", productId);
            productObject.put("quantity", 1);

            JSONArray productsArray = new JSONArray();
            productsArray.put(productObject);

            JSONObject cartObject = new JSONObject();
            cartObject.put("products", productsArray);

            RequestBody body = RequestBody.create(cartObject.toString(), MediaType.get("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(CART_API_URL)
                    .post(body)
                    .addHeader("Authorization", "users API-Key " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Error adding product to cart", e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Product added to cart successfully");
                    } else {
                        Log.e(TAG, "Unsuccessful response: " + response.code());
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON object for cart", e);
        }
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewProductName;
        TextView textViewCategoryType;
        TextView textViewProductPrice;
        Button buttonAddToCart;

        public ProductViewHolder(View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewCategoryType = itemView.findViewById(R.id.textViewOrderStatus2);
            textViewProductPrice = itemView.findViewById(R.id.textViewTotalPrice);
            buttonAddToCart = itemView.findViewById(R.id.ButtonaddToCart);
        }
    }
}