package lk.macna.nawwa_mc;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import lk.macna.nawwa_mc.databinding.ActivityHomeBinding;
import lk.macna.nawwa_mc.network.ApiConfig;

/**
 * HomeActivity serves as the main entry point after login.
 * Updated to handle Android 13+ Notification Permissions.
 */
public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private static final String PREFS_NAME = "MyPrefs";
    private static final int NOTIFICATION_PERMISSION_CODE = 1002;

    private ActivityHomeBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        setupNavigationHeader();
        
        // Request Notification Permission for Android 13+
        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. You might miss important updates.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupNavigation() {
        setSupportActionBar(binding.appBarHome.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_my_orders, R.id.nav_cart, 
                R.id.nav_product, R.id.nav_my_profile, R.id.nav_category)
                .setOpenableLayout(drawer)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                drawer.closeDrawers();
            }
            return handled;
        });
    }

    private void setupNavigationHeader() {
        View headerView = binding.navView.getHeaderView(0);
        TextView nameTextView = headerView.findViewById(R.id.LoggedUserName);
        TextView emailTextView = headerView.findViewById(R.id.LoggedUserEmail);
        ImageView profileImageView = headerView.findViewById(R.id.UserProfilePic);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String name = prefs.getString("name", "User");
        String email = prefs.getString("email", "");
        String profileUrl = prefs.getString("profileImageUrl", "");

        nameTextView.setText(name);
        emailTextView.setText(email);

        if (profileUrl != null && !profileUrl.isEmpty()) {
            Glide.with(this)
                    .load(ApiConfig.BASE_URL + profileUrl)
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher_round)
                    .into(profileImageView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_reload) {
            reloadCurrentFragment();
            return true;
        } else if (id == R.id.action_sign_out) {
            handleLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void reloadCurrentFragment() {
        int currentDestinationId = navController.getCurrentDestination().getId();
        navController.popBackStack();
        navController.navigate(currentDestinationId);
    }

    private void handleLogout() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
