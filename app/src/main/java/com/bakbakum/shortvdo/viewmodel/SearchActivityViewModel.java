package com.bakbakum.shortvdo.viewmodel;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bakbakum.shortvdo.adapter.SearchUserAdapter;
import com.bakbakum.shortvdo.adapter.VideoListAdapter;
import com.bakbakum.shortvdo.utils.Global;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SearchActivityViewModel extends ViewModel {
    public ObservableInt searchtype = new ObservableInt(0);
    public MutableLiveData<Boolean> onLoadMoreComplete = new MutableLiveData<>();
    public String search_text;
    public int userStart = 0;
    public int videoStart = 0;
    public ObservableBoolean noUserData = new ObservableBoolean(false);
    public ObservableBoolean noVideoData = new ObservableBoolean(false);
    public SearchUserAdapter searchUseradapter = new SearchUserAdapter();
    public VideoListAdapter videoListAdapter = new VideoListAdapter();
    ObservableBoolean isloading = new ObservableBoolean(true);
    private int count = 10;
    private CompositeDisposable disposable = new CompositeDisposable();

    public void searchForUser(boolean isLoadMore) {


        disposable.add(Global.initRetrofit().searchUser(search_text, count, userStart)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> isloading.set(true))
                .doOnTerminate(() -> {
                    onLoadMoreComplete.setValue(true);
                    isloading.set(false);
                })
                .subscribe((searchUser, throwable) -> {
                    if (searchUser != null && searchUser.getData() != null) {
                        if (isLoadMore) {
                            searchUseradapter.loadMore(searchUser.getData());
                        } else {
                            searchUseradapter.updateData(searchUser.getData());
                        }

                        userStart = userStart + count;
                    }
                    noUserData.set(searchUseradapter.mList.isEmpty());

                }));
    }

    public void searchForVideos(boolean isLoadMore) {

        disposable.add(Global.initRetrofit().searchVideo(search_text, count, videoStart, Global.USER_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnTerminate(() -> onLoadMoreComplete.setValue(true))
                .subscribe((searchVideo, throwable) -> {
                    if (searchVideo != null && searchVideo.getData() != null) {
                        if (isLoadMore) {
                            videoListAdapter.loadMore(searchVideo.getData());
                        } else {
                            videoListAdapter.updateData(searchVideo.getData());
                        }
                        isloading.set(false);
                        videoStart = videoStart + count;
                    }
                    noVideoData.set(videoListAdapter.mList.isEmpty());

                }));
    }

    public void afterTextChanged(CharSequence s) {
        search_text = s.toString();

        if (searchtype.get() == 1) {
            userStart = 0;
            searchForUser(false);
        } else {
            videoStart = 0;
            videoListAdapter.word = search_text;
            searchForVideos(false);
        }
    }

    public void onUserLoadMore() {

        searchForUser(true);
    }

    public void onVideoLoadMore() {
        searchForVideos(true);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }

}
