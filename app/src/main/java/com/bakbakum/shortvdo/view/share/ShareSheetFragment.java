package com.bakbakum.shortvdo.view.share;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.OnProgressListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.FragmentShareSheetBinding;
import com.bakbakum.shortvdo.model.videos.Video;
import com.bakbakum.shortvdo.utils.Const;
import com.bakbakum.shortvdo.utils.CustomDialogBuilder;
import com.bakbakum.shortvdo.view.home.ReportSheetFragment;
import com.bakbakum.shortvdo.viewmodel.ShareSheetViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;
import static com.facebook.FacebookSdk.getApplicationContext;

public class ShareSheetFragment extends BottomSheetDialogFragment {


    private static final int MY_PERMISSIONS_REQUEST = 101;
    FragmentShareSheetBinding binding;
    ShareSheetViewModel viewModel;
    private CustomDialogBuilder customDialogBuilder;

    public ShareSheetFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog dialog = (BottomSheetDialog) dialog1;
            dialog.setCanceledOnTouchOutside(false);

        });

        return bottomSheetDialog;

    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_share_sheet, container, false);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new ShareSheetViewModel()).createFor()).get(ShareSheetViewModel.class);
        customDialogBuilder = new CustomDialogBuilder(getActivity());
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        initListeners();
        initObserve();
    }

    private void initView() {
        binding.setViewModel(viewModel);
        if (getArguments() != null && getArguments().getString("video") != null) {
            viewModel.video = new Gson().fromJson(getArguments().getString("video"), Video.Data.class);
        }
        createVideoShareLink();

    }


    private void initListeners() {
        binding.btnCopy.setOnClickListener(v -> {
            if (getActivity() != null) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Video link", viewModel.shareUrl);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getActivity(), "Copied Clipboard To Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.btnDownload.setOnClickListener(view -> initPermission());
        binding.btnReport.setOnClickListener(view -> {
            ReportSheetFragment fragment = new ReportSheetFragment();
            Bundle args = new Bundle();
            args.putString("postid", viewModel.video.getPostId());
            args.putInt("reporttype", 1);
            fragment.setArguments(args);
            if (getParentFragment() != null) {
                fragment.show(getParentFragment().getChildFragmentManager(), fragment.getClass().getSimpleName());
            }
            dismiss();
        });
    }

    private void initObserve() {
        viewModel.onItemClick.observe(this, type -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            switch (type) {
                case 1:     // Instagram
                    share.setPackage("com.instagram.android");
                    break;
                case 2:   // facebook
                    share.setPackage("com.facebook.katana");
                    break;
                case 3:   // whatsapp
                    share.setPackage("com.whatsapp");
                    break;
                // other
                case 4:

                    break;
                case 5:
                    share.setPackage("com.twitter.android");
                    break;
                case 6:
                    share.setPackage("org.telegram.messenger");
                    break;
                case 7:
                    share.setPackage("com.facebook.orca");
                    break;
                case 8:
                    share.setPackage("com.google.android.youtube");
                    break;
            }
            initPermission(share);
            customDialogBuilder.showLoadingDialog();
        });
    }


    private void initPermission() {
        if (getActivity() != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);
            } else {
                startDownload();
            }
        }
    }

    private void initPermission(Intent share) {
        if (getActivity() != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "initPermission: request permission" );
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);
            } else {
                Log.e(TAG, "initPermission: not request" );
                startDownload(share);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                 startDownload();
            }
        }
    }

    private void startDownload() {
//        Log.d("DOWNLOAD", "start Download: ");
        final TextView percent = customDialogBuilder.showLoadingDialogWithPercentage();
        PRDownloader.download(Const.ITEM_BASE_URL + viewModel.video.getPostVideo(),
                getContext().getCacheDir().getPath(), "temp.mp4")
                .build()
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        int p = (int) ((progress.currentBytes / (float) progress.totalBytes) * 99);
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

                        String filename=file  + File.separator + viewModel.video.getPostVideo().substring(viewModel.video.getPostVideo().lastIndexOf("/")+1);
                        applyWatermark(getContext().getCacheDir().getPath() + File.separator + "temp.mp4",filename);

                    }

                    @Override
                    public void onError(Error error) {
                        customDialogBuilder.hideLoadingDialog();
                        Log.d("DOWNLOAD", "onError: " + error.getConnectionException().getMessage());
                    }
                });
    }

    String TAG = this.getClass().getName();
    private void applyWatermark(String input, String output) {
        //InputStream is = context.getAssets().open("icon/loop.gif");
        Context context = getContext();
        try {
            AssetManager assetManager = context.getAssets();
            String[] files = assetManager.list("");
            //Log.e(TAG, "files: " + files.length);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        String tempStr = context.getExternalFilesDir("") + "/icon/logo.png";
        File mWatermarkFile = new File(tempStr);

        String tempFont = context.getExternalFilesDir("") + "/icon/sans_bold.ttf";
        File mFontFile = new File(tempFont);

        if (mWatermarkFile.exists() && mFontFile.exists()) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(input);
            String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            int durationInt = Integer.parseInt(duration);
            durationInt = durationInt / 1000;
            String frames = durationInt * 30 + "";

            String[] commandArray = new String[]{};
            //-i test.mp4 -ignore_loop 0 -i loop.gif -frames:v 450 -filter_complex "[0:v][1:v]overlay=x='if(lt(mod(t,10),5),10,W-w-10)':y='if(lt(mod(t,10),5),10,H-h-10)',drawtext=fontfile='sans-bold.ttf':text='@ismailqasim':fontsize=18:fontcolor=white:x='if(lt(mod(t,10),5),50,W-tw-50)':y='if(lt(mod(t,10),5),251,h-th-20)'" output.mp4
            commandArray = new String[]{"-y","-i", input, "-i", mWatermarkFile.getAbsolutePath(),
                    "-frames:v", frames, "-filter_complex", "[0:v][1:v]overlay=x='if(lt(mod(t,6),3),W-w-W*2/100,W*2/100)': y='if(lt(mod(t+12,6),3),H-h-H*5/100,H*50/100)' ",
                    output};
            FFmpeg.executeAsync(commandArray, new ExecuteCallback() {
                @Override
                public void apply(long executionId, int rc) {
                    if (rc == RETURN_CODE_SUCCESS) {
                        customDialogBuilder.hideLoadingDialog();
                        //Log.i(TAG, "Async command execution completed successfully.");
                        dismiss();
                        Toast.makeText(getActivity(), "Saved Successfully", Toast.LENGTH_SHORT).show();
                    } else if (rc == RETURN_CODE_CANCEL) {
                        customDialogBuilder.hideLoadingDialog();
                        Log.i(TAG, "Async command execution cancelled by user.");
                    } else {
                        customDialogBuilder.hideLoadingDialog();
                        Log.i(TAG, String.format("Async command execution failed with rc=%d.", rc));
                    }
                }
            });
        } else {
            Log.e(TAG, "Watermark file does not exist");
            customDialogBuilder.hideLoadingDialog();
        }
    }

    private void startDownload(Intent share) {
        Log.d("DOWNLOAD", "startDownload: ");
        PRDownloader.download(Const.ITEM_BASE_URL + viewModel.video.getPostVideo(),
                getPath().getPath(), viewModel.video.getPostVideo())
                .build()
                .setOnStartOrResumeListener(() -> customDialogBuilder.showLoadingDialog())
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        customDialogBuilder.hideLoadingDialog();
                        //New
//                        File path = Environment.getExternalStoragePublicDirectory(
//                                Environment.DIRECTORY_DOWNLOADS);
//                        File file = new File(path + File.separator + getString(R.string.app_name) + File.separator);
//                        if (!file.exists()) file.mkdirs();
//                        File f = new File(getPath().getPath(), viewModel.video.getPostVideo());
//                        String filename=file  + File.separator + viewModel.video.getPostVideo().substring(viewModel.video.getPostVideo().lastIndexOf("/")+1);
//                        applyWatermark(f.toString(),filename);


                        //Old
                        File f = new File(getPath().getPath(), viewModel.video.getPostVideo());
                        String shareBody = "\nWatch this amazing video on %s App\nhttps://play.google.com/store/apps/details?id=%playstore".replace("%s", getString(R.string.app_name))
                                .replace("%playstore", getApplicationContext().getPackageName());
                        share.setType("image/*|video/*");
                        share.putExtra(Intent.EXTRA_SUBJECT, "Share Video");
                        share.putExtra(Intent.EXTRA_TEXT, shareBody);
                        share.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getContext(),
                                getContext().getPackageName() + ".fileprovider", f));
                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(share, "Share Video"));
                        dismiss();
                        Toast.makeText(getActivity(), "Saved Successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Error error) {
                        customDialogBuilder.hideLoadingDialog();
                        Log.d("DOWNLOAD", "onError: " + error.getConnectionException().getMessage());
                    }
                });
    }

    public File getPath() {
        if (getActivity() != null) {
            String state = Environment.getExternalStorageState();
            File filesDir;
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // We can read and write the media
                filesDir = getActivity().getExternalFilesDir(null);
            } else {
                // Load another directory, probably local memory
                filesDir = getActivity().getFilesDir();
            }
            return filesDir;
        }
        return new File(Environment.getRootDirectory().getAbsolutePath());
    }

    private void createVideoShareLink() {
        String json = new Gson().toJson(viewModel.video);
        String title = viewModel.video.getPostDescription();

        Log.i("ShareJson", "Json Object: " + json);
        BranchUniversalObject buo = new BranchUniversalObject()
                .setCanonicalIdentifier("content/12345")
                .setTitle(title)
                .setContentImageUrl(Const.ITEM_BASE_URL + viewModel.video.getPostImage())
                .setContentDescription(viewModel.video.getPostDescription())
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(new ContentMetadata().addCustomMetadata("data", json));

        LinkProperties lp = new LinkProperties()
                .setFeature("sharing")
                .setCampaign("Content launch")
                .setStage("Video")
                .addControlParameter("custom", "data")
                .addControlParameter("custom_random", Long.toString(Calendar.getInstance().getTimeInMillis()));

        if (getActivity() != null) {
            buo.generateShortUrl(getActivity(), lp, (url, error) -> {
                Log.d("VIDEO_URL", "shareProfile: " + url);
                viewModel.shareUrl = url;
            });
        }

    }


}