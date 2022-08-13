package com.bakbakum.shortvdo.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bakbakum.shortvdo.model.wallet.CoinRate;
import com.bakbakum.shortvdo.model.wallet.MyWallet;
import com.bakbakum.shortvdo.model.wallet.RewardingActions;
import com.bakbakum.shortvdo.utils.Global;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class WalletViewModel extends ViewModel {

    public MutableLiveData<MyWallet> myWallet = new MutableLiveData<>();
    public MutableLiveData<CoinRate> coinRate = new MutableLiveData<>();
    public MutableLiveData<RewardingActions> rewardingActions = new MutableLiveData<>();
    public List<RewardingActions.Data> rewardingActionsList = new ArrayList<>();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void fetchMyWallet() {
        disposable.add(Global.initRetrofit().getMyWalletDetails(Global.ACCESS_TOKEN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe((wallet, throwable) -> {
                    if (wallet != null && wallet.getStatus() != null) {
                        myWallet.setValue(wallet);
                    }
                }));
    }

    public void fetchRewardingActions() {
        disposable.add(Global.initRetrofit().getRewardingAction(Global.ACCESS_TOKEN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe((rewardingActions, throwable) -> {
                    if (rewardingActions != null && rewardingActions.getStatus() != null) {
                        rewardingActionsList = rewardingActions.getData();
                        this.rewardingActions.setValue(rewardingActions);
                    }
                }));
    }

    public void fetchCoinRate() {
        disposable.add(Global.initRetrofit().getCoinRate(Global.ACCESS_TOKEN)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe((coinRate, throwable) -> {
                    if (coinRate != null && coinRate.getStatus() != null) {
                        this.coinRate.setValue(coinRate);
                    }
                }));
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }

}
