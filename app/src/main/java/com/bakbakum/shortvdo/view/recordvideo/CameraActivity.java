package com.bakbakum.shortvdo.view.recordvideo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraX;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.VideoCapture;
import androidx.camera.core.VideoCaptureConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coremedia.iso.boxes.Container;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.otaliastudios.transcoder.Transcoder;
import com.otaliastudios.transcoder.TranscoderListener;
import com.otaliastudios.transcoder.TranscoderOptions;
import com.otaliastudios.transcoder.engine.TrackType;
import com.otaliastudios.transcoder.strategy.DefaultVideoStrategy;
import com.otaliastudios.transcoder.strategy.size.AtMostResizer;
import com.otaliastudios.transcoder.strategy.size.FractionResizer;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ActivityCameraBinding;
import com.bakbakum.shortvdo.databinding.DailogProgressBinding;
//import com.tejash.tejtok.utils.AutoFitPreviewBuilder;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.CustomDialogBuilder;
import com.bakbakum.shortvdo.view.base.BaseActivity;
import com.bakbakum.shortvdo.view.media.BottomSheetImagePicker;
import com.bakbakum.shortvdo.view.music.MusicFrameFragment;
import com.bakbakum.shortvdo.view.preview.PreviewActivity;
import com.bakbakum.shortvdo.viewmodel.CameraViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE;

public class CameraActivity extends BaseActivity {

    private static final int MY_PERMISSIONS_REQUEST = 101;
    public CameraViewModel viewModel;
    private ActivityCameraBinding binding;
    private CustomDialogBuilder customDialogBuilder;
    private Dialog mBuilder;
    private DailogProgressBinding progressBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new CameraViewModel()).createFor()).get(CameraViewModel.class);
        customDialogBuilder = new CustomDialogBuilder(CameraActivity.this);
        initView();
        initListener();
        initObserve();
        initProgressDialog();
        binding.setViewModel(viewModel);
    }

    @SuppressLint("RestrictedApi")
    private void initView() {
        String musicUrl = getIntent().getStringExtra("music_url");
        if (musicUrl != null && !musicUrl.isEmpty()) {
            downLoadMusic(getIntent().getStringExtra("music_url"));
            if (getIntent().getStringExtra("music_title") != null) {
                binding.tvSoundTitle.setText(getIntent().getStringExtra("music_title"));
            }
            if (getIntent().getStringExtra("soundId") != null) {
                viewModel.soundId = getIntent().getStringExtra("soundId");
            }
        }

        if (viewModel.onDurationUpdate.getValue() != null) {
            binding.progressBar.enableAutoProgressView(viewModel.onDurationUpdate.getValue());
        }
        binding.progressBar.setDividerColor(Color.WHITE);
        binding.progressBar.setDividerEnabled(true);
        binding.progressBar.setDividerWidth(4);
        binding.progressBar.SetListener(mills -> {
            viewModel.isEnabled.set(mills >= 14500);
            if (mills == viewModel.onDurationUpdate.getValue()) {
                stopRecording();
            }
        });
        binding.ivSelect.setOnClickListener(v -> {
            if (!viewModel.isRecording.get() && (binding.progressBar.timePassed >= 14500 ||
                    binding.progressBar.timePassed >= viewModel.onDurationUpdate.getValue() - 500)) {
                saveData(true);
            } else {
                Toast.makeText(this, "Make sure video is longer than 15s...!", Toast.LENGTH_LONG).show();
            }
        });
        binding.progressBar.setShader(new int[]{ContextCompat.getColor(this, R.color.colorTheme2), ContextCompat.getColor(this, R.color.colorTheme1), ContextCompat.getColor(this, R.color.colorTheme)});
    }

    private void initListener() {
        binding.btnCapture.setOnClickListener(v -> {
            if (!viewModel.isRecording.get()) {
                startReCording();
            } else {
                stopRecording();
            }
        });
        binding.btnFlip.setOnClickListener(v -> {
            viewModel.isFacingFront.set(!viewModel.isFacingFront.get());
            if (viewModel.isFacingFront.get()) {
                viewModel.lensFacing = CameraX.LensFacing.FRONT;
            } else {
                viewModel.lensFacing = CameraX.LensFacing.BACK;
            }
            recreateCamera();
        });
        binding.tvSelect.setOnClickListener(v -> {
            BottomSheetImagePicker bottomSheetImagePicker = BottomSheetImagePicker.Companion.getNewInstance(1);
            bottomSheetImagePicker.setOnDismiss(uri -> {
                if (!uri.isEmpty()) {
                    if (uri.contains(".gif")) {
                        Toast.makeText(this, "GIF Not Supported", Toast.LENGTH_SHORT).show();
                    } else {
                        File file = new File(uri);
                        // Get length of file in bytes
                        long fileSizeInBytes = file.length();
                        // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
                        long fileSizeInKB = fileSizeInBytes / 1024;
                        // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
                        long fileSizeInMB = fileSizeInKB / 1024;
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(this, Uri.fromFile(new File(uri)));
                        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                        long timeInMilliSec = Long.parseLong(time);
                        if (timeInMilliSec > 60000) {
                            customDialogBuilder.showSimpleDialog("Too long video", "This video is longer than 1 min.\nPlease select onOther..",
                                    "Cancel", "Select onOther", new CustomDialogBuilder.OnDismissListener() {
                                        @Override
                                        public void onPositiveDismiss() {
                                            bottomSheetImagePicker.show(getSupportFragmentManager(), BottomSheetImagePicker.class.getSimpleName());
                                        }

                                        @Override
                                        public void onNegativeDismiss() {

                                        }
                                    });
                        } else if (fileSizeInMB < 60) {

                            viewModel.videoPaths = new ArrayList<>();
                            viewModel.videoPaths.add(uri);
                            if (fileSizeInMB > 5) {
                                saveData(true);
                            } else {
                                customDialogBuilder.showLoadingDialog();
                                saveData(false);
                            }
                        } else {
                            customDialogBuilder.showSimpleDialog("Too long video's size", "This video's size is grater than 60Mb.\nPlease select onOther..",
                                    "Cancel", "Select onOther", new CustomDialogBuilder.OnDismissListener() {
                                        @Override
                                        public void onPositiveDismiss() {
                                            bottomSheetImagePicker.show(getSupportFragmentManager(), BottomSheetImagePicker.class.getSimpleName());
                                        }

                                        @Override
                                        public void onNegativeDismiss() {

                                        }
                                    });
                        }
                        retriever.release();
                    }
                }
            });
            bottomSheetImagePicker.show(getSupportFragmentManager(), BottomSheetImagePicker.class.getSimpleName());
        });
        binding.ivClose.setOnClickListener(v -> customDialogBuilder.showSimpleDialog("Are you sure", "Do you really wan to go back ?",
                "No", "Yes", new CustomDialogBuilder.OnDismissListener() {
                    @Override
                    public void onPositiveDismiss() {
                        //1.86
                        onBackPressed();
                    }

                    @Override
                    public void onNegativeDismiss() {

                    }
                }));
    }


    private void recreateCamera() {
        CameraX.unbindAll();
        startCamera();
    }


    @SuppressLint("RestrictedApi")
    private void startCamera() {
        binding.viewFinder.setSystemUiVisibility(SYSTEM_UI_FLAG_LAYOUT_STABLE);
        TextureView viewFinder = binding.viewFinder;
        AspectRatio ratio = AspectRatio.RATIO_16_9;
        viewModel.builder = new PreviewConfig.Builder();
        viewModel.previewConfig = viewModel.builder.setTargetAspectRatio(ratio)
                .setLensFacing(viewModel.lensFacing)
                .setTargetRotation(Surface.ROTATION_90)
                .build();
        //viewModel.preview = AutoFitPreviewBuilder.Companion.build(viewModel.previewConfig, viewFinder);
        viewModel.builder1 = new VideoCaptureConfig.Builder();
        viewModel.videoCaptureConfig = viewModel.builder1.setTargetAspectRatio(ratio)
                .setLensFacing(viewModel.lensFacing)
                .setVideoFrameRate(24)
                .setTargetRotation(Surface.ROTATION_0)
                .build();
        viewModel.videoCapture = new VideoCapture(viewModel.videoCaptureConfig);
        CameraX.bindToLifecycle(this, viewModel.preview, viewModel.videoCapture);
    }

    private void initObserve() {
        viewModel.parentPath = getPath().getPath();
        viewModel.onItemClick.observe(this, type -> {
            if (type != null) {
                if (type == 1) {
                    MusicFrameFragment frameFragment = new MusicFrameFragment();
                    frameFragment.show(getSupportFragmentManager(), frameFragment.getClass().getSimpleName());
                }
                viewModel.onItemClick.setValue(null);
            }
        });
        viewModel.onSoundSelect.observe(this, sound -> {
            if (sound != null) {
                binding.tvSoundTitle.setText(sound.getSoundTitle());
                viewModel.soundId = sound.getSoundId();
                downLoadMusic(sound.getSound());
            }
        });
        viewModel.onDurationUpdate.observe(this, aLong -> binding.progressBar.enableAutoProgressView(aLong));
    }

    @SuppressLint("RestrictedApi")
    private void stopRecording() {
        binding.btnCapture.clearAnimation();
        if (viewModel.audio != null) {
            viewModel.audio.pause();
        }
        viewModel.count += 1;
        binding.progressBar.pause();
        binding.progressBar.addDivider();
        viewModel.isRecording.set(false);
        viewModel.videoCapture.stopRecording();
    }

    private void initPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST);
        } else {
            recreateCamera();
        }
    }

    public void initProgressDialog() {
        mBuilder = new Dialog(this);
        mBuilder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBuilder.setCancelable(false);
        mBuilder.setCanceledOnTouchOutside(false);
        if (mBuilder.getWindow() != null) {
            mBuilder.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        mBuilder.setCancelable(false);
        mBuilder.setCanceledOnTouchOutside(false);
        progressBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dailog_progress, null, false);
        progressBinding.progressBar.setShader(new int[]{ContextCompat.getColor(this, R.color.colorTheme2), ContextCompat.getColor(this, R.color.colorTheme1), ContextCompat.getColor(this, R.color.colorTheme)});

        Animation rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
        Animation reverseAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_reverse);
        progressBinding.ivParent.startAnimation(rotateAnimation);
        progressBinding.ivChild.startAnimation(reverseAnimation);
        mBuilder.setContentView(progressBinding.getRoot());
    }

    public void showProgressDialog() {
        if (!mBuilder.isShowing()) {
            mBuilder.show();
        }
    }

    public void hideProgressDialog() {
        try {
            mBuilder.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    onBackPressed();
                }
                recreateCamera();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void downLoadMusic(String endPoint) {

        PRDownloader.download(Const.ITEM_BASE_URL + endPoint, getPath().getPath(), "recordSound.aac")
                .build()
                .setOnStartOrResumeListener(() -> customDialogBuilder.showLoadingDialog())
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        customDialogBuilder.hideLoadingDialog();
                        viewModel.isStartRecording.set(true);
                        viewModel.createAudioForCamera();
                    }

                    @Override
                    public void onError(Error error) {
                        customDialogBuilder.hideLoadingDialog();
                    }
                });
    }

    @SuppressLint("RestrictedApi")
    private void startReCording() {
        viewModel.isStartRecording.set(true);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale);
        binding.btnCapture.startAnimation(animation);
        if (binding.progressBar.getProgressPercent() != 100) {
            if (viewModel.audio != null) {
                viewModel.audio.start();
            }
            File file = new File(getPath(), "video".concat(String.valueOf(viewModel.count)).concat(".mp4"));
            viewModel.videoPaths.add(getPath().getPath().concat("/video").concat(String.valueOf(viewModel.count)).concat(".mp4"));
            viewModel.videoCapture.startRecording(file, Runnable::run, new VideoCapture.OnVideoSavedListener() {
                @Override
                public void onVideoSaved(@NonNull File file) {
                }

                @Override
                public void onError(@NonNull VideoCapture.VideoCaptureError videoCaptureError, @NonNull String message, @Nullable Throwable cause) {

                }
            });
            binding.progressBar.resume();
            viewModel.isRecording.set(true);
        }
    }

    public void saveData(boolean isCompress) {
        if (isCompress) {
            DefaultVideoStrategy strategy = new DefaultVideoStrategy.Builder()
                    .addResizer(new FractionResizer(0.7F))
                    .addResizer(new AtMostResizer(1000))
                    .build();
            TranscoderOptions.Builder options = Transcoder.into(getPath().getPath().concat("/append.mp4"));
            for (int i = 0; i < viewModel.videoPaths.size(); i++) {
                options.addDataSource(viewModel.videoPaths.get(i));
            }
            if (viewModel.audio == null) {
                options.setVideoTrackStrategy(strategy);
            }
            options.setListener(new TranscoderListener() {
                public void onTranscodeProgress(double progress) {
                    showProgressDialog();
                    if (progressBinding != null) {
                        if (viewModel.audio != null) {
                            progressBinding.progressBar.publishProgress((float) progress / 2);
                        } else {
                            progressBinding.progressBar.publishProgress((float) progress);
                        }
                    }
                    Log.d("TAG", "onTranscodeProgress: " + progress);
                }

                public void onTranscodeCompleted(int successCode) {
                    Log.d("TAG", "onTranscodeCompleted: " + successCode);
                    if (viewModel.audio != null) {

                        Transcoder.into(getPath().getPath().concat("/finally.mp4"))
                                .addDataSource(TrackType.VIDEO, getPath().getPath().concat("/append.mp4"))
                                .addDataSource(TrackType.AUDIO, getPath().getPath().concat("/recordSound.aac"))
                                .setVideoTrackStrategy(strategy)
                                .setListener(new TranscoderListener() {
                                    @Override
                                    public void onTranscodeProgress(double progress) {
                                        if (progressBinding != null && viewModel.audio != null) {
                                            progressBinding.progressBar.publishProgress((float) (1 + progress) / 2);
                                        }
                                    }

                                    @Override
                                    public void onTranscodeCompleted(int successCode) {
                                        File thumbFile = new File(getPath(), "temp.jpg");
                                        try {
                                            FileOutputStream stream = new FileOutputStream(thumbFile);

                                            Bitmap bmThumbnail;
                                            bmThumbnail = ThumbnailUtils.createVideoThumbnail(getPath().getPath().concat("/append.mp4"),
                                                    MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

                                            if (bmThumbnail != null) {
                                                bmThumbnail.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                            }
                                            stream.flush();
                                            stream.close();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        hideProgressDialog();
                                        Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
                                        intent.putExtra("post_video", getPath().getPath().concat("/finally.mp4"));
                                        intent.putExtra("post_image", thumbFile.getPath());
                                        if (viewModel.soundId != null && !viewModel.soundId.isEmpty()) {
                                            intent.putExtra("soundId", viewModel.soundId);
                                        }
//                                        intent.putExtra("post_sound", getPath().getPath().concat("/originalSound.aac"));
//                                        intent.putExtra("sound_image", getPath().getPath().concat("/soundImage.jpeg"));
                                        startActivityForResult(intent, 101);
                                    }

                                    @Override
                                    public void onTranscodeCanceled() {

                                    }

                                    @Override
                                    public void onTranscodeFailed(@NonNull Throwable exception) {

                                    }
                                }).transcode();

                    } else {
                        Log.i("TAG", "onCombineFinished: " + "is original sound");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Track audio;
                                try {
                                    Movie m1 = MovieCreator.build(getPath().getPath().concat("/append.mp4"));
                                    audio = m1.getTracks().get(1);
                                    Movie m2 = new Movie();
                                    m2.addTrack(audio);
                                    DefaultMp4Builder builder = new DefaultMp4Builder();
                                    Container stdMp4 = builder.build(m2);
                                    FileOutputStream fos = new FileOutputStream(getPath().getPath().concat("/originalSound.aac"));
                                    stdMp4.writeContainer(fos.getChannel());
                                    fos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                File thumbFile = new File(getPath(), "temp.jpg");
                                try {
                                    FileOutputStream stream = new FileOutputStream(thumbFile);

                                    Bitmap bmThumbnail;
                                    bmThumbnail = ThumbnailUtils.createVideoThumbnail(getPath().getPath().concat("/append.mp4"),
                                            MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

                                    if (bmThumbnail != null) {
                                        bmThumbnail.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    }
                                    stream.flush();
                                    stream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Glide.with(CameraActivity.this)
                                        .asBitmap()
                                        .load(Const.ITEM_BASE_URL + sessionManager.getUser().getData().getUserProfile())
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                File soundImage = new File(getPath(), "soundImage.jpeg");
                                                try {
                                                    FileOutputStream stream = new FileOutputStream(soundImage);
                                                    resource.compress(Bitmap.CompressFormat.JPEG, 10, stream);
                                                    stream.flush();
                                                    stream.close();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                hideProgressDialog();
                                                Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
                                                intent.putExtra("post_video", getPath().getPath().concat("/append.mp4"));
                                                intent.putExtra("post_image", thumbFile.getPath());
                                                intent.putExtra("post_sound", getPath().getPath().concat("/originalSound.aac"));
                                                intent.putExtra("sound_image", getPath().getPath().concat("/soundImage.jpeg"));
                                                startActivityForResult(intent, 101);
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                            }

                                            @Override
                                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                                super.onLoadFailed(errorDrawable);

                                                hideProgressDialog();
                                                Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
                                                intent.putExtra("post_video", getPath().getPath().concat("/append.mp4"));
                                                intent.putExtra("post_image", thumbFile.getPath());
                                                intent.putExtra("post_sound", getPath().getPath().concat("/originalSound.aac"));

                                                startActivityForResult(intent, 101);
                                            }
                                        });

                            }
                        }).start();
                    }
                }

                public void onTranscodeCanceled() {
                    Log.d("TAG", "onTranscodeCanceled: ");
                }

                public void onTranscodeFailed(@NonNull Throwable exception) {
                    Log.d("TAG", "onTranscodeCanceled: " + exception);
                }
            }).transcode();

        } else {
            new Thread(() -> {
                Track audio;
                try {
                    Movie m1 = MovieCreator.build(viewModel.videoPaths.get(0));
                    if (m1.getTracks().size() >= 2) {
                        audio = m1.getTracks().get(1);
                        Movie m2 = new Movie();
                        m2.addTrack(audio);
                        DefaultMp4Builder builder = new DefaultMp4Builder();
                        Container stdMp4 = builder.build(m2);
                        FileOutputStream fos = new FileOutputStream(getPath().getPath().concat("/originalSound.aac"));
                        stdMp4.writeContainer(fos.getChannel());
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File thumbFile = new File(getPath(), "temp.jpg");
                try {
                    FileOutputStream stream = new FileOutputStream(thumbFile);

                    Bitmap bmThumbnail;
                    bmThumbnail = ThumbnailUtils.createVideoThumbnail(viewModel.videoPaths.get(0),
                            MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);

                    if (bmThumbnail != null) {
                        bmThumbnail.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    }
                    stream.flush();
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Glide.with(CameraActivity.this)
                        .asBitmap()
                        .load(Const.ITEM_BASE_URL + sessionManager.getUser().getData().getUserProfile())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                File soundImage = new File(getPath(), "soundImage.jpeg");
                                try {
                                    FileOutputStream stream = new FileOutputStream(soundImage);
                                    resource.compress(Bitmap.CompressFormat.JPEG, 10, stream);
                                    stream.flush();
                                    stream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                customDialogBuilder.hideLoadingDialog();
                                Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
                                intent.putExtra("post_video", viewModel.videoPaths.get(0));
                                intent.putExtra("post_image", thumbFile.getPath());
                                File file = new File(getPath().getPath().concat("/originalSound.aac"));
                                if (file.exists()) {
                                    intent.putExtra("post_sound", getPath().getPath().concat("/originalSound.aac"));
                                }
                                intent.putExtra("sound_image", getPath().getPath().concat("/soundImage.jpeg"));
                                startActivityForResult(intent, 101);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                customDialogBuilder.hideLoadingDialog();
                                Intent intent = new Intent(CameraActivity.this, PreviewActivity.class);
                                intent.putExtra("post_video", viewModel.videoPaths.get(0));
                                intent.putExtra("post_image", thumbFile.getPath());
                                File file = new File(getPath().getPath().concat("/originalSound.aac"));
                                if (file.exists()) {
                                    intent.putExtra("post_sound", getPath().getPath().concat("/originalSound.aac"));
                                }
                                startActivityForResult(intent, 101);
                            }
                        });

            }).start();
        }

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
        if (filesDir != null) {
            viewModel.parentPath = filesDir.getPath();
        }
        return filesDir;
    }

    @Override
    protected void onDestroy() {
        if (viewModel.isRecording.get()) {
            stopRecording();
        }
        CameraX.unbindAll();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        initPermission();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (viewModel.isRecording.get()) {
            stopRecording();
        }
        CameraX.unbindAll();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (viewModel.isRecording.get()) {
            stopRecording();
        }
        CameraX.unbindAll();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        stopRecording();
        CameraX.unbindAll();
        setResult(RESULT_OK);
        super.onBackPressed();
    }
}
