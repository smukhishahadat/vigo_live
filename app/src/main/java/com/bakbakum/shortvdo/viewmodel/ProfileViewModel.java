package com.bakbakum.shortvdo.viewmodel;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import android.webkit.URLUtil;
import android.widget.Toast;


import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bakbakum.shortvdo.model.user.RestResponse;
import com.bakbakum.shortvdo.model.user.User;
import com.bakbakum.shortvdo.utils.Global;

import java.net.MalformedURLException;
import java.net.URL;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ProfileViewModel extends ViewModel {

    public MutableLiveData<User> user = new MutableLiveData<>();
    public MutableLiveData<Integer> onItemClick = new MutableLiveData<>();
    public MutableLiveData<Integer> selectPosition = new MutableLiveData<>();
    public MutableLiveData<Intent> intent = new MutableLiveData<>();
    public ObservableBoolean isloading = new ObservableBoolean(false);
    public ObservableBoolean isBackBtn = new ObservableBoolean(false);
    public String userId = "";
    public MutableLiveData<RestResponse> followApi = new MutableLiveData<>();
    public ObservableBoolean isLikedVideos = new ObservableBoolean(false);
    public ObservableInt isMyAccount = new ObservableInt(0);
    private CompositeDisposable disposable = new CompositeDisposable();

    public void setOnItemClick(int type) {

        onItemClick.setValue(type);
    }

    public void onSocialClick(int type) {
        String url = "";
        String uri="";
        if (user.getValue() != null) {
            switch (type) {
                case 1:
                    url = user.getValue().getData().getFbUrl();
                    uri="https://facebook.com/";
                    break;
                case 2:
                    url = user.getValue().getData().getInstaUrl();
                    uri="https://www.instagram.com/";
                    break;
                case 3:
                    url = user.getValue().getData().getTwitterUrl();
                    uri="https://www.twitter.com/";
                    break;
                default:
                    url = user.getValue().getData().getYoutubeUrl();
                    uri="https://youtube.com/";
                    break;
            }
        }
        if (!IsValidUrl(url)) {
            url=uri+url;
        }
        //Log.e("PreviewViewModel", url);
        intent.setValue(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    public static boolean IsValidUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            return URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString).matches();
        } catch (MalformedURLException ignored) {
        }
        return false;
    }

    public void fetchUserById(String userid) {
        disposable.add(Global.initRetrofit().getUserDetails(userid, Global.USER_ID)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> isloading.set(true))
                .doOnTerminate(() -> isloading.set(false))
                .subscribe((user, throwable) -> {
                    if (user != null && user.getData() != null) {
                        this.user.setValue(user);
                        if (isMyAccount.get() != 0) {
                            if (user.getData().getIsFollowing() == 1) {
                                isMyAccount.set(1);
                            } else {
                                isMyAccount.set(2);
                            }
                        }
                    }
                }));
    }

    public void followUnfollow() {

        disposable.add(Global.initRetrofit().followUnFollow(Global.ACCESS_TOKEN, userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe((followRequest, throwable) -> {
                    if (followRequest != null && followRequest.getStatus() != null) {
                        if (isMyAccount.get() == 1) {
                            isMyAccount.set(2);
                        } else {
                            isMyAccount.set(1);
                        }
                        followApi.setValue(followRequest);
                    }
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
