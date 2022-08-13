package com.bakbakum.shortvdo.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bakbakum.shortvdo.model.videos.Video;

public class ShareSheetViewModel extends ViewModel {

    public Video.Data video;
    public MutableLiveData<Integer> onItemClick = new MutableLiveData<>();
    public String shareUrl;

    public void setOnItemClick(int type) {
        onItemClick.setValue(type);
    }

}
