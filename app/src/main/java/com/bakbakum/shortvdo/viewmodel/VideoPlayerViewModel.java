package com.bakbakum.shortvdo.viewmodel;

import android.content.Intent;
import android.text.TextUtils;

import androidx.databinding.ObservableInt;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bakbakum.shortvdo.adapter.VideoFullAdapter;
import com.bakbakum.shortvdo.model.user.RestResponse;
import com.bakbakum.shortvdo.model.videos.Video;
import com.bakbakum.shortvdo.utils.Global;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static android.view.View.GONE;

public class VideoPlayerViewModel extends ViewModel {
    public VideoFullAdapter adapter = new VideoFullAdapter();
    public ArrayList<Video.Data> list = new ArrayList<>();
    public int position = 0;
    public int type = 0;
    public MutableLiveData<Boolean> onCommentSuccess = new MutableLiveData<>();
    public MutableLiveData<Boolean> onLoadMoreComplete = new MutableLiveData<>();
    public MutableLiveData<RestResponse> coinSend = new MutableLiveData<>();
    public ObservableInt loadingVisibility = new ObservableInt(GONE);
    public int count = 10;
    public int start = 0;
    public String userId = "";
    public String hashTag = "";
    public String soundId = "";
    public String keyword = "";
    public String postId;
    private String comment;
    private CompositeDisposable disposable = new CompositeDisposable();

    public void handleType(Intent intent) {
        switch (type) {
            //UserVideos
            //UserLikesVideos
            case 0:
            case 1:
                userId = intent.getStringExtra("user_id");
                break;
            //HasTagVideo
            case 2:
                hashTag = intent.getStringExtra("hash_tag");
                break;
            //SearchVideos
            case 3:
                keyword = intent.getStringExtra("keyword");
                break;
            //SoundVideos
            case 4:
                soundId = intent.getStringExtra("sound_id");
                break;
        }
    }

    public void onLoadMore() {
        switch (type) {
            //UserVideos
            case 0:
                getUserVideos();
                break;
            //UserLikesVideos
            case 1:
                getUserLikesVideos();
                break;
            //HasTagVideo
            case 2:
                getHasTagVideo();
                break;
            //SearchVideos
            case 3:
                getSearchVideos();
                break;
            //SoundVideos
            case 4:
                getSoundVideos();
                break;
        }
    }


    public void getUserVideos() {
        disposable.add(Global.initRetrofit().getUserVideos(userId, count, start, Global.USER_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnTerminate(() -> onLoadMoreComplete.setValue(true))
                .subscribe((video, throwable) -> {
                    if (video != null && video.getData() != null && !video.getData().isEmpty()) {
                        adapter.loadMore(video.getData());
                        start = start + count;
                    }
                }));
    }

    public void getUserLikesVideos() {
        disposable.add(Global.initRetrofit().getUserLikedVideos(userId, count, start, Global.USER_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnTerminate(() -> onLoadMoreComplete.setValue(true))
                .subscribe((video, throwable) -> {
                    if (video != null && video.getData() != null && !video.getData().isEmpty()) {
                        adapter.loadMore(video.getData());
                        start = start + count;
                    }
                }));
    }

    public void afterUserNameTextChanged(CharSequence s) {
        comment = s.toString();
    }

    public void getHasTagVideo() {
        disposable.add(Global.initRetrofit().fetchHasTagVideo(hashTag, count, start, Global.USER_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnTerminate(() -> onLoadMoreComplete.setValue(true))
                .subscribe((video, throwable) -> {
                    if (video != null && video.getData() != null && !video.getData().isEmpty()) {
                        adapter.loadMore(video.getData());
                        start = start + count;
                    }
                }));
    }

    public void getSearchVideos() {
        disposable.add(Global.initRetrofit().searchVideo(keyword, count, start, Global.USER_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnTerminate(() -> onLoadMoreComplete.setValue(true))
                .subscribe((video, throwable) -> {
                    if (video != null && video.getData() != null && !video.getData().isEmpty()) {
                        adapter.loadMore(video.getData());
                        start = start + count;
                    }
                }));
    }

    public void getSoundVideos() {
        disposable.add(Global.initRetrofit().getSoundVideos(count, start, soundId, Global.USER_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnTerminate(() -> onLoadMoreComplete.setValue(true))
                .subscribe((video, throwable) -> {
                    if (video != null && video.getData() != null && !video.getData().isEmpty()) {
                        adapter.loadMore(video.getData());
                        start = start + count;
                    }
                }));
    }

    public void sendBubble(String toUserId, String coin) {

        disposable.add(Global.initRetrofit().sendCoin(Global.ACCESS_TOKEN, coin, toUserId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnTerminate(() -> onLoadMoreComplete.setValue(true))
                .subscribe((coinSend, throwable) -> {
                    if (coinSend != null && coinSend.getStatus() != null) {
                        this.coinSend.setValue(coinSend);
                    }

                }));
    }

    public void likeUnlikePost(String postId) {

        disposable.add(Global.initRetrofit().likeUnlike(Global.ACCESS_TOKEN, postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe((likeRequest, throwable) -> {

                }));
    }

    public void addComment() {
        if (!TextUtils.isEmpty(comment)) {
            callApiToSendComment();
        }
    }

    private void callApiToSendComment() {
        disposable.add(Global.initRetrofit().addComment(Global.ACCESS_TOKEN, postId, comment)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())

                .subscribe((comment, throwable) -> {
                    if (comment != null && comment.getStatus() != null) {
                        onCommentSuccess.setValue(true);
                    }
                }));
    }

}
