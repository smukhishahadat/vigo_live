package com.bakbakum.shortvdo.view.search;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.adapter.SearchItemPagerAdapter;
import com.bakbakum.shortvdo.databinding.ActivitySearchBinding;
import com.bakbakum.shortvdo.view.base.BaseActivity;
import com.bakbakum.shortvdo.viewmodel.SearchActivityViewModel;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class SearchActivity extends BaseActivity {

    ActivitySearchBinding binding;
    SearchActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search);
        viewModel = ViewModelProviders.of(this).get(SearchActivityViewModel.class);
        initView();
        initListeners();
        binding.setViewmodel(viewModel);

    }


    private void initView() {

        viewModel.search_text = getIntent().getStringExtra("search");
        binding.etSearch.setText(viewModel.search_text);

        SearchItemPagerAdapter adapter = new SearchItemPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        binding.viewPager.setAdapter(adapter);


    }

    private void initListeners() {

        binding.imgBack.setOnClickListener(v -> onBackPressed());

        binding.tvVids.setOnClickListener(v -> {
            viewModel.searchtype.set(0);
            binding.viewPager.setCurrentItem(0);
        });

        binding.tvUsers.setOnClickListener(v -> {
            viewModel.searchtype.set(1);
            binding.viewPager.setCurrentItem(1);
        });
        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewModel.searchtype.set(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

}