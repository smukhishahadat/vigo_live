package com.bakbakum.shortvdo.viewmodel;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bakbakum.shortvdo.adapter.CoinPlansAdapter;
import com.bakbakum.shortvdo.model.user.RestResponse;
import com.bakbakum.shortvdo.utils.Global;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class CoinPurchaseViewModel extends ViewModel {

    public CoinPlansAdapter adapter = new CoinPlansAdapter();
    public ObservableBoolean isLoading = new ObservableBoolean(false);
    public MutableLiveData<RestResponse> purchase = new MutableLiveData<>();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void fetchCoinPlans() {

        disposable.add(Global.initRetrofit().getCoinPlans(Global.ACCESS_TOKEN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> isLoading.set(true))
                .doOnTerminate(() -> isLoading.set(false))
                .subscribe((purchase, throwable) -> {

                    if (purchase != null && purchase.getData() != null && !purchase.getData().isEmpty()) {
                        adapter.updateData(purchase.getData());
                    }

                }));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
