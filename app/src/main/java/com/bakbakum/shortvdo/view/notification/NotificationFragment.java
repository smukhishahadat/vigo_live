package com.bakbakum.shortvdo.view.notification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.FragmentNotificationBinding;
import com.bakbakum.shortvdo.viewmodel.MainViewModel;
import com.bakbakum.shortvdo.viewmodel.NotificationViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import org.jetbrains.annotations.NotNull;

public class NotificationFragment extends Fragment {

    FragmentNotificationBinding binding;
    NotificationViewModel viewModel;
    private MainViewModel parentViewModel;

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getParentFragment() != null) {
            parentViewModel = ViewModelProviders.of(getParentFragment()).get(MainViewModel.class);
        }
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new NotificationViewModel()).createFor()).get(NotificationViewModel.class);
        initView();
        initListeners();
        initObserve();
        binding.setViewModel(viewModel);
        super.onViewCreated(view, savedInstanceState);
    }

    private void initView() {

        binding.refreshlout.setEnableRefresh(false);
        viewModel.fetchNotificationData(false);

    }


    private void initListeners() {
        binding.refreshlout.setOnLoadMoreListener(refreshLayout -> viewModel.fetchNotificationData(true));
    }

    private void initObserve() {
        parentViewModel.selectedPosition.observe(this, position -> {

            if (position != null && position == 2) {
                viewModel.start = 0;
                viewModel.fetchNotificationData(false);
            }
        });
        viewModel.onLoadMoreComplete.observe(this, onLoadMore -> binding.refreshlout.finishLoadMore());
    }
}
