package com.bakbakum.shortvdo.view.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.gson.Gson;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ActivitySettingsBinding;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.CustomDialogBuilder;
import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.view.SplashActivity;
import com.bakbakum.shortvdo.view.base.BaseActivity;
import com.bakbakum.shortvdo.view.wallet.WalletActivity;
import com.bakbakum.shortvdo.view.web.WebViewActivity;
import com.bakbakum.shortvdo.viewmodel.SettingsActivityViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import java.util.Calendar;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;

public class SettingsActivity extends BaseActivity {

    ActivitySettingsBinding binding;
    SettingsActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new SettingsActivityViewModel()).createFor()).get(SettingsActivityViewModel.class);
        initListeners();
        initObserve();

    }


    private void initListeners() {

        binding.lnrHelp.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, WebViewActivity.class);
            intent.putExtra("type", 2);
            startActivity(intent);
        });

        binding.lnrPrivacyPolicies.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, WebViewActivity.class);
            intent.putExtra("type", 1);
            startActivity(intent);
        });


        binding.lnrMonetizeSoon.setOnClickListener(view -> {
            Intent intent = new Intent(SettingsActivity.this, WebViewActivity.class);
            intent.putExtra("type", 2);
            startActivity(intent);
        });

        binding.lnrTerm.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, WebViewActivity.class);
            intent.putExtra("type", 0);
            startActivity(intent);
        });

        binding.developerSupport.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, WebViewActivity.class);
            intent.putExtra("url", "...");
            startActivity(intent);
        });

        if (sessionManager.getUser() != null && sessionManager.getUser().getData() != null && sessionManager.getUser().getData().isVerified()) {
            binding.loutVerify.setVisibility(View.GONE);
        } else {
            binding.loutVerify.setVisibility(View.VISIBLE);
        }
        binding.loutShareProfile.setOnClickListener(v -> shareProfile());
        binding.imgBack.setOnClickListener(v -> onBackPressed());
        binding.loutWallet.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, WalletActivity.class)));
        binding.loutVerify.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, VerificationActivity.class)));
        binding.loutMycode.setOnClickListener(v -> startActivity(new Intent(SettingsActivity.this, MyQRActivity.class)));

        binding.loutLogout.setOnClickListener(v -> new CustomDialogBuilder(this).showSimpleDialog("Log out", "Do you really want\nto log out?", "Cancel", "Log out", new CustomDialogBuilder.OnDismissListener() {
            @Override
            public void onPositiveDismiss() {

                viewModel.logOutUser();
            }

            @Override
            public void onNegativeDismiss() {

            }
        }));
    }

    private void shareProfile() {

        String json = new Gson().toJson(sessionManager.getUser());
        String title = sessionManager.getUser().getData().getFullName();

        Log.i("ShareJson", "Json Object: " + json);
        BranchUniversalObject buo = new BranchUniversalObject()
                .setCanonicalIdentifier("content/12345")
                .setTitle(title)
                .setContentImageUrl(Const.ITEM_BASE_URL + sessionManager.getUser().getData().getUserProfile())
                .setContentDescription("Hey There, Check This %s Profile".replace("%s", getString(R.string.app_name)))
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(new ContentMetadata().addCustomMetadata("data", json));

        LinkProperties lp = new LinkProperties()
                .setFeature("sharing")
                .setCampaign("Content launch")
                .setStage("User")
                .addControlParameter("custom", "data")
                .addControlParameter("custom_random", Long.toString(Calendar.getInstance().getTimeInMillis()));

        String url = "https://play.google.com/store/apps/details?id=%playstore".replace("%playstore", getApplicationContext().getPackageName());
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        String shareBody = url + "\nHey There, Check This %s Profile".replace("%s", getString(R.string.app_name));
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Profile Share");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(intent, "Share Profile"));

    }

    private void initObserve() {
        viewModel.logOut.observe(this, logout -> logOutUser());
    }

    private void logOutUser() {
        if (sessionManager.getUser().getData().getLoginType().equals("google")) {
            GoogleSignInOptions gso = new GoogleSignInOptions.
                    Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                    build();

            GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(SettingsActivity.this, gso);
            googleSignInClient.signOut();

        } else {
            LoginManager.getInstance().logOut();
        }

        sessionManager.clear();
        Global.ACCESS_TOKEN = "";
        Global.USER_ID = "";
        startActivity(new Intent(SettingsActivity.this, SplashActivity.class));
        finishAffinity();
    }
}