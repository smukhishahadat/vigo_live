package com.bakbakum.shortvdo.view.home;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aliyun.svideo.recorder.activity.AlivcSvideoRecordActivity;
import com.aliyun.svideo.recorder.bean.AlivcRecordInputParam;
import com.aliyun.svideo.recorder.bean.RenderingMode;
import com.google.android.material.tabs.TabLayout;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.ViloApplication;
import com.bakbakum.shortvdo.adapter.MainViewPagerAdapter;
import com.bakbakum.shortvdo.databinding.BtnAddLytBinding;
import com.bakbakum.shortvdo.databinding.FragmentMainHomeBinding;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.utils.LittleVideoParamConfig;
import com.bakbakum.shortvdo.utils.SessionManager;
import com.bakbakum.shortvdo.viewmodel.MainViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class MainHomeFragment extends Fragment {

    FragmentMainHomeBinding binding;
    private MainViewModel viewModel;
    private MainActivity context;

    @Override
    public void onAttach(@NonNull Context context) {
        if (context instanceof MainActivity) {
            this.context = (MainActivity) context;
        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        context = null;
        super.onDetach();
    }

    public MainHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_home, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new MainViewModel()).createFor()).get(MainViewModel.class);
        initView();
        initTabLayout();
        binding.setViewModel(viewModel);
    }

    private void initTabLayout() {
        MainViewPagerAdapter adapter = new MainViewPagerAdapter(getChildFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        binding.viewPager.setAdapter(adapter);
        binding.viewPager.setOffscreenPageLimit(4);
    }

    private void initView() {
        if (!Global.ACCESS_TOKEN.isEmpty()) {
            //timerTask = new TimerTask();
        }
        for (int i = 0; i <= 4; i++) {
            switch (i) {
                case 0:
                 /*   Bitmap icon = BitmapFactory.decodeResource(MainActivity.this.getResources(),
                            R.drawable.ic_home_tab);
                    Bitmap bitmapResized = Bitmap.createScaledBitmap(icon, 20, 20, false);
                    BitmapDrawable(getResources(), bitmapResized);*/

                    binding.tabLout.addTab(binding.tabLout.newTab().setIcon(R.drawable.home_icon));
                    TabLayout.Tab tab = binding.tabLout.getTabAt(0);
                    if (tab != null && tab.getIcon() != null) {
                        tab.getIcon().setTint(ContextCompat.getColor(context, R.color.colorTheme));
                    }
                    break;
                case 1:
                    binding.tabLout.addTab(binding.tabLout.newTab().setIcon(R.drawable.discover_icon));
                    break;
                case 2:
                    BtnAddLytBinding addLytBinding = DataBindingUtil.inflate(LayoutInflater.from(context),
                            R.layout.btn_add_lyt, null, false);
                    binding.tabLout.addTab(binding.tabLout.newTab().setCustomView(addLytBinding.getRoot()));
                    //binding.tabLout.addTab(binding.tabLout.newTab().setIcon(R.drawable.add_png));
                    break;
                case 3:
                    binding.tabLout.addTab(binding.tabLout.newTab().setIcon(R.drawable.inbox_icon));
                    break;
                case 4:
                    binding.tabLout.addTab(binding.tabLout.newTab().setIcon(R.drawable.me_icon));
                    break;
            }
        }
        binding.tabLout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                if (tab != null) {
                    if (tab.getPosition() == 0) {
                        context.enableScroll(true);
                        viewModel.onStop.postValue(false);
                    } else {
                        viewModel.onStop.postValue(true);
                    }

                    switch (tab.getPosition()) {
                        case 0:
                            context.setStatusBarTransparentFlag();
                            viewModel.selectedPosition.setValue(0);
                            binding.viewPager.setCurrentItem(0);
                            tab.setIcon(R.drawable.home_icon);
                            if (tab.getIcon() != null)
                                tab.getIcon().setTint(ContextCompat.getColor(context, R.color.colorTheme));
                            break;
                        case 1:
                            context.removeStatusBarTransparentFlag();
                            viewModel.selectedPosition.setValue(1);
                            binding.viewPager.setCurrentItem(1);
                            tab.setIcon(R.drawable.discover_icon);
                            if (tab.getIcon() != null)
                                tab.getIcon().setTint(ContextCompat.getColor(context, R.color.colorTheme));
                            break;
                        case 2:
                            if (new SessionManager(context).getBooleanValue(Const.IS_LOGIN)) {
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
                                AlivcSvideoRecordActivity.startRecord(context, recordInputParam);
                                //startActivityForResult(new Intent(MainActivity.this, CameraActivity.class), CAMERA);
                            } else {
                                context.initLogin(context, () -> {
                                    TabLayout.Tab tab1 = binding.tabLout.getTabAt(0);
                                    if (tab1 != null) {
                                        tab1.select();
                                    }
                                });
                            }
                            break;
                        case 3:
                            context.removeStatusBarTransparentFlag();
                            viewModel.selectedPosition.setValue(2);
                            binding.viewPager.setCurrentItem(2);
                            tab.setIcon(R.drawable.inbox_icon);
                            if (tab.getIcon() != null)
                                tab.getIcon().setTint(ContextCompat.getColor(context, R.color.colorTheme));
                            break;
                        case 4:
                            context.removeStatusBarTransparentFlag();
                            if (ViloApplication.getInstance().getSessionManager().getBooleanValue(Const.IS_LOGIN)) {
                                binding.viewPager.setCurrentItem(3);
                                viewModel.selectedPosition.setValue(3);
                                tab.setIcon(R.drawable.me_icon);
                                if (tab.getIcon() != null)
                                    tab.getIcon().setTint(ContextCompat.getColor(context, R.color.colorTheme));
                            } else {
                                context.initLogin(context, () -> {
                                    TabLayout.Tab tab1 = binding.tabLout.getTabAt(0);
                                    if (tab1 != null) {
                                        tab1.select();
                                    }
                                });
                            }
                            break;
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        context.enableScroll(false);
                        tab.setIcon(R.drawable.home_icon);
                        if (tab.getIcon() != null)
                            tab.getIcon().setTint(ContextCompat.getColor(context, R.color.white));
                        break;
                    case 1:
                        tab.setIcon(R.drawable.discover_icon);
                        if (tab.getIcon() != null)
                            tab.getIcon().setTint(ContextCompat.getColor(context, R.color.white));
                        break;
                    case 2:
                        //tab.setIcon(R.drawable.ic_home_strock);
                        break;
                    case 3:
                        tab.setIcon(R.drawable.inbox_icon);
                        if (tab.getIcon() != null)
                            tab.getIcon().setTint(ContextCompat.getColor(context, R.color.white));
                        break;
                    case 4:
                        tab.setIcon(R.drawable.me_icon);
                        if (tab.getIcon() != null)
                            tab.getIcon().setTint(ContextCompat.getColor(context, R.color.white));
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 2) {
                    if (ViloApplication.getInstance().getSessionManager().getBooleanValue(Const.IS_LOGIN)) {
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
                        AlivcSvideoRecordActivity.startRecord(context, recordInputParam);
                    } else {
                        context.initLogin(context, () -> {
                            TabLayout.Tab tab1 = binding.tabLout.getTabAt(0);
                            if (tab1 != null) {
                                tab1.select();
                            }
                        });
                    }
                }
            }
        });
    }

    @Override
    public void onStop() {
        viewModel.onStop.setValue(true);
        super.onStop();
    }

    @Override
    public void onResume() {
        if (binding.tabLout.getSelectedTabPosition() == 0) {
            viewModel.onStop.setValue(false);
        }
        if (binding.tabLout.getSelectedTabPosition() >= 3) {
            viewModel.selectedPosition.setValue(binding.tabLout.getSelectedTabPosition() - 1);
        } else {
            viewModel.selectedPosition.setValue(binding.tabLout.getSelectedTabPosition());
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        viewModel.onStop.setValue(true);

        super.onPause();
    }
}