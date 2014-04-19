/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Modified by A. Meijer and D. Prudente April 2014
 */

package com.google.android.glass.TMM;

import com.google.android.glass.timeline.LiveCard;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import android.os.AsyncTask;

import android.os.IBinder;

import android.util.Log;

import android.widget.RemoteViews;

/**
 * Service owning the LiveCard living in the timeline. The card contains a
 * tooltip explaining to the user how to activate the raminder of the app.
 */
public class TMMService extends Service {

	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" + ", "
			+ TMMService.class.getSimpleName();

	/** The Constant LIVE_CARD_TAG. Used when creating the tootip card tag. */
	private static final String LIVE_CARD_TAG = "TMM";

	/**
	 * A flag used to indicate whether the service is running or not, and act
	 * accordingly.
	 */
	private boolean running;

	/** The live card published by this service. */
	private LiveCard mLiveCard;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();

		running = true;

	}

	/**
	 * Publish the tooltip card to the timeline.
	 * 
	 * @param context
	 *            The service's context. Used to publish the livecard.
	 */
	private void publishCard(Context context) {
		if (mLiveCard == null) {

			mLiveCard = new LiveCard(context, LIVE_CARD_TAG);

			mLiveCard.setViews(new RemoteViews(context.getPackageName(),
					R.layout.notification_card_text));
			Intent menuintent = new Intent(context, StartMenuActivity.class);
			mLiveCard.setAction(PendingIntent.getActivity(context, 0,
					menuintent, 0));
			mLiveCard.publish(LiveCard.PublishMode.REVEAL);
		} else {
			// Card is already published.
			return;
		}
	}

	/**
	 * Unpublish the tooltip card from the timeline.
	 * 
	 * @param context
	 *            The service's context. Used to unpublish the livecard.
	 */
	private void unpublishCard(Context context) {
		if (mLiveCard != null) {
			mLiveCard.unpublish();
			mLiveCard = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("TMM", "TMM service started");
		running = true;

		// start the card blinking service, which will run asynchronously
		new BlinkLiveCardTask().execute(this);

		return START_STICKY;
	}

	/**
	 * The Class BlinkLiveCardTask. This class is a runnable which blinks the
	 * tooltip card on and off.
	 */
	private class BlinkLiveCardTask extends AsyncTask<Context, Integer, Long> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		protected Long doInBackground(Context... contexts) {
			Log.i("TMM", "doing in background");
			while (running) {

				publishCard(contexts[0]);
				try {
					Thread.sleep(7000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				unpublishCard(contexts[0]);

				try {
					Thread.sleep(7000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

			return (long) 100;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		if (mLiveCard != null && mLiveCard.isPublished()) {
			mLiveCard.unpublish();
			mLiveCard = null;

		}
		running = false;
		super.onDestroy();
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
