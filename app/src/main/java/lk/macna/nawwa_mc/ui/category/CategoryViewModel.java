package lk.macna.nawwa_mc.ui.category;

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
import java.util.Arrays;
import java.util.List;

import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.model.Category;
import lk.macna.nawwa_mc.network.ApiConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CategoryViewModel extends ViewModel {
    private static final String TAG = "CategoryViewModel";

    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<Integer>> bannerImages = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public CategoryViewModel() {
        // Initialize with default banner images
        bannerImages.setValue(Arrays.asList(
                R.drawable.weclome_baner,
                R.drawable.weekend_baner,
                R.drawable.new_year_baner
        ));
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<List<Integer>> getBannerImages() {
        return bannerImages;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchCategories(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            errorMessage.postValue("API Key not found");
            return;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(ApiConfig.CATEGORIES_URL)
                .addHeader("Authorization", "users API-Key " + apiKey)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Network call failed: " + e.getMessage());
                errorMessage.postValue("Failed to connect to server");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Server returned error: " + response.code());
                    errorMessage.postValue("Server error: " + response.code());
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    parseCategoryJson(jsonData);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON Parsing error: " + e.getMessage());
                    errorMessage.postValue("Data format error");
                }
            }
        });
    }

    private void parseCategoryJson(String json) throws JSONException {
        JSONObject responseObj = new JSONObject(json);
        JSONArray docsArray = responseObj.getJSONArray("docs");

        List<Category> tempCategories = new ArrayList<>();
        for (int i = 0; i < docsArray.length(); i++) {
            JSONObject obj = docsArray.getJSONObject(i);
            Category category = new Category();
            category.setName(obj.getString("name"));
            category.setId(obj.getString("id"));
            tempCategories.add(category);
        }
        categories.postValue(tempCategories);
    }
}
