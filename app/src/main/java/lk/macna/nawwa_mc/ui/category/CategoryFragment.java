package lk.macna.nawwa_mc.ui.category;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import java.util.List;

import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.databinding.FragmentCategoryBinding;
import lk.macna.nawwa_mc.model.Category;

/**
 * CategoryFragment displays available product categories and a promotional banner.
 */
public class CategoryFragment extends Fragment {

    private static final String PREFS_NAME = "MyPrefs";
    private FragmentCategoryBinding binding;
    private CategoryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);

        observeViewModel();
        fetchInitialData();
    }

    private void observeViewModel() {
        // When categories list updates, show them in the UI
        viewModel.getCategories().observe(getViewLifecycleOwner(), this::displayCategories);

        // When banner images update, show them in the horizontal scroll
        viewModel.getBannerImages().observe(getViewLifecycleOwner(), this::displayBanners);

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCategories(List<Category> categories) {
        // Clear old items
        binding.categoryContainer.removeAllViews();
        
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        
        for (Category category : categories) {
            // Inflate (create) a single category item view
            View itemView = inflater.inflate(R.layout.category_item, binding.categoryContainer, false);
            
            // Set the name
            TextView nameTextView = itemView.findViewById(R.id.categoryName);
            nameTextView.setText(category.getName());
            
            // Handle clicks
            itemView.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("categoryId", category.getId());
                bundle.putString("categoryName", category.getName());
                Navigation.findNavController(v).navigate(R.id.nav_product, bundle);
            });
            
            // Add it to the vertical list (LinearLayout)
            binding.categoryContainer.addView(itemView);
        }
    }

    private void displayBanners(List<Integer> images) {
        binding.bannerContainer.removeAllViews();
        
        // Get screen width to make images full-width
        int screenWidth = getResources().getDisplayMetrics().widthPixels;

        for (int imageResId : images) {
            ImageView imageView = new ImageView(requireContext());
            
            // Layout params: Full width, fill height
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    screenWidth, 
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(imageResId);
            
            binding.bannerContainer.addView(imageView);
        }
    }

    private void fetchInitialData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = prefs.getString("apiKey", "");
        viewModel.fetchCategories(apiKey);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
