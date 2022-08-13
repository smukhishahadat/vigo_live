package com.bakbakum.shortvdo.viewmodel;

import android.util.Log;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bakbakum.shortvdo.ViloApplication;
import com.bakbakum.shortvdo.adapter.FamousCreatorAdapter;
import com.bakbakum.shortvdo.adapter.VideoFullAdapter;
import com.bakbakum.shortvdo.model.user.RestResponse;
import com.bakbakum.shortvdo.model.videos.Video;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.Global;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ForUViewModel extends ViewModel {
    private static final String TAG = "ForUViewModel";

    public VideoFullAdapter adapter = new VideoFullAdapter();
    public MutableLiveData<Boolean> onLoadMoreComplete = new MutableLiveData<>();
    public int start = 0;
    public int count = 10;
    public String postType;
    public FamousCreatorAdapter famousAdapter = new FamousCreatorAdapter();
    public ObservableBoolean isEmpty = new ObservableBoolean(false);
    public MutableLiveData<RestResponse> coinSend = new MutableLiveData<>();
    public ObservableBoolean isloading = new ObservableBoolean(true);
    private CompositeDisposable disposable = new CompositeDisposable();

    public void fetchPostVideos(boolean isLoadMore) {
        isloading.set(true);
        disposable.add(Global.initRetrofit().getPostVideos(postType, 10, start, Global.USER_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnTerminate(() -> onLoadMoreComplete.setValue(true))
                .subscribe((video, throwable) -> {
                    Log.e(TAG, "fetchPostVideos: video is "+video );
                    if (video != null && video.getData() != null && !video.getData().isEmpty()) {
                        ArrayList<String> caches = new ArrayList<>();
                        for (Video.Data datum : video.getData()) {
                            caches.add(Const.ITEM_BASE_URL + datum.getPostVideo());
                        }
//                        CacheWorker.enqueueWork(caches);
                        ViloApplication.getInstance().cacheVideos(caches);
//                        Log.e("Videos", "Caches called");
//                        ViloApplication.getInstance().callCache(caches);
//                        Log.e("Videos", "Caches completed");
                        if (isLoadMore) {
                            adapter.loadMore(video.getData());
                        } else {
                            adapter.updateData(video.getData());
                        }
                        isloading.set(false);
                        start = start + count;
                    } else {
                        if (adapter.mList.isEmpty() && postType.equals("following")) {
                            fetchFamousVideos();
                            isEmpty.set(true);
                        }
                    }
                }));
    }

    private void fetchFamousVideos() {
        disposable.add(Global.initRetrofit().getPostVideos("trending", 50, 0, Global.USER_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnTerminate(() -> onLoadMoreComplete.setValue(true))
                .subscribe((video, throwable) -> {
                    if (video != null && video.getData() != null && !video.getData().isEmpty()) {
                        famousAdapter.updateData(video.getData());
                    }
                }));
    }

    public void onLoadMore() {
        fetchPostVideos(true);
    }

    public void likeUnlikePost(String postId) {
        disposable.add(Global.initRetrofit().likeUnlike(Global.ACCESS_TOKEN, postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe((likeRequest, throwable) -> {

                }));
    }

    public void sendBubble(String toUserId, String coin) {

        disposable.add(Global.initRetrofit().sendCoin(Global.ACCESS_TOKEN, coin, toUserId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())

                .doOnTerminate(() -> {
                    onLoadMoreComplete.setValue(true);
                })
                .subscribe((coinSend, throwable) -> {
                    if (coinSend != null && coinSend.getStatus() != null) {
                        this.coinSend.setValue(coinSend);
                    }

                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
