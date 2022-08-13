package com.bakbakum.shortvdo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.LogCallback;
import com.arthenica.mobileffmpeg.LogMessage;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;

import java.io.File;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

/*
 Created over mobile-ffmpeg by vativeApps
*/
public class Compressor {
    private int crf = 28; // default is 28
    private String preset = "fast"; // default is fast
    private String vcodec = "libx264";  // default is libx264
    private int frame = 30; // default is 30
    private String input = null;
    private String output = null;
    private double startingDuration = 0;
    private Context context;
    private CompressorCallbacks listener;
    private final String TAG = "Compressor";
    private int minimumSize = 5; // in MB

    private long lastMinValue = -1;
    private long lastMaxValue = -1;
    // constants
    public static final int COMPRESSION_NOT_NEEDED = 300;
    public static final int PERMISSION_NOT_GRANTED = 86;

    public Compressor(Context context) {
        this.context = context;
        enableLogCallback();
        enableStatisticsCallback();
    }

    public interface CompressorCallbacks {
        void onCompleted(String destination, File output, long seconds);
        void onProgress(int progress, final Statistics newStatistics);
        void onLog(final LogMessage message);
        void onFailure(int rc);
    }

    public Compressor setTrim(long lastMinValue, long lastMaxValue) {
        this.lastMinValue = lastMinValue;
        this.lastMaxValue = lastMaxValue;
        return this;
    }

    public Compressor setListener(CompressorCallbacks listener) {
        this.listener = listener;
        return this;
    }

    public Compressor setMinimumCompressionSize(int minimumSize) {
        this.minimumSize = minimumSize;
        return this;
    }

    public Compressor setCrf(int crf) {
        this.crf = crf;
        return this;
    }

    public Compressor setPreset(String preset) {
        this.preset = preset;
        return this;
    }

    public Compressor setVcodec(String vcodec) {
        this.vcodec = vcodec;
        return this;
    }

    public Compressor setFrame(int frame) {
        this.frame = frame;
        return this;
    }

    public Compressor setInput(String input) {
        this.input = input;
        return this;
    }

    public Compressor setOutput(String output) {
        this.output = output;
        return this;
    }

    private void enableLogCallback() {
        Config.enableLogCallback(new LogCallback() {

            @Override
            public void apply(final LogMessage message) {
                if (listener != null)
                    listener.onLog(message);
            }
        });
    }

    private void enableStatisticsCallback() {
        Config.enableStatisticsCallback(new StatisticsCallback() {
            @Override
            public void apply(final Statistics newStatistics) {
                long percentage = Math.round((newStatistics.getTime() / startingDuration) * 100);
                if (listener != null)
                    listener.onProgress(((int) percentage), newStatistics);
            }
        });
    }

    private File createTemporaryFile() {
        final File f = new File(context.getExternalFilesDir(null), "temp.mp4");
        output = f.getPath();
        return f;
    }

    public void execute() {
        File destination = null;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            if (output != null) {
                destination = new File(output);
            } else {
                destination = createTemporaryFile();
            }
        } else {
            if (listener != null)
                listener.onFailure(PERMISSION_NOT_GRANTED);
            return;
        }

        final long startTime = System.currentTimeMillis();
        File file = new File(input);
        if ((file.length() / (1024 * 1024)) < minimumSize) {
            Log.e(TAG, "Compression is not needed");
            long endTime = System.currentTimeMillis();
            float length = file.length() / 1024f; // Size in KB
            Log.e(TAG, String.format("Old size: %s, New size: %s, How Long? %s seconds", parseSize(file.length() / 1024f),
                    parseSize(length), (endTime - startTime) / 1000));
            if (listener != null)
                listener.onFailure(COMPRESSION_NOT_NEEDED);
            return;
        }

        final long l = file.length();
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(input);
        String width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        Log.e("MainActivity", "duration: " + duration);

        int originalWidth = Integer.parseInt(width);
        int originalHeight = Integer.parseInt(height);
        int rotationValue = Integer.parseInt(rotation);
        startingDuration = Integer.parseInt(duration);

        if (rotationValue == 90 || rotationValue == 270) {
            int temp = originalHeight;
            originalHeight = originalWidth;
            originalWidth = temp;
        }

        int scaling = 2;
        if (originalHeight > 720 && originalWidth > 1280) {
            scaling = 3;
        } else if (originalHeight <= 640 && originalWidth <= 360 ||
                originalWidth <= 640 && originalHeight <= 360) {
            scaling = 1;
        }

        Log.d(TAG, String.format("Selected video size: %s\nResolution: %s x %s (%s)\nScaling level: %s (%s)\nCRF: %s", parseSize(l / 1024f),
                height, width, rotationValue, scaling, originalWidth > 720 && originalHeight > 1280, crf));

        String[] commandArray = new String[]{};
        if (lastMaxValue != -1 && lastMinValue != -1) {
            // compress and trim
            commandArray = new String[]{"-y", "-ss", formatCSeconds(lastMinValue), "-i", input, "-r", String.valueOf(frame),
                    "-vcodec", "libx264", "-b:a", "128k", "-vf", "scale=iw/" + scaling + ":ih/"
                    + scaling, "-preset", "fast", "-crf", String.valueOf(crf), "-t",
                    formatCSeconds(lastMaxValue - lastMinValue), output};
        } else {
            commandArray = new String[]{"-y", "-i", input, "-r", String.valueOf(frame),
                    "-vcodec", "libx264", "-b:a", "128k", "-vf", "scale=iw/" + scaling + ":ih/"
                    + scaling, "-preset", "fast", "-crf", String.valueOf(crf), output};
        }

        Config.resetStatistics();

        FFmpeg.executeAsync(commandArray, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int rc) {
                if (rc == RETURN_CODE_SUCCESS) {
                    Log.i(TAG, "Async command execution completed successfully.");
                    long endTime = System.currentTimeMillis();
                    Config.printLastCommandOutput(Log.INFO);
                    File destination = new File(output);
                    Log.d(TAG, String.format("Old size: %s, New size: %s, How Long? %s seconds", parseSize(l / 1024f),
                            parseSize(destination.length()  / 1024f), (endTime - startTime) / 1000));

                    if (listener != null)
                        listener.onCompleted(output, destination, (endTime - startTime) / 1000);

                } else if (rc == RETURN_CODE_CANCEL) {
                    Log.i(TAG, "Async command execution cancelled by user.");
                } else {
                    Log.i(TAG, String.format("Async command execution failed with rc=%d.", rc));
                    if (listener != null)
                        listener.onFailure(rc);
                }
            }
        });
    }

    public static String formatCSeconds(long timeInSeconds) {
        long hours = timeInSeconds / 3600;
        long secondsLeft = timeInSeconds - hours * 3600;
        long minutes = secondsLeft / 60;
        long seconds = secondsLeft - minutes * 60;

        String formattedTime = "";
        if (hours < 10)
            formattedTime += "0";
        formattedTime += hours + ":";

        if (minutes < 10)
            formattedTime += "0";
        formattedTime += minutes + ":";

        if (seconds < 10)
            formattedTime += "0";
        formattedTime += seconds;

        return formattedTime;
    }

    private String parseSize(float length) {
        String value;
        if (length >= 1024)
            value = length / 1024f + " MB";
        else
            value = length + " KB";
        return value;
    }
}
