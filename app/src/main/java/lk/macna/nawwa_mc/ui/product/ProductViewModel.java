package lk.macna.nawwa_mc.ui.product;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lk.macna.nawwa_mc.model.Product;
import lk.macna.nawwa_mc.network.ApiConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProductViewModel extends ViewModel {
    private static final String TAG = "ProductViewModel";
    private final MutableLiveData<List<Product>> productItems = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final OkHttpClient httpClient = new OkHttpClient();

    public LiveData<List<Product>> getProductItems() {
        return productItems;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchProducts(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            errorMessage.postValue("Auth error");
            return;
        }

        Request request = new Request.Builder()
                .url(ApiConfig.PRODUCTS_URL)
                .addHeader("Authorization", "users API-Key " + apiKey)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                errorMessage.postValue("Fetch failed");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        parseProductJson(response.body().string());
                    } catch (JSONException e) {
                        errorMessage.postValue("Data error");
                    }
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
            
            JSONObject imageObj = prodJson.optJSONObject("image");
            if (imageObj != null) {
                String imageUrl = imageObj.optString("url");
                if (!imageUrl.isEmpty()) {
                    // Check if URL is relative or absolute
                    if (imageUrl.startsWith("http")) {
                        product.setImageUrl(imageUrl);
                    } else {
                        product.setImageUrl(ApiConfig.BASE_URL + imageUrl);
                    }
                }
            }
            
            tempProducts.add(product);
        }
        productItems.postValue(tempProducts);
    }
}
