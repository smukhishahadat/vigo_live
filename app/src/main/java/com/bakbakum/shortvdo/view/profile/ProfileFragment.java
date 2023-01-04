package com.bakbakum.shortvdo.view.profile;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.adapter.ProfileVideoPagerAdapter;
import com.bakbakum.shortvdo.databinding.FragmentProfileBinding;
import com.bakbakum.shortvdo.model.user.User;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.Global;
import com.bakbakum.shortvdo.view.base.BaseFragment;
import com.bakbakum.shortvdo.view.home.ReportSheetFragment;
import com.bakbakum.shortvdo.viewmodel.MainViewModel;
import com.bakbakum.shortvdo.viewmodel.ProfileViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;

import static android.app.Activity.RESULT_OK;
import static androidx.fragment.app.FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends BaseFragment {
    private static final String TAG = "ProfileFragment";


    public ProfileViewModel viewModel;
    private int UPDATE_DATA = 100;
    private MainViewModel parentViewModel;
    private FragmentProfileBinding binding;


    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getParentFragment() != null) {
            parentViewModel = ViewModelProviders.of(getParentFragment()).get(MainViewModel.class);
        }

        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new ProfileViewModel()).createFor()).get(ProfileViewModel.class);
        initView();
        initObserver();
        initListener();
        binding.setViewModel(viewModel);
    }

    private void initView() {
        if (getArguments() != null) {
            String userId = getArguments().getString("userid");
            if (userId != null && !userId.equals(Global.USER_ID)) {
                viewModel.isMyAccount.set(-1);
            }
            viewModel.fetchUserById(getArguments().getString("userid"));
            viewModel.userId = getArguments().getString("userid");
            viewModel.isBackBtn.set(true);
        }
        ProfileVideoPagerAdapter adapter = new ProfileVideoPagerAdapter(getChildFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        binding.viewPager.setAdapter(adapter);
    }

    private void initObserver() {
        if (parentViewModel != null) {
            parentViewModel.selectedPosition.observe(this, position -> {
                if (position != null && position == 3) {
                    viewModel.selectPosition.setValue(position);

                    if (sessionManager.getUser() != null) {
                        viewModel.user.setValue(sessionManager.getUser());
                        viewModel.fetchUserById(sessionManager.getUser().getData().getUserId());
                        viewModel.userId = sessionManager.getUser().getData().getUserId();
                        viewModel.isBackBtn.set(false);
                    }

                }
            });
        }
        viewModel.onItemClick.observe(getViewLifecycleOwner(), type -> {
            if (type != null) {
                switch (type) {
                    // On option menu click
                    case 0:
                        if (viewModel.isMyAccount.get() == 1 || viewModel.isMyAccount.get() == 2) {
                            // other user profile
                            showPopMenu();
                        } else {
                            // my profile
                            startActivity(new Intent(getActivity(), SettingsActivity.class));
                        }
                        break;
                    // On Follow, UnFollow, edit btn click
                    case 1:
                        handleButtonClick();
                        break;
                    // Back btn
                    case 2:
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                        break;
                    // On My videos tab click
                    case 3:
                        viewModel.isLikedVideos.set(false);
                        binding.viewPager.setCurrentItem(0);
                        break;
                    // On liked videos click
                    case 4:
                        viewModel.isLikedVideos.set(true);
                        binding.viewPager.setCurrentItem(1);
                        break;
                    // On Followers Click
                    case 5:
                        handleFollowerClick(0);
                        break;
                    // On Following Click
                    case 6:
                        handleFollowerClick(1);
                        break;
                }
                viewModel.onItemClick.setValue(null);
            }
        });
        viewModel.intent.observe(getViewLifecycleOwner(), intent -> {
            if (intent != null) {
                try {
                    startActivity(intent);
                } catch (Exception ignored) {
                }
            }
        });

        viewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                if (sessionManager.getUser() != null && sessionManager.getUser().getData() != null && sessionManager.getUser().getData().getUserId().equals(user.getData().getUserId())) {
                    sessionManager.saveUser(user);
                }
                binding.setViewModel(viewModel);
            }
        });
        viewModel.followApi.observe(getViewLifecycleOwner(), checkUsername -> {
            if (viewModel.user.getValue() != null) {
                if (viewModel.isMyAccount.get() == 1) {
                    viewModel.user.getValue().getData().setFollowersCount(viewModel.user.getValue().getData().getFollowersCount() + 1);
                } else {
                    viewModel.user.getValue().getData().setFollowersCount(viewModel.user.getValue().getData().getFollowersCount() - 1);
                }
                binding.tvFansCount.setText(Global.prettyCount(viewModel.user.getValue().getData().getFollowersCount()));
            }
        });

    }

    private void showPopMenu() {
        if (getActivity() != null) {
            PopupMenu popupMenu = new PopupMenu(getActivity(), binding.imgOption);
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.share:
                        shareProfile();
                        return true;
                    case R.id.block:
                        if (sessionManager.getBooleanValue(Const.IS_LOGIN)) {
                            ReportSheetFragment blockfragment = new ReportSheetFragment();
                            Bundle block_urg = new Bundle();
                            block_urg.putString("userid", viewModel.userId);
                            block_urg.putInt("reporttype", 2);
                            block_urg.putString("user", new Gson().toJson(viewModel.user.getValue()));
                            blockfragment.setArguments(block_urg);
                            blockfragment.show(getChildFragmentManager(), blockfragment.getClass().getSimpleName());
                            Toast.makeText(getContext(), "Your request accepted. We will review "+viewModel.user.getValue().getData().getFullName()+" 's profile.", Toast.LENGTH_LONG).show();

                        } else {
                            Toast.makeText(getContext(), "You have to login first", Toast.LENGTH_LONG).show();
                        }
                        return true;
                    case R.id.report:
                        ReportSheetFragment fragment = new ReportSheetFragment();
                        Bundle args = new Bundle();
                        args.putString("userid", viewModel.userId);
                        args.putInt("reporttype", 0);
                        fragment.setArguments(args);
                        fragment.show(getChildFragmentManager(), fragment.getClass().getSimpleName());
                        return true;

                }
                return false;
            });
            popupMenu.show();
        }
    }

    private void shareProfile() {
        if (getActivity() != null && viewModel.user.getValue() != null) {
            String json = new Gson().toJson(viewModel.user.getValue());
            String title = viewModel.user.getValue().getData().getFullName();

            Log.e(TAG, "shareProfile: user profile is "+Const.ITEM_BASE_URL + viewModel.user.getValue().getData().getUserProfile() );
            Log.i("ShareJson", "Json Object: " + json);
            BranchUniversalObject buo = new BranchUniversalObject()
                    .setCanonicalIdentifier("content/12345")
                    .setTitle(title)
                    .setContentImageUrl(Const.ITEM_BASE_URL + viewModel.user.getValue().getData().getUserProfile())
                    .setContentDescription("Hey There, Check This %s's Profile"
                            .replace("%s", getString(R.string.app_name)))
                    .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                    .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                    .setContentMetadata(new ContentMetadata().addCustomMetadata("data", json));

            LinkProperties lp = new LinkProperties()
                    .setFeature("sharing")
                    .setCampaign("Content launch")
                    .setStage("User")
                    .addControlParameter("custom", "data")
                    .addControlParameter("custom_random", Long.toString(Calendar.getInstance().getTimeInMillis()));

            String url = "https://play.google.com/store/apps/details?id=%playstore"
                                .replace("%playstore", getApplicationContext().getPackageName());
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            String shareBody = url + "\nThis Profile Is Amazing On Bakbakum App";
            intent.setType("text/plain");
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share Profile");
            intent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(intent, "Share Profile"));
//            buo.generateShortUrl(getActivity(), lp, (url, error) -> {
//
//                Log.d("PROFILEURL", "shareProfile: " + url);
//
//
//            });
        }
    }

    private void handleFollowerClick(int itemType) {

        Intent intent = new Intent(getActivity(), FollowerFollowingActivity.class);
        intent.putExtra("itemtype", itemType);
        intent.putExtra("user", new Gson().toJson(viewModel.user.getValue()));
        startActivity(intent);

    }

    private void handleButtonClick() {

        if (sessionManager.getBooleanValue(Const.IS_LOGIN)) {
            if (viewModel.isMyAccount.get() == 1 || viewModel.isMyAccount.get() == 2) {
                viewModel.followUnfollow();
            } else {
                startActivityForResult(new Intent(getActivity(), EditProfileActivity.class), UPDATE_DATA);
            }
        } else {
            Toast.makeText(getContext(), "You have to login first", Toast.LENGTH_SHORT).show();
        }
    }

    private void initListener() {
        binding.btnFollow.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));

        binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewModel.isLikedVideos.set(position == 1);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == UPDATE_DATA) {
                if (data != null) {
                    viewModel.user.setValue(new Gson().fromJson(data.getStringExtra("user"), User.class));
                    binding.setViewModel(viewModel);
                }
            }
        }
    }
}
