package com.bakbakum.shortvdo.viewmodel;

import android.util.Log;

import androidx.databinding.ObservableInt;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.utils.ProgressRequestBody;
import com.bakbakum.shortvdo.utils.SessionManager;

import java.io.File;
import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class PreviewViewModel extends ViewModel {
    public String postDescription = "", hashTag = "";
    public String videoPath = " ", videoThumbnail = "", soundImage = "", soundPath = "", soundId = "";
    public MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    public MutableLiveData<String> onClickUpload = new MutableLiveData<>();
    public ObservableInt position = new ObservableInt(0);
    public SessionManager sessionManager;
    private CompositeDisposable disposable = new CompositeDisposable();
    public boolean allowDuet = true, allowComments = true, saveLocally = true;
    public boolean compressing = false;
    public String video_privacy="1";

    public void onDescriptionTextChanged(CharSequence text) {
        postDescription = text.toString();
        position.set(postDescription.length());
    }

    public void uploadPost() {
        compressing = false;
        Log.e("PreviewViewModal", "upload");
        onClickUpload.setValue("hash");
        HashMap<String, RequestBody> hashMap = new HashMap<>();
        hashMap.put("post_description", toRequestBody(postDescription));
        hashMap.put("post_hash_tag", toRequestBody(hashTag));
        hashMap.put("is_orignal_sound", toRequestBody(soundPath == null || soundPath.isEmpty() ? "0" : "1"));
        hashMap.put("allow_duet", toRequestBody(allowDuet ? "1" : "0"));
        hashMap.put("allow_comments", toRequestBody(allowComments ? "1" : "0"));
        hashMap.put("video_privacy", toRequestBody(video_privacy));

        if (soundId != null && !soundId.isEmpty()) {
            hashMap.put("sound_id", toRequestBody(soundId));
        } else {
            hashMap.put("sound_title", toRequestBody("Original sound by " + sessionManager.getUser().getData().getFullName()));
            hashMap.put("duration", toRequestBody("1:00"));
            hashMap.put("singer", toRequestBody(sessionManager.getUser().getData().getUserName()));
        }
        MultipartBody.Part body;

        File file = new File(videoPath);
        ProgressRequestBody requestBody = new ProgressRequestBody(file, percentage -> {

        });


        body = MultipartBody.Part.createFormData("post_video", file.getName(), requestBody);

        File file1 = new File(videoThumbnail);
        ProgressRequestBody requestBody2 = new ProgressRequestBody(file1, percentage -> {

        });

        MultipartBody.Part body1 = MultipartBody.Part.createFormData("post_image", file1.getName(), requestBody2);
        MultipartBody.Part body2 = null;
        MultipartBody.Part body3 = null;
        if (soundId == null || soundId.isEmpty()) {

            File file2 = new File(soundPath);
            RequestBody requestFile2 =
                    RequestBody.create(MediaType.parse("multipart/form-data"), file2);

            body2 = MultipartBody.Part.createFormData("post_sound", file2.getName(), requestFile2);

            if (soundImage != null) {
                File file3 = new File(soundImage);
                RequestBody requestFile3 =
                        RequestBody.create(MediaType.parse("multipart/form-data"), file3);

                body3 = MultipartBody.Part.createFormData("sound_image", file1.getName(), requestFile3);
            }
        }
        disposable.add(Global.initRetrofit().uploadPost(Global.ACCESS_TOKEN, hashMap, body, body1, body2, body3)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(disposable1 -> isLoading.setValue(true))
                .doOnTerminate(() -> {
                    isLoading.setValue(false);
                    if (!saveLocally) {
                        try {
                            file.delete();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                })
                .subscribe((updateUser, throwable) -> {
                }));
    }

    public RequestBody toRequestBody(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
