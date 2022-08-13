package com.bakbakum.shortvdo.viewmodel;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.Gson;
import com.bakbakum.shortvdo.adapter.ProfileVideosAdapter;
import com.bakbakum.shortvdo.utils.Global;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ProfileVideosViewModel extends ViewModel {
    public int userVidStart = 0;
    public int likeVidStart = 0;
    public int count = 10;
    public int vidType;
    public ProfileVideosAdapter adapter = new ProfileVideosAdapter();
    public String userId = Global.USER_ID;
    public ObservableBoolean noUserVideos = new ObservableBoolean(false);
    public ObservableBoolean noLikedVideos = new ObservableBoolean(false);
    public MutableLiveData<Boolean> onLoadMoreComplete = new MutableLiveData<>();
    ObservableBoolean isloading = new ObservableBoolean(true);
    private CompositeDisposable disposable = new CompositeDisposable();


    public void fetchUserVideos(boolean isLoadMore) {
        adapter.videosViewModel = this;
        disposable.add(Global.initRetrofit().getUserVideos(userId, count, userVidStart, Global.USER_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> isloading.set(true))
                .doOnTerminate(() -> {
                    onLoadMoreComplete.setValue(true);
                    isloading.set(false);
                })
                .subscribe((videos, throwable) -> {
                    if (videos != null && videos.getData() != null) {
                        if (isLoadMore) {
                            adapter.loadMore(videos.getData());
                        } else {
                            if (!new Gson().toJson(videos.getData()).equals(new Gson().toJson(adapter.mList))) {
                                adapter.updateData(videos.getData());
                                noUserVideos.set(adapter.mList.isEmpty());
                            }
                        }
                        userVidStart = userVidStart + count;

                    }

                }));
    }

    public void fetchUserLikedVideos(boolean isLoadMore) {
        adapter.videosViewModel = this;

        disposable.add(Global.initRetrofit().getUserLikedVideos(userId, count, likeVidStart, Global.USER_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> isloading.set(true))
                .doOnTerminate(() -> {
                    onLoadMoreComplete.setValue(true);
                    isloading.set(false);
                })
                .subscribe((videos, throwable) -> {
                    if (videos != null && videos.getData() != null) {
                        if (isLoadMore) {
                            adapter.loadMore(videos.getData());
                        } else {
                            if (!new Gson().toJson(videos.getData()).equals(new Gson().toJson(adapter.mList))) {
                                adapter.updateData(videos.getData());
                                noLikedVideos.set(adapter.mList.isEmpty());
                            }
                        }
                        likeVidStart = likeVidStart + count;
                    }

                }));
    }


    public void onUserVideoLoadMore() {
        fetchUserVideos(true);
    }

    public void onUserLikedVideoLoadMore() {
        fetchUserLikedVideos(true);
    }


    public void deletePost(String postId, int position) {
        disposable.add(Global.initRetrofit().deletePost(Global.ACCESS_TOKEN, postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe((deletePost, throwable) -> {

                    if (deletePost != null && deletePost.getStatus() != null) {
                        adapter.mList.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeRemoved(position, adapter.mList.size());

                    }
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
