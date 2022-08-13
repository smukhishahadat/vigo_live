package com.bakbakum.shortvdo.view.music;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.adapter.VideoTimelineAdapter;
import com.bakbakum.shortvdo.databinding.FragmentMusicFrameBinding;
import com.bakbakum.shortvdo.databinding.ItemMusicBinding;
import com.bakbakum.shortvdo.model.music.Musics;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.SessionManager;
import com.bakbakum.shortvdo.viewmodel.CameraViewModel;
import com.bakbakum.shortvdo.viewmodel.MusicMainViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

public class MusicFrameFragment extends BottomSheetDialogFragment implements Player.EventListener {

    private FragmentMusicFrameBinding binding;
    private MusicMainViewModel viewModel;
    private CameraViewModel parentViewModel;
    private SimpleExoPlayer player;
    private ItemMusicBinding previousView;
    private String previousUrl = "none";
    private SessionManager sessionManager;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog = getActivity() != null ? new BottomSheetDialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                if (getChildFragmentManager().getBackStackEntryCount() > 0) {
                    getChildFragmentManager().popBackStack();
                    viewModel.isMore.set(false);
                    viewModel.isSearch.set(false);
                } else {
                    super.onBackPressed();
                }
            }
        } : (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        bottomSheetDialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialog1;
            FrameLayout bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
                if (getActivity() != null) {
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    getActivity().getWindow().setNavigationBarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                }

            }
        });

        return bottomSheetDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_music_frame, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            parentViewModel = ViewModelProviders.of(getActivity()).get(CameraViewModel.class);
        }
        binding.myLayout.requestFocus();
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new MusicMainViewModel()).createFor()).get(MusicMainViewModel.class);
        closeKeyboard();
        if (getActivity() != null) {
            sessionManager = new SessionManager(getActivity());
        }
        initListener();
        initObserve();
        getChildFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left)
                .add(R.id.frame, new MusicMainFragment())
                .commit();
        binding.setViewModel(viewModel);
    }

    private void initObserve() {
        viewModel.music.observe(getViewLifecycleOwner(), music -> {
            if (music != null) {
                parentViewModel.onSoundSelect.setValue(music);
                dismiss();
            }
        });
    }

    private void initListener() {
        binding.setOnBackClick(v -> {
            if (getDialog() != null) {
                getDialog().onBackPressed();
            }
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
                getChildFragmentManager().beginTransaction()
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
                getChildFragmentManager().popBackStack();
            } else {
                viewModel.onSearchTextChanged(binding.etSearch.getText());
            }
        });

    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getActivity() != null) {
            stopPlay();
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    public void closeKeyboard() {
        binding.etSearch.clearFocus();
        if (getDialog() != null) {
            InputMethodManager im = (InputMethodManager) getDialog().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (im != null && getDialog().getCurrentFocus() != null) {
                im.hideSoftInputFromWindow(getDialog().getCurrentFocus().getWindowToken(), 0);
            }
            if (getDialog().getWindow() != null) {
                getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }
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
            if (getActivity() != null) {
                previousUrl = musics.getSound();
                previousView.btnSelect.setVisibility(View.VISIBLE);

                player = ExoPlayerFactory.newSimpleInstance(getContext(), new DefaultTrackSelector(), VideoTimelineAdapter.loadControl);

                DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity(),
                        Util.getUserAgent(getActivity(), "tejash"));

                MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(Uri.parse(Const.ITEM_BASE_URL + musics.getSound()));
                player.prepare(videoSource);
                player.addListener(this);
                player.setPlayWhenReady(true);
            }
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
}