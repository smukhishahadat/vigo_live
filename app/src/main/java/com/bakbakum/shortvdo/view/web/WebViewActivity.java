package com.bakbakum.shortvdo.view.web;


import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.databinding.DataBindingUtil;

import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ActivityWebViewBinding;
import com.bakbakum.shortvdo.view.base.BaseActivity;

public class WebViewActivity extends BaseActivity {

    ActivityWebViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_web_view);
        int type = getIntent().getIntExtra("type", 0);
        String url = "";
        switch (type) {
            //Terms
            case 0:
                url = getString(R.string.terms_link);
                break;
            //Privacy
            case 1:
                url = getString(R.string.privacy_link);
                break;
            //Monetization coming soon
            case 2:
                url = getString(R.string.monetization_soon);
                break;
            //Help
            default:
                finish();
                break;
        }
        if (getIntent().hasExtra("url")) url = getIntent().getStringExtra("url");
        binding.imgBack.setOnClickListener(v -> onBackPressed());
        binding.webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
        binding.webview.getSettings().setJavaScriptEnabled(true);
        binding.webview.loadUrl(url);
    }
}