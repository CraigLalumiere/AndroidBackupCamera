package com.serenegiant.backgroundcam;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.serenegiant.usb_libuvccamera.LibUVCCameraUSBMonitor;
import com.serenegiant.usb_libuvccamera.UVCCamera;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


public class MyCameraHandler extends Handler {
    private static final String TAG = "My Camera Handler";

    private final Object mSync = new Object();

//    for accessing UVC camera
    private UVCCamera mUVCCamera;

    private boolean mIsPreviewing;
    public static final float DEFAULT_BANDWIDTH = 1.0f;
    private int mWidth = 640;
    private int mHeight = 480;
    private float mBandwidthFactor = DEFAULT_BANDWIDTH;
    private int mPreviewMode = 1;

    private final Set<CameraCallback> mCallbacks = new CopyOnWriteArraySet<CameraCallback>();


    public interface CameraCallback {
        public void onOpen();
        public void onClose();
        public void onStartPreview();
        public void onStopPreview();
        public void onError(final Exception e);
    }


    private static final int MSG_OPEN = 0;
    private static final int MSG_CLOSE = 1;
    private static final int MSG_PREVIEW_START = 2;
    private static final int MSG_PREVIEW_STOP = 3;

    public MyCameraHandler(Looper looper) {
        super(looper);
    }


    @Override
    public void handleMessage(final Message msg) {
        switch (msg.what) {
            case MSG_OPEN:
                Log.v(TAG, "--handler received message open");
                handleOpen((LibUVCCameraUSBMonitor.UsbControlBlock)msg.obj);
                break;
            case MSG_CLOSE:
                Log.v(TAG, "--handler received message close");
                handleClose();
                break;
            case MSG_PREVIEW_START:
                Log.v(TAG, "--handler received message preview start");
                handleStartPreview(msg.obj);
                break;
            case MSG_PREVIEW_STOP:
                Log.v(TAG, "--handler received message preview stop");
                handleStopPreview();
                break;
            default:
                throw new RuntimeException("unsupported message: what = " + msg.what);
        }
    }



    private void handleOpen(final LibUVCCameraUSBMonitor.UsbControlBlock ctrlBlock) {
        Log.v(TAG, "handler handleOpen:");
        handleClose();
        Log.v(TAG, "handler still handleOpen:");
        try {
            final UVCCamera camera = new UVCCamera();
            camera.open(ctrlBlock);
            synchronized (mSync) {
                mUVCCamera = camera;
            }
            callOnOpen();
        } catch (final Exception e) {
            callOnError(e);
        }
        Log.i(TAG, "supportedSize:" + (mUVCCamera != null ? mUVCCamera.getSupportedSize() : null));
    }

    private void handleClose() {
        Log.v(TAG, "handler handleClose:");
        final UVCCamera camera;
        synchronized (mSync) {
            camera = mUVCCamera;
            mUVCCamera = null;
        }
        if (camera != null) {
            camera.stopPreview();
            camera.destroy();
            callOnClose();
        }
    }



    private void handleStartPreview(final Object surface) {
        Log.v(TAG, "handler handleStartPreview:");
        if ((mUVCCamera == null) || mIsPreviewing) return;
        try {
            mUVCCamera.setPreviewSize(mWidth, mHeight, 0,1, 31, mPreviewMode, mBandwidthFactor);
        } catch (final IllegalArgumentException e) {
            try {
                // fallback to YUV mode
                mUVCCamera.setPreviewSize(mWidth, mHeight, 0, 1, 31, UVCCamera.DEFAULT_PREVIEW_MODE, mBandwidthFactor);
            } catch (final IllegalArgumentException e1) {
                callOnError(e1);
                return;
            }
        }
        if (surface instanceof SurfaceHolder) {
            mUVCCamera.setPreviewDisplay((SurfaceHolder)surface);
        } if (surface instanceof Surface) {
            mUVCCamera.setPreviewDisplay((Surface)surface);
        } else {
            mUVCCamera.setPreviewTexture((SurfaceTexture)surface);
        }
        mUVCCamera.startPreview();
        mUVCCamera.updateCameraParams();
        synchronized (mSync) {
            mIsPreviewing = true;
        }
        callOnStartPreview();
    }



    private void handleStopPreview() {
        Log.v(TAG, "handler handleStopPreview:");
        if (mIsPreviewing) {
            if (mUVCCamera != null) {
                mUVCCamera.stopPreview();
            }
            synchronized (mSync) {
                mIsPreviewing = false;
                mSync.notifyAll();
            }
            callOnStopPreview();
        }
        Log.v(TAG, "handler handleStopPreview:finished");
    }




    private void callOnOpen() {
        for (final CameraCallback callback: mCallbacks) {
            try {
                callback.onOpen();
            } catch (final Exception e) {
                mCallbacks.remove(callback);
                Log.w(TAG, e);
            }
        }
    }

    private void callOnClose() {
        for (final CameraCallback callback: mCallbacks) {
            try {
                callback.onClose();
            } catch (final Exception e) {
                mCallbacks.remove(callback);
                Log.w(TAG, e);
            }
        }
    }

    private void callOnStartPreview() {
        for (final CameraCallback callback: mCallbacks) {
            try {
                callback.onStartPreview();
            } catch (final Exception e) {
                mCallbacks.remove(callback);
                Log.w(TAG, e);
            }
        }
    }

    private void callOnStopPreview() {
        for (final CameraCallback callback: mCallbacks) {
            try {
                callback.onStopPreview();
            } catch (final Exception e) {
                mCallbacks.remove(callback);
                Log.w(TAG, e);
            }
        }
    }


    private void callOnError(final Exception e) {
        for (final CameraCallback callback: mCallbacks) {
            try {
                callback.onError(e);
            } catch (final Exception e1) {
                mCallbacks.remove(callback);
                Log.w(TAG, e);
            }
        }
    }



    public void addCallback(final CameraCallback callback) {
        if ((callback != null)) {
            mCallbacks.add(callback);
        }
    }

    public void removeCallback(final CameraCallback callback) {
        if (callback != null) {
            mCallbacks.remove(callback);
        }
    }



    protected void startPreview(final Object surface) {
        Log.v(TAG, "delivering message start preview");
        if (!((surface instanceof SurfaceHolder) || (surface instanceof Surface) || (surface instanceof SurfaceTexture))) {
            throw new IllegalArgumentException("surface should be one of SurfaceHolder, Surface or SurfaceTexture");
        }
        sendMessage(obtainMessage(MSG_PREVIEW_START, surface));
    }


    public void open(final LibUVCCameraUSBMonitor.UsbControlBlock ctrlBlock) {
        Log.v(TAG, "delivering message open");
        Log.v(TAG, "looper = " + getLooper().toString());
        sendMessage(obtainMessage(MSG_OPEN, ctrlBlock));
    }

    public void close() {
        Log.v(TAG, "delivering message close");
        sendEmptyMessage(MSG_CLOSE);
    }

}
