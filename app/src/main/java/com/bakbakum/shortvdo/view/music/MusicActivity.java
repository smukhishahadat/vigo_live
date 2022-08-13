package com.bakbakum.shortvdo.view.music;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

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
import com.bakbakum.shortvdo.databinding.ActivityMusicBinding;
import com.bakbakum.shortvdo.databinding.ItemMusicBinding;
import com.bakbakum.shortvdo.model.music.Musics;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.CustomDialogBuilder;
import com.bakbakum.shortvdo.utils.SessionManager;
import com.bakbakum.shortvdo.viewmodel.MusicMainViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import java.io.File;
import java.io.IOException;

import static com.bakbakum.shortvdo.view.home.SoundVideosActivity.copyFile;

public class MusicActivity extends AppCompatActivity  implements Player.EventListener {

    private ActivityMusicBinding binding;
    private MusicMainViewModel viewModel;
    private SimpleExoPlayer player;
    private ItemMusicBinding previousView;
    private String previousUrl = "none";
    private SessionManager sessionManager;
    private CustomDialogBuilder customDialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_music);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new MusicMainViewModel()).createFor()).get(MusicMainViewModel.class);
        customDialogBuilder = new CustomDialogBuilder(this);
        binding.setViewModel(viewModel);
        binding.myLayout.requestFocus();
        closeKeyboard();
        sessionManager = new SessionManager(this);
        initListener();
        initObserve();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left)
                .add(R.id.frame, new MusicMainFragment())
                .commit();
//        Log.e("BaseActivity", "Music: Start");
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
    private void initObserve() {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path + File.separator + getString(R.string.app_name) + File.separator);
        if (!file.exists()) file.mkdirs();

       // final String app_folder = Environment.getExternalStorageDirectory().toString()+
                //File.separator+getString(R.string.app_name) + File.separator;
        final String app_folder=file.toString();
        viewModel.music.observe(this, music -> {
            if (music != null) {
//                parentViewModel.onSoundSelect.setValue(music);
//                dismiss();
// id, title, displayName, path, uri, duration, artist, musicId

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                    return;
                }

                TextView percent = customDialogBuilder.showLoadingDialogWithPercentage();

                PRDownloader.download(Const.PROFILE_BASE_URL + music.getSound(),app_folder , SelectedAudio_AAC)
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
                                File path = Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_DOWNLOADS);
                                File file = new File(path + File.separator + getString(R.string.app_name) + File.separator);
                                if (!file.exists()) file.mkdirs();


                                File audio_file = new File( file + File.separator + SelectedAudio_AAC);
                                Log.e("Video", "loc: " + audio_file.getAbsolutePath() );
                                if(audio_file.exists()) {
                                    String location = app_folder+music.getSoundId()+".mp3";
                                    try {
                                        // String appName = getResources().getResourceName(R.string.app_name)
                                        copyFile(audio_file,
                                                new File(location));
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    Intent i = new Intent();
                                    i.putExtra("id", 0);
                                    i.putExtra("title", music.getSoundTitle());
                                    i.putExtra("displayName", music.getSinger());
                                    i.putExtra("path", location);
                                    i.putExtra("uri", Const.PROFILE_BASE_URL + music.getSound());
                                    i.putExtra("artist", music.getAddedBy());
                                    i.putExtra("musicId", music.getSoundId());
                                    setResult(RESULT_OK, i);
                                    customDialogBuilder.hideLoadingDialog();
                                    finish();
                                } else {
                                    customDialogBuilder.hideLoadingDialog();
                                    Toast.makeText(MusicActivity.this, "Unable to find audio file", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onError(Error error) {
                                customDialogBuilder.hideLoadingDialog();
                                Toast.makeText(MusicActivity.this, "Unable to download the audio file", Toast.LENGTH_SHORT).show();
                                Log.d("DOWNLOAD", "onError: " + error.getConnectionException().getMessage());
                            }
                        });
            }
        });
    }

    private void initListener() {
        binding.setOnBackClick(v -> {
            onBackPressed();
        });
        viewModel.searchMusicAdapter.onMusicClick = (view, position, musics, type) -> {
            switch (type) {
                case 0:
                    stopPlay();
                    playAudio(view, musics);
                    break;
                case 1:
                    sessionManager.saveFavouriteMusic(musics.getSoundId());
                    if (sessionManager != null && sessionManager.getFavouriteMusic() != null) {
                        view.setIsFav(sessionManager.getFavouriteMusic().contains(musics.getSoundId()));
                    }
                    break;
                case 2:
                    stopPlay();
                    viewModel.music.setValue(musics);
                    break;
            }
        };

        binding.etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !viewModel.isSearch.get()) {
                viewModel.isSearch.set(true);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left, R.anim.slide_from_left, R.anim.slide_to_right)
                        .replace(R.id.frame, new SearchMusicFragment())
                        .addToBackStack(SearchMusicFragment.class.getSimpleName())
                        .commit();
                viewModel.stopMusic.setValue(true);
            }
        });

        binding.tvCancel.setOnClickListener(v -> {
            if (binding.tvCancel.getText().equals(getResources().getString(R.string.cancel))) {
                closeKeyboard();
                binding.etSearch.clearFocus();
                viewModel.isSearch.set(false);
                getSupportFragmentManager().popBackStack();
            } else {
                viewModel.onSearchTextChanged(binding.etSearch.getText());
            }
        });

    }

    public void closeKeyboard() {
        binding.etSearch.clearFocus();
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (im != null && getCurrentFocus() != null) {
            im.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        if (getWindow() != null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    public void playAudio(ItemMusicBinding view, final Musics.SoundList musics) {

        if (player != null) {
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }
        if (previousView != null) {
            previousView.btnSelect.setVisibility(View.GONE);
            previousView.spinKit.setVisibility(View.GONE);
        }
        previousView = view;

        if (previousUrl.equals(musics.getSound())) {
            previousUrl = "none";
            previousView.btnSelect.setVisibility(View.VISIBLE);

        } else {
            previousUrl = musics.getSound();
            previousView.btnSelect.setVisibility(View.VISIBLE);

            player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector(), VideoTimelineAdapter.loadControl);

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                    Util.getUserAgent(this, "tejash"));

            MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(Const.ITEM_BASE_URL + musics.getSound()));
            player.prepare(videoSource);
            player.addListener(this);
            player.setPlayWhenReady(true);
        }
    }

    private void stopPlay() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.removeListener(this);
            player.release();
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_BUFFERING) {
            previousView.spinKit.setVisibility(View.VISIBLE);
        } else if (playbackState == Player.STATE_READY) {
            previousView.spinKit.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            viewModel.isMore.set(false);
            viewModel.isSearch.set(false);
        } else {
            super.onBackPressed();
        }
    }
}
