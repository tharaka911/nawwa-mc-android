package lk.macna.nawwa_mc.ui.product;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import lk.macna.nawwa_mc.databinding.FragmentProductBinding;
import lk.macna.nawwa_mc.model.Product;

/**
 * ProductFragment handles the display of product listings.
 * Optimized to use ViewModel and improved image loading handling.
 */
public class ProductFragment extends Fragment {

    private static final String PREFS_NAME = "MyPrefs";
    private FragmentProductBinding binding;
    private ProductViewModel viewModel;
    private ProductAdapter productAdapter;
    private final List<Product> productList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        setupProductList();
        observeViewModel();
        
        fetchInitialData();
    }

    private void setupProductList() {
        productAdapter = new ProductAdapter(productList, requireContext());
        binding.recyclerViewProduct.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewProduct.setAdapter(productAdapter);
    }

    private void observeViewModel() {
        viewModel.getProductItems().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                productList.clear();
                productList.addAll(products);
                productAdapter.notifyDataSetChanged();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchInitialData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String apiKey = prefs.getString("apiKey", "");
        viewModel.fetchProducts(apiKey);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
