package com.bakbakum.shortvdo.view.wallet;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.CustomToastBinding;
import com.bakbakum.shortvdo.databinding.FragmentPurchaseCoinSheetBinding;
import com.bakbakum.shortvdo.utils.CustomDialogBuilder;
import com.bakbakum.shortvdo.viewmodel.CoinPurchaseViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import org.jetbrains.annotations.NotNull;


public class CoinPurchaseSheetFragment extends BottomSheetDialogFragment {

    FragmentPurchaseCoinSheetBinding binding;
    CoinPurchaseViewModel viewModel;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialog1;
            dialog.setCanceledOnTouchOutside(true);

        });

        return bottomSheetDialog;

    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_purchase_coin_sheet, container, false);

        return binding.getRoot();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new CoinPurchaseViewModel()).createFor()).get(CoinPurchaseViewModel.class);

        initView();
        initListeners();
        initObserve();
        binding.setViewmodel(viewModel);
    }

    private void initView() {
        viewModel.fetchCoinPlans();
    }


    private void initListeners() {
        viewModel.adapter.onRecyclerViewItemClick = (data, position) -> {
            new CustomDialogBuilder(getActivity()).showSimpleDialog("Attention !", "buy coins", "OK", "Contact Us", new CustomDialogBuilder.OnDismissListener() {
                @Override
                public void onPositiveDismiss() {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://bakbakum.app/coins"));
                        startActivity(intent);
                    } catch (Exception ignored) {

                    }
                }

                @Override
                public void onNegativeDismiss() {
                    Toast.makeText(getActivity(), "Thanks for taking interest..", Toast.LENGTH_SHORT).show();
                }
            });
            //viewModel.purchaseCoin(data.getCoinAmount());
        };
    }

    private void initObserve() {
        viewModel.purchase.observe(this, purchase -> showPurchaseResultToast(purchase.getStatus()));
    }

    private void showPurchaseResultToast(Boolean status) {

        dismiss();
        CustomToastBinding binding;
        binding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.custom_toast, null, false);
        binding.setStatus(status);
        if (status) {
            binding.tvToastMessage.setText("Coins Added To Your Wallet\nSuccessfully..");
        } else {
            String string = "Something Went Wrong !";
            binding.tvToastMessage.setText(string);
        }
        Toast toast = new Toast(getContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(binding.getRoot());
        toast.show();
    }


}