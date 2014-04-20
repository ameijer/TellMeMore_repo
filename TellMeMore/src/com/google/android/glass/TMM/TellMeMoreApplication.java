/*
 * File: TellMeMoreApplication.java
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

import java.io.File;
import java.io.IOException;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * The Class TellMeMoreApplication. This is an application-level singleton that
 * provides resources to all components of the application. It's primary
 * function in this application is to contain the database and its manager for
 * this application.
 */
public class TellMeMoreApplication extends Application {

	/** The global card data DB, abstracted through a manager */
	public DBManager db;

	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" + ", "
			+ TellMeMoreApplication.class.getSimpleName();

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();

		Log.d(TAG, "Application-level oncreate called");
		try {

			// open up the DB or create a new one if it doesn't exist
			db = new DBManager(this);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// check state of external storage, as detailed in
		// http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
		boolean iswriteable = true;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			Log.i(TAG, "Application onCreate reports storage ready for R+W");
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			Log.w(TAG, "Application onCreate reports storage ready for R only");
			iswriteable = false;
			Toast.makeText(
					getApplicationContext(),
					"Warning - readable only storage. No new audio may be downloaded",
					Toast.LENGTH_LONG).show();
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			Log.e(TAG, "Application onCreate reports storage NON R/W-able");
			iswriteable = false;
			Toast.makeText(
					getApplicationContext(),
					"Warning - storage is inaccesible. No audio content available",
					Toast.LENGTH_LONG).show();
		}

		// mkdir for the data if it isn't already there
		if (iswriteable) {
			File dir = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/tmm");
			boolean exists = dir.exists();
			if (!exists) {
				dir.mkdirs();
			}
		}

	}

}
