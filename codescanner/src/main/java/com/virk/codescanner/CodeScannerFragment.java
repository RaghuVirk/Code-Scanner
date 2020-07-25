package com.virk.codescanner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.snackbar.Snackbar;
import com.virk.codescanner.cameraUI.CameraSource;
import com.virk.codescanner.cameraUI.CameraSourcePreview;
import com.virk.codescanner.cameraUI.GraphicOverlay;


import java.io.IOException;

import static com.virk.codescanner.BarcodeCaptureActivity.BarcodeFormats;
import static com.virk.codescanner.BarcodeCaptureActivity.RC_HANDLE_CAMERA_PERM;
import static com.virk.codescanner.BarcodeCaptureActivity.RC_HANDLE_GMS;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CodeScannerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CodeScannerFragment extends Fragment {
    private static final String TAG = "Barcode-reader";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;
    private CheckBox useFlashLight;

    // helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private boolean autoFocus = true;
    private boolean useFlash = false;
    private boolean previewCode = false;
    private boolean toolbarVisible = true;
    private boolean shouldHaveBack = true;
    private int barcodeFormats = Barcode.ALL_FORMATS;

    private Toolbar toolBar;
    private Switch fSwitch;
    private String toolBarTitle = "";
    //    private EditText scannedCode;
    private Barcode capturedBarCode;
    View rootView;
    private TextView tvToolbarTitle;
    private BarcodeGraphicTracker.BarcodeUpdateListener listener;

    public CodeScannerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (BarcodeGraphicTracker.BarcodeUpdateListener) context;
        } catch (ClassCastException castException) {
            Log.i(TAG, "onAttach:  BarcodeGraphicTracker.BarcodeUpdateListener Is not implemented in Parent activity");
            /** The activity does not implement the listener. */
            castException.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CodeScannerFragment.
     */
    public static CodeScannerFragment newInstance() {
        CodeScannerFragment fragment = new CodeScannerFragment();
        Bundle args = new Bundle();
        args.putBoolean(BarcodeCaptureActivity.AutoFocus, true);
        args.putBoolean(BarcodeCaptureActivity.UseFlash, false);
        args.putBoolean(BarcodeCaptureActivity.PreviewCode, false);
        args.putBoolean(BarcodeCaptureActivity.PreviewCode, false);
        args.putInt(BarcodeFormats, Barcode.ALL_FORMATS);
        fragment.setArguments(args);
        return fragment;
    }

    public static CodeScannerFragment newInstance(Activity context, boolean shouldUseFlash,
                                                  boolean shouldPreviewCode,
                                                  boolean shouldHaveToolbar,
                                                  boolean shouldHaveBack,
                                                  @NonNull int barcodeFormats,
                                                  String toolTitle) {
        CodeScannerFragment fragment = new CodeScannerFragment();
        Bundle args = new Bundle();
        args.putBoolean(BarcodeCaptureActivity.AutoFocus, true);
        args.putBoolean(BarcodeCaptureActivity.UseFlash, shouldUseFlash);
        args.putBoolean(BarcodeCaptureActivity.PreviewCode, shouldPreviewCode);
        args.putString(BarcodeCaptureActivity.title, toolTitle);
        args.putBoolean(BarcodeCaptureActivity.shouldTitleVisible, shouldHaveToolbar);
        args.putBoolean(BarcodeCaptureActivity.shouldShowBack, shouldHaveBack);
        args.putInt(BarcodeFormats, barcodeFormats);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            autoFocus = getArguments().getBoolean(BarcodeCaptureActivity.AutoFocus);
            useFlash = getArguments().getBoolean(BarcodeCaptureActivity.UseFlash);
            previewCode = getArguments().getBoolean(BarcodeCaptureActivity.PreviewCode);
            toolbarVisible = getArguments().getBoolean(BarcodeCaptureActivity.shouldTitleVisible);
            shouldHaveBack = getArguments().getBoolean(BarcodeCaptureActivity.shouldShowBack);
            toolBarTitle = getArguments().getString(BarcodeCaptureActivity.title);
            barcodeFormats = getArguments().getInt(BarcodeFormats, Barcode.ALL_FORMATS);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_code_scanner, container, false);
        }
        mPreview = rootView.findViewById(R.id.preview);
        mGraphicOverlay = rootView.findViewById(R.id.graphicOverlay);
        toolBar = rootView.findViewById(R.id.toolbar);
        fSwitch = rootView.findViewById(R.id.flash_switch);
        tvToolbarTitle = rootView.findViewById(R.id.toolbar_title);
        try {
            if (toolbarVisible) {
                if (TextUtils.isEmpty(toolBarTitle)) {
                    toolBarTitle = "Scanner";
                }
                setHasOptionsMenu(true);
                setupToolBar(toolBarTitle);
                fSwitch.setVisibility(View.GONE);
            } else {
                toolBar.setVisibility(View.GONE);
                fSwitch.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        initializeScanner(autoFocus, useFlash, previewCode);
        Snackbar.make(mGraphicOverlay, "Pinch/Stretch to zoom",
                Snackbar.LENGTH_SHORT)/*.setBackgroundTint(getResources().getColor(R.color.white))
                .setTextColor(getResources().getColor(R.color.black))*/
                .show();
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                boolean b = scaleGestureDetector.onTouchEvent(e);

                boolean c = gestureDetector.onTouchEvent(e);

                return b || c || onTouch(rootView, e);
            }
        });
        fSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useFlash = !useFlash;
                initializeScanner(autoFocus, useFlash, previewCode);
                onResume();
            }
        });
               /* fSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        useFlash = !useFlash;
                        initializeScanner(autoFocus, useFlash, previewCode);
                        onResume();
                    }
                });*/
        return rootView;
    }

    public void setupToolBar(String title) throws Exception {
        if (shouldHaveBack) {
            toolBar.setTitle(title);
            tvToolbarTitle.setVisibility(View.GONE);
        } else {
            toolBar.setTitle("");
            tvToolbarTitle.setText(title);
            tvToolbarTitle.setVisibility(View.VISIBLE);
        }
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolBar);
        if (shouldHaveBack && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.frag_scanner_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_use_flash) {
            useFlash = !useFlash;
            initializeScanner(autoFocus, useFlash, previewCode);
            onResume();
        } else if (item.getItemId() == R.id.menu_done) {
            /* if (capturedBarCode != null) {
             *//*  Intent data = new Intent();
                data.putExtra(BarcodeObject, capturedBarCode);
                setResult(CommonStatusCodes.SUCCESS, data);
                finish();*//*
            } else {
                Toast.makeText(getActivityContext(), "Please scan QR/Bar code properly.", Toast.LENGTH_SHORT).show();
            }*/
        } else if (item.getItemId() == android.R.id.home) {
//            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkFlash() {
        fSwitch.setChecked(useFlash);
        if (useFlash) {
            changeMenuIcon(R.id.menu_use_flash, R.drawable.ic_flash_off);
        } else {
            changeMenuIcon(R.id.menu_use_flash, R.drawable.ic_flash_on);
        }
    }

    private void changeMenuIcon(@IdRes int menuId, @DrawableRes int icon) {
        MenuItem item = toolBar.getMenu().findItem(menuId);
        if (item != null) {
            item.setIcon(icon);
        }
    }

    private void enableMenu() {
        MenuItem item = toolBar.getMenu().findItem(R.id.menu_done);
        MenuItem flashItem = toolBar.getMenu().findItem(R.id.menu_use_flash);
        if (item != null) {
            item.setEnabled(true);
        }
        if (flashItem != null) {
            flashItem.setEnabled(false);
        }
        fSwitch.setEnabled(false);
        changeMenuIcon(R.id.menu_use_flash, R.drawable.ic_flash_on);
    }

    private void initializeScanner(boolean autoFocus, boolean useFlash, boolean previewCode) {
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        if (mPreview != null) {
            mPreview.release();
        }
        int rc = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash, previewCode);
        } else {
            requestCameraPermission();
        }

        checkFlash();
        gestureDetector = new GestureDetector(getActivity(), new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w("TAG", "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(getActivity(), permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = getActivity();

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        2/*RC_HANDLE_CAMERA_PERM*/);
            }
        };

        rootView.findViewById(R.id.topLayout).setOnClickListener(listener);
        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_SHORT)
                .setAction(R.string.ok, listener)
                .show();
    }


/*
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }
*/

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     * <p>
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash, boolean previewCode) {
        Context context = getActivity().getApplicationContext();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
//        BarcodeDetector bx = new BarcodeDetector(barcodeDetector,pxFromDp(280), height, wight);

        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).setBarcodeFormats(barcodeFormats).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay, getActivity(), previewCode, new BarcodeTrackerFactory.CodeDetectionListener() {
            @Override
            public void onCodeDetected(final Barcode barcode) {
                Log.e("TAG", "onCodeDetected: ScannedCode - " + barcode.rawValue);
                capturedBarCode = barcode;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /*if (listener != null) {
                            listener.onBarcodeDetected(barcode);
                        }*/
                        enableMenu();
                        if (mPreview != null) {
                            mPreview.stop();
                        }
                    }
                });


            }
        });
        barcodeDetector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w("TAG", "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = getActivity().registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(getContext(), R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w("TAG", getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        CameraSource.Builder builder = new CameraSource.Builder(getActivity(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1024, 1024)
                .setRequestedFps(15.0f);

        // make sure that auto focus is an available option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setFocusMode(
                    autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);
        }

        mCameraSource = builder
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d("TAG", "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("TAG", "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
         /*   boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);*/
            createCameraSource(autoFocus, useFlash, previewCode);
            return;
        }

        Log.e("TAG", "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
//                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Permission Required!")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getActivity().getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e("TAG", "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /**
     * onTap returns the tapped barcode result to the calling Activity.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the activity is ending.
     */
    private boolean onTap(float rawX, float rawY) {
        // Find tap point in preview frame coordinates.
        int[] location = new int[2];
        mGraphicOverlay.getLocationOnScreen(location);
        float x = (rawX - location[0]) / mGraphicOverlay.getWidthScaleFactor();
        float y = (rawY - location[1]) / mGraphicOverlay.getHeightScaleFactor();

        // Find the barcode whose center is closest to the tapped point.
        Barcode best = null;
        float bestDistance = Float.MAX_VALUE;
        for (BarcodeGraphic graphic : mGraphicOverlay.getGraphics()) {
            Barcode barcode = graphic.getBarcode();
            if (barcode.getBoundingBox().contains((int) x, (int) y)) {
                // Exact hit, no need to keep looking.
                best = barcode;
                break;
            }
            float dx = x - barcode.getBoundingBox().centerX();
            float dy = y - barcode.getBoundingBox().centerY();
            float distance = (dx * dx) + (dy * dy);  // actually squared distance
            if (distance < bestDistance) {
                best = barcode;
                bestDistance = distance;
            }
        }

        if (best != null) {
            if (listener != null)
                listener.onBarcodeDetected(best);
            return true;
        }
        return false;
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }

}
