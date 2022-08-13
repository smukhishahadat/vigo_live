package com.bakbakum.shortvdo.view.wallet;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ActivityRedeemBinding;
import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.view.base.BaseActivity;
import com.bakbakum.shortvdo.view.home.MainActivity;
import com.bakbakum.shortvdo.viewmodel.RedeemViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

public class RedeemActivity extends BaseActivity {

    ActivityRedeemBinding binding;
    RedeemViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_redeem);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new RedeemViewModel()).createFor()).get(RedeemViewModel.class);

        initView();
        initListeners();
        initObserve();
        binding.setViewmodel(viewModel);
    }

    private void initView() {
        if (getIntent().getStringExtra("coins") != null) {
            viewModel.coindCount = getIntent().getStringExtra("coins");
            viewModel.coinRate = getIntent().getStringExtra("coinrate");
            binding.tvCount.setText(Global.prettyCount(Integer.parseInt(viewModel.coindCount)));
        }
    }


    private void initListeners() {
        String[] paymentTypes = getResources().getStringArray(R.array.payment);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.color_text_light));
                // ((TextView) parent.getChildAt(0)).setTextSize(5);
                viewModel.requestType = paymentTypes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binding.btnRedeem.setOnClickListener(v -> {
            if (viewModel.accountId != null && !TextUtils.isEmpty(viewModel.accountId)) {
                viewModel.callApiToRedeem();
            } else {
                Toast.makeText(this, "Please enter your account ID", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initObserve() {
        viewModel.redeem.observe(this, redeem -> {
            if (redeem.getStatus()) {
                Toast.makeText(this, "Request Submitted Successfully", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finishAffinity();
            }
        });
    }


}