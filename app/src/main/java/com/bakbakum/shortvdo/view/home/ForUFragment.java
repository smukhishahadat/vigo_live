package com.bakbakum.shortvdo.view.home;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

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
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.gson.Gson;
import com.plattysoft.leonids.ParticleSystem;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.ViloApplication;
import com.bakbakum.shortvdo.adapter.VideoFullAdapter;
import com.bakbakum.shortvdo.adapter.VideoTimelineAdapter;
import com.bakbakum.shortvdo.databinding.FragmentForUBinding;
import com.bakbakum.shortvdo.databinding.ItemFamousCreatorBinding;
import com.bakbakum.shortvdo.databinding.ItemVideoListBinding;
import com.bakbakum.shortvdo.model.videos.Video;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.CustomDialogBuilder;
import com.bakbakum.shortvdo.utils.SessionManager;
import com.bakbakum.shortvdo.utils.customViewPager2.widget.ViewPager2;
import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.utils.GlobalApi;
import com.bakbakum.shortvdo.view.base.BaseActivity;
import com.bakbakum.shortvdo.view.base.BaseFragment;
import com.bakbakum.shortvdo.view.search.FetchUserActivity;
import com.bakbakum.shortvdo.view.share.ShareSheetFragment;
import com.bakbakum.shortvdo.view.wallet.CoinPurchaseSheetFragment;
import com.bakbakum.shortvdo.viewmodel.ForUViewModel;
import com.bakbakum.shortvdo.viewmodel.HomeViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import static android.content.ContentValues.TAG;

public class ForUFragment extends BaseFragment
        implements Player.EventListener {

    private final LinearLayoutManager layoutManager1 = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
    private final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
    public int lastPosition = -1;
    private SimpleExoPlayer player;
    private FragmentForUBinding binding;
    private ForUViewModel viewModel;
    private HomeViewModel parentViewModel;
    private SimpleCache simpleCache;
    private CacheDataSourceFactory cacheDataSourceFactory;
    private ItemFamousCreatorBinding binding2;
    private String type;
    private NativeAd nativeAd;
    private CustomDialogBuilder customDialogBuilder;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static ForUFragment getNewInstance(String type) {
        ForUFragment fragment = new ForUFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_for_u, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString("type");
        }
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        customDialogBuilder = new CustomDialogBuilder(getActivity());
        if (getActivity() != null) {
            parentViewModel = ViewModelProviders.of(getActivity()).get(HomeViewModel.class);
        }
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new ForUViewModel()).createFor()).get(ForUViewModel.class);

        initView();
        //initAds();
        initListeners();
        initObserve();
        binding.setViewmodel(viewModel);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        AlivcSvideoMixRecordActivity.startMixRecord(getActivity(), mAliyunSnapVideoParam, filePath,
                RenderingMode.FaceUnity, true);
    }

    private void initAds() {
        if (getActivity() != null) {
            AdLoader.Builder builder = new AdLoader.Builder(getActivity(), getActivity().getResources().getString(R.string.admobe_native_ad_id));
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
            nativeAd = new NativeAd(getActivity(), getActivity().getResources().getString(R.string.fb_native_ad_id));

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
                    //viewModel.viewPagerVideosAdapter.facebookNativeAd = nativeAd;

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
    }

    private void initView() {
        //binding.recyclerview.setLayoutManager(layoutManager);
        binding.popularRecyclerview.setLayoutManager(layoutManager1);
        binding.refreshlout.setEnableRefresh(false);
        //SnapHelper snapHelper = new PagerSnapHelper();
        //snapHelper.attachToRecyclerView(binding.recyclerview);
        SnapHelper snapHelper1 = new PagerSnapHelper();
        snapHelper1.attachToRecyclerView(binding.popularRecyclerview);
        binding.viewPager.setAdapter(viewModel.adapter);
        binding.viewPager.setOffscreenPageLimit(5);
        if (getArguments() != null) {
            type = getArguments().getString("type");
            if (type != null && type.equals("1")) {
                viewModel.postType = "related";
            } else {
                viewModel.postType = "following";
            }
            viewModel.fetchPostVideos(false);
        }
    }

    private void initListeners() {
        viewModel.famousAdapter.onRecyclerViewItemClick = (model, position, binding, type) -> {
            if (type == 1) {
                if (parentViewModel.onPageSelect.getValue() != null &&
                        parentViewModel.onPageSelect.getValue() == Integer.parseInt(ForUFragment.this.type)) {
                    lastPosition = position;
                    playVideo(Const.ITEM_BASE_URL + model.getPostVideo(), binding);
                }
            } else {
                Intent intent = new Intent(getContext(), FetchUserActivity.class);
                intent.putExtra("userid", model.getUserId());
                startActivity(intent);
            }
        };
        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    ((MainActivity) getActivity()).enableScroll(false);
                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
//                    new GlobalApi().increaseView(viewModel.adapter.mList.get(binding.viewPager.getCurrentItem()).getPostId());
//                    if (getActivity() instanceof MainActivity) {
//                        ((MainActivity) getActivity()).updateProfile(viewModel.adapter.mList.get(binding.viewPager.getCurrentItem()).getUserId());
//                        ((MainActivity) getActivity()).enableScroll(true);
//                    }
                    int position = binding.viewPager.getCurrentItem();
                    if (position != -1 && lastPosition != position) {
                        if (viewModel.adapter.mList.size() > 0 &&
                                viewModel.adapter.mList.get(position) != null) {
                            Animation animation = AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.slow_rotate);
                            if (binding.viewPager.getLayoutManager() != null) {
                                View view = binding.viewPager.getLayoutManager().findViewByPosition(position);
                                if (view != null) {
                                    lastPosition = position;
                                    ItemVideoListBinding binding1 = DataBindingUtil.bind(view);
                                    if (binding1 != null) {
                                        binding1.imgSound.startAnimation(animation);
                                        new GlobalApi().increaseView(binding1.getModel().getPostId());
                                        if (getActivity() instanceof MainActivity) {
                                            ((MainActivity) getActivity()).updateProfile(viewModel.adapter.mList.get(position).getUserId());
                                            ((MainActivity) getActivity()).enableScroll(true);
                                        }
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

//        binding.recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int state) {
//                if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
//                    ((MainActivity) getActivity()).enableScroll(false);
//                } else if (state == RecyclerView.SCROLL_STATE_IDLE) {
//
//                    int position = layoutManager.findFirstCompletelyVisibleItemPosition();
//                    if (position != -1 && lastPosition != position) {
//                        if (viewModel.adapter.mList.get(position) != null) {
//                            Animation animation = AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.slow_rotate);
//                            if (binding.recyclerview.getLayoutManager() != null) {
//                                View view = binding.recyclerview.getLayoutManager().findViewByPosition(position);
//                                if (view != null) {
//                                    lastPosition = position;
//                                    ItemVideoListBinding binding1 = DataBindingUtil.bind(view);
//                                    if (binding1 != null) {
//                                        binding1.imgSound.startAnimation(animation);
//                                        new GlobalApi().increaseView(binding1.getModel().getPostId());
//                                        if (getActivity() instanceof MainActivity) {
//                                            ((MainActivity) getActivity()).updateProfile(viewModel.adapter.mList.get(position).getUserId());
//                                            ((MainActivity) getActivity()).enableScroll(true);
//                                        }
//                                        playVideo(Const.ITEM_BASE_URL + viewModel.adapter.mList.get(position).getPostVideo(), binding1);
//                                    }
//                                }
//                            }
//                        } else {
//                            if (player != null) {
//                                player.setPlayWhenReady(false);
//                                player.stop();
//                                player.release();
//                                player = null;
//                                lastPosition = position;
//                            }
//                        }
//                    }
//                }
//            }
//        });

        viewModel.adapter.onRecyclerViewItemClick = new VideoFullAdapter.OnRecyclerViewItemClick() {
            @Override
            public void onItemClick(Video.Data model, int position, int type, ItemVideoListBinding binding) {
                switch (type) {
                    // Send to FetchUser Activity
                    case 1:
                        Intent intent = new Intent(getContext(), FetchUserActivity.class);
                        intent.putExtra("userid", model.getUserId());
                        startActivity(intent);
                        break;
                    // Play/Pause video
                    case 2:
                        if (player != null) {
                            if (player.getPlayWhenReady()) {
                                player.setPlayWhenReady(false);
                            } else {
                                player.setPlayWhenReady(true);
                            }
                        }
                        break;
                    // Send Bubble to creator
                    case 3:
                        if (!Global.ACCESS_TOKEN.isEmpty()) {
                            showSendBubblePopUp(model.getUserId());
                        } else {
                            if (getActivity() != null && getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).initLogin(getActivity(), () -> {
                                    if (!Global.ACCESS_TOKEN.isEmpty()) {
                                        showSendBubblePopUp(model.getUserId());
                                    }
                                });
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
                        fragment.setArguments(args);
                        fragment.show(getChildFragmentManager(), fragment.getClass().getSimpleName());
                        break;
                    // On Share Click
                    case 6:
                        handleShareClick(model);
                        break;
                    // On Sound Disk Click
                    case 7:
                        if (Global.ACCESS_TOKEN.isEmpty()) {
                            if (getActivity() != null && getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).initLogin(getActivity(), () -> viewModel.likeUnlikePost(model.getPostId()));
                            }
                        } else {
                            Intent intent1 = new Intent(getContext(), SoundVideosActivity.class);
                            intent1.putExtra("soundid", model.getSoundId());
                            intent1.putExtra("sound", model.getSound());
                            startActivity(intent1);
                        }
                        break;
                    // On Long Click (Report Video)
                    case 8:
                        new CustomDialogBuilder(getContext()).showSimpleDialog("Report this post", "Are you sure you want to\nreport this post?", "Cancel", "Yes, Report", new CustomDialogBuilder.OnDismissListener() {
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
                        if (getActivity() instanceof MainActivity) {
                            ((MainActivity) getActivity()).updateProfile(model.getUserId());
                            ((MainActivity) getActivity()).enableScroll(true);
                        }
                        break;
                    case 10:
                        if (new SessionManager(getContext()).getBooleanValue(Const.IS_LOGIN)) {
                            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                                    || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        1);
                            } else {
                                final TextView percent = customDialogBuilder.showLoadingDialogWithPercentage();
                                PRDownloader.download(Const.ITEM_BASE_URL + model.getPostVideo(), getPath().getPath(), model.getPostVideo().substring(model.getPostVideo().lastIndexOf("/")+1))
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
                                                File file = new File(getPath().getPath(), model.getPostVideo().substring(model.getPostVideo().lastIndexOf("/")+1));
                                                Log.e(TAG, "onDownloadComplete: duet file path is "+file );
                                                startDuetRecording(file.getPath());
                                                Toast.makeText(getActivity(), "Saved Successfully", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onError(Error error) {
                                                customDialogBuilder.hideLoadingDialog();
                                                Log.d("DOWNLOAD", "onError: " + error.getConnectionException().getMessage());
                                            }
                                        });
                            }
                        } else {
                            if (getContext() instanceof MainActivity) {
                                ((MainActivity) getContext()).initLogin(getContext(), new BaseActivity.OnLoginSheetClose() {
                                    @Override
                                    public void onClose() {

                                    }
                                });
                            }
                        }
                        break;
                }

            }

            @Override
            public void onHashTagClick(String hashTag) {
//                Intent intent = new Intent(getContext(), HashTagActivity.class);
//                intent.putExtra("hashtag", hashTag);
//                startActivity(intent);
            }

            @Override
            public void onDoubleClick(Video.Data model, MotionEvent event, ItemVideoListBinding binding) {
                if (!binding.likebtn.isLiked()) {
                    binding.likebtn.performClick();
                }
                if (!player.getPlayWhenReady()) {
                    player.setPlayWhenReady(true);
                }
                showHeart(event, binding);
            }
        };
        binding.popularRecyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int position = layoutManager1.findFirstCompletelyVisibleItemPosition();
                    if (!(position <= -1) && lastPosition != position) {
                        if (binding.popularRecyclerview.getLayoutManager() != null) {
                            View view = binding.popularRecyclerview.getLayoutManager().findViewByPosition(position);
                            if (view != null) {
                                lastPosition = position;
                                ItemFamousCreatorBinding binding1 = DataBindingUtil.bind(view);

                                playVideo(Const.ITEM_BASE_URL + viewModel.famousAdapter.mList.get(position).getPostVideo(), binding1);
                            }
                        }
                    }
                }
            }
        });


        binding.refreshlout.setOnLoadMoreListener(refreshLayout -> viewModel.onLoadMore());
    }

    public void showHeart(MotionEvent e, ItemVideoListBinding binding) {

        new ParticleSystem(getActivity(), 5, R.drawable.ic_heart_red, 5000)
                .setSpeedRange(0.1f, 0.25f)
                .setRotationSpeedRange(90, 180)
                .setInitialRotationRange(0, 360)
                .oneShot(binding.getRoot(), 10);

    }

    private void handleShareClick(Video.Data model) {
        ShareSheetFragment fragment = new ShareSheetFragment();
        Bundle args = new Bundle();
        args.putString("video", new Gson().toJson(model));
        fragment.setArguments(args);
        fragment.show(getChildFragmentManager(), fragment.getClass().getSimpleName());
    }

    private void initObserve() {
        parentViewModel.onRefresh.observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean != null && aBoolean) {
                viewModel.start = 0;
                viewModel.fetchPostVideos(false);
            }
        });
        parentViewModel.onStop.observe(getViewLifecycleOwner(), onStop -> {
            if (onStop != null) {
                if (parentViewModel.onPageSelect.getValue() != null && parentViewModel.onPageSelect.getValue() == Integer.parseInt(type) && player != null) {
                    player.setPlayWhenReady(!onStop);
                }
            }
        });
        viewModel.onLoadMoreComplete.observe(getViewLifecycleOwner(), onLoadMore -> {
            binding.refreshlout.finishLoadMore();
            parentViewModel.onRefresh.setValue(false);
        });
        viewModel.coinSend.observe(getViewLifecycleOwner(), coinSend -> showSendResult(coinSend.getStatus()));
    }

    ItemVideoListBinding lastBinding;
    private void playVideo(String videoUrl, ItemVideoListBinding binding) {
        if (player != null) {
            player.removeListener(this);
            player.setPlayWhenReady(false);
            player.release();
        }
        if (getActivity() != null) {
            if (lastBinding != null)
                lastBinding.thumbnailView.setVisibility(View.VISIBLE);
            player = ExoPlayerFactory.newSimpleInstance(getContext());
            simpleCache = ViloApplication.simpleCache;
            cacheDataSourceFactory = new CacheDataSourceFactory(simpleCache,
                    new DefaultHttpDataSourceFactory(Util.getUserAgent(getActivity(), "tejash"))
                    , CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);

            ProgressiveMediaSource progressiveMediaSource = new ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                    .createMediaSource(Uri.parse(videoUrl));
            binding.playerView.setPlayer(player);
            if (parentViewModel.onPageSelect.getValue() != null && parentViewModel.onPageSelect.getValue().equals(Integer.parseInt(ForUFragment.this.type))) {
                player.setPlayWhenReady(true);
            }
            player.seekTo(0, 0);
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.addListener(this);
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
            player.prepare(progressiveMediaSource, true, false);
            lastBinding = binding;
        }
    }

    private void playVideo(String videoUrl, ItemFamousCreatorBinding binding) {
        if (player != null) {
            player.removeListener(this);
            player.setPlayWhenReady(false);
            player.release();
        }
        if (binding2 != null) {
            // run scale animation and make it smaller
            Animation anim = AnimationUtils.loadAnimation(binding2.getRoot().getContext(), R.anim.scale_out_tv);
            binding2.getRoot().startAnimation(anim);
            anim.setFillAfter(true);
        }
        binding2 = binding;
        // run scale animation and make it bigger
        Animation anim = AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.scale_in_tv);
        binding.getRoot().startAnimation(anim);
        anim.setFillAfter(true);
        if (getActivity() != null) {
            player = ExoPlayerFactory.newSimpleInstance(getContext(), new DefaultTrackSelector(), VideoTimelineAdapter.loadControl);
            simpleCache = ViloApplication.simpleCache;
            cacheDataSourceFactory = new CacheDataSourceFactory(simpleCache, new DefaultHttpDataSourceFactory(Util.getUserAgent(getActivity(), "tejash"))
                    , CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);

            ProgressiveMediaSource progressiveMediaSource = new ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(Uri.parse(videoUrl));
            binding.playerView.setPlayer(player);
            player.setPlayWhenReady(true);
            player.seekTo(0, 0);
            player.setRepeatMode(Player.REPEAT_MODE_ALL);
            player.addListener(this);
            binding.playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
            player.prepare(progressiveMediaSource, true, false);
        }
    }

    private void showSendBubblePopUp(String userId) {

        new CustomDialogBuilder(getActivity()).showSendCoinDialogue(new CustomDialogBuilder.OnCoinDismissListener() {
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
        new CustomDialogBuilder(getContext()).showSendCoinResultDialogue(success, success1 -> {
            if (!success1) {
                CoinPurchaseSheetFragment fragment = new CoinPurchaseSheetFragment();
                fragment.show(getChildFragmentManager(), fragment.getClass().getSimpleName());
            }
        });
    }

    private void reportPost(Video.Data model) {
        ReportSheetFragment fragment = new ReportSheetFragment();
        Bundle args = new Bundle();
        args.putString("postid", model.getPostId());
        args.putInt("reporttype", 1);
        fragment.setArguments(args);
        fragment.show(getChildFragmentManager(), fragment.getClass().getSimpleName());
    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_BUFFERING) {
            parentViewModel.loadingVisibility.set(View.VISIBLE);
        } else if (playbackState == Player.STATE_READY) {
            parentViewModel.loadingVisibility.set(View.GONE);

            if (lastBinding!=null && lastBinding.thumbnailView!=null){
                lastBinding.thumbnailView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onResume() {
        if (player != null)
            player.setPlayWhenReady(true);
//        new Handler().postDelayed(() -> {
//            if (binding.viewPager.getRecyclerView() != null) binding.viewPager.getRecyclerView().setPlayerSelector(PlayerSelector.DEFAULT);
//        }, 500);
        super.onResume();
    }

    @Override
    public void onPause() {
        if (player != null)
            player.setPlayWhenReady(false);
//        new Handler().postDelayed(() -> {
//            if (binding.viewPager.getRecyclerView() != null) binding.viewPager.getRecyclerView().setPlayerSelector(PlayerSelector.NONE);
//        }, 500);
        super.onPause();
    }

    public File getPath() {
        if (getActivity() != null) {
            String state = Environment.getExternalStorageState();
            File filesDir;
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // We can read and write the media
                filesDir = getActivity().getExternalFilesDir(null);
            } else {
                // Load another directory, probably local memory
                filesDir = getActivity().getFilesDir();
            }
            return filesDir;
        }
        return new File(Environment.getRootDirectory().getAbsolutePath());
    }
}
