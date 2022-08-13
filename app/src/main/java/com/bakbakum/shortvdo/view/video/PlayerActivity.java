package com.bakbakum.shortvdo.view.video;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.aliyun.svideo.recorder.activity.AlivcSvideoMixRecordActivity;
import com.aliyun.svideo.recorder.bean.RenderingMode;
import com.aliyun.svideo.sdk.external.struct.common.VideoDisplayMode;
import com.aliyun.svideo.sdk.external.struct.common.VideoQuality;
import com.aliyun.svideo.sdk.external.struct.encoder.VideoCodecs;
import com.aliyun.svideo.sdk.external.struct.snap.AliyunSnapVideoParam;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdListener;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.adapter.VideoFullAdapter;
import com.bakbakum.shortvdo.databinding.ActivityPlayerBinding;
import com.bakbakum.shortvdo.databinding.ItemVideoListBinding;
import com.bakbakum.shortvdo.model.videos.Video;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.CustomDialogBuilder;
import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.utils.GlobalApi;
import com.bakbakum.shortvdo.utils.customViewPager2.widget.ViewPager2;
import com.bakbakum.shortvdo.view.base.BaseActivity;
import com.bakbakum.shortvdo.view.home.CommentSheetFragment;
import com.bakbakum.shortvdo.view.home.MainActivity;
import com.bakbakum.shortvdo.view.home.ReportSheetFragment;
import com.bakbakum.shortvdo.view.home.SoundVideosActivity;
import com.bakbakum.shortvdo.view.search.FetchUserActivity;
import com.bakbakum.shortvdo.view.share.ShareSheetFragment;
import com.bakbakum.shortvdo.view.wallet.CoinPurchaseSheetFragment;
import com.bakbakum.shortvdo.viewmodel.VideoPlayerViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import java.io.File;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;
import static com.bakbakum.shortvdo.ViloApplication.simpleCache;

public class PlayerActivity extends BaseActivity implements Player.EventListener {

    private ActivityPlayerBinding binding;
    private VideoPlayerViewModel viewModel;
    private CustomDialogBuilder customDialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarTransparentFlag();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_player);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new VideoPlayerViewModel()).createFor()).get(VideoPlayerViewModel.class);
        viewModel.adapter.onRecyclerViewItemClick = new VideoFullAdapter.OnRecyclerViewItemClick() {
            @Override
            public void onItemClick(Video.Data model, int position, int type, ItemVideoListBinding binding) {
                switch (type) {
                    // Send to FetchUser Activity
                    case 1:
                        Intent intent = new Intent(PlayerActivity.this, FetchUserActivity.class);
                        intent.putExtra("userid", model.getUserId());
                        startActivity(intent);
                        break;
                    // Send Bubble to creator
                    case 3:
                        if (!Global.ACCESS_TOKEN.isEmpty()) {
                            showSendBubblePopUp(model.getUserId());
                        } else {
                            if (!Global.ACCESS_TOKEN.isEmpty()) {
                                showSendBubblePopUp(model.getUserId());
                            }
                        }
                        break;
                    // On like btn click
                    case 4:
                        if (!Global.ACCESS_TOKEN.isEmpty()) {
                            viewModel.likeUnlikePost(model.getPostId());
                        }
                        break;
                    // On Comment Click
                    case 5:
                        CommentSheetFragment fragment = new CommentSheetFragment();
                        fragment.onDismissListener = count -> {
                            model.setPostCommentsCount(count);
                            binding.tvCommentCount.setText(Global.prettyCount(count));

                        };
                        Bundle args = new Bundle();
                        args.putString("postid", model.getPostId());
                        args.putInt("commentCount", model.getPostCommentsCount());
                        args.putBoolean("allow_comments", model.getAllow_comments() == 1);
                        fragment.setArguments(args);
                        fragment.show(getSupportFragmentManager(), fragment.getClass().getSimpleName());
                        break;
                    // On Share Click
                    case 6:
                        handleShareClick(model);
                        break;
                    // On Sound Disk Click
                    case 7:
                        if (Global.ACCESS_TOKEN.isEmpty()) {
                            initLogin(PlayerActivity.this, () -> viewModel.likeUnlikePost(model.getPostId()));

                        } else {
                            Intent intent1 = new Intent(PlayerActivity.this, SoundVideosActivity.class);
                            intent1.putExtra("soundid", model.getSoundId());
                            intent1.putExtra("sound", model.getSound());
                            startActivity(intent1);
                        }
                        break;
                    // On Long Click (Report Video)
                    case 8:
                        new CustomDialogBuilder(PlayerActivity.this).showSimpleDialog("Report this post", "Are you sure you want to\nreport this post?", "Cancel", "Yes, Report", new CustomDialogBuilder.OnDismissListener() {
                            @Override
                            public void onPositiveDismiss() {
                                reportPost(model);
                            }

                            @Override
                            public void onNegativeDismiss() {

                            }
                        });

                        break;
                        case 9:
                        lastPosition = position;
                        playVideo(Const.ITEM_BASE_URL + model.getPostVideo(), binding);
                        break;
                    case 10:

                        final TextView percent = customDialogBuilder.showLoadingDialogWithPercentage();
                        PRDownloader.download(Const.ITEM_BASE_URL + model.getPostVideo(), Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), model.getPostVideo())
                                .build()
                                .setOnProgressListener(new OnProgressListener() {
                                    @Override
                                    public void onProgress(Progress progress) {
                                        int p = (int) ((progress.currentBytes / (float) progress.totalBytes) * 100);
                                        percent.setText(p + "%");
                                    }
                                })
                                .start(new OnDownloadListener() {
                                    @Override
                                    public void onDownloadComplete() {
                                        customDialogBuilder.hideLoadingDialog();
                                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(), model.getPostVideo());
                                        startDuetRecording(file.getPath());
                                        Toast.makeText(PlayerActivity.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onError(Error error) {
                                        customDialogBuilder.hideLoadingDialog();
                                        Log.d("DOWNLOAD", "onError: " + error.getConnectionException().getMessage());
                                    }
                                });
                }
            }

            @Override
            public void onHashTagClick(String hashTag) { }

            @Override
            public void onDoubleClick(Video.Data model, MotionEvent event, ItemVideoListBinding binding) { }
        };
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    int position = binding.viewPager.getCurrentItem();
                    if (position != -1 && lastPosition != position) {
                        if (viewModel.adapter.mList.get(position) != null) {
                            Animation animation = AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.slow_rotate);
                            if (binding.viewPager.getLayoutManager() != null) {
                                View view = binding.viewPager.getLayoutManager().findViewByPosition(position);
                                if (view != null) {
                                    lastPosition = position;
                                    ItemVideoListBinding binding1 = DataBindingUtil.bind(view);
                                    if (binding1 != null) {
                                        binding1.imgSound.startAnimation(animation);
                                        new GlobalApi().increaseView(binding1.getModel().getPostId());
                                        playVideo(Const.ITEM_BASE_URL + viewModel.adapter.mList.get(position).getPostVideo(), binding1);
                                    }
                                }
                            }
                        } else {
                            if (player != null) {
                                player.setPlayWhenReady(false);
                                player.stop();
                                player.release();
                                player = null;
                                lastPosition = position;
                            }
                        }
                    }
                }
            }
        });
        binding.viewPager.setAdapter(viewModel.adapter);
        binding.viewPager.setOffscreenPageLimit(3);
        customDialogBuilder = new CustomDialogBuilder(this);
        initAds();
        initListener();
        initIntent();
        initObserver();
        binding.setViewModel(viewModel);
    }
    public int lastPosition = -1;

    private void initObserver() {
        viewModel.onCommentSuccess.observe(this, isSuccess -> {
            if (isSuccess != null && isSuccess) {
//                playerBinding.getModel().setPostCommentsCount(playerBinding.getModel().getPostCommentsCount() + 1);
//                playerBinding.tvCommentCount.setText(Global.prettyCount(playerBinding.getModel().getPostCommentsCount()));
                binding.etComment.setText("");
                closeKeyboard();
            }
        });
        viewModel.onLoadMoreComplete.observe(this, onLoadMore -> binding.refreshlout.finishLoadMore());
        viewModel.coinSend.observe(this, coinSend -> showSendResult(coinSend.getStatus()));
    }

    private void initListener() {
        binding.imgBack.setOnClickListener(v -> {
            if (viewModel.type == 5) {
                startActivity(new Intent(this, MainActivity.class));
                finishAffinity();
            } else {
                onBackPressed();
            }
        });
        binding.refreshlout.setEnableRefresh(false);
        binding.refreshlout.setOnLoadMoreListener(refreshLayout -> viewModel.onLoadMore());
    }

    public void startDuetRecording(String filePath){
        int gop = 30;
        AliyunSnapVideoParam mAliyunSnapVideoParam = new AliyunSnapVideoParam.Builder()
                .setResulutionMode(AliyunSnapVideoParam.RESOLUTION_360P)
                .setRatioMode(AliyunSnapVideoParam.RATIO_MODE_9_16)
                .setGop(gop)
                .setVideoCodec(VideoCodecs.H264_SOFT_FFMPEG)
                .setFrameRate(30)
                .setCropMode(VideoDisplayMode.FILL)
                .setVideoQuality(VideoQuality.HD)
                .build();
        AlivcSvideoMixRecordActivity.startMixRecord(this, mAliyunSnapVideoParam, filePath,
                RenderingMode.FaceUnity, false);
    }

    private void handleShareClick(Video.Data model) {
        ShareSheetFragment fragment = new ShareSheetFragment();
        Bundle args = new Bundle();
        args.putString("video", new Gson().toJson(model));
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(), fragment.getClass().getSimpleName());
    }

    private void reportPost(Video.Data model) {
        ReportSheetFragment fragment = new ReportSheetFragment();
        Bundle args = new Bundle();
        args.putString("postid", model.getPostId());
        args.putInt("reporttype", 1);
        fragment.setArguments(args);
        fragment.show(getSupportFragmentManager(), fragment.getClass().getSimpleName());
    }

    private void showSendBubblePopUp(String userId) {

        new CustomDialogBuilder(this).showSendCoinDialogue(new CustomDialogBuilder.OnCoinDismissListener() {
            @Override
            public void onCancelDismiss() {

            }

            @Override
            public void on5Dismiss() {
                viewModel.sendBubble(userId, "5");
            }

            @Override
            public void on10Dismiss() {
                viewModel.sendBubble(userId, "10");
            }

            @Override
            public void on20Dismiss() {
                viewModel.sendBubble(userId, "20");
            }
        });
    }

    private void showSendResult(boolean success) {
        new CustomDialogBuilder(this).showSendCoinResultDialogue(success, success1 -> {
            if (!success1) {
                CoinPurchaseSheetFragment fragment = new CoinPurchaseSheetFragment();
                fragment.show(getSupportFragmentManager(), fragment.getClass().getSimpleName());
            }
        });
    }

    private void initIntent() {
        String videoStr = getIntent().getStringExtra("video_list");
        int position = getIntent().getIntExtra("position", 0);

        viewModel.type = getIntent().getIntExtra("type", 0);
        viewModel.handleType(getIntent());

        if (videoStr != null && !videoStr.isEmpty()) {
            viewModel.list = new Gson().fromJson(videoStr, new TypeToken<ArrayList<Video.Data>>() {
            }.getType());
            viewModel.adapter.itemToPlay = position;
            viewModel.start = viewModel.list.size();
            viewModel.adapter.updateData(viewModel.list);
            binding.viewPager.setCurrentItem(position, false);
        }
    }

    private SimpleExoPlayer player;
    ItemVideoListBinding lastBinding;
    private CacheDataSourceFactory cacheDataSourceFactory;
    private void playVideo(String videoUrl, ItemVideoListBinding binding) {
        if (player != null) {
            player.removeListener(this);
            player.setPlayWhenReady(false);
            player.release();
        }
        if (lastBinding != null)
            lastBinding.thumbnailView.setVisibility(View.VISIBLE);
        player = ExoPlayerFactory.newSimpleInstance(this);
        simpleCache = simpleCache;
        cacheDataSourceFactory = new CacheDataSourceFactory(simpleCache,
                new DefaultHttpDataSourceFactory(Util.getUserAgent(this, "tejash"))
                , CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);

        ProgressiveMediaSource progressiveMediaSource = new ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(Uri.parse(videoUrl));
        binding.playerView.setPlayer(player);
        player.setPlayWhenReady(true);
        player.seekTo(0, 0);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.addListener(this);
        binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
        player.prepare(progressiveMediaSource, true, false);
        lastBinding = binding;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_READY) {
            lastBinding.thumbnailView.setVisibility(View.GONE);
        }
    }

    private void initAds() {
        AdLoader.Builder builder = new AdLoader.Builder(this, getResources().getString(R.string.admobe_native_ad_id));
        builder.forUnifiedNativeAd(unifiedNativeAd -> {
            // You must call destroy on old ads when you are done with them,
            // otherwise you will have a memory leak.
            if (viewModel.adapter.unifiedNativeAd != null) {
                viewModel.adapter.unifiedNativeAd.destroy();
            }
            viewModel.adapter.unifiedNativeAd = unifiedNativeAd;

        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);
        AdLoader adLoader = builder.build();
        adLoader.loadAd(new AdRequest.Builder().build());
        NativeAd nativeAd = new NativeAd(this, getResources().getString(R.string.admobe_native_ad_id));

        nativeAd.setAdListener(new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {

                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                if (nativeAd == null || nativeAd != ad) {
                    return;
                }
                //viewModel.adapter.facebookNativeAd = nativeAd;
                Log.d(TAG, "Native ad is loaded and ready to be displayed!");
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Native ad clicked
                Log.d(TAG, "Native ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
                Log.d(TAG, "Native ad impression logged!");
            }
        });

        // Request an ad
        nativeAd.loadAd();

    }

    @Override
    public void onResume() {

        if (player != null)
            player.setPlayWhenReady(true);
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        if (player != null)
            player.setPlayWhenReady(false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}