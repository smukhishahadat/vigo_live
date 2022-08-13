package com.bakbakum.shortvdo.viewmodel;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bakbakum.shortvdo.model.user.RestResponse;
import com.bakbakum.shortvdo.utils.Global;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class RedeemViewModel extends ViewModel {

    public String coindCount;
    public String coinRate;
    public String requestType;
    public String accountId;
    public ObservableBoolean isLoading = new ObservableBoolean();
    public MutableLiveData<RestResponse> redeem = new MutableLiveData<>();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void afterPaymentAccountChanged(CharSequence s) {
        accountId = s.toString();
    }

    public void callApiToRedeem() {

        disposable.add(Global.initRetrofit().sendRedeemRequest(Global.ACCESS_TOKEN, coindCount, requestType, accountId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> isLoading.set(true))
                .doOnTerminate(() -> isLoading.set(false))
                .subscribe((redeem, throwable) -> {
                    if (redeem != null && redeem.getStatus() != null) {
                        this.redeem.setValue(redeem);
                    }
                }));

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
