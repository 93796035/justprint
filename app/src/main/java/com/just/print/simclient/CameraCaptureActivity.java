package com.just.print.simclient;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** In-app camera: one shutter press saves immediately, without a review screen. */
public final class CameraCaptureActivity extends Activity implements LifecycleOwner {
    public static final String EXTRA_OUTPUT_PATH = "outputPath";
    private static final int CAMERA_PERMISSION = 701;
    private static final String TAG = "SimClientCamera";
    private final LifecycleRegistry lifecycle = new LifecycleRegistry(this);
    private final ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    private PreviewView previewView;
    private Button shutter;
    private ProcessCameraProvider cameraProvider;
    private Preview preview;
    private ImageCapture imageCapture;
    private File output;

    @Override public Lifecycle getLifecycle() { return lifecycle; }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setResult(RESULT_CANCELED);
        output = validOutput(getIntent().getStringExtra(EXTRA_OUTPUT_PATH));
        if (output == null) { finish(); return; }
        createUi();
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera();
        else requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
    }

    private File validOutput(String path) {
        if (path == null) return null;
        try {
            File directory = new File(getCacheDir(), "age-check").getCanonicalFile();
            File file = new File(path).getCanonicalFile();
            return file.getParentFile().equals(directory) ? file : null;
        } catch (Exception ignored) { return null; }
    }

    private void createUi() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        previewView = new PreviewView(this);
        previewView.setScaleType(PreviewView.ScaleType.FILL_CENTER);
        root.addView(previewView, new FrameLayout.LayoutParams(-1, -1));

        TextView hint = new TextView(this);
        hint.setText("Point at an ID or beer barcode");
        hint.setTextColor(Color.WHITE);
        hint.setTextSize(18);
        hint.setGravity(Gravity.CENTER);
        hint.setBackgroundColor(0x66000000);
        FrameLayout.LayoutParams hintParams = new FrameLayout.LayoutParams(-1, dp(56), Gravity.TOP);
        hintParams.setMargins(dp(12), dp(18), dp(12), 0);
        root.addView(hint, hintParams);

        shutter = new Button(this);
        shutter.setText("●");
        shutter.setTextSize(46);
        shutter.setTextColor(Color.WHITE);
        shutter.setContentDescription("Take photo");
        shutter.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xcc087f8c));
        shutter.setEnabled(false);
        shutter.setOnClickListener(view -> takePhoto());
        FrameLayout.LayoutParams shutterParams = new FrameLayout.LayoutParams(dp(84), dp(84), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        shutterParams.bottomMargin = dp(30);
        root.addView(shutter, shutterParams);

        Button close = new Button(this);
        close.setText("×");
        close.setTextSize(30);
        close.setTextColor(Color.WHITE);
        close.setContentDescription("Cancel camera");
        close.setBackgroundColor(Color.TRANSPARENT);
        close.setOnClickListener(view -> finish());
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(dp(64), dp(64), Gravity.TOP | Gravity.END);
        closeParams.setMargins(0, dp(14), dp(8), 0);
        root.addView(close, closeParams);
        setContentView(root);
    }

    private void startCamera() {
        // The provider is already initialized on subsequent captures, so its callback can run
        // during onCreate before PreviewView has a Display. Post the bind until the view is attached.
        previewView.post(this::bindCamera);
    }

    private void bindCamera() {
        if (isFinishing() || isDestroyed()) return;
        ListenableFuture<ProcessCameraProvider> future = ProcessCameraProvider.getInstance(this);
        future.addListener(() -> {
            try {
                if (isFinishing() || isDestroyed()) return;
                cameraProvider = future.get();
                preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                        .setTargetRotation(previewView.getDisplay().getRotation()).build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture);
                shutter.setEnabled(true);
            } catch (Exception failure) {
                Log.e(TAG, "Unable to bind camera", failure);
                Toast.makeText(this, "Camera unavailable", Toast.LENGTH_LONG).show();
                finish();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null || !shutter.isEnabled()) return;
        shutter.setEnabled(false);
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(output).build();
        imageCapture.takePicture(options, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
            @Override public void onImageSaved(ImageCapture.OutputFileResults result) {
                runOnUiThread(() -> { setResult(RESULT_OK); finish(); });
            }
            @Override public void onError(ImageCaptureException failure) {
                runOnUiThread(() -> {
                    Toast.makeText(CameraCaptureActivity.this, "Photo failed. Try again.", Toast.LENGTH_LONG).show();
                    shutter.setEnabled(true);
                });
            }
        });
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode != CAMERA_PERMISSION) return;
        if (results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) startCamera();
        else { Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show(); finish(); }
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    @Override protected void onStart() { super.onStart(); lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START); }
    @Override protected void onResume() { super.onResume(); lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME); }
    @Override protected void onPause() { lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE); super.onPause(); }
    @Override protected void onStop() { lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP); super.onStop(); }
    @Override protected void onDestroy() {
        if (cameraProvider != null) {
            try {
                if (preview != null && imageCapture != null) cameraProvider.unbind(preview, imageCapture);
                else cameraProvider.unbindAll();
            } catch (RuntimeException failure) {
                Log.w(TAG, "Unable to release camera cleanly", failure);
            }
        }
        preview = null;
        imageCapture = null;
        cameraProvider = null;
        lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        cameraExecutor.shutdown();
        super.onDestroy();
    }
}
