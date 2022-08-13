package com.bakbakum.shortvdo.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.facebook.ads.AdIconView;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.plattysoft.leonids.ParticleSystem;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ItemAdsLayBinding;
import com.bakbakum.shortvdo.databinding.ItemVideoListBinding;
import com.bakbakum.shortvdo.model.videos.Video;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.GlideLoader;
import com.bakbakum.shortvdo.utils.Global;

import java.util.ArrayList;
import java.util.List;

public class VideoFullAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int AD_TYPE = 1;
    private static final int POST_TYPE = 2;
    public ArrayList<Video.Data> mList = new ArrayList<>();
    public OnRecyclerViewItemClick onRecyclerViewItemClick;
    public int itemToPlay = 0;
    public String postId = "";
    public UnifiedNativeAd unifiedNativeAd;
    public NativeAd facebookNativeAd;
    private NativeAdLayout nativeAdLayout;


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == AD_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ads_lay, parent, false);
            return new AdsViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_list, parent, false);
            return new VideoFullViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof VideoFullViewHolder) {
            Log.e("VideoFullAdapter", "onBind");
            VideoFullViewHolder holder = (VideoFullViewHolder) viewHolder;
            holder.setModel(position);
        } else {
            if (viewHolder instanceof AdsViewHolder) {
                AdsViewHolder holder = (AdsViewHolder) viewHolder;

                if (unifiedNativeAd != null) {
                    Log.e("VideoFullAdapter", "Is not null");
                    holder.binding.frame.setVisibility(View.VISIBLE);
                    LinearLayout frameLayout = holder.binding.frame;
                    UnifiedNativeAdView
                            adView = (UnifiedNativeAdView) LayoutInflater.from(holder.binding.getRoot().getContext())
                            .inflate(R.layout.admob_native, null, false);
                    populateUnifiedNativeAdView(unifiedNativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                    holder.binding.unbind();
                } else {

                    Log.e("VideoFullAdapter", "Is null");
                }
            }
        }
    }

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

    private void inflateAd(NativeAd nativeAd,  ItemAdsLayBinding binding) {

        nativeAd.unregisterView();

        // Add the Ad view into the ad container.
        nativeAdLayout = binding.fbNative;
        binding.fbNative.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(binding.getRoot().getContext());
        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        RelativeLayout adView = (RelativeLayout) inflater.inflate(R.layout.fb_native_full, nativeAdLayout, false);
        nativeAdLayout.addView(adView);

        // Add the AdOptionsView
        LinearLayout adChoicesContainer = adView.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(adView.getContext(), nativeAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        AdIconView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);

        // Set the Text.
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);


        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
                adView,
                nativeAdMedia,
                nativeAdIcon,
                clickableViews);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {

        if (mList.get(position) == null) {
            return AD_TYPE;
        } else {
            return POST_TYPE;
        }

    }

    public void updateData(List<Video.Data> list) {
        mList = (ArrayList<Video.Data>) list;
        //mList.add(null);
        notifyDataSetChanged();

    }

    public void loadMore(List<Video.Data> data) {
        mList.addAll(data);
        //mList.add(null);
        notifyDataSetChanged();

    }

    public interface OnRecyclerViewItemClick {

        void onItemClick(Video.Data model, int position, int type, ItemVideoListBinding binding);

        void onHashTagClick(String hashTag);

        void onDoubleClick(Video.Data model, MotionEvent event, ItemVideoListBinding binding);

    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof VideoFullViewHolder) {
            Glide.with(holder.itemView.getContext()).clear(((VideoFullViewHolder) holder).binding.thumbnailView);
        }
        super.onViewRecycled(holder);

    }

    long lastClick = 0;
    class VideoFullViewHolder extends RecyclerView.ViewHolder {
        ItemVideoListBinding binding;

        VideoFullViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            if (binding != null) {
                binding.executePendingBindings();
            }
            binding.aspectRatioFrameLayout.setAspectRatio(9f/16f);
        }

        @SuppressLint("ClickableViewAccessibility")
        public void setModel(int position) {
            binding.setModel(mList.get(position));
            if (position == itemToPlay || postId.equals(mList.get(position).getPostId())) {
                Animation animation = AnimationUtils.loadAnimation(binding.getRoot().getContext(), R.anim.slow_rotate);
                binding.imgSound.startAnimation(animation);
                onRecyclerViewItemClick.onItemClick(mList.get(position), position, 9, binding);
            }
            Glide.with(itemView.getContext())
                    .asBitmap()
                    .load(Const.ITEM_BASE_URL + binding.getModel().getPostImage())
                    .placeholder(android.R.color.black)
                    .transition(BitmapTransitionOptions.withCrossFade())
                    .into(binding.thumbnailView);
            binding.tvSoundName.setSelected(true);
            binding.tvLikeCount.setText(Global.prettyCount(mList.get(position).getDummyLikeCount()));
            binding.playerView.setOnTouchListener(new View.OnTouchListener() {
                GestureDetector gestureDetector = new GestureDetector(binding.getRoot().getContext(), new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if (lastClick > System.currentTimeMillis()) return false;
                        onRecyclerViewItemClick.onItemClick(mList.get(position), position,2, binding);
                        return true;
                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        super.onSingleTapUp(e);
                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        onRecyclerViewItemClick.onItemClick(mList.get(position), position,8, binding);
                        super.onLongPress(e);
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
            binding.txtUsername.setOnClickListener((view) -> onRecyclerViewItemClick.onItemClick(mList.get(position), position,1, binding));
            binding.tvDescreption.setOnHashtagClickListener((view, text) -> onRecyclerViewItemClick.onHashTagClick(text.toString()));

            binding.loutUser.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(mList.get(position), position,1, binding));

            binding.imgSendBubble.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(mList.get(position),position,3, binding));
            binding.likebtn.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    onRecyclerViewItemClick.onItemClick(mList.get(position), position,4, binding);
                    mList.get(position).setPostLikesCount(String.valueOf(Integer.parseInt(mList.get(position).getPostLikesCount()) + 1));
                    binding.tvLikeCount.setText(Global.prettyCount(Long.parseLong(mList.get(position).getPostLikesCount())));
                    mList.get(position).setVideoIsLiked(1);
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    onRecyclerViewItemClick.onItemClick(mList.get(position), position,4, binding);
                    mList.get(position).setPostLikesCount(String.valueOf(Integer.parseInt(mList.get(position).getPostLikesCount()) - 1));
                    binding.tvLikeCount.setText(Global.prettyCount(Long.parseLong(mList.get(position).getPostLikesCount())));
                    mList.get(position).setVideoIsLiked(0);

                }
            });
            binding.imgComment.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(mList.get(position),position, 5, binding));
            binding.imgShare.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(mList.get(position), position,6, binding));
            binding.imgSound.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(mList.get(position), position, 7, binding));
            if (mList.get(position).getAllow_duet() == 1) {
                binding.imgDuet.setOnClickListener(v -> onRecyclerViewItemClick.onItemClick(mList.get(position), position,
                        10, binding));
            } else {
                binding.imgDuet.setVisibility(View.GONE);
            }
        }

    }

    static class AdsViewHolder extends RecyclerView.ViewHolder {
        ItemAdsLayBinding binding;

        public AdsViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
            binding.executePendingBindings();
        }
    }
}
