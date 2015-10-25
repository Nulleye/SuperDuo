package it.jaschke.alexandria.CameraPreview;

/*
 * Barebones implementation of displaying camera preview.
 *
 * Created by lisah0 on 2012-02-24
 */

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

import it.jaschke.alexandria.R;

/** A basic Camera preview class */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static String TAG = CameraPreview.class.getName();

    private SurfaceHolder mHolder;

    private Camera mCamera;
    private Camera.PreviewCallback previewCallback;
    private Camera.AutoFocusCallback autoFocusCallback;

    private int orientation = 90;

    private boolean meteringAreasSupported = false;

    private int focusAreaSize;

    private Matrix matrix;

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        focusAreaSize = getResources().getDimensionPixelSize(R.dimen.camera_focus_area_size);
    }

    public CameraPreview(Context context, Camera camera,
                         Camera.PreviewCallback previewCb,
                         Camera.AutoFocusCallback autoFocusCb) {
        super(context);
        focusAreaSize = getResources().getDimensionPixelSize(R.dimen.camera_focus_area_size);
        setupCamera(camera, previewCb, autoFocusCb);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mHolder = holder;
            updateMatrix();
            startCamera();
        } catch(Exception e) {
            Log.d(TAG, "surfaceCreated(): " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolder = holder;
        stopCamera();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged: " + Integer.toString(format) + ", " + Integer.toString(width) +
                ", " + Integer.toString(height));

        try {
            mHolder = holder;
            updateMatrix();
            Surface surface = mHolder.getSurface();
            if (surface == null) return;
            stopCamera();
            startCamera();
        } catch(Exception e) {
            Log.d(TAG, "surfaceChanged(): " + e.getMessage());
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////


    public void setupCamera(Camera camera, Camera.PreviewCallback previewCb,
                          Camera.AutoFocusCallback autoFocusCb) {
        if (mCamera == camera) return;

        releaseCamera();

        try {

            mCamera = camera;
            previewCallback = previewCb;
            autoFocusCallback = autoFocusCb;

            Camera.Parameters p = mCamera.getParameters();
            if (p.getMaxNumMeteringAreas() > 0) meteringAreasSupported = true;

            /*
             * Set camera to continuous focus if supported, otherwise use
             * software auto-focus. Only works for API level >=9.
             */
            /*
            Camera.Parameters parameters = camera.getParameters();
            for (String f : parameters.getSupportedFocusModes()) {
                if (f == Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) {
                    mCamera.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    autoFocusCallback = null;
                    break;
                }
            }
            */

            if (mHolder == null) mHolder = getHolder();
            updateMatrix();
            mHolder.addCallback(this);

            mCamera.setDisplayOrientation(orientation);
            mCamera.setPreviewDisplay(mHolder);

            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

//            Camera.Parameters params = mCamera.getParameters();
//            if (params != null) {
//                params.setPictureFormat(ImageFormat.JPEG);
//                mCamera.setParameters(params);
//            }

        } catch(Exception e) {
            Log.d(TAG, "setupCamera(): " + e.getMessage());
        }
    }


    public void startCamera() {
        try {
//                List<Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
//                mSupportedPreviewSizes = localSizes;
            //requestLayout();

            mCamera.setDisplayOrientation(orientation);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            if (autoFocusCallback != null) mCamera.autoFocus(autoFocusCallback);

        } catch (Exception e) {
            Log.d(TAG, "startCamera(): " + e.getMessage());
        }
    }

    public void stopCamera() {
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            Log.d(TAG, "stopCamera(): " + e.getMessage());
        }
    }

     public void releaseCamera() {
        stopCamera();
        try {
            mCamera.setPreviewCallback(null);
            mCamera.release();
        } catch (Exception e) {
            Log.d(TAG, "releaseCamera(): " + e.getMessage());
        }
        mCamera = null;
    }


    public void setFlashMode(final String mode) {
        try {
            Camera.Parameters params = mCamera.getParameters();
            if (params != null) {
                params.setFlashMode(mode);
                mCamera.setParameters(params);
            }
        } catch (Exception e) {
            Log.d(TAG, "setFlashMode(" + mode + "): " + e.getMessage());
        }
    }


    public void takePicture(Camera.ShutterCallback shutter, Camera.PictureCallback raw,
        Camera.PictureCallback postview, Camera.PictureCallback jpeg) {
        mCamera.takePicture(shutter, raw, postview, jpeg);
    }


    public void setOrientation(int orientation) {
        try {
            if (this.orientation != orientation) {
                this.orientation = orientation;
                mCamera.setDisplayOrientation(orientation);
                updateMatrix();
            }
        } catch (Exception e) {
            Log.d(TAG, "setOrientation((" + orientation + "): " + e.getMessage());
        }
    }


    private void updateMatrix() {
        Rect frame = mHolder.getSurfaceFrame();
        matrix = new Matrix();
        matrix.postRotate(orientation);
        matrix.postScale(frame.width() / 2000f, frame.height() / 2000f);
        matrix.postTranslate(frame.width() / 2f, frame.height() / 2f);
        matrix.invert(this.matrix);
    }


    public void onTouchFocus(MotionEvent event) {
        try {
            Rect focusArea = calculateTapArea(event.getX(), event.getY(), 1f);
            Log.d(TAG, "onTouchFocus(" + event.getX() + "," + event.getY() + ") on [" +
                    getLeft() + "," + getTop() + ":" + getWidth() + "," + getHeight() + "] rect [" +
                    Integer.toString(focusArea.left) + "," + Integer.toString(focusArea.top) + ":" +
                    Integer.toString(focusArea.width()) + "," + Integer.toString(focusArea.height())+ "]");
            mCamera.cancelAutoFocus();
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            List<Camera.Area> cameraAreas = new ArrayList<Camera.Area>();
            cameraAreas.add(new Camera.Area(focusArea, 1000));
            parameters.setFocusAreas(cameraAreas);
            if (meteringAreasSupported) {
                cameraAreas = new ArrayList<Camera.Area>();
                cameraAreas.add(new Camera.Area(calculateTapArea(event.getX(), event.getY(), 1.5f), 1000));
                parameters.setMeteringAreas(cameraAreas);
            }
            mCamera.setParameters(parameters);
            if (autoFocusCallback != null) mCamera.autoFocus(autoFocusCallback);
        } catch (Exception e) {
            Log.d(TAG, "onTouchFocus(): " + e.getMessage());
        }
    }

    private Rect calculateTapArea(float x, float y, float coefficient) {
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int left = clamp((int) x - (areaSize / 2), 0, getWidth() - areaSize);
        int top = clamp((int) y - (areaSize / 2), 0, getHeight() - areaSize);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        matrix.mapRect(rectF);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private int clamp(int x, int min, int max) {
        if (x > max) return max;
        if (x < min) return min;
        return x;
    }

}
