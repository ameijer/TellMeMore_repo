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
 */

package com.google.android.glass.TMM;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import com.google.android.glass.app.Card;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.google.android.glass.timeline.TimelineManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RemoteViews;

/**
 * Service owning the LiveCard living in the timeline.
 */
public class TMMService extends Service {
	public static final String TAG = "TMM" +", " + TMMService.class.getSimpleName();
	private static final String LIVE_CARD_TAG = "TMM";

	private boolean running;
	private TimelineManager mTimelineManager;
	private LiveCard mLiveCard;




	@Override
	public void onCreate() {
		super.onCreate();




		mTimelineManager = TimelineManager.from(this);
		//mTimerDrawer = new TimerDrawer(this);
		running = true;



	}

	private void publishCard(Context context) {
		if (mLiveCard == null) {
			TimelineManager tm = TimelineManager.from(context);
			mLiveCard = tm.createLiveCard(LIVE_CARD_TAG);

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



	private void unpublishCard(Context context) {
		if (mLiveCard != null) {
			mLiveCard.unpublish();
			mLiveCard = null;
		}
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//        if (mLiveCard == null) {
		//            mLiveCard = mTimelineManager.createLiveCard(LIVE_CARD_TAG);
		//
		//            mLiveCard.setDirectRenderingEnabled(true).getSurfaceHolder().addCallback(mTimerDrawer);
		//
		//            Intent menuIntent = new Intent(this, MenuActivity.class);
		//            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		//            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
		//
		//            mLiveCard.publish(PublishMode.REVEAL);
		//        } else {
		//            // TODO(alainv): Jump to the LiveCard when API is available.
		//        }
		Log.i("TMM", "TMM service started");
		running = true;
		new BlinkLiveCardTask().execute(this);

		return START_STICKY;
	}


	private class BlinkLiveCardTask extends AsyncTask<Context, Integer, Long> {
		protected Long doInBackground(Context...contexts) {
			Log.i("TMM","doing in background");
			while(running){


				publishCard(contexts[0]);
				try {
					Thread.sleep(7000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}



				unpublishCard(contexts[0]);

				try {
					Thread.sleep(7000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			return (long) 100;
		}

		protected void onProgressUpdate() {

		}

		protected void onPostExecute() {

		}
	}


	@Override
	public void onDestroy() {
		if (mLiveCard != null && mLiveCard.isPublished()) {
			mLiveCard.unpublish();
			mLiveCard = null;

		}
		running = false;
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}


}
