package com.bakbakum.shortvdo.view.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ActivityVerificationBinding;
import com.bakbakum.shortvdo.utils.CustomDialogBuilder;
import com.bakbakum.shortvdo.view.base.BaseActivity;
import com.bakbakum.shortvdo.view.media.BottomSheetImagePicker;
import com.bakbakum.shortvdo.viewmodel.VerificationViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

import static com.bakbakum.shortvdo.utils.BindingAdapters.loadMediaImage;
import static com.bakbakum.shortvdo.utils.BindingAdapters.loadMediaRoundBitmap;

public class VerificationActivity extends BaseActivity {

    private static int CAPTURE_IMAGE = 100;
    ActivityVerificationBinding binding;
    VerificationViewModel viewModel;
    CustomDialogBuilder customDialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_verification);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new VerificationViewModel()).createFor()).get(VerificationViewModel.class);
        customDialogBuilder = new CustomDialogBuilder(this);
        initObserve();
        initListeners();
        binding.setViewModel(viewModel);
    }

    private void initObserve() {
        viewModel.isLoading.observe(this, isLoading -> {
            if (isLoading) {
                customDialogBuilder.showLoadingDialog();
            } else {
                customDialogBuilder.hideLoadingDialog();
                onBackPressed();
            }
        });
    }

    private void initListeners() {
        binding.imgBack.setOnClickListener(v -> onBackPressed());

        binding.setOnCaptureClick(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 10);

            } else {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                startActivityForResult(cameraIntent, CAPTURE_IMAGE);
            }
        });

        binding.setOnAttachClick(v -> showPhotoSelectSheet());

    }

    private void showPhotoSelectSheet() {

        BottomSheetImagePicker bottomSheetImagePicker = new BottomSheetImagePicker();
        bottomSheetImagePicker.setOnDismiss(uri -> {
            if (!uri.isEmpty()) {
                loadMediaImage(binding.ivProof, uri, false);
                viewModel.proofUri = uri;
            }
        });
        bottomSheetImagePicker.show(getSupportFragmentManager(), BottomSheetImagePicker.class.getSimpleName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == CAPTURE_IMAGE) {
            //if (data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path + File.separator + getString(R.string.app_name) + File.separator + "image" + File.separator);
            if (!file.exists()) file.mkdirs();

            File thumbFile = new File(file, "verification.jpg");

            if (thumbFile.exists()) {
                thumbFile.delete();
            }
            try {
                FileOutputStream stream = new FileOutputStream(thumbFile);

                if (photo != null) {
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                }
                stream.flush();
                stream.close();

                viewModel.useUri = thumbFile.getAbsolutePath();
                Bitmap bitmap = BitmapFactory.decodeFile(viewModel.useUri);
                loadMediaRoundBitmap(binding.roundImg, bitmap);
                Toast.makeText(this, "Image Captured", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            //}
        }
        //super.onActivityResult(requestCode, resultCode, data);
    }

    public File getPath() {
        String state = Environment.getExternalStorageState();
        File filesDir;
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            filesDir = getExternalFilesDir(null);
        } else {
            // Load another directory, probably local memory
            filesDir = getFilesDir();
        }

        return filesDir;
    }
}