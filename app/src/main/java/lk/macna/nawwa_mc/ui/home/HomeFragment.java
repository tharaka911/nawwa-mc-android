package lk.macna.nawwa_mc.ui.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.databinding.FragmentHomeBinding;
import lk.macna.nawwa_mc.ui.category.BannerAdapter;

/**
 * HomeFragment handles the main dashboard display, including a sliding banner
 * and quick access cards for core application features.
 */
public class HomeFragment extends Fragment {

    private static final int BANNER_SCROLL_DELAY_MS = 3000;
    private static final String SUPPORT_PHONE_NUMBER = "0718888777";

    private FragmentHomeBinding binding;
    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;

    private final List<Integer> bannerImages = Arrays.asList(
            R.drawable.weclome_baner,
            R.drawable.weekend_baner,
            R.drawable.new_year_baner
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupBanner();
        setupDashboardNavigation();
    }

    /**
     * Configures the top sliding banner with auto-scroll and snapping.
     */
    private void setupBanner() {
        BannerAdapter adapter = new BannerAdapter(new ArrayList<>(bannerImages));
        RecyclerView recyclerView = binding.recyclerViewHomeBanner;

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
        
        // Ensure smooth snapping to center
        new LinearSnapHelper().attachToRecyclerView(recyclerView);

        startBannerAutoScroll(recyclerView, adapter);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void startBannerAutoScroll(RecyclerView recyclerView, BannerAdapter adapter) {
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && adapter.getItemCount() > 0) {
                    int nextPosition = (layoutManager.findFirstVisibleItemPosition() + 1) % adapter.getItemCount();
                    recyclerView.smoothScrollToPosition(nextPosition);
                    autoScrollHandler.postDelayed(this, BANNER_SCROLL_DELAY_MS);
                }
            }
        };

        autoScrollHandler.postDelayed(autoScrollRunnable, BANNER_SCROLL_DELAY_MS);

        // Pause auto-scroll on manual touch to improve UX
        recyclerView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    autoScrollHandler.removeCallbacks(autoScrollRunnable);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    autoScrollHandler.postDelayed(autoScrollRunnable, BANNER_SCROLL_DELAY_MS);
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        v.performClick();
                    }
                    break;
            }
            return false;
        });
    }

    /**
     * Sets up click listeners for all dashboard quick-action cards.
     */
    private void setupDashboardNavigation() {
        NavController navController = Navigation.findNavController(requireView());

        binding.cardMyOrders.setOnClickListener(v -> navController.navigate(R.id.nav_my_orders));
        binding.cardCart.setOnClickListener(v -> navController.navigate(R.id.nav_cart));
        binding.cardCategory.setOnClickListener(v -> navController.navigate(R.id.nav_category));
        binding.cardProduct.setOnClickListener(v -> navController.navigate(R.id.nav_product));
        binding.cardMyProfile.setOnClickListener(v -> navController.navigate(R.id.nav_my_profile));
        binding.cardContactAdmin.setOnClickListener(v -> dialSupportNumber());
    }

    private void dialSupportNumber() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + SUPPORT_PHONE_NUMBER));
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
        binding = null;
    }
}