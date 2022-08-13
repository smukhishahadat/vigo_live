package com.bakbakum.shortvdo.viewmodel;

import androidx.databinding.ObservableInt;
import androidx.lifecycle.ViewModel;

import com.bakbakum.shortvdo.model.user.User;

public class FollowerFollowingViewModel extends ViewModel {

    public ObservableInt itemType = new ObservableInt(0);
    public User user;


}
