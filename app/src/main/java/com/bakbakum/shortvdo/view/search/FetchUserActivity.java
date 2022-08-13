package com.bakbakum.shortvdo.view.search;

import android.os.Bundle;

import androidx.databinding.DataBindingUtil;

import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ActivityFetchUserBinding;
import com.bakbakum.shortvdo.view.base.BaseActivity;
import com.bakbakum.shortvdo.view.profile.ProfileFragment;

public class FetchUserActivity extends BaseActivity {


    ActivityFetchUserBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fetch_user);

        String userid = getIntent().getStringExtra("userid");

        ProfileFragment fragment = new ProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("userid", userid);
        fragment.setArguments(bundle);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.lout_main, fragment)
                .commit();

    }

}