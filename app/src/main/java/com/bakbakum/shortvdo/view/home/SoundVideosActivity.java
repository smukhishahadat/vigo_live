package com.bakbakum.shortvdo.view.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.aliyun.svideo.recorder.activity.AlivcSvideoRecordActivity;
import com.aliyun.svideo.recorder.bean.AlivcRecordInputParam;
import com.aliyun.svideo.recorder.bean.RenderingMode;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.adapter.VideoTimelineAdapter;
import com.bakbakum.shortvdo.databinding.ActivitySoundVideosBinding;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.CustomDialogBuilder;
import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.utils.LittleVideoParamConfig;
import com.bakbakum.shortvdo.view.base.BaseActivity;
import com.bakbakum.shortvdo.viewmodel.SoundActivityViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class SoundVideosActivity extends BaseActivity {


    ActivitySoundVideosBinding binding;
    SoundActivityViewModel viewModel;
    SimpleExoPlayer player;
    private CustomDialogBuilder customDialogBuilder;
    File audio_file;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sound_videos);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new SoundActivityViewModel()).createFor()).get(SoundActivityViewModel.class);
        customDialogBuilder = new CustomDialogBuilder(this);
        initView();
        initListeners();
        initObserve();
        binding.setViewmodel(viewModel);
    }

    private void initView() {
        viewModel.soundId = getIntent().getStringExtra("soundid");
        viewModel.soundUrl = getIntent().getStringExtra("sound");
        viewModel.adapter.soundId = viewModel.soundId;
        viewModel.fetchSoundVideos(false);

        player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(), VideoTimelineAdapter.loadControl);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "tejash"));
        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(Const.ITEM_BASE_URL + viewModel.soundUrl));
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.prepare(videoSource);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale);
        binding.loutShoot.startAnimation(animation);
        binding.tvSoundTitle.setSelected(true);


        if (sessionManager != null && sessionManager.getFavouriteMusic() != null) {
            viewModel.isFavourite.set(sessionManager.getFavouriteMusic().contains(viewModel.soundId));
        }

    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
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
        return filesDir;
    }

    public final String SelectedAudio_AAC ="SelectedAudio.aac";
    private void initListeners() {
        final String app_folder = Environment.getExternalStorageDirectory().toString()+
                File.separator+getString(R.string.app_name) + File.separator;
        binding.loutFavourite.setOnClickListener(v -> {
            sessionManager.saveFavouriteMusic(viewModel.soundId);
            viewModel.isFavourite.set(!viewModel.isFavourite.get());
        });

        binding.imgPlay.setOnClickListener(v -> {
            if (viewModel.isPlaying.get()) {
                player.setPlayWhenReady(false);
                viewModel.isPlaying.set(false);
            } else {
                player.setPlayWhenReady(true);
                viewModel.isPlaying.set(true);
            }
        });
        binding.refreshlout.setOnLoadMoreListener(refreshLayout -> viewModel.onLoadMore());
        binding.imgBack.setOnClickListener(v -> onBackPressed());
        binding.loutShoot.setOnClickListener(v -> {

            if (ActivityCompat.checkSelfPermission(SoundVideosActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(SoundVideosActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SoundVideosActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
                return;
            }

            TextView percent = customDialogBuilder.showLoadingDialogWithPercentage();
            Log.e("DOWNLOAD", "url: " + Const.ITEM_BASE_URL + viewModel.soundUrl);
            PRDownloader.download(Const.ITEM_BASE_URL + viewModel.soundUrl, getPath().getPath(), "SelectedAudio.aac")
                    .build()
                    .setOnProgressListener(new OnProgressListener() {
                        @Override
                        public void onProgress(Progress progress) {
                            int p = (int) ((progress.currentBytes / (float) progress.totalBytes) * 100);
                            percent.setText(p + "%");
                        }
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            audio_file = new File( getPath().getPath() + File.separator + SelectedAudio_AAC);
                            Log.e("Video", "loc: " + audio_file.getAbsolutePath() );
                            if(audio_file.exists()) {
                                try {
                                    // String appName = getResources().getResourceName(R.string.app_name)
                                    copyFile(audio_file,
                                            new File(getPath().getPath()+viewModel.soundId+".mp3"));
                                    Log.e("Video", "loc: " + app_folder+viewModel.soundId+".mp3");
                                } catch (IOException e) {
                                    Log.e("Video", "error: " + e.getMessage());
                                    e.printStackTrace();
                                }
                                String location = getPath().getPath()+viewModel.soundId+".mp3";
                                final AlivcRecordInputParam recordInputParam = new AlivcRecordInputParam.Builder()
                                        .setResolutionMode(LittleVideoParamConfig.Recorder.RESOLUTION_MODE)
                                        .setRatioMode(LittleVideoParamConfig.Recorder.RATIO_MODE)
                                        .setMaxDuration(LittleVideoParamConfig.Recorder.MAX_DURATION)
                                        .setMinDuration(LittleVideoParamConfig.Recorder.MIN_DURATION)
                                        .setVideoQuality(LittleVideoParamConfig.Recorder.VIDEO_QUALITY)
                                        .setGop(LittleVideoParamConfig.Recorder.GOP)
                                        .setVideoCodec(LittleVideoParamConfig.Recorder.VIDEO_CODEC)
                                        .setVideoRenderingMode(RenderingMode.Race)
                                        .build();
                                AlivcSvideoRecordActivity.startRecordWithMusic(SoundVideosActivity.this, recordInputParam, location,
                                        viewModel.soundData.getValue() != null ? viewModel.soundData.getValue().getSoundTitle() : "Sound_Title", viewModel.soundId);
                                customDialogBuilder.hideLoadingDialog();
                                finish();

//                            audio_file = new File( app_folder + SelectedAudio_AAC);
//                            if(audio_file.exists()) {
//                                try {
//                                    // String appName = getResources().getResourceName(R.string.app_name)
//                                    copyFile(audio_file,
//                                            new File(app_folder+getResources().getResourceName(R.string.app_name)+" "+viewModel.soundId+".mp3"));
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                            AndroidAudioConverter.load(SoundVideosActivity.this, new ILoadCallback() {
//                                @Override
//                                public void onSuccess() {
//                                    String location = app_folder+getResources().getResourceName(R.string.app_name)+" "+viewModel.soundId+".mp3";
//                                    File flacFile = new File(location);
//                                    IConvertCallback callback = new IConvertCallback() {
//                                        @Override
//                                        public void onSuccess(File convertedFile) {
//                                            customDialogBuilder.hideLoadingDialog();
//                                            final AlivcRecordInputParam recordInputParam = new AlivcRecordInputParam.Builder()
//                                                    .setResolutionMode(LittleVideoParamConfig.Recorder.RESOLUTION_MODE)
//                                                    .setRatioMode(LittleVideoParamConfig.Recorder.RATIO_MODE)
//                                                    .setMaxDuration(LittleVideoParamConfig.Recorder.MAX_DURATION)
//                                                    .setMinDuration(LittleVideoParamConfig.Recorder.MIN_DURATION)
//                                                    .setVideoQuality(LittleVideoParamConfig.Recorder.VIDEO_QUALITY)
//                                                    .setGop(LittleVideoParamConfig.Recorder.GOP)
//                                                    .setVideoCodec(LittleVideoParamConfig.Recorder.VIDEO_CODEC)
//                                                    .setVideoRenderingMode(RenderingMode.Race)
//                                                    .build();
//                                            AlivcSvideoRecordActivity.startRecordWithMusic(SoundVideosActivity.this, recordInputParam, location,
//                                                    viewModel.soundData.getValue() != null ? viewModel.soundData.getValue().getSoundTitle() : "Sound_Title", viewModel.soundId);
//                                            //  Open_video_recording();
//                                        }
//                                        @Override
//                                        public void onFailure(Exception error) {
//                                            customDialogBuilder.hideLoadingDialog();
//                                            Log.e("SoundVideosActivity", "error: " + error.getMessage());
//                                            Toast.makeText(SoundVideosActivity.this, "Unable to save the audio file", Toast.LENGTH_SHORT).show();
//                                            // Toast.makeText(VideoSound_A.this, "Save Audio First"+error, Toast.LENGTH_SHORT).show();
//                                        }
//                                    };
//                                    AndroidAudioConverter.with(SoundVideosActivity.this)
//                                            .setFile(flacFile)
//                                            .setFormat(AudioFormat.AAC)
//                                            .setCallback(callback)
//                                            .convert();
//                                }
//
//                                @Override
//                                public void onFailure(Exception error) {
//                                    customDialogBuilder.hideLoadingDialog();
//                                }
//                            });
                            } else {
                                customDialogBuilder.hideLoadingDialog();
                                Toast.makeText(SoundVideosActivity.this, "Unable to find audio file", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Error error) {
                            customDialogBuilder.hideLoadingDialog();
                            Toast.makeText(SoundVideosActivity.this, "Unable to download the audio file", Toast.LENGTH_SHORT).show();
                            Log.d("DOWNLOAD", "onError: " + error.getConnectionException().getMessage());
                        }
                    });


//            Intent intent = new Intent(this, CameraActivity.class);
//            intent.putExtra("music_url", viewModel.soundUrl);
//            intent.putExtra("music_title", viewModel.soundData.getValue() != null ? viewModel.soundData.getValue().getSoundTitle() : "Sound_Title");
//            intent.putExtra("soundId", viewModel.soundId);
//            startActivity(intent);
        });
    }

    private void initObserve() {
        viewModel.onLoadMoreComplete.observe(this, onLoadMore -> binding.refreshlout.finishLoadMore());
        viewModel.soundData.observe(this, soundData -> {
            binding.setSoundData(soundData);
            binding.tvVideoCount.setText(Global.prettyCount(soundData.getPostVideoCount()).concat(" Videos"));

        });
    }

    @Override
    protected void onPause() {
        if (player != null) {
            player.setPlayWhenReady(false);
        }
        super.onPause();
    }
}