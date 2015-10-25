package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import it.jaschke.alexandria.CameraPreview.CameraPreview;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class BarcodeScanner extends Activity implements
        View.OnClickListener, View.OnTouchListener,
        Camera.AutoFocusCallback, Camera.PreviewCallback, Camera.PictureCallback {

    //Request code to SCAN a BARCODE
    public static final int BARCODE_SCAN = 1001;
    //Scanned code on result intent
    public static final String BARCODE_SCAN_CODE = "barcode_scan_code";

    private static final String BUTTON_FLASH ="button_flash";
    private static final String BUTTON_CAMERA ="button_camera";

    private CameraPreview mCameraView;
    private View mControlsView;
    private Button mFlashButton;
    private Button mPhotoButton;
    private Button mCameraButton;

    private static final int NO_CAMERA = -1;
    private int frontCamera = NO_CAMERA;
    private int backCamera = NO_CAMERA;
    private boolean hasFlash = false;

    private boolean lockButtons = true;

    private OrientationEventListener mOrientationListener = null;

    MultiFormatReader multiFormatReader = new MultiFormatReader();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_barcode_scanner);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);

        mCameraView = (CameraPreview) findViewById(R.id.cameraPreview);
        mCameraView.setOnTouchListener(this);

        mControlsView = findViewById(R.id.cameraControls);

        mFlashButton = (Button) findViewById(R.id.flashButton);
        mPhotoButton = (Button) findViewById(R.id.photoButton);
        mCameraButton = (Button) findViewById(R.id.cameraButton);

        setupButtons(savedInstanceState);
    }


    private void setupButtons(Bundle savedInstanceState) {

        //Set material typeface
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/MaterialIcons-Regular.ttf");
        mFlashButton.setTypeface(typeFace);
        mPhotoButton.setTypeface(typeFace);
        mCameraButton.setTypeface(typeFace);

        //Determine available cameras
        for(int i=0;i<Camera.getNumberOfCameras();i++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) backCamera = i;
            else frontCamera = i;
        }

        //Has flash
        hasFlash = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        CharSequence buttonFlash = null;
        CharSequence buttonCamera = null;
        //Get last config if available
        if (savedInstanceState != null) {
            buttonFlash = savedInstanceState.getCharSequence(BUTTON_FLASH, null);
            buttonCamera = savedInstanceState.getCharSequence(BUTTON_CAMERA, null);
        }

        //Setup flash button
        if (hasFlash) {
            mFlashButton.setOnClickListener(this);
            setButtonFlash(buttonFlash);
        } else mFlashButton.setVisibility(View.GONE);

        //Setup camera button
        if ((backCamera != NO_CAMERA) && (frontCamera != NO_CAMERA)) {
            mCameraButton.setOnClickListener(this);
            setButtonCamera(buttonCamera);
        } else mCameraButton.setVisibility(View.GONE);

        //Setup photo button
        mPhotoButton.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        if (isLockButtons()) return;
             if (v.getId() == mFlashButton.getId()) toggleButtonFlash(true);
        else if (v.getId() == mCameraButton.getId()) toggleButtonCamera(true);
        else if (v.getId() == mPhotoButton.getId()) takePicture();
    }

    public boolean isLockButtons() {
        return lockButtons;
    }

    public void setLockButtons(boolean lockButtons) {
        this.lockButtons = lockButtons;
    }

    private void takePicture() {
        setLockButtons(true);
        mCameraView.takePicture(null, null, null, this);
    }

    private void setButtonFlash(CharSequence charSequence) {
        if (charSequence == null) mFlashButton.setText(getString(R.string.flashButtonAuto));
        else mFlashButton.setText(charSequence);
    }

    private void toggleButtonFlash(boolean update) {
        CharSequence next = null;
        CharSequence current = mFlashButton.getText();
        if (current.equals(getString(R.string.flashButtonAuto))) next = getString(R.string.flashButtonOn);
        else if (current.equals(getString(R.string.flashButtonOn))) next = getString(R.string.flashButtonOff);
        else if (current.equals(getString(R.string.flashButtonOff))) next = getString(R.string.flashButtonAuto);
        setButtonFlash(next);
        if (update) updateFlash();
    }

    protected void updateFlash() {
        String flashMode = null;
        if (mFlashButton.getText().equals(getString(R.string.flashButtonAuto))) flashMode = "auto";
        else if (mFlashButton.getText().equals(getString(R.string.flashButtonOn))) flashMode = "on";
        else if (mFlashButton.getText().equals(getString(R.string.flashButtonOff))) flashMode = "off";
        if (flashMode != null) mCameraView.setFlashMode(flashMode);
    }


    private void setButtonCamera(CharSequence charSequence) {
        if (charSequence == null) mCameraButton.setText(getString(R.string.cameraButtonBack));
        else mCameraButton.setText(charSequence);
    }

    private void toggleButtonCamera(boolean update) {
        CharSequence next = null;
        CharSequence current = mCameraButton.getText();
        if (current.equals(getString(R.string.cameraButtonBack))) next = getString(R.string.cameraButtonFront);
        else if (current.equals(getString(R.string.cameraButtonFront))) next = getString(R.string.cameraButtonBack);
        setButtonCamera(next);
        if (update) updateCamera();
    }

    protected void updateCamera() {
        int cameraId = getCurrentCamera();
        if (cameraId != NO_CAMERA) setupCamera(cameraId);
    }

    protected int getCurrentCamera() {
        int cameraId = NO_CAMERA;
        if (mCameraButton.getText().equals(getString(R.string.cameraButtonBack))) cameraId = backCamera;
        else if (mCameraButton.getText().equals(getString(R.string.cameraButtonFront))) cameraId = frontCamera;
        return cameraId;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putCharSequence(BUTTON_FLASH, mFlashButton.getText());
        outState.putCharSequence(BUTTON_CAMERA, mCameraButton.getText());
    }

    @Override
    protected void onPause() {
        super.onPause();
        setupOrientationListener(false);
        releaseCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCamera(); //Setup camera based on current loaded config
        setupOrientationListener(true);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    private void setupCamera(final int cameraId) {

        setLockButtons(true);
        new AsyncTask<Integer,Void, android.hardware.Camera>() {

            @Override
            protected void onPreExecute() {
                mCameraView.releaseCamera();
            }

            @Override
            protected android.hardware.Camera doInBackground(Integer... params) {
                try {
                    Camera camera = Camera.open(cameraId);
                    if (camera != null) return camera;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Camera camera) {
                if (camera != null) {
                    //Setup camera
                    mCameraView.setupCamera(camera, BarcodeScanner.this, BarcodeScanner.this);
                    //Set current flash mode
                    updateFlash();
                    //Start camera
                    mCameraView.startCamera();
                }
                setLockButtons(false);
            }

        }.execute(cameraId);

    }


    private void releaseCamera() {
        mCameraView.releaseCamera();
    }


    @Override
    public void onAutoFocus(boolean success, Camera camera) {
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        final Camera.Parameters parameters = camera.getParameters();
//        final int format = parameters.getPreviewFormat();
//        final Camera.Size size = parameters.getPreviewSize();
//        YuvImage img = new YuvImage( data, format, size.width, size.height, null);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        new DecodeAsyncTask(camera.getParameters()).execute(data);
        mCameraView.startCamera();
        setLockButtons(false);
    }


    /**
     * Asynchronous task for decoding and finding barcode
     */
    private class DecodeAsyncTask extends AsyncTask<byte[], Void, Result> {

        private final String TAG = DecodeAsyncTask.class.getName();

        private int format;
        private int width;
        private int height;

        private DecodeAsyncTask(Camera.Parameters parameters) {
            Camera.Size size = parameters.getPreviewSize();
            this.format = parameters.getPreviewFormat();
            this.width = size.width;
            this.height = size.height;
        }

        @Override
        protected void onPostExecute(Result result) {
            final String code;
            if ((result != null) && ((code = result.getText()) != null)) {
                Log.i(TAG, "onPostExecute()=" + code);
                Intent resData = new Intent();
                resData.putExtra(BARCODE_SCAN_CODE, code);
                try {
                    //Because we reenable the camera on onPictureTaken() just after
                    //DecodeAsyncTask is started, several DecodeAsyncTasks may overlap
                    //so one could end the activity and another could get here expecting
                    //the activity to be present, so here we eat any exceptions
                    BarcodeScanner.this.setResult(RESULT_OK, resData);
                    BarcodeScanner.this.finish();
                } catch (Exception ignore) {}
            } else Log.i(TAG, "onPostExecute()=[failed]");
        }

        @Override
        protected Result doInBackground(byte[] ... data) {
            Result rawResult = null;
            Bitmap bmp = BitmapFactory.decodeByteArray(data[0], 0, data[0].length);
            if (bmp != null) {
                BinaryBitmap bitmap = cameraBytesToBinaryBitmap(bmp);
                if (bitmap != null) {
                    try {
                        Map<DecodeHintType,Object> tmpHintsMap = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
                        tmpHintsMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
                        formats.add(BarcodeFormat.EAN_13);
                        tmpHintsMap.put(DecodeHintType.POSSIBLE_FORMATS, formats);
                        //tmpHintsMap.put(DecodeHintType.PURE_BARCODE, Boolean.FALSE);
                        rawResult = multiFormatReader.decode(bitmap, tmpHintsMap);
                        //rawResult = multiFormatReader.decode(bitmap);
                    } catch (Exception e) {
                        Log.e(TAG, "doInBackground(): " + e);
                    } finally {
                        multiFormatReader.reset();
                    }
                }
            }
            return rawResult;
        }

        public BinaryBitmap cameraBytesToBinaryBitmap(Bitmap bitmap) {
            BinaryBitmap binaryBitmap = null;
            if (bitmap != null) {
                try {
                    int[] pixels = new int[bitmap.getHeight() * bitmap.getWidth()];
                    bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0,
                            bitmap.getWidth(), bitmap.getHeight());
                    RGBLuminanceSource source = new RGBLuminanceSource(
                            bitmap.getWidth(), bitmap.getHeight(), pixels);
                    HybridBinarizer bh = new HybridBinarizer(source);
                    binaryBitmap = new BinaryBitmap(bh);
                } catch(Exception e) {
                    Log.e(TAG, "cameraBytesToBinaryBitmap(): " + e);
                }
            } else Log.d(TAG,"cameraBytesToBinaryBitmap(null)");
            return binaryBitmap;
        }

    } //DecodeAsyncTask


    private void setupOrientationListener(final boolean enable) {
        if (enable) {
            if (mOrientationListener == null)
                mOrientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
                    @Override
                    public void onOrientationChanged(int orientation) {
                        int newOrientation = 0;
                        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
                        switch (display.getRotation()) {
                            case Surface.ROTATION_0:
                                //mCameraView.setOrientation(90);
                                newOrientation = 0;
                                break;
                            case Surface.ROTATION_180:
                                //mCameraView.setOrientation(270);
                                newOrientation = 180;
                                break;
                            case Surface.ROTATION_270:
                                //mCameraView.setOrientation(180);
                                newOrientation = 270;
                                break;
                            case Surface.ROTATION_90:
                                //mCameraView.setOrientation(0);
                                newOrientation = 90;
                                break;
                        };
                        Camera.CameraInfo info = new Camera.CameraInfo();
                        Camera.getCameraInfo(getCurrentCamera(), info);
                        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            newOrientation = (info.orientation + newOrientation) % 360;
                            newOrientation = (360 - newOrientation) % 360;  // compensate the mirror
                        } else {  // back-facing
                            newOrientation = (info.orientation - newOrientation + 360) % 360;
                        }
                        mCameraView.setOrientation(newOrientation);
                    }
                };
            mOrientationListener.enable();
        } else if (mOrientationListener != null) mOrientationListener.disable();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mCameraView.onTouchFocus(event);
        return false;
    }

}
