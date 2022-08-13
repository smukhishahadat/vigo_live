package com.bakbakum.shortvdo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheUtil;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;

public class CacheWorker extends JobIntentService {
    public static void enqueueWork(Context context, ArrayList<String> urls) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra("urls", urls);
        enqueueWork(context, CacheWorker.class, 1, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        ArrayList<String> urls = intent.getStringArrayListExtra("urls");
        for (String url : urls) {
            Uri dataUri = Uri.parse(url);
            DataSpec dataSpec = new DataSpec(dataUri, 0, 1024 * 1024, null);
            DataSource dataSource = new DefaultDataSourceFactory(this,
                    Util.getUserAgent(this, "tejash")).createDataSource();
            preloadVideo(dataSpec, ViloApplication.simpleCache, dataSource, new CacheUtil.ProgressListener() {
                @Override
                public void onProgress(long requestLength,
                                       long bytesCached, long newBytesCached) {
                    double downloadPercentage = (bytesCached * 100.0
                            / requestLength);
                    //Log.d("ViloApplication", "downloadPercentage: " + downloadPercentage);
                }
            });
        }
    }

    private void preloadVideo(DataSpec dataSpec, Cache cache,
                              DataSource upstream,
                              CacheUtil.ProgressListener progressListener) {
        //Log.d("ViloApplication", "Preloading called!");
        try {
            CacheUtil.cache(dataSpec, cache, CacheUtil.DEFAULT_CACHE_KEY_FACTORY,
                    upstream, progressListener, null);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
