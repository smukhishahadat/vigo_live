package com.bakbakum.shortvdo.viewmodel;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bakbakum.shortvdo.adapter.VideoListAdapter;
import com.bakbakum.shortvdo.model.videos.Video;
import com.bakbakum.shortvdo.utils.Global;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HashTagViewModel extends ViewModel {

    public VideoListAdapter adapter = new VideoListAdapter();
    public MutableLiveData<Boolean> onLoadMoreComplete = new MutableLiveData<>();
    public int start = 0;
    public String hashtag;
    public MutableLiveData<Video> video = new MutableLiveData<>();
    ObservableBoolean isloading = new ObservableBoolean(true);
    private int count = 10;
    private CompositeDisposable disposable = new CompositeDisposable();

    public void fetchHashTagVideos(boolean isLoadMore) {
        if (!disposable.isDisposed()) {
            disposable.clear();
        }
        disposable.add(Global.initRetrofit().fetchHasTagVideo(hashtag, count, start, Global.USER_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnTerminate(() -> onLoadMoreComplete.setValue(true))
                .subscribe((video, throwable) -> {
                    if (video != null && video.getData() != null && !video.getData().isEmpty()) {
                        this.video.setValue(video);
                        if (isLoadMore) {
                            adapter.loadMore(video.getData());
                        } else {
                            adapter.updateData(video.getData());
                        }
                        isloading.set(false);
                        start = start + count;
                    }

                }));
    }

    public void onLoadMore() {
        fetchHashTagVideos(true);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}

