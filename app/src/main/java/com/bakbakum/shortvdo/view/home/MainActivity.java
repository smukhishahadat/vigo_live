package com.bakbakum.shortvdo.view.home;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.AppUpdaterUtils;
import com.github.javiersantos.appupdater.enums.AppUpdaterError;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.github.javiersantos.appupdater.objects.Update;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ActivityMainBinding;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.utils.GlobalApi;
import com.bakbakum.shortvdo.view.base.BaseActivity;
import com.bakbakum.shortvdo.view.profile.ProfileFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import static com.bakbakum.shortvdo.utils.Global.RC_SIGN_IN;

public class MainActivity extends BaseActivity {

    private static int CAMERA = 101;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
    private ActivityMainBinding binding;
   // private MainViewModel viewModel;
//    private TimerTask timerTask;
    private String userid = "";
    private ProfileFragment fragment;

    private Context mContext;


    public void updateProfile(String userid) {
        if (!this.userid.equals(userid)) {
            if (fragment != null)
                adapter.getFragments().remove(fragment);
            fragment = new ProfileFragment();
            Bundle bundle = new Bundle();
            bundle.putString("userid", userid);
            fragment.setArguments(bundle);
            adapter.getFragments().add(fragment);
            this.userid = userid;
            binding.viewPager.post(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyItemChanged(1);
                }
            });
        }
    }

    public void enableScroll(boolean b) {
        binding.viewPager.setUserInputEnabled(b);
    }

    private class HomeActivityStateAdapter extends FragmentStateAdapter {

        ArrayList<Fragment> fragments = new ArrayList<>();

        public ArrayList<Fragment> getFragments() {
            return fragments;
        }

        public HomeActivityStateAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }

    }
    HomeActivityStateAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarTransparentFlag();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        //viewModel = ViewModelProviders.of(this, new ViewModelFactory(new MainViewModel()).createFor()).get(MainViewModel.class);
        startReceiver();

        AppUpdater appUpdater = new AppUpdater(this);
        appUpdater.start();

        this.mContext = this;

        new AppUpdater(mContext)
                .setUpdateFrom(UpdateFrom.GOOGLE_PLAY)
                .setTitleOnUpdateAvailable("Update available")
                .setDisplay(Display.DIALOG)
                .setButtonUpdate("Update now?")
                .setIcon(R.drawable.ic_download)
//              .showAppUpdated(true)
                .setContentOnUpdateAvailable("Check out the latest version available!")
                .start();

        //initView();
        //initTabLayout();
        initFaceBook();
        rewardDailyCheckIn();
        Log.d("TOKEN", "onCreate: " + Global.FIREBASE_DEVICE_TOKEN);
       // binding.setViewModel(viewModel);

        adapter = new HomeActivityStateAdapter(this);
        adapter.getFragments().add(new MainHomeFragment());
        binding.viewPager.setAdapter(adapter);
        //binding.viewPager.setUserInputEnabled(false);

        copyAssets();
    }

    private AsyncTask<Void, Void, Void> copyAssetsTask;
    private void copyAssets() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                copyAssetsTask = new CopyAssetsTask(MainActivity.this).executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }, 700);
    }

    public static class CopyAssetsTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<MainActivity> weakReference;
        CopyAssetsTask(MainActivity activity) {
            weakReference = new WeakReference<>(activity);
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            MainActivity activity = weakReference.get();
            if (activity != null) {
                copyIcons(activity);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private static String getExtFileDir(Context cxt) {
        return cxt.getExternalFilesDir("") + File.separator;
    }

    private static String SD_DIR ;
    public final static String RACE_NAME = "icon";
    private static String RACE_DIR;
    static public void copyIcons(Context cxt) {
        SD_DIR = getExtFileDir(cxt);
        RACE_DIR = SD_DIR + RACE_NAME + File.separator;
        File dir = new File(RACE_DIR);
        copySelf(cxt, RACE_NAME);
        dir.mkdirs();
    }

    private static void copySelf(Context cxt, String root) {
        try {
            String[] files = cxt.getAssets().list(root);
            if (files.length > 0) {
                File subdir = new File(SD_DIR + root);
                if (!subdir.exists()) {
                    subdir.mkdirs();
                }
                for (String fileName : files) {
                    if (new File(SD_DIR + root + File.separator + fileName).exists()) {
                        continue;
                    }
                    copySelf(cxt, root + "/" + fileName);
                }
            } else {
                Log.d("MainActivity", "copy...." + SD_DIR + root);
                OutputStream myOutput = new FileOutputStream(SD_DIR + root);
                InputStream myInput = cxt.getAssets().open(root);
                byte[] buffer = new byte[1024 * 8];
                int length = myInput.read(buffer);
                while (length > 0) {
                    myOutput.write(buffer, 0, length);
                    length = myInput.read(buffer);
                }

                myOutput.flush();
                myInput.close();
                myOutput.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Log.e("onActivityResult", "onActivityResult: start" );
            handleSignInResult(task);
        } else if (resultCode == RESULT_OK && requestCode == CAMERA) {
//            TabLayout.Tab tab = binding.tabLout.getTabAt(0);
//            if (tab != null) {
//                tab.select();
//            }
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        //Log.e("Google login", "handleSignInResult: start" );
//        HashMap<String, Object> hashMap1 = new HashMap<>();
//        hashMap1.put("device_token", Global.FIREBASE_DEVICE_TOKEN);
//        hashMap1.put("user_email", "nazmus.shahadat12@gmail.com");
//        hashMap1.put("full_name", "Nazmus Shahadat");
//        hashMap1.put("login_type", Const.GOOGLE_LOGIN);
//        hashMap1.put("user_name", Objects.requireNonNull("nazmus.shahadat12"));
//        hashMap1.put("identity", "nazmus.shahadat12@gmail.com");
//        registerUser(hashMap1);
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                Log.e("Google login", "handleSignInResult: account not null" );
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("device_token", Global.FIREBASE_DEVICE_TOKEN);
                hashMap.put("user_email", account.getEmail());
                hashMap.put("full_name", account.getDisplayName());
                hashMap.put("login_type", Const.GOOGLE_LOGIN);
                hashMap.put("user_name", Objects.requireNonNull(account.getEmail()).split("@")[0]);
                hashMap.put("identity", account.getEmail());
                registerUser(hashMap);
                Log.e("Google login", "handleSignInResult: " + account.getEmail());
            }
            else {
                Log.e("Google login", "handleSignInResult: account not null" );
            }
            // Signed in successfully, show authenticated UI.

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Google login", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void rewardDailyCheckIn() {
        if (sessionManager.getStringValue("intime").isEmpty()) {
            new GlobalApi().rewardUser("2");
            sessionManager.saveStringValue("intime", new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date()));
        } else {
            try {
                simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
                Date date1 = simpleDateFormat.parse(sessionManager.getStringValue("intime"));
                Date date2 = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
                long difference = 0;
                if (date2 != null) {
                    if (date1 != null) {
                        difference = date2.getTime() - date1.getTime();
                    }
                }
                int days = (int) (difference / (1000 * 60 * 60 * 24));
                int hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                int min = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
                hours = (hours < 0 ? -hours : hours);
                if (hours >= 24) {
                    new GlobalApi().rewardUser("2");
                    sessionManager.saveStringValue("intime", new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date()));

                }
                Log.i("======= Hours", " :: " + hours + ":" + min);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    protected void onPause() {
//        if (timerTask != null) {
//            timerTask.stopTimerTask();
//        }
        super.onPause();
    }

    @Override
    protected void onStop() {
//        viewModel.onStop.setValue(true);
//        if (timerTask != null) {
//            timerTask.stopTimerTask();
//        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkChanges();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        long size = 0;
        File[] files = getExternalCacheDir().listFiles();
        if (files != null) {
            for (File f : files) {
                size = size + f.length();
            }
        }
        super.onResume();
    }



    @Override
    public void onBackPressed() {
        if (binding.viewPager.getCurrentItem() == 1) {
            binding.viewPager.setCurrentItem(0);
            return;
        }
//        if (viewModel.selectedPosition.getValue() != null && viewModel.selectedPosition.getValue() != 0) {
//            TabLayout.Tab tab = binding.tabLout.getTabAt(0);
//            if (tab != null) {
//                tab.select();
//            }
//            viewModel.selectedPosition.setValue(0);
//        } else if (viewModel.selectedPosition.getValue() != null && viewModel.selectedPosition.getValue() == 0 && !viewModel.isBack) {
//            viewModel.isBack = true;
//            Toast.makeText(this, "Press Again To Exit", Toast.LENGTH_SHORT).show();
//            new Handler().postDelayed(() -> viewModel.isBack = false, 2000);
//        } else {
//            super.onBackPressed();
//        }
        super.onBackPressed();
    }
}
