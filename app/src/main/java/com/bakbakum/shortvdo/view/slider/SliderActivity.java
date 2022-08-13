package com.bakbakum.shortvdo.view.slider;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;

import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ActivitySliderBinding;
import com.bakbakum.shortvdo.view.base.BaseActivity;
import com.bakbakum.shortvdo.view.home.MainActivity;

public class SliderActivity extends BaseActivity {

    ActivitySliderBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_slider);
        initListener();
    }

    private void initListener() {
        binding.tvSkip.setOnClickListener(v -> {
            openActivity(new MainActivity());
            finish();
        });
    }
}
