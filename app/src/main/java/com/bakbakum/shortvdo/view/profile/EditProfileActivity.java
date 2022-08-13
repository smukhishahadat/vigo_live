package com.bakbakum.shortvdo.view.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ActivityEditProfileBinding;
import com.bakbakum.shortvdo.view.base.BaseActivity;
import com.bakbakum.shortvdo.view.media.BottomSheetImagePicker;
import com.bakbakum.shortvdo.viewmodel.EditProfileViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import static com.bakbakum.shortvdo.utils.BindingAdapters.loadMediaImage;

public class EditProfileActivity extends BaseActivity {

    ActivityEditProfileBinding binding;
    private EditProfileViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile);
        startReceiver();
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new EditProfileViewModel()).createFor()).get(EditProfileViewModel.class);
        initView();
        initObserve();
        initListener();
        binding.setViewmodel(viewModel);
    }

    private void initView() {
        viewModel.user = sessionManager.getUser();
        viewModel.updateData();
        if (viewModel.user != null) {
            viewModel.cur_userName = sessionManager.getUser().getData().getUserName();
            if (viewModel.user.getData().getBio() != null && !viewModel.user.getData().getBio().isEmpty()) {
                viewModel.length.set(viewModel.user.getData().getBio().length());
            }
        }
    }

    private void initObserve() {
        viewModel.toast.observe(this, s -> {
            if (s != null && !s.isEmpty()) {
                Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.updateProfile.observe(this, isUpdate -> {
            if (isUpdate != null && isUpdate) {
                Intent intent = new Intent();
                intent.putExtra("user", new Gson().toJson(viewModel.user));
                sessionManager.saveUser(viewModel.user);
                setResult(RESULT_OK, intent);
                onBackPressed();
            }
        });
    }

    private void initListener() {
        binding.setOnChangeClick(view -> showPhotoSelectSheet());

        binding.imgBack.setOnClickListener(v -> onBackPressed());
    }


    private void showPhotoSelectSheet() {
        BottomSheetImagePicker bottomSheetImagePicker = new BottomSheetImagePicker();
        bottomSheetImagePicker.setOnDismiss(uri -> {
            if (!uri.isEmpty()) {
                loadMediaImage(binding.profileImg, uri, true);
                viewModel.imageUri = uri;
            }
        });
        bottomSheetImagePicker.show(getSupportFragmentManager(), BottomSheetImagePicker.class.getSimpleName());
    }

    @Override
    protected void onDestroy() {
        unregisterNetworkChanges();
        super.onDestroy();
    }


}