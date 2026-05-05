package com.example.myapplication;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class WarrantyClaimAdminActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private final String[] statusTabs = {"Tất cả", "Chờ xử lý", "Đang xử lý", "Đã giải quyết", "Từ chối"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warranty_claim_admin);

        Toolbar toolbar = findViewById(R.id.toolbar_warranty_admin);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý yêu cầu bảo hành");
        }

        tabLayout = findViewById(R.id.tab_layout_warranty);
        viewPager = findViewById(R.id.view_pager_warranty);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public androidx.fragment.app.Fragment createFragment(int position) {
                return WarrantyClaimListFragment.newInstance(statusTabs[position]);
            }

            @Override
            public int getItemCount() {
                return statusTabs.length;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(statusTabs[position])).attach();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
