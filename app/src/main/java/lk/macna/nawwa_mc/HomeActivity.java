package lk.macna.nawwa_mc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;

import lk.macna.nawwa_mc.databinding.ActivityHomeBinding;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivityLog";
    private static final String BASE_URL = "https://ecom-api.macna.app";
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarHome.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_my_orders, R.id.nav_cart, R.id.nav_product, R.id.nav_my_profile, R.id.nav_category)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Get the header view
        View headerView = navigationView.getHeaderView(0);

        // Get the TextViews and ImageView
        TextView loggedUserName = headerView.findViewById(R.id.LoggedUserName);
        TextView loggedUserEmail = headerView.findViewById(R.id.LoggedUserEmail);
        ImageView userProfilePic = headerView.findViewById(R.id.UserProfilePic);

        // Fetch user data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("name", "User Name");
        String userEmail = sharedPreferences.getString("email", "user@example.com");
        String userProfilePicUrl = sharedPreferences.getString("profileImageUrl", "");

        // Log the fetched data
        Log.d(TAG, "Fetched userName: " + userName);
        Log.d(TAG, "Fetched userEmail: " + userEmail);
        Log.d(TAG, "Fetched userProfilePicUrl: " + userProfilePicUrl);

        // Append base URL to profile image URL
        String fullProfileImageUrl = BASE_URL + userProfilePicUrl;

        // Log the full profile image URL
        Log.d(TAG, "Full profile image URL: " + fullProfileImageUrl);

        // Set the user data in the Navigation Header
        loggedUserName.setText(userName);
        loggedUserEmail.setText(userEmail);
        if (!userProfilePicUrl.isEmpty()) {
            Glide.with(this)
                    .load(fullProfileImageUrl)
                    .centerCrop()
                    .placeholder(R.mipmap.ic_launcher_round) // default image
                    .into(userProfilePic);
        } else {
            userProfilePic.setImageResource(R.mipmap.ic_launcher_round);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_reload) {
            reloadCurrentFragment();
            return true;
        }

        if (id == R.id.action_sign_out) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void reloadCurrentFragment() {
        int currentFragmentId = navController.getCurrentDestination().getId();
        navController.popBackStack();
        navController.navigate(currentFragmentId);
    }

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}