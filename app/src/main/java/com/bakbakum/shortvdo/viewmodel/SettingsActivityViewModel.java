package com.bakbakum.shortvdo.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bakbakum.shortvdo.model.user.RestResponse;
import com.bakbakum.shortvdo.utils.Global;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SettingsActivityViewModel extends ViewModel {

    public CompositeDisposable disposable = new CompositeDisposable();
    public MutableLiveData<RestResponse> logOut = new MutableLiveData<>();

    public void logOutUser() {
        disposable.add(Global.initRetrofit().logOutUser(Global.ACCESS_TOKEN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())

                .subscribe((logoutUser, throwable) -> {
                    if (logoutUser != null && logoutUser.getStatus() != null) {
                        logOut.setValue(logoutUser);
                    }
                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
