package com.bakbakum.shortvdo.view.base;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ItemLoginBinding;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.CustomDialogBuilder;
import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.utils.NetWorkChangeReceiver;
import com.bakbakum.shortvdo.utils.SessionManager;
import com.bakbakum.shortvdo.view.home.MainActivity;
import com.bakbakum.shortvdo.view.web.WebViewActivity;

import org.json.JSONException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.bakbakum.shortvdo.utils.Global.RC_SIGN_IN;


/**
 * Created by DeveloperAndroid on 06/05/2019.
 */
@SuppressLint("NewApi")
public abstract class BaseActivity extends AppCompatActivity {

    public SessionManager sessionManager = null;
    private NetWorkChangeReceiver netWorkChangeReceiver = null;
    private BottomSheetDialog dialog;
    private CompositeDisposable disposable = new CompositeDisposable();
    private FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            GraphRequest request = GraphRequest.newMeRequest(
                    loginResult.getAccessToken(), (object, response) -> {
                        try {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("device_token", Global.FIREBASE_DEVICE_TOKEN);
                            hashMap.put("user_email", object.getString("email"));
                            hashMap.put("full_name", object.getString("name"));
                            hashMap.put("login_type", Const.FACEBOOK_LOGIN);
                            hashMap.put("user_name", object.getString("id"));
                            hashMap.put("identity", object.getString("id"));
                            registerUser(hashMap);
                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "id,name,email,gender,birthday");
            request.setParameters(parameters);
            request.executeAsync();
        }

        @Override
        public void onCancel() {
        }

        @Override
        public void onError(FacebookException error) {
            Log.e("BaseActivity", "error: " + error.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        sessionManager = new SessionManager(this);
    }

    protected void startReceiver() {
        netWorkChangeReceiver = new NetWorkChangeReceiver(this::showHideInternet);
        registerNetworkBroadcastForNougat();
    }

    private void showHideInternet(Boolean isOnline) {
        final TextView tvInternetStatus = findViewById(R.id.tv_internet_status);

        if (isOnline) {
            if (tvInternetStatus != null && tvInternetStatus.getVisibility() == View.VISIBLE && tvInternetStatus.getText().toString().equalsIgnoreCase(getString(R.string.no_internet_connection))) {
                tvInternetStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.kellygreen));
                tvInternetStatus.setText(R.string.back_online);
                new Handler().postDelayed(() -> slideToBottom(tvInternetStatus), 200);
            }
        } else {
            if (tvInternetStatus != null) {
                tvInternetStatus.setBackgroundColor(ContextCompat.getColor(this, R.color.india_red));
                tvInternetStatus.setText(R.string.no_internet_connection);
                if (tvInternetStatus.getVisibility() == View.GONE) {
                    slideToTop(tvInternetStatus);
                }
            }
        }
    }

    private void slideToTop(View view) {
        TranslateAnimation animation = new TranslateAnimation(0f, 0f, view.getHeight(), 0f);
        animation.setDuration(300);
        view.startAnimation(animation);
        view.setVisibility(View.VISIBLE);
    }

    private void slideToBottom(final View view) {
        TranslateAnimation animation = new TranslateAnimation(0f, 0f, 0f, view.getHeight());
        animation.setDuration(300);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(animation);
    }

    private void registerNetworkBroadcastForNougat() {
        registerReceiver(netWorkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    protected void unregisterNetworkChanges() {
        try {
            unregisterReceiver(netWorkChangeReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    /**
     * Hide keyboard when user click anywhere on screen
     *
     * @param event contains int value for motion event actions
     * @return boolean value of touch event.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        closeKeyboard();
        return true;
    }

    public void closeKeyboard() {
        try {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (im != null) {
                if (getCurrentFocus() != null) {
                    im.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
            }
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void openActivity(Activity activity) {
        startActivity(new Intent(this, activity.getClass()));
    }
    CallbackManager callbackManager;
    public void initFaceBook() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, mFacebookCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        callbackManager.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    public static void makeLinks(TextView textView, String completeString, String partToClick, View.OnClickListener link) {
        SpannableString spannableString = new SpannableString(textView.getText());
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                (link).onClick(textView);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
            }
        };


        int startPosition = completeString.indexOf(partToClick);
        int endPosition = completeString.lastIndexOf(partToClick) + partToClick.length();

        spannableString.setSpan(clickableSpan, startPosition, endPosition,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD),startPosition,endPosition,0);
        spannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(textView.getContext(), R.color.blue)),
                startPosition, endPosition, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        textView.setText(spannableString);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void initLogin(Context context, OnLoginSheetClose close) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);

        dialog = new BottomSheetDialog(context);

        ItemLoginBinding loginBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_login, null, false);
        dialog.setContentView(loginBinding.getRoot());
        dialog.setCanceledOnTouchOutside(false);
        dialog.setDismissWithAnimation(true);

        makeLinks(loginBinding.terms, getString(R.string.continue_privacy_policy), "Terms of Use", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent web = new Intent(BaseActivity.this, WebViewActivity.class);
                web.putExtra("url", getString(R.string.terms_link));
                startActivity(web);
            }
        });

        makeLinks(loginBinding.terms, getString(R.string.continue_privacy_policy), "Privacy Policy", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent web = new Intent(BaseActivity.this, WebViewActivity.class);
                web.putExtra("url", getString(R.string.privacy_link));
                startActivity(web);
            }
        });

        FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        }
//        loginBinding.tvTerm.setOnClickListener(v -> startActivity(new Intent(this, WebViewActivity.class).putExtra("type", 0)));
//        loginBinding.tvPrivacy.setOnClickListener(v -> startActivity(new Intent(this, WebViewActivity.class).putExtra("type", 1)));
        loginBinding.setOnGoogleClick(v -> new CustomDialogBuilder(this).showRequestTermDialogue(new CustomDialogBuilder.OnDismissListener() {
            @Override
            public void onPositiveDismiss() {

                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                ((MainActivity) context).startActivityForResult(signInIntent, RC_SIGN_IN);
            }

            @Override
            public void onNegativeDismiss() {
                dismissBottom();
            }
        }));

        //Twitter
        loginBinding.setOnTwitterClick(v ->{
            Toast.makeText(this, "FBCS", Toast.LENGTH_SHORT).show();
        });

        loginBinding.setOnFacebookClick(v -> {

            LoginManager.getInstance().logInWithReadPermissions((MainActivity) context, Collections.singletonList("public_profile"));

            LoginManager.getInstance().logInWithReadPermissions(
                    (MainActivity) context,
                    Arrays.asList("user_photos", "email", "user_birthday", "public_profile")
            );
        });

        loginBinding.setOnCloseClick(v -> dismissBottom());

        dialog.setOnDismissListener(view -> close.onClose());

        dialog.show();

    }

    public void dismissBottom() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    public void registerUser(HashMap<String, Object> hashMap) {
        disposable.add(Global.initRetrofit().registrationUser(hashMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe((user, throwable) -> {
                    if (user != null && user.getStatus()) {
                        sessionManager.saveBooleanValue(Const.IS_LOGIN, true);
                        sessionManager.saveUser(user);
                        Global.ACCESS_TOKEN = sessionManager.getUser().getData() != null ? sessionManager.getUser().getData().getToken() : "";
                        Global.USER_ID = sessionManager.getUser().getData() != null ? sessionManager.getUser().getData().getUserId() : "";
                        dismissBottom();
                        Toast.makeText(this, "Login SuccessFully", Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    public void setStatusBarTransparentFlag() {
/*
        View decorView = getWindow().getDecorView();
        decorView.setOnApplyWindowInsetsListener((v, insets) -> {
            WindowInsets defaultInsets = v.onApplyWindowInsets(insets);
            return defaultInsets.replaceSystemWindowInsets(
                    defaultInsets.getSystemWindowInsetLeft(),
                    0,
                    defaultInsets.getSystemWindowInsetRight(),
                    defaultInsets.getSystemWindowInsetBottom());
        });
        ViewCompat.requestApplyInsets(decorView);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.transparent));*/
        View decorView = getWindow().getDecorView();
        decorView.setOnApplyWindowInsetsListener((v, insets) -> {
            WindowInsets defaultInsets = v.onApplyWindowInsets(insets);
            return defaultInsets.replaceSystemWindowInsets(
                    defaultInsets.getSystemWindowInsetLeft(),
                    defaultInsets.getSystemWindowInsetTop(),
                    defaultInsets.getSystemWindowInsetRight(),
                    defaultInsets.getSystemWindowInsetBottom());
        });
        ViewCompat.requestApplyInsets(decorView);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }

    public void removeStatusBarTransparentFlag() {
        View decorView = getWindow().getDecorView();
        decorView.setOnApplyWindowInsetsListener((v, insets) -> {
            WindowInsets defaultInsets = v.onApplyWindowInsets(insets);
            return defaultInsets.replaceSystemWindowInsets(
                    defaultInsets.getSystemWindowInsetLeft(),
                    defaultInsets.getSystemWindowInsetTop(),
                    defaultInsets.getSystemWindowInsetRight(),
                    defaultInsets.getSystemWindowInsetBottom());
        });
        ViewCompat.requestApplyInsets(decorView);
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
    }

    public interface OnLoginSheetClose {
        void onClose();
    }
}