/*
 * File: ScanActivity.java
 * Date: May 14, 2014
 * Author: Joe Kale (jpk017)
 * 
 * Copyright 2014 jpk017
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


import java.util.HashMap;
import com.google.android.glass.media.Sounds;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



/**
 * The Class LocationActivity.  This activity uses the Mirror API to check last known
 * location of user.  It also uses compass functionality to check the heading of the user.
 */
public class LocationActivity extends Activity {

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

	
	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" + ", "
			+ LocationActivity.class.getSimpleName();
	
	/**
	 * The Constant KEY_SWIPE_DOWN. Used to handle the backwards compatibility
	 * for the swipe down action used to dismiss views in glass.
	 */
	private static final int KEY_SWIPE_DOWN = 4;
	
	/**
	 * The Constant CARDS_READY_KEY. Used in intent passing to contain the
	 * status of the synchronization process, either true for completed or false
	 * for not completed.
	 */
	public static final String CARDS_READY_KEY = "cards_ready";
	
	/**
	 * The audio manager for this class. Used to provide a response to user
	 * actions.
	 */
	private AudioManager mAudioManager;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		setContentView(R.layout.location_activity_layout);
		Log.i(TAG, "onCreateCalled");
		
		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);		

        LocationManager locationManager=
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = getLocation(locationManager);
        // TODO get db url/ip.  Unsure how to do this at this stage.
        HashMap<Location, String> dbLocations = pullLocations("/*DB_URL*/");
        checkLocationAgainstDB(location, dbLocations);
	}
	
	// temporary debug code?
	// stolen from ScanActivity
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

			// start the next activity
			Intent intent = new Intent(this, SelectCardActivity.class);
			intent.putExtra(CARDS_READY_KEY, false);
			startActivity(intent);
			finish();
			return true;
		}
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	public void onPause() {
		super.onPause();
		
		// TODO insert any other pause behavior
	}
	
	/**
	 * Finds a suitable location provider given stringent criteria, then
	 * gets the location that provider is giving.  This should only be called 
	 * explicitly by the onCreate method.
	 * 
	 * @param locationManager 
	 * 			Location manager used to get location.
	 */
	Location getLocation(LocationManager locationManager) {
		// Set criteria necessary for a provider
		Criteria providerCriteria = new Criteria();
		providerCriteria.setAltitudeRequired(true);
		providerCriteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
		providerCriteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
		providerCriteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
		
		String providerString = locationManager.getBestProvider(providerCriteria, true);
		LocationProvider provider = locationManager.getProvider(providerString);
		
		// Checks if there is a provider and if that provider meets the criteria
		// getBestProvider() can return a provider with criteria less than specified
		if (provider == null || !provider.meetsCriteria(providerCriteria)) {
			
			// No suitable provider found meeting the criteria.  Ask user to enable 
			// location services.
			// TODO implement user interface to prompt user to enable gps
			Log.e(TAG, "No location providers.  Enable location services to allow "
					+ "location based content.");
			return null;
		}
		return locationManager.getLastKnownLocation(providerString);
	}
	
	HashMap<Location, String> pullLocations(String url){
		// TODO implement pull from db using url or default url
		if (url.length() == 0) {
			// Pull from default url
		} else {
			// Pull from provided url
		}
		// Parse JSON array into location objects and store in hashmap
		// Return hashmap
		return null;
	}
	
	/**
	 * 
	 * @param location
	 * 			Location object with horizontal, vertical, 
	 */
	void checkLocationAgainstDB(Location location, HashMap<Location, String> dbLocations) {
		//TODO pull location objects from database, store them in hashset
		if (dbLocations.containsKey(location)){
			startCardDownload(dbLocations.get(location));
		} else {
			Log.e(TAG, "There is nothing around you to learn more about.");
		}
	}
	
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
	
}
