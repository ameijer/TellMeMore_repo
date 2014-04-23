/*
 * File: ScanActivity.java
 * Date: Apr 16, 2014
 * Authors: A. Meijer (atm011) and D. Prudente (dcp017)
 * 
 * Written for ELEC429, Independent Study
 * 
 * Copyright 2014 atm011 and dcp017
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this 
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.google.android.glass.TMM;

import com.google.android.glass.media.Sounds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;


import android.widget.Toast;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Button;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;

/* Import ZBar Class files */
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;

/**
 * The Class ScanActivity. This activity handles the scanning of QR codes and
 * their interpretation. The cardloader service is started from here, as is the
 * selectcardactivity.
 */
public class ScanActivity extends Activity {

	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" + ", "
			+ ScanActivity.class.getSimpleName();

	/**
	 * The Constant TARGET_SERVER_KEY. This is used for retrieving intent data,
	 * specifically the name of the DB to synchronize.
	 */
	public static final String TARGET_SERVER_KEY = "target_server";

	/**
	 * The Constant EXAMPLE_CARD_SERVER. This is our default server to use if
	 * there is no QR code available.
	 */
	public static final String EXAMPLE_CARD_SERVER = "example_card_generator";

	/**
	 * The Constant CARDS_READY_KEY. Used to alert the scanactivity that the
	 * cards have not yet been updated by the cardloader service and that it
	 * should splay a waiting screen.
	 */
	public static final String CARDS_READY_KEY = "cards_ready";

	/**
	 * The Constant KEY_SWIPE_DOWN. Used to handle the backwards compatibility
	 * for the swipe down action used to dismiss views in glass.
	 */
	private static final int KEY_SWIPE_DOWN = 4;

	/** The camera object used by the app to read QR codes to determine its context. */
	private Camera mCamera;

	/** The camera preview used in this activity to obtain QR code scans. */
	private CameraPreview mPreview;

	/** The auto focus handler. */
	private Handler autoFocusHandler;

	/** The audio manager for this class. Used to provide a response to user actions. */
	private AudioManager mAudioManager;

	/** The scanner object used to interpret QR codes. */
	ImageScanner scanner;

	/** The previewing flag controlling the behavior of the activity. */
	private boolean previewing = true;

	static {
		System.loadLibrary("iconv");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		setContentView(R.layout.scan_activity_layout);
		Log.i(TAG, "onCreateCalled");

		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		autoFocusHandler = new Handler();

		// For some reason, right after launching from the "ok, glass" menu the
		// camera is locked
		// Try 3 times to grab the camera, with a short delay in between.
		for (int i = 0; i < 3; i++) {
			mCamera = getCameraInstance();
			if (mCamera != null)
				break;

			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (mCamera == null) {
			Toast.makeText(this, "Camera cannot be locked", Toast.LENGTH_SHORT)
					.show();
			finish();
		}

		/* Instance barcode scanner */
		createScanner();

		//call the scanQR method
		scanQR();
	}

	// temporary debug code
	// atm011
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KEY_SWIPE_DOWN) {

			// when we start the select card activity from here, we are going to
			// want to download/update the new cards, so
			// tell the selectcardactivity that the cards aren't ready yet

			// there was a swipe down event
			Log.i(TAG, "hacky swipe_down method called");
			mAudioManager.playSoundEffect(Sounds.DISMISSED);

			// start the card downloader service using the default server
			startCardDownload(EXAMPLE_CARD_SERVER);
			
			//start the next activity
			Intent intent = new Intent(this, SelectCardActivity.class);
			intent.putExtra(CARDS_READY_KEY, false);
			startActivity(intent);
			finish();
			return true;
		}
		return false;
	}

	/**
	 * Creates and configures the scanner.
	 */
	public void createScanner() {
		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);
	}

	/**
	 * Scan the QR code.
	 */
	public void scanQR() {
		mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
		FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
		preview.addView(mPreview);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	public void onPause() {
		super.onPause();
		releaseCamera();
	}

	/**
	 * A safe way to get an instance of the Camera object.
	 * 
	 * @return the camera instance
	 */
	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
			Log.d(TAG, "getCamera = " + c);
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
		return c;
	}

	/**
	 * Release camera.
	 */
	private void releaseCamera() {
		if (mCamera != null) {
			previewing = false;
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	/** The auto-focusing thread. */
	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (previewing)
				mCamera.autoFocus(autoFocusCB);
		}
	};

	/** The callback that is called when the preview view is running. */
	PreviewCallback previewCb = new PreviewCallback() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.hardware.Camera.PreviewCallback#onPreviewFrame(byte[],
		 * android.hardware.Camera)
		 */
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();

			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(data);

			int result = scanner.scanImage(barcode);

			if (result != 0) {
				previewing = false;
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();

				String text = "";
				SymbolSet syms = scanner.getResults();
				for (Symbol sym : syms) {
					text = sym.getData();
					
					//start the carddownloader service as soon as we have a good URL
					startCardDownload(text);
					break;
				}
				mAudioManager.playSoundEffect(Sounds.SUCCESS);
				
				//start the next activity
				Context context = getApplicationContext();
				Intent intent = new Intent(context, SelectCardActivity.class);
				intent.putExtra(CARDS_READY_KEY, false);
				
				startActivity(intent);
				finish();
			}
		}
	};

	/**
	 * Start card download.
	 * 
	 * @param url
	 *            The name of the DB to obtain/synchronize
	 */
	void startCardDownload(String url) {

		// start the card download service
		Intent intent = new Intent(this, CardLoaderService.class);
		intent.putExtra(TARGET_SERVER_KEY, url);
		startService(intent);
		return;
	}

	/** Mimic continuous auto-focusing*/
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};

}