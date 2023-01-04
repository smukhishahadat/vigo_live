package com.bakbakum.shortvdo.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bakbakum.shortvdo.utils.Global;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ReportViewModel extends ViewModel {

    public int reportType;
    public String postId;
    public String userId;
    public String reason;
    public String description = "";
    public String contactInfo = "";
    public MutableLiveData<Boolean> isValid = new MutableLiveData<>();
    public MutableLiveData<Boolean> isSuccess = new MutableLiveData<>();
    private CompositeDisposable disposable = new CompositeDisposable();

    public void afterUserNameTextChanged(CharSequence s) {
        description = s.toString();
    }

    public void afterContactDetailsChanged(CharSequence s) {
        contactInfo = s.toString();
    }


    public void callApiToReport() {
        if (!description.isEmpty() && !contactInfo.isEmpty()) {
            HashMap<String, Object> hashMap = new HashMap<>();
            if (reportType == 1) {
                hashMap.put("report_type", "report_video");
                hashMap.put("post_id", postId);
            }else if (reportType == 2) {
                hashMap.put("report_type", "block_user");
                hashMap.put("user_id", userId);
            } else {
                hashMap.put("report_type", "report_user");
                hashMap.put("user_id", userId);
            }
            hashMap.put("reason", reason);
            hashMap.put("description", description);
            hashMap.put("contact_info", contactInfo);

            disposable.add(Global.initRetrofit().reportSomething(hashMap)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe((report, throwable) -> {
                        if (report != null && report.getStatus()) {
                            isSuccess.setValue(true);
                        }
                    }));
        } else {
            isValid.setValue(false);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
