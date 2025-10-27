package com.example.cs361v2;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private FrameLayout frameLayout;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing widgets
        frameLayout = findViewById(R.id.frameLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Setting default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new HomeFragment()).commit();
            bottomNavigationView.setSelectedItemId(R.id.bottom_nav_home);
        }

        // Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment selectedFragment = null;
                int id = menuItem.getItemId();

                // Choosing fragment based on selected menu item
                if (id == R.id.bottom_nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (id == R.id.bottom_nav_list) {
                    selectedFragment = new ListFragment();
                } else if (id == R.id.bottom_nav_setting) {
                    selectedFragment = new settingsFragment();
                }

                // Replace current fragment if not null
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, selectedFragment).commit();
                }
                return true;
            }
        });
    }
}
