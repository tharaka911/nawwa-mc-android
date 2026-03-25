package lk.macna.nawwa_mc.ui.home;

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
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lk.macna.nawwa_mc.R;
import lk.macna.nawwa_mc.databinding.FragmentHomeBinding;
import lk.macna.nawwa_mc.ui.category.BannerAdapter;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private BannerAdapter bannerAdapterTop;
    private BannerAdapter bannerAdapterBottom1;
    private BannerAdapter bannerAdapterBottom2;
    private Handler handlerTop, handlerBottom1, handlerBottom2;
    private Runnable runnableTop, runnableBottom1, runnableBottom2;

    private static final int SCROLL_DELAY = 2000;
    private static final String ADMIN_CONTACT_NUMBER = "0710351156";

    private final List<Integer> bannerImages = Arrays.asList(
            R.drawable.banner1,
            R.drawable.banner6,
            R.drawable.banner2,
            R.drawable.banner7,
            R.drawable.banner3,
            R.drawable.banner8,
            R.drawable.banner4,
            R.drawable.banner9,
            R.drawable.banner5,
            R.drawable.banner10
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerViews();
        setupAutoScroll();
        setupNavigationButtons();

        // Updated to use card ID
        binding.cardContactAdmin.setOnClickListener(v -> openDialerWithContactNumber());

        return root;
    }

    private void setupRecyclerViews() {
        // Initialize Adapters
        bannerAdapterTop = new BannerAdapter(new ArrayList<>(bannerImages));
        bannerAdapterBottom1 = new BannerAdapter(new ArrayList<>(bannerImages));
        bannerAdapterBottom2 = new BannerAdapter(new ArrayList<>(bannerImages));

        // Setup RecyclerViews with Linear Layout Managers
        setupRecyclerView(binding.recyclerViewHomeBanner, bannerAdapterTop);
        setupRecyclerView(binding.recyclerViewCategoryBanner1, bannerAdapterBottom1);
        setupRecyclerView(binding.recyclerViewCategoryBanner2, bannerAdapterBottom2);
    }

    private void setupRecyclerView(RecyclerView recyclerView, BannerAdapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);

        // Add smooth snapping effect
        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
    }

    private void setupAutoScroll() {
        handlerTop = new Handler(Looper.getMainLooper());
        handlerBottom1 = new Handler(Looper.getMainLooper());
        handlerBottom2 = new Handler(Looper.getMainLooper());

        startAutoScroll(binding.recyclerViewHomeBanner, bannerAdapterTop, handlerTop, () -> runnableTop);
        startAutoScroll(binding.recyclerViewCategoryBanner1, bannerAdapterBottom1, handlerBottom1, () -> runnableBottom1);
        startAutoScroll(binding.recyclerViewCategoryBanner2, bannerAdapterBottom2, handlerBottom2, () -> runnableBottom2);
    }

    private void startAutoScroll(RecyclerView recyclerView, BannerAdapter adapter, Handler handler, RunnableSupplier runnableSupplier) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (adapter.getItemCount() == 0) return;

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int currentPosition = layoutManager.findFirstVisibleItemPosition();
                int nextPosition = (currentPosition + 1) % adapter.getItemCount();

                recyclerView.smoothScrollToPosition(nextPosition);
                handler.postDelayed(this, SCROLL_DELAY);
            }
        };

        handler.postDelayed(runnable, SCROLL_DELAY);

        recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                handler.removeCallbacks(runnable);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                handler.postDelayed(runnable, SCROLL_DELAY);
            }
            return false;
        });

        // Store the runnable reference for cleanup
        if (runnableSupplier.get() == runnableTop) {
            runnableTop = runnable;
        } else if (runnableSupplier.get() == runnableBottom1) {
            runnableBottom1 = runnable;
        } else if (runnableSupplier.get() == runnableBottom2) {
            runnableBottom2 = runnable;
        }
    }

    private void setupNavigationButtons() {
        // Updated to use new Card IDs from the dashboard grid
        binding.cardMyOrders.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_my_orders));
        binding.cardCart.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_cart));
        binding.cardCategory.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_category));
        binding.cardProduct.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_product));
        binding.cardMyProfile.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_my_profile));
    }

    private void openDialerWithContactNumber() {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + ADMIN_CONTACT_NUMBER));
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up handlers to avoid memory leaks
        if (handlerTop != null) handlerTop.removeCallbacks(runnableTop);
        if (handlerBottom1 != null) handlerBottom1.removeCallbacks(runnableBottom1);
        if (handlerBottom2 != null) handlerBottom2.removeCallbacks(runnableBottom2);
        binding = null;
    }

    private interface RunnableSupplier {
        Runnable get();
    }
}