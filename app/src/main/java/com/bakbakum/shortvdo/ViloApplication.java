package com.bakbakum.shortvdo;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.danikula.videocache.HttpProxyCacheServer;
import com.facebook.FacebookSdk;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.utils.SessionManager;

import java.io.File;
import java.util.ArrayList;

import io.branch.referral.Branch;

import static android.content.ContentValues.TAG;


public class ViloApplication extends Application {

    public static SimpleCache simpleCache = null;
    public static LeastRecentlyUsedCacheEvictor leastRecentlyUsedCacheEvictor = null;
    public static ExoDatabaseProvider exoDatabaseProvider = null;
    public static Long exoPlayerCacheSize = (long) (90 * 1024 * 1024);

    private static HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy(Context context) {
        return proxy == null ? (proxy = newProxy(context)) : proxy;
    }

    private static HttpProxyCacheServer newProxy(Context ctx) {
        return new HttpProxyCacheServer.Builder(ctx)
                .maxCacheSize(1024 * 1024 * 1024)
                .maxCacheFilesCount(20)
                .build();
    }

    private SessionManager sessionManager;
    public SessionManager getSessionManager() {
        if (sessionManager == null)
            sessionManager = new SessionManager(this);
        return sessionManager;
    }

    private static ViloApplication instance;

    public static ViloApplication getInstance() {
        return instance;
    }

    private CacheDataSourceFactory cacheDataSourceFactory;
    public void cacheVideos(ArrayList<String> urls) {
        CacheWorker.enqueueWork(this, urls);
//        Log.e("ViloApplication", "Called");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Log.e("ViloApplication", "run()");
//                for (String url : urls) {
//                    Uri dataUri = Uri.parse(url);
//                    DataSpec dataSpec = new DataSpec(dataUri, 0, 500 * 1024, null);
//                    DataSource dataSource = new DefaultDataSourceFactory(ViloApplication.this,
//                            Util.getUserAgent(ViloApplication.this, "tejash")).createDataSource();
//                    preloadVideo(dataSpec, simpleCache, dataSource, new CacheUtil.ProgressListener() {
//                        @Override
//                        public void onProgress(long requestLength,
//                                               long bytesCached, long newBytesCached) {
//                            double downloadPercentage = (bytesCached * 100.0
//                                    / requestLength);
//                            Log.d("ViloApplication", "downloadPercentage: " + downloadPercentage);
//                        }
//                    });
//                }
//            }
//        });
    }


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Branch.getAutoInstance(this);
        AudienceNetworkAds.initialize(this);
        FacebookSdk.sdkInitialize(this);
        AppEventsLogger.activateApp(this);
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("notification", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        if (task.getResult() != null) {
                            String token = task.getResult().getToken();
                            Global.FIREBASE_DEVICE_TOKEN = token;
                            Log.d("notification", token);
                        }
                        // Log and toast

                    }
                });
        SessionManager sessionManager = new SessionManager(this);
        Global.ACCESS_TOKEN = sessionManager.getUser() != null ? sessionManager.getUser().getData().getToken() : "";
        Global.USER_ID = sessionManager.getUser() != null ? sessionManager.getUser().getData().getUserId() : "";
        if (leastRecentlyUsedCacheEvictor == null) {
            leastRecentlyUsedCacheEvictor = new LeastRecentlyUsedCacheEvictor(exoPlayerCacheSize);
        }

        if (exoDatabaseProvider != null) {
            exoDatabaseProvider = new ExoDatabaseProvider(this);
        }

        if (simpleCache == null) {
            simpleCache = new SimpleCache(getCacheDir(), leastRecentlyUsedCacheEvictor, exoDatabaseProvider);
            if (simpleCache.getCacheSpace() >= 400207768) {
                freeMemory();
            }
            Log.i(TAG, "onCreate: " + simpleCache.getCacheSpace());
        }
    }

    public void freeMemory() {

        try {
            File dir = getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    public boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }
}
