    package com.bakbakum.shortvdo.view.wallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ActivityWalletBinding;
import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.view.base.BaseActivity;
import com.bakbakum.shortvdo.viewmodel.WalletViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

public class WalletActivity extends BaseActivity {

    ActivityWalletBinding binding;
    WalletViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new WalletViewModel()).createFor()).get(WalletViewModel.class);

        initView();
        initListeners();
        initObserve();

    }


    private void initView() {

        viewModel.fetchMyWallet();
        viewModel.fetchCoinRate();
        viewModel.fetchRewardingActions();
    }

    private void initListeners() {

        binding.imgBack.setOnClickListener(v -> onBackPressed());
        binding.setOnMoreClick(v -> showBuyBottomSheet());
        binding.setOnRedeemClick(v -> {
            if (viewModel.myWallet.getValue() != null) {
                if (Integer.parseInt(viewModel.myWallet.getValue().getData().getMyWallet()) > 100) {
                    Intent intent = new Intent(this, RedeemActivity.class);
                    intent.putExtra("coins", viewModel.myWallet.getValue().getData().getMyWallet());
                    intent.putExtra("coinrate", viewModel.coinRate.getValue() != null ? viewModel.coinRate.getValue().getData().getUsdRate() : "0");
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "You can redeem minimum 100 ".concat(getString(R.string.coins)), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void initObserve() {
        viewModel.myWallet.observe(this, wallet -> {
            binding.tvCount.setText(Global.prettyCount(Integer.parseInt(wallet.getData().getMyWallet())));
            binding.tvDailyCheckin.setText(Global.prettyCount(Integer.parseInt(wallet.getData().getCheckIn())));
            binding.tvFromYourFans.setText(Global.prettyCount(Integer.parseInt(wallet.getData().getFromFans())));
            binding.tvPurchased.setText(Global.prettyCount(Integer.parseInt(wallet.getData().getPurchased())));
            binding.tvTimeSpent.setText(Global.prettyCount(Integer.parseInt(wallet.getData().getSpenInApp())));
            binding.tvVideoUpload.setText(Global.prettyCount(Integer.parseInt(wallet.getData().getUploadVideo())));
            binding.tvTotalEarning.setText(Global.prettyCount(Integer.parseInt(wallet.getData().getTotalReceived())));
            binding.tvTotalSpending.setText(Global.prettyCount(Integer.parseInt(wallet.getData().getTotalSend())));
        });
        viewModel.coinRate.observe(this, coinRate -> binding.tvCoinRate.setText("1 Coin = ".concat(coinRate.getData().getUsdRate() + " USD")));
        viewModel.rewardingActions.observe(this, rewardingActions -> {
            binding.tvEvery10Reward.setText(viewModel.rewardingActionsList.get(0).getCoin());
            binding.tvDailyReward.setText(viewModel.rewardingActionsList.get(1).getCoin());
            binding.tvUploadReward.setText(viewModel.rewardingActionsList.get(2).getCoin());
        });
    }


    private void showBuyBottomSheet() {
        CoinPurchaseSheetFragment fragment = new CoinPurchaseSheetFragment();
        fragment.show(getSupportFragmentManager(), fragment.getClass().getSimpleName());
    }
}