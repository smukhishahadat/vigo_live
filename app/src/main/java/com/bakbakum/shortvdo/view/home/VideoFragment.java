package com.bakbakum.shortvdo.view.home;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.adapter.VideoFullAdapter;
import com.bakbakum.shortvdo.adapter.ViewPagerVideosAdapter;
import com.bakbakum.shortvdo.databinding.ItemVideoListBinding;
import com.bakbakum.shortvdo.model.videos.Video;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.Global;

import java.lang.ref.WeakReference;

import static com.bakbakum.shortvdo.ViloApplication.simpleCache;

public class VideoFragment extends Fragment {

    ViewPagerVideosAdapter adapter;
    Video.Data videoModal;
    VideoFullAdapter.OnRecyclerViewItemClick onRecyclerViewItemClick;

    public VideoFragment() {
        // Required empty public constructor
    }

    public VideoFragment(VideoFullAdapter.OnRecyclerViewItemClick onRecyclerViewItemClick, ViewPagerVideosAdapter adapter) {
        this.onRecyclerViewItemClick = onRecyclerViewItemClick;
        this.adapter = adapter;
    }

    public static VideoFragment newInstance(Video.Data videoModal,
                                            VideoFullAdapter.OnRecyclerViewItemClick onRecyclerViewItemClick,
                                            ViewPagerVideosAdapter adapter) {
        VideoFragment fragment = new VideoFragment(onRecyclerViewItemClick, adapter);
        Bundle args = new Bundle();
        args.putSerializable("video", videoModal);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            videoModal = ((Video.Data) getArguments().getSerializable("video"));
        }
    }

    private SimpleExoPlayer simplePlayer;
    private CacheDataSourceFactory cacheDataSourceFactory;
    private SimpleExoPlayer getPlayer() {
        if (simplePlayer == null) {
            prepareVideoPlayer();
        }
        return simplePlayer;
    }
    private void prepareVideoPlayer() {
       // simplePlayer = new SimpleExoPlayer.Builder(getContext()).build(); //ExoPlayerFactory.newSimpleInstance(getContext());
        cacheDataSourceFactory = new CacheDataSourceFactory(simpleCache,
                new DefaultHttpDataSourceFactory(Util.getUserAgent(getActivity(), "tejash"))
                , CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
    }
    private void loadPlayer() {
        //Log.e("VideoFragment", "Load video, player: " + player == null ? "Yes" : "NO");

//        HttpProxyCacheServer proxy = Utils.getProxy(getActivity());

//        LoadControl loadControl = new DefaultLoadControl.Builder()
//                .setAllocator(new DefaultAllocator(true, 16))
//                .setBufferDurationsMs(1 * 1024, 1 * 1024, 500, 1024)
//                .setTargetBufferBytes(-1)
//                .setPrioritizeTimeOverSizeThresholds(true)
//                .createDefaultLoadControl();
//
//        DefaultTrackSelector trackSelector = new DefaultTrackSelector();

//        player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, loadControl);
        //player = adapter.getPlayer();
//        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity(),
//                Util.getUserAgent(getActivity(), getActivity().getResources().getString(R.string.app_name)));
//        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
//                .createMediaSource(Uri.parse(proxy.getProxyUrl(videoModal.videoUrl)));

//        simpleCache = ViloApplication.simpleCache;
//        cacheDataSourceFactory = new CacheDataSourceFactory(simpleCache, new DefaultHttpDataSourceFactory(Util.getUserAgent(getActivity(), "tejash"))
//                , CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR);
//
//        Log.e("VideoFragment", "Uri: " + Const.ITEM_BASE_URL + videoModal.getPostVideo());
//
        binding.playerView.setPlayer(getPlayer());
        ProgressiveMediaSource progressiveMediaSource = new ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(Uri.parse(Const.ITEM_BASE_URL + videoModal.getPostVideo()));

//        HttpProxyCacheServer proxy = ViloApplication.getProxy(getActivity());
//        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity(),
//                Util.getUserAgent(getActivity(), getActivity().getResources().getString(R.string.app_name)));
//        MediaSource videoSource = new ExtractorMediaSource.Factory(cacheDataSourceFactory)
//                .createMediaSource(Uri.parse(Const.ITEM_BASE_URL + videoModal.getPostVideo()));

//        player.addListener(listener);
        binding.playerView.getPlayer().seekTo(0, 0);
        binding.playerView.getPlayer().setRepeatMode(Player.REPEAT_MODE_ONE);
        ((SimpleExoPlayer) binding.playerView.getPlayer()).prepare(progressiveMediaSource, true, true);
        binding.playerView.getPlayer().setPlayWhenReady(false);
    }

    ItemVideoListBinding binding;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = ItemVideoListBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View itemView, @Nullable Bundle savedInstanceState) {
        binding.setModel(videoModal);
//        if (position == itemToPlay || postId.equals(videoModal.getPostId())) {


//            onRecyclerViewItemClick.onItemClick(videoModal, position, 9, binding);
//        }
        loadPlayer();

        binding.tvSoundName.setSelected(true);
        binding.tvLikeCount.setText(Global.prettyCount(videoModal.getDummyLikeCount()));
//        binding.playerView.setOnTouchListener(new View.OnTouchListener() {
//            GestureDetector gestureDetector = new GestureDetector(binding.getRoot().getContext(), new GestureDetector.SimpleOnGestureListener() {
//                @Override
//                public boolean onSingleTapUp(MotionEvent e) {
//                    onRecyclerViewItemClick.onItemClick(videoModal,  2, binding);
//                    return true;
//                }
//
//                @Override
//                public void onLongPress(MotionEvent e) {
//                    onRecyclerViewItemClick.onItemClick(videoModal, 8, binding);
//                    super.onLongPress(e);
//                }
//
//                @Override
//                public boolean onDoubleTap(MotionEvent e) {
//                    onRecyclerViewItemClick.onDoubleClick(videoModal, e, binding);
//                    return true;
//                }
//            });
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                gestureDetector.onTouchEvent(event);
//                return false;
//            }
//        });
//        binding.tvDescreption.setOnHashtagClickListener((view, text) -> onRecyclerViewItemClick.onHashTagClick(text.toString()));
//        binding.loutUser.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(videoModal, 1, binding));
//        binding.imgSendBubble.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(videoModal, 3, binding));
//        binding.likebtn.setOnLikeListener(new OnLikeListener() {
//            @Override
//            public void liked(LikeButton likeButton) {
//                onRecyclerViewItemClick.onItemClick(videoModal, 4, binding);
//                videoModal.setPostLikesCount(String.valueOf(Integer.parseInt(videoModal.getPostLikesCount()) + 1));
//                binding.tvLikeCount.setText(Global.prettyCount(Long.parseLong(videoModal.getPostLikesCount())));
//                videoModal.setVideoIsLiked(1);
//            }
//
//            @Override
//            public void unLiked(LikeButton likeButton) {
//                onRecyclerViewItemClick.onItemClick(videoModal, 4, binding);
//                videoModal.setPostLikesCount(String.valueOf(Integer.parseInt(videoModal.getPostLikesCount()) - 1));
//                binding.tvLikeCount.setText(Global.prettyCount(Long.parseLong(videoModal.getPostLikesCount())));
//                videoModal.setVideoIsLiked(0);
//
//            }
//        });
//        binding.txtUsername.setOnClickListener((v) -> onRecyclerViewItemClick.onItemClick(videoModal, 1, binding));
//        binding.imgComment.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(videoModal, 5, binding));
//        binding.imgShare.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(videoModal,  6, binding));
//        binding.imgSound.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(videoModal, 7, binding));
//        if (videoModal.getAllow_duet() == 1) {
//            binding.imgDuet.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(videoModal, 10, binding));
//        } else {
//            binding.imgDuet.setVisibility(View.GONE);
//        }
    }

    public void play() {
        if (binding.playerView.getPlayer() != null)
            binding.playerView.getPlayer().setPlayWhenReady(true);
    }

    public void pause() {
        if (binding.playerView.getPlayer() != null)
            binding.playerView.getPlayer().setPlayWhenReady(false);
    }

    @Override
    public void onResume() {
        this.adapter.currentFragment = new WeakReference<>(this);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateProfile(videoModal.getUserId());
        }
        if (binding.playerView.getPlayer() != null) {
            adapter.setListener(binding.playerView.getPlayer());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    binding.playerView.getPlayer().seekToDefaultPosition();
                    binding.playerView.getPlayer().setPlayWhenReady(true);
                }
            }, 200);
        } else {
            loadPlayer();
        }
        Animation animation = AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.slow_rotate);
        binding.imgSound.startAnimation(animation);
        super.onResume();
    }

    @Override
    public void onPause() {
        if (binding.playerView.getPlayer() != null) {
            binding.playerView.getPlayer().setPlayWhenReady(false);
        }
        binding.imgSound.clearAnimation();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (binding.playerView.getPlayer() != null) {
            simplePlayer.release();
//            player.removeListener(listener);
//            player.release();
//            playerView = null;
        }
        super.onDestroy();
    }
}