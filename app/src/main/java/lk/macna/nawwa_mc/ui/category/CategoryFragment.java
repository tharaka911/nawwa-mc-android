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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.databinding.FragmentCategoryBinding;
import lk.macna.nawwa_mc.model.Category;
import lk.macna.nawwa_mc.network.ApiConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * CategoryFragment displays available product categories and a promotional banner.
 */
public class CategoryFragment extends Fragment {

    private static final String TAG = "CategoryFragment";
    private static final String PREFS_NAME = "MyPrefs";
    private static final int BANNER_SCROLL_DELAY_MS = 2000;

    private FragmentCategoryBinding binding;
    private CategoryAdapter categoryAdapter;
    private final List<Category> categoryList = new ArrayList<>();
    
    private final Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;

    private final List<Integer> bannerImages = Arrays.asList(
            R.drawable.weclome_baner,
            R.drawable.weekend_baner,
            R.drawable.new_year_baner
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupCategoryList();
        setupPromoBanner();
        fetchCategories();
    }

    private void setupCategoryList() {
        categoryAdapter = new CategoryAdapter(categoryList);
        binding.recyclerViewCategory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewCategory.setAdapter(categoryAdapter);
    }

    private void setupPromoBanner() {
        BannerAdapter bannerAdapter = new BannerAdapter(new ArrayList<>(bannerImages));
        RecyclerView recyclerView = binding.recyclerViewBanners;

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(bannerAdapter);

        // Snap banner to center
        new LinearSnapHelper().attachToRecyclerView(recyclerView);

        startBannerAutoScroll(recyclerView, bannerAdapter);
    }

    private void startBannerAutoScroll(RecyclerView recyclerView, BannerAdapter adapter) {
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && adapter.getItemCount() > 0) {
                    int nextPos = (layoutManager.findFirstVisibleItemPosition() + 1) % adapter.getItemCount();
                    recyclerView.smoothScrollToPosition(nextPos);
                    bannerHandler.postDelayed(this, BANNER_SCROLL_DELAY_MS);
                }
            }
        };

        bannerHandler.postDelayed(bannerRunnable, BANNER_SCROLL_DELAY_MS);

        recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                bannerHandler.removeCallbacks(bannerRunnable);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                bannerHandler.postDelayed(bannerRunnable, BANNER_SCROLL_DELAY_MS);
            }
            return false;
        });
    }

    /**
     * Retrieves category data from the remote API using OkHttp.
     */
    private void fetchCategories() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = prefs.getString("apiKey", "");

        if (apiKey.isEmpty()) {
            Log.e(TAG, "API Key not found in SharedPreferences");
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
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Server returned error: " + response.code());
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    parseCategoryJson(jsonData);
                } catch (JSONException e) {
                    Log.e(TAG, "JSON Parsing error: " + e.getMessage());
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

        // Update UI on main thread
        if (isAdded()) {
            requireActivity().runOnUiThread(() -> {
                categoryList.clear();
                categoryList.addAll(tempCategories);
                categoryAdapter.notifyDataSetChanged();
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bannerHandler.removeCallbacks(bannerRunnable);
        binding = null;
    }
}