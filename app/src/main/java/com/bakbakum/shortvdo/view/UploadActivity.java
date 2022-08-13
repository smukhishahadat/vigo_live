package com.bakbakum.shortvdo.view;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import com.bakbakum.shortvdo.customview.Customtoast;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.LogMessage;
import com.arthenica.mobileffmpeg.Statistics;
import com.bakbakum.shortvdo.Compressor;
import com.bakbakum.shortvdo.R;
import com.bakbakum.shortvdo.databinding.ActivityUploadBinding;
import com.bakbakum.shortvdo.databinding.CustomToastBinding;
import com.bakbakum.shortvdo.utils.CustomDialogBuilder;
import com.bakbakum.shortvdo.utils.GlobalApi;
import com.bakbakum.shortvdo.view.base.BaseActivity;
import com.bakbakum.shortvdo.view.home.MainActivity;
import com.bakbakum.shortvdo.view.profile.FollowerFollowingActivity;
import com.bakbakum.shortvdo.view.profile.ProfileFragment;
import com.bakbakum.shortvdo.view.web.WebViewActivity;
import com.bakbakum.shortvdo.viewmodel.PreviewViewModel;
import com.bakbakum.shortvdo.viewmodelfactory.ViewModelFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

public class UploadActivity extends BaseActivity  {

    private ActivityUploadBinding binding;
    private PreviewViewModel viewModel;
    private CustomDialogBuilder customDialogBuilder;

    private RadioGroup radioGroup;
    private RadioButton radioButton;
    private RadioButton radioButton2;
    private RadioButton radioButton3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new PreviewViewModel()).createFor()).get(PreviewViewModel.class);
        customDialogBuilder = new CustomDialogBuilder(this);
        viewModel.sessionManager = sessionManager;
        binding.setViewModel(viewModel);
        initView();
        initObserve();

        createThumbnail(viewModel.videoPath, getPath().getPath() + File.separator + "thumbnail.jpg");
    }

    private void initView() {
        viewModel.videoPath = getIntent().getStringExtra("post_video");
        viewModel.videoThumbnail = getIntent().getStringExtra("post_image");
        viewModel.soundPath = getIntent().getStringExtra("post_sound");
        viewModel.soundImage = getIntent().getStringExtra("sound_image");
        viewModel.soundId = getIntent().getStringExtra("soundId");
    }

    private void createThumbnail(String input, String output) {
        binding.btnPublish.setEnabled(false);

        FFmpeg.executeAsync("-y -i "+input+" -ss 0 -vframes 1 -q:v 10 "+output, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int rc) {
                if (rc == RETURN_CODE_SUCCESS) {
                    viewModel.videoThumbnail = output;
                    binding.btnPublish.setEnabled(true);
                    Log.i(getClass().getName(), "Async command execution completed successfully.");
                } else {
                    // not working
                }
            }
        });
    }

    private void compress(Context context) {
        TextView percent = customDialogBuilder.showLoadingDialogWithPercentage();
        Compressor c = new Compressor(context)
                .setCrf(23)
                .setMinimumCompressionSize(5)
                .setInput(viewModel.videoPath)
                .setListener(new Compressor.CompressorCallbacks() {
                    @Override
                    public void onCompleted(String destination, File output, long seconds) {
                        viewModel.videoPath = destination;
                        customDialogBuilder.hideLoadingDialog();
                        viewModel.uploadPost();
                    }

                    @Override
                    public void onProgress(int progress, Statistics newStatistics) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                percent.setText(progress + "%");
                            }
                        });
                    }

                    @Override
                    public void onLog(LogMessage message) {
                        Log.e("PreviewViewModel", message.toString());
                    }

                    @Override
                    public void onFailure(int rc) {
                        customDialogBuilder.hideLoadingDialog();
                        if (rc == Compressor.COMPRESSION_NOT_NEEDED) {
                            viewModel.uploadPost();
                        } else {
                            Toast.makeText(context, "Sorry we are unable to process this video", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        c.execute();
    }

    private void initObserve() {
        viewModel.isLoading.observe(this, isLoading -> {
            if (isLoading) {
                customDialogBuilder.showLoadingDialog();
            } else {
                deleteRecursive(getPath());
                String filepath=getPath().toString();
                customDialogBuilder.hideLoadingDialog();
                setResult(RESULT_OK);
                new GlobalApi().rewardUser("3");
                Toast.makeText(this, "Video Uploaded SuccessFully", Toast.LENGTH_SHORT).show();
                Intent I=new Intent(UploadActivity.this, MainActivity.class);
//                Intent I=new Intent(UploadActivity.this, ProfileFragment.class);
                startActivity(I);
                //onBackPressed();
            }
        });
        binding.btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compress(UploadActivity.this);
            }
        });
        binding.ivThumb.setImageURI(Uri.parse(viewModel.videoThumbnail));
        binding.imgClose.setOnClickListener(v1 -> onBackPressed());
        binding.tvPrivacy.setOnClickListener(v1 -> startActivity(new Intent(this, WebViewActivity.class).putExtra("type", 1)));
        binding.edtDes.setOnHashtagClickListener((view, text) -> {});
        binding.allowDuetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.allowDuet = isChecked;
            }
        });
        binding.allowCommentsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.allowComments = isChecked;
            }
        });

        //binding.radioGroup.setOnCheckedChangeListener();
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
              @Override
              public void onCheckedChanged(RadioGroup group, int checkedId)
              {
                  radioButton = (RadioButton) findViewById(checkedId);
                  String btnValue=radioButton.getText().toString();
                  String privacy_val="1";
                  if (btnValue.equals("Public"))
                  {
                      privacy_val="1";
                  }
                  else if (btnValue.equals("Friends"))
                  {
                      privacy_val="2";
                  }
                  else
                  {
                      privacy_val="3";
                  }
                  viewModel.video_privacy=privacy_val;
                  //Customtoast customtoast = new Customtoast();
                  //customtoast.showCustomToast(viewModel.video_privacy,"#006699",R.drawable.logo);

                  //Customtoast.showCustomToasts(viewModel.video_privacy,"#006699",R.drawable.logo);
//                  LayoutInflater li = getLayoutInflater();
//                  View layout = li.inflate(R.layout.custom_toast,(ViewGroup) findViewById(R.id.custom_toast_layout));
//
//                  TextView text = (TextView) layout.findViewById(R.id.tv_toast_message);
//                  text.setText(radioButton.getText());
//
//                  Toast toast = new Toast(getBaseContext());
//                  toast.setDuration(Toast.LENGTH_SHORT);
//                  toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//                  toast.setView(layout.getRootView());//setting the view of custom toast layout
//                  toast.show();
//                  Toast.makeText(getBaseContext(), radioButton.getText(), Toast.LENGTH_SHORT).show();
              }
        });


        binding.savePhoneSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                viewModel.saveLocally = isChecked;
            }
        });
        viewModel.onClickUpload.observe(this, s -> {
            if (!binding.edtDes.getHashtags().isEmpty()) {
                viewModel.hashTag = TextUtils.join(",", binding.edtDes.getHashtags());
            }
        });
    }

    public File getPath() {
        String state = Environment.getExternalStorageState();
        File filesDir;
        if (Environment.MEDIA_MOUNTED.equals(state)) {

            filesDir = getExternalFilesDir(null);
        } else {
            filesDir = getFilesDir();
        }
        return filesDir;
    }

    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory != null && fileOrDirectory.isDirectory() && fileOrDirectory.listFiles() != null) {
            for (File child : Objects.requireNonNull(fileOrDirectory.listFiles())) {
                deleteRecursive(child);
            }
        }
        if (fileOrDirectory != null) {
            fileOrDirectory.delete();
        }
    }
}