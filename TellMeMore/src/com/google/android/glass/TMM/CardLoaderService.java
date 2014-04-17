/*
 * File: CardLoaderService.java
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

import java.io.IOException;

import com.couchbase.lite.CouchbaseLiteException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

/**
 * The Class CardLoaderService. This is an android service that manages the
 * loading/synchronization of the cards and their databases. It is also
 * responsible for alerting the system
 */
public class CardLoaderService extends Service {

	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" + ", "
			+ CardLoaderService.class.getSimpleName();

	/**
	 * A reference to the parent application. This is used primarily to access
	 * the DB of cards.
	 */
	private TellMeMoreApplication app;

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

	/** The target server name (this will be synchronized). */
	private String targetServer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// obtain a reference to the application singleton
		app = ((TellMeMoreApplication) this.getApplication());

		// attempt to retrieve name of target server from intent
		if (intent != null) {
			targetServer = intent.getStringExtra(TARGET_SERVER_KEY);
		}
		Log.d(TAG, "target server retreived from intent: " + targetServer);

		// if there is no target server passed, we default to the example server
		if (targetServer == null) {
			Log.e(TAG, "no target server retreived! Using example server");
			targetServer = EXAMPLE_CARD_SERVER;
		}

		if (targetServer.equalsIgnoreCase(EXAMPLE_CARD_SERVER)) { // we are
			// using the
			// example
			// server
			Log.d(TAG, "Cardloader service is using DB: " + app.db);

			try {

				// open + sync the database up
				app.db.open(EXAMPLE_CARD_SERVER, this);

				// start an async task to let the system know when the DB has
				// finished synchronizing
				new cardsLoadedBroadcaster().execute(this);

				Log.i(TAG,
						"CONTENTS OF ENTIRE DB IN CARDLOADERSERVICE FOLLOWS: "
								+ app.db.getEntireDbAsJSON());
			} catch (IOException e) {
				e.printStackTrace();
			} catch (CouchbaseLiteException e) {
				e.printStackTrace();
			}
		} else { // we have an actual target
			try {

				// open + sync the database up
				app.db.open(targetServer, this);

				// start an async task to let the system know when the DB has
				// finished synchronizing
				new cardsLoadedBroadcaster().execute(this);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (CouchbaseLiteException e) {
				e.printStackTrace();
			}

		}

		return Service.START_STICKY;
	}

	/**
	 * The Class cardsLoadedBroadcaster. This is an AsyncTask that repeatedly
	 * checks if the DB has completed its synchronizaton, and broadcasts a
	 * notification to the entire system when it has completed the sync.
	 */
	private class cardsLoadedBroadcaster extends
	AsyncTask<Context, Integer, Long> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		protected Long doInBackground(Context... contexts) {
			while (!app.db.isSynced()) {
				Log.i(TAG, "cardsloaded broadcaster, issynched reports: "
						+ app.db.isSynced());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e3) {
					Log.e(TAG, "broadcast sleeper thread was interrupted", e3);

				}
			}

			//here, we have fallen out of the while loop which means the DB must be synched
			broadcastCardsLoaded(contexts[0], targetServer);

			return (long) 1;

		}

	}

	/**
	 * Broadcast cards loaded. Use this to broadcast to the system when the cards are loaded. 
	 * 
	 * @param context
	 *            The service context to use for the intent broadcast. 
	 * @param serverName
	 *            The name of the synchronized server to include in the intent. 
	 */
	public void broadcastCardsLoaded(Context context, String serverName) {
		Intent intent = new Intent("cards_loaded");
		intent.putExtra("server_used", serverName);
		context.sendBroadcast(intent);
		Log.d(TAG + "broadcast", "cards loaded broadcasted");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
