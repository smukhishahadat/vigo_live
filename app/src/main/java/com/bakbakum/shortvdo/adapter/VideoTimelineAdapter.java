package com.bakbakum.shortvdo.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.plattysoft.leonids.ParticleSystem;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.ViloApplication;
import com.bakbakum.shortvdo.databinding.ItemVideoListBinding;
import com.bakbakum.shortvdo.model.videos.Video;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.GlideLoader;
import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.view.search.FetchUserActivity;
import com.bakbakum.shortvdo.view.search.HashTagActivity;

import java.util.ArrayList;
import java.util.List;

import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.exoplayer.Config;
import im.ene.toro.exoplayer.ExoPlayerViewHelper;
import im.ene.toro.exoplayer.Playable;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;

public class VideoTimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Video.Data> videos;
    public boolean loading = false, finished = false;
    public static LoadControl loadControl = new DefaultLoadControl.Builder()
            .setAllocator(new DefaultAllocator(true, 16))
            .setBufferDurationsMs(Const.MIN_BUFFER_DURATION,
                    Const.MAX_BUFFER_DURATION,
                    Const.MIN_PLAYBACK_START_BUFFER,
                    Const.MIN_PLAYBACK_RESUME_BUFFER)
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .createDefaultLoadControl();
    VideoFullAdapter.OnRecyclerViewItemClick onRecyclerViewItemClick;
    Animation animation;
    public UnifiedNativeAd unifiedNativeAd;


    private void populateUnifiedNativeAdView(UnifiedNativeAd unifiedNativeAd, UnifiedNativeAdView adView) {

        adView.setMediaView(adView.findViewById(R.id.ad_media));
        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(unifiedNativeAd.getHeadline());
        //adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (unifiedNativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.GONE);

        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(unifiedNativeAd.getBody());
        }

        if (unifiedNativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(unifiedNativeAd.getCallToAction());
        }

        if (unifiedNativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            new GlideLoader(adView.getContext()).loadRoundDrawable(unifiedNativeAd.getIcon().getDrawable(), (ImageView) adView.getIconView());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (unifiedNativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(unifiedNativeAd.getPrice());
        }

        if (unifiedNativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(unifiedNativeAd.getStore());
        }

        if (unifiedNativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(unifiedNativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (unifiedNativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(unifiedNativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(unifiedNativeAd);

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        VideoController vc = unifiedNativeAd.getVideoController();

        // Updates the UI to say whether or not this ad has a video asset.

    }

    public VideoTimelineAdapter(
            Context context,
            ArrayList<Video.Data> videos,
                                VideoFullAdapter.OnRecyclerViewItemClick onRecyclerViewItemClick) {
        this.videos = videos;
        this.onRecyclerViewItemClick = onRecyclerViewItemClick;
        this.animation = AnimationUtils.loadAnimation(context, R.anim.slow_rotate);
    }

    public ArrayList<Video.Data> getVideos() {
        return videos;
    }

    public void updateData(List<Video.Data> list) {
        videos = (ArrayList<Video.Data>) list;
        notifyDataSetChanged();

    }

    public void loadMore(List<Video.Data> data) {
        videos.addAll(data);
        notifyDataSetChanged();
    }

//    @Override
//    public long getItemId(int position) {
//        return binding.getModel().getPostId();
//    }

    public class VideoViewHolder extends RecyclerView.ViewHolder implements ToroPlayer {
        ExoPlayerViewHelper helper;
        ItemVideoListBinding binding;
        long lastClick = 0;
        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            if (binding != null) {
                binding.executePendingBindings();
            }
            binding.aspectRatioFrameLayout.setAspectRatio(9f/16f);
            binding.tvDescreption.setOnHashtagClickListener((view, text) -> {
                Intent intent = new Intent(itemView.getContext(), HashTagActivity.class);
                intent.putExtra("hashtag", text);
                itemView.getContext().startActivity(intent);
            });

            binding.loutUser.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), FetchUserActivity.class);
                intent.putExtra("userid", binding.getModel().getUserId());
                itemView.getContext().startActivity(intent);
            });
            binding.txtUsername.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), FetchUserActivity.class);
                intent.putExtra("userid", binding.getModel().getUserId());
                itemView.getContext().startActivity(intent);
            });
            binding.imgSendBubble.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(binding.getModel(), getAdapterPosition(),3, binding));
            binding.likebtn.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    onRecyclerViewItemClick.onItemClick(binding.getModel(), getAdapterPosition(),4, binding);
                    binding.getModel().setPostLikesCount(String.valueOf(Integer.parseInt(binding.getModel().getPostLikesCount()) + 1));
                    binding.tvLikeCount.setText(Global.prettyCount(Long.parseLong(binding.getModel().getPostLikesCount())));
                    binding.getModel().setVideoIsLiked(1);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    onRecyclerViewItemClick.onItemClick(binding.getModel(), getAdapterPosition(),4, binding);
                    binding.getModel().setPostLikesCount(String.valueOf(Integer.parseInt(binding.getModel().getPostLikesCount()) - 1));
                    binding.tvLikeCount.setText(Global.prettyCount(Long.parseLong(binding.getModel().getPostLikesCount())));
                    binding.getModel().setVideoIsLiked(0);

                }
            });
            binding.imgComment.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(binding.getModel(), getAdapterPosition(), 5, binding));
            binding.imgShare.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(binding.getModel(), getAdapterPosition(),6, binding));
            binding.imgSound.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(binding.getModel(), getAdapterPosition(), 7, binding));
            binding.imgDuet.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(binding.getModel(), getAdapterPosition(),
                    10, binding));
            binding.playerView.setOnTouchListener(new View.OnTouchListener() {
                GestureDetector gestureDetector = new GestureDetector(binding.getRoot().getContext(), new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if (lastClick > System.currentTimeMillis()) return false;
                        if (binding.playerView.getPlayer() != null) {
                            if (binding.playerView.getPlayer().getPlayWhenReady()) {
                                pause();
                            } else {
                                play();
                            }
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        super.onSingleTapUp(e);
                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        Toast.makeText(itemView.getContext(), "Long pressed", Toast.LENGTH_SHORT).show();
                        onRecyclerViewItemClick.onItemClick(binding.getModel(), getAdapterPosition(),8, binding);
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        lastClick = System.currentTimeMillis() + (1000);
                        if (!binding.likebtn.isLiked()) {
                            binding.likebtn.performClick();
                        }
                        if (itemView.getContext() instanceof Activity) {
                            new ParticleSystem(((Activity) itemView.getContext()), 5, R.drawable.ic_heart_red, 5000)
                                    .setSpeedRange(0.1f, 0.25f)
                                    .setRotationSpeedRange(90, 180)
                                    .setInitialRotationRange(0, 360)
                                    .oneShot(binding.getRoot(), 10);
                        }
                        onRecyclerViewItemClick.onDoubleClick(binding.getModel(), e, binding);
                        return true;
                    }
                });

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
            });
        }

        @SuppressLint("ClickableViewAccessibility")
        public void setModel(int position) {
            binding.setModel(videos.get(position));
//            if (position == itemToPlay || postId.equals(binding.getModel().getPostId())) {
//                Animation animation = AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.slow_rotate);
//                binding.imgSound.startAnimation(animation);
//                onRecyclerViewItemClick.onItemClick(binding.getModel(), getAdapterPosition(), 9, binding);
//            }

            Log.e("VideoTimelineAdapter", "url: " + videos.get(position).getPostImage());
            Glide.with(itemView.getContext())
                    .asBitmap()
                    .load(Const.ITEM_BASE_URL + binding.getModel().getPostImage())
                    .placeholder(android.R.color.black)
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .into(binding.thumbnailView);
            binding.tvSoundName.setSelected(true);
            binding.tvLikeCount.setText(Global.prettyCount(binding.getModel().getDummyLikeCount()));

            if (binding.getModel().getAllow_duet() == 1) {
                binding.imgDuet.setVisibility(View.VISIBLE);
            } else {
                binding.imgDuet.setVisibility(View.GONE);
            }
        }
        
        @NonNull
        @Override
        public View getPlayerView() {
            return binding.playerView;
        }

        @NonNull
        @Override
        public PlaybackInfo getCurrentPlaybackInfo() {
            return helper != null ? helper.getLatestPlaybackInfo() : new PlaybackInfo();
        }

        @Override
        public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
            if (helper == null) {
                Config newConfig = new Config.Builder(itemView.getContext())
                        .setCache(ViloApplication.simpleCache)
                        .setLoadControl(loadControl).build();
                helper = new ExoPlayerViewHelper(this, Uri.parse(Const.ITEM_BASE_URL +
                        videos.get(getAdapterPosition()).getPostVideo()),
                        null, newConfig);
                helper.addEventListener(listener);
            }
            helper.initialize(container, playbackInfo);
        }

        @Override
        public void release() {
            if (binding.thumbnailView != null && binding.thumbnailView.getVisibility() != View.VISIBLE)
                binding.thumbnailView.setVisibility(View.VISIBLE);
            if (helper != null) {
                helper.removeEventListener(listener);
                helper.release();
                helper = null;
            }
            // if (handler != null) handler.removeCallbacks(hideOverlay);
        }

        @Override
        public boolean isPlaying() {
            return helper != null && helper.isPlaying();
        }

        @Override
        public boolean wantsToPlay() {
            return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= 0.85;
        }

        @Override
        public int getPlayerOrder() {
            return getAdapterPosition();
        }

        @Override
        public void play() {
            if (helper != null) helper.play();
//            if (itemView.findViewById(R.id.postLayout).getVisibility() == View.VISIBLE) {
                //handler.postDelayed(hideOverlay, 2000);
         //   }
        }

        @Override
        public void pause() {
            if (helper != null) helper.pause();
        }

//        Handler handler = new Handler();
//        private Runnable hideOverlay = new Runnable() {
//            @Override
//            public void run() {
//                itemView.findViewById(R.id.postLayout).setVisibility(View.INVISIBLE);
//                itemView.findViewById(R.id.layoutShadow).setVisibility(View.INVISIBLE);
//                btnPlay.setVisibility(View.INVISIBLE);
//            }
//        };

        private final Playable.EventListener listener = new Playable.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    // Log.e("VideoAdapter", "playback ready");
                    //playerView.getPlayer().seekTo(1000);
                    //playerView.getPlayer().seekTo(0);
                    binding.thumbnailView.setVisibility(View.GONE);
                } else if (playbackState == Player.STATE_ENDED) {
//                    if (!videoModal.viewed) {
//                        HashMap<String, String> params = new HashMap<>();
//                        params.put("video_id", String.valueOf(videoModal.id));
//                        new NetworkManager(itemView.getContext()).post(Constants.videoViewed, params,
//                                Utils.getInstance().getSessionManager().getAuthorization(),
//                                Priority.HIGH, "viewed");
//                        videoModal.viewed = true;
//                    }
//                    playerView.getPlayer().seekTo(0);
                }
                if (playWhenReady) {
                    // thumbnailView.setVisibility(View.GONE);
                    binding.imgSound.startAnimation(animation);
                } else {
                    binding.imgSound.clearAnimation();
                }
            }
        };
    }

    private static final int AD_TYPE = 1;
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == AD_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ads_lay, parent, false);
            return new VideoFullAdapter.AdsViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_list, parent, false);
            return new VideoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VideoViewHolder) {
            ((VideoViewHolder) holder).setModel(position);
        } else if (holder instanceof VideoFullAdapter.AdsViewHolder) {
            VideoFullAdapter.AdsViewHolder viewHolder = (VideoFullAdapter.AdsViewHolder) holder;
            if (unifiedNativeAd != null) {
                viewHolder.binding.frame.setVisibility(View.VISIBLE);
                LinearLayout frameLayout = viewHolder.binding.frame;
                UnifiedNativeAdView
                        adView = (UnifiedNativeAdView) LayoutInflater.from(viewHolder.binding.getRoot().getContext())
                        .inflate(R.layout.admob_native, null, false);
                populateUnifiedNativeAdView(unifiedNativeAd, adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);
                viewHolder.binding.unbind();
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof VideoViewHolder) {
            Glide.with(holder.itemView.getContext()).clear(((VideoViewHolder) holder).binding.thumbnailView);
            ((VideoViewHolder) holder).release();
            super.onViewRecycled(holder);
        }
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }
}