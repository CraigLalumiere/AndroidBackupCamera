/*
 *  UVCCamera
 *  library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014-2017 saki t_saki@serenegiant.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 *  All files in the folder are under this Apache License, Version 2.0.
 *  Files in the libjpeg-turbo, libusb, libuvc, rapidjson folder
 *  may have a different license, see the respective files.
 */

package com.serenegiant.backgroundcam;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.usb.UsbDevice;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.serenegiant.common.BaseActivity;

import com.serenegiant.usb_libuvccamera.CameraDialog;
import com.serenegiant.usb_libuvccamera.LibUVCCameraDeviceFilter;
import com.serenegiant.usbcameracommon.UVCCameraHandler;
import com.serenegiant.usb_libuvccamera.LibUVCCameraUSBMonitor;
import com.serenegiant.usb_libuvccamera.LibUVCCameraUSBMonitor.OnDeviceConnectListener;
import com.serenegiant.usb_libuvccamera.LibUVCCameraUSBMonitor.UsbControlBlock;
import com.serenegiant.usb_libuvccamera.UVCCamera;
import com.serenegiant.widget.CameraViewInterface;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class MainActivity extends Activity {
	private static final String TAG = "MainActivity";

	int CODE_PERM_SYSTEM_ALERT_WINDOW = 6111;
	int CODE_PERM_CAMERA = 6112;



	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate");
//		setContentView(R.layout.activity_main);
//		getActionBar().hide();
		Log.v(TAG, "onCreate");


		String permission = Manifest.permission.CAMERA;
		if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
			// We don't have camera permission yet. Request it from the user.
			ActivityCompat.requestPermissions(this, new String[]{permission}, CODE_PERM_CAMERA);
		}


		if (!Settings.canDrawOverlays((Context)MainActivity.this)) {
			Intent settingsIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
			startActivityForResult(settingsIntent, CODE_PERM_SYSTEM_ALERT_WINDOW);
		}
		else {

			if (!isServiceRunning((Context) MainActivity.this, CamService.class)) {
				Intent intent = new Intent((Context) this, CamService.class);
				startService(intent);
			} else
				stopService(new Intent(this, CamService.class));
			finish();
		}

	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.v(TAG, "onStart");
	}
	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");
	}
	@Override
	protected void onPause() {
		super.onPause();
//		mCameraHandler.stopPreview();
		Log.v(TAG, "onPause");
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.v(TAG, "onDestroy");
	}




	public boolean isServiceRunning(Context context, Class serviceClass) {
		try {
			ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
				if (serviceClass.getName().equals(service.service.getClassName())) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}


}
