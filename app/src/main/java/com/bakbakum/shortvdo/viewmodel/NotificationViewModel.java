package com.bakbakum.shortvdo.viewmodel;

import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bakbakum.shortvdo.adapter.NotificationAdapter;
import com.bakbakum.shortvdo.utils.Global;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class NotificationViewModel extends ViewModel {
    public String userId;
    public int count = 15, start = 0;
    public MutableLiveData<Boolean> onLoadMoreComplete = new MutableLiveData<>();
    public NotificationAdapter adapter = new NotificationAdapter();
    public ObservableBoolean isEmpty = new ObservableBoolean(false);
    private CompositeDisposable disposable = new CompositeDisposable();
    private ObservableBoolean isLoading = new ObservableBoolean();

    public void fetchNotificationData(boolean isLoadMore) {

        disposable.add(Global.initRetrofit().getNotificationList(Global.ACCESS_TOKEN, Global.USER_ID, count, start)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> isLoading.set(true))
                .doOnTerminate(() -> {
                    onLoadMoreComplete.setValue(true);
                    isLoading.set(false);
                })
                .subscribe((notification, throwable) -> {
                    if (notification != null && notification.getData() != null) {
                        if (isLoadMore) {
                            adapter.loadMore(notification.getData());
                        } else {
                            adapter.updateData(notification.getData());
                        }
                        start = start + count;
                    }
                    isEmpty.set(adapter.mList.isEmpty());
                }));
    }
}
