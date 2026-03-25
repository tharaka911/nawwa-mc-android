package lk.macna.nawwa_mc.ui.category;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.databinding.FragmentCategoryBinding;
import lk.macna.nawwa_mc.model.Category;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private RecyclerView recyclerViewCategory;
    private RecyclerView recyclerViewBannersTop;
    private RecyclerView recyclerViewBannersBottom;
    private CategoryAdapter categoryAdapter;
    private BannerAdapter bannerAdapterTop;
    private BannerAdapter bannerAdapterBottom;
    private List<Category> categoryList = new ArrayList<>();
    private List<Integer> bannerImages = Arrays.asList(
            R.drawable.banner1,
            R.drawable.banner2,
            R.drawable.banner3,
            R.drawable.banner4,
            R.drawable.banner5
    );

    private static final String API_URL = "https://ecom-api.macna.app/api/categories";
    private static final String PREFS_NAME = "MyPrefs";
    private static final String TAG = "CategoryFragmentLog";

    private Handler handler;
    private Runnable runnableTop;
    private Runnable runnableBottom;
    private boolean isScrollingTop = true;
    private boolean isScrollingBottom = true;
    private Random random;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        random = new Random();

        // Set up RecyclerView for categories
        recyclerViewCategory = root.findViewById(R.id.recyclerViewCategory);
        recyclerViewCategory.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up adapter for categories
        categoryAdapter = new CategoryAdapter(categoryList);
        recyclerViewCategory.setAdapter(categoryAdapter);

        // Set up RecyclerView for top banners
        recyclerViewBannersTop = root.findViewById(R.id.recyclerViewBanners);
        recyclerViewBannersTop.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Set up RecyclerView for bottom banners
        recyclerViewBannersBottom = root.findViewById(R.id.recyclerViewBannersBottom);
        recyclerViewBannersBottom.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Set up adapter for banners
        bannerAdapterTop = new BannerAdapter(new ArrayList<>(bannerImages));
        bannerAdapterBottom = new BannerAdapter(new ArrayList<>(bannerImages));
        recyclerViewBannersTop.setAdapter(bannerAdapterTop);
        recyclerViewBannersBottom.setAdapter(bannerAdapterBottom);

        // Auto scroll banners
        handler = new Handler(Looper.getMainLooper());
        runnableTop = new Runnable() {
            @Override
            public void run() {
                if (isScrollingTop) {
                    Collections.shuffle(bannerAdapterTop.getBannerImages(), random);
                    bannerAdapterTop.notifyDataSetChanged();
                    recyclerViewBannersTop.smoothScrollToPosition(random.nextInt(bannerAdapterTop.getItemCount()));
                    handler.postDelayed(this, 2000);
                }
            }
        };
        handler.postDelayed(runnableTop, 3000);

        runnableBottom = new Runnable() {
            @Override
            public void run() {
                if (isScrollingBottom) {
                    Collections.shuffle(bannerAdapterBottom.getBannerImages(), random);
                    bannerAdapterBottom.notifyDataSetChanged();
                    recyclerViewBannersBottom.smoothScrollToPosition(random.nextInt(bannerAdapterBottom.getItemCount()));
                    handler.postDelayed(this, 3000);
                }
            }
        };
        handler.postDelayed(runnableBottom, 2000);

        // Set touch listeners to stop/resume scrolling
        recyclerViewBannersTop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isScrollingTop = false;
                        handler.removeCallbacks(runnableTop);
                        break;
                    case MotionEvent.ACTION_UP:
                        isScrollingTop = true;
                        handler.postDelayed(runnableTop, 2000);
                        break;
                }
                return false;
            }
        });

        recyclerViewBannersBottom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isScrollingBottom = false;
                        handler.removeCallbacks(runnableBottom);
                        break;
                    case MotionEvent.ACTION_UP:
                        isScrollingBottom = true;
                        handler.postDelayed(runnableBottom, 2000);
                        break;
                }
                return false;
            }
        });

        // Fetch data from API
        fetchCategories();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(runnableTop);
        handler.removeCallbacks(runnableBottom);
        binding = null;
    }

    private void fetchCategories() {
        OkHttpClient client = new OkHttpClient();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = sharedPreferences.getString("apiKey", "");

        if (apiKey.isEmpty()) {
            Log.e(TAG, "API key is missing");
            return;
        }

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "users API-Key " + apiKey)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                Log.e(TAG, "Error fetching categories", e);
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray categoriesJsonArray = jsonResponse.getJSONArray("docs");  // Get categories from "docs" array
                        categoryList.clear();  // Clear any existing data

                        // Parse categories
                        for (int i = 0; i < categoriesJsonArray.length(); i++) {
                            JSONObject categoryJson = categoriesJsonArray.getJSONObject(i);
                            Category category = new Category();
                            category.setName(categoryJson.getString("name"));
                            category.setId(categoryJson.getString("id"));
                            categoryList.add(category);
                        }

                        // Notify adapter on the main thread
                        getActivity().runOnUiThread(() -> categoryAdapter.notifyDataSetChanged());

                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing categories", e);
                    }
                } else {
                    Log.e(TAG, "Unsuccessful response: " + response.code());
                }
            }
        });
    }
}