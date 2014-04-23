/*
 * File: AudioPlayer.java
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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.glass.widget.SliderView;

/**
 * The Class AudioPlayer. This class is the Android Activity responsible for
 * handling the content in AudioCard objects, most often by playing the audio
 * file contain information the user wants to hear.
 */
public class AudioPlayer extends Activity {

	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" + ", "
			+ AudioPlayer.class.getSimpleName();

	/**
	 * The {@link GestureDetector} that handles user input while this activity
	 * is running.
	 */
	private GestureDetector mGestureDetector;
	
	/** Controls the dimness of the backgrounds on the cards. */
	private final float ALPHA_VALUE = 0.30f;

	/**
	 * The Constant EXTRA_SELECTED_POS. This is used for intent extra passing.
	 * Designed to map to the position of the selected card from the
	 * {@link SelectCardActivity} so that when the user returns to it, the card
	 * they just tapped on is in focus.
	 */
	public static final String EXTRA_SELECTED_POS = "selected_pos";

	/**
	 * The Constant EXTRA_SELECTED_ID. This is used for intent extra passing.
	 * This maps to the String UUID of the card so that each activity my access
	 * it from the DB directly
	 */
	public static final String EXTRA_SELECTED_ID = "selected_id";

	/**
	 * The Constant EXTRA_PLAYER_POS. This is used for intent extra passing.
	 * Designed to map to the last position of the player within the file, so
	 * that it can be resumed properly.
	 */
	public static final String EXTRA_PLAYER_POS = "selected_player_pos";

	/**
	 * The Constant EXTRA_LAST_PLAYER_POS. This is used for intent extra
	 * passing. This maps to the last known progress of the player through the
	 * audio clip, can be used to restart the player in a specific state.
	 */
	public static final String EXTRA_LAST_PLAYER_POS = "last_player_pos";

	/**
	 * The Constant TIME_TO_SEEK. When the user scrolls either forwards or
	 * backwards, each scroll advances the player position by this many ms.
	 */
	public static final int TIME_TO_SEEK = 100;

	/**
	 * The Constant KEY_SWIPE_DOWN. Used to handle the backwards compatibility
	 * for the swipe down action used to dismiss views in glass.
	 */
	private static final int KEY_SWIPE_DOWN = 4;

	/**
	 * The Constant DEFAULT_POS. Used to obtain a numeric position of the player
	 * within the audio file, in the event that one cannot be obtained from the
	 * intent
	 */
	private static final int DEFAULT_POS = 0;

	/**
	 * The position of the card within the cardscroll view from which the user
	 * launched this audioplayer activity.
	 */
	private int cardPos;

	/**
	 * The UUID of the audio card which is serving as the source for this audio
	 * player activity.
	 */
	private String cardId;

	/**
	 * The context in which this audioplayer is operating. Used for obtaining
	 * access to system resources.
	 */
	private Context act_context;

	/** Images displayed to the user */
	private ImageView bkgrnd, stat_icon;

	/** A textview used to give the user clues as to what actions they can take. */
	private TextView help_txt;

	/** The progress bar used to indicate the playing track's status. */
	private SliderView prog;

	/** The layout for this activity. */
	private FrameLayout layout;

	/**
	 * The AudioManager used to play user interaction sounds in response to
	 * events.
	 */
	private AudioManager mAudioManager;

	/**
	 * A reference to the card object supplying the information used by this
	 * player.
	 */
	private AudioCard thisCard;

	/** A self-reference. */
	public static Activity player;

	/**
	 * Reference to the parent application, which contains the database with the
	 * card information.
	 */
	private TellMeMoreApplication app;

	/**
	 * The media player object. This is used to open and play the audio file
	 * specified by the card used to launch this activity.
	 */
	MediaPlayer mediaPlayer;

	/**
	 * The progress updater thread. This keeps the progress bar at the top of
	 * the screen shwoing .
	 */
	Thread progUpdater;

	/**
	 * The position of the player within the audio file when the intent was
	 * launched.
	 */
	int lastPos;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get information passed through intents
		cardPos = getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		cardId = getIntent().getStringExtra(EXTRA_SELECTED_ID);
		lastPos = getIntent().getIntExtra(EXTRA_LAST_PLAYER_POS, DEFAULT_POS);

		Log.i(TAG, "this card is at position: " + cardPos);
		setContentView(R.layout.audio_player_layout);

		// find the views
		bkgrnd = (ImageView) findViewById(R.id.background_audio_activity);
		stat_icon = (ImageView) findViewById(R.id.status_icon);
		help_txt = (TextView) findViewById(R.id.audio_activity_helper);
		prog = (SliderView) findViewById(R.id.prog);
		layout = (FrameLayout) findViewById(R.id.audioPlayerFrame);
		Log.i(TAG, "cardid passed: " + cardId);
		player = this;

		// obtain a reference to the application singleton
		app = ((TellMeMoreApplication) this.getApplication());

		try {
			// get the relevant card from the DB
			thisCard = (AudioCard) app.db.findCardById(cardId);
		} catch (ClassCastException e) {
			Log.e(TAG, "Tried to cast card from DB", e);
			finish();
		}

		// set the background color
		if (!thisCard.hasBackground()) {
			// set a black background
			layout.setBackgroundColor(getResources().getColor(R.color.black));
		} else {
			bkgrnd.setImageBitmap(BitmapFactory.decodeFile(thisCard
					.getBackgroundPath()));
			bkgrnd.setAlpha(ALPHA_VALUE);
		}

		// set the default values
		stat_icon.setImageResource(R.drawable.ic_pause);
		help_txt.setText(R.string.audio_help_txt);

		// set up audio and start the clip automatically
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
	
		mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(thisCard.getAudioClipPath());
		} catch (IllegalArgumentException e) {
		
			e.printStackTrace();
		} catch (SecurityException e) {
			
			e.printStackTrace();
		} catch (IllegalStateException e) {
		
			e.printStackTrace();
		} catch (IOException e) {
		
			e.printStackTrace();
		}
		try {
			mediaPlayer.prepare();
		} catch (IllegalStateException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		Log.i(TAG, "lastPos as read by audioplayer: " + lastPos);
		if (lastPos > 0) {
			mediaPlayer.seekTo(lastPos);

		}
		mediaPlayer.start();

		LocalBroadcastManager.getInstance(this).registerReceiver(
				broadcastReceiver, new IntentFilter("AudioPlayer"));

		// start the progress bar updater thread
		progUpdater = new updaterControl();
		progUpdater.start();

		// register detectors and listeners
		this.mGestureDetector = createGestureDetector(this);
		act_context = this;

		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mp) {
				Log.d(TAG, "Media player completed - quit ordered.");
				peaceOut(act_context);
				
			}
		});

		Log.i(TAG, "onCreate finished");

	}

	/**
	 * The broadcast receiver that kills the audio player gracefully. Used in
	 * the audio menu when the user wants to return to the select cards view
	 */
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getStringExtra("action");
			if (action.equals("close")) {

				Log.i(TAG, "CLOSE ORDER RECEIVED");
				mediaPlayer.release();
				finish();
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();

	}

	/**
	 * Gets the context. Used as a helper method when registering listeners,
	 * etc.
	 * 
	 * @return The current activity Context.
	 */
	public Context getContext() {
		return this;
	}

	/**
	 * The Class UpdateStatusRunnable. This class keeps the progress bar at the
	 * top of the activity updated to reflect the current status of the player
	 * on the playing file.
	 */
	private class UpdateStatusRunnable implements Runnable {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				// calculate how much of the total length that we have played
				float portionDone = (float) ((double) mediaPlayer
						.getCurrentPosition() / (double) mediaPlayer
						.getDuration());

				// update the progress bar.
				prog.setManualProgress(portionDone, true);
			} catch (IllegalStateException e) {
				return;
			}
		}

	}

	/**
	 * The control thread for the progress bar. Currently updates the progress
	 * bar once every 300 ms.
	 */
	private class updaterControl extends Thread {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				while (mediaPlayer.isPlaying()) {

					// wait for 0.3 s between every update
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						return;
					}

					// we need to make the actual updates on the UI thread
					AudioPlayer.this.runOnUiThread(new UpdateStatusRunnable());
				}
			} catch (IllegalStateException e) {
				return;
			}

		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();

		// configure the view to look good while the transparent menu is
		// overlaid
		stat_icon.setImageResource(R.color.black);
		help_txt.setText(R.string.null_string);
		bkgrnd.setImageResource(R.color.black);

		try {

			// free up some resources, we don't need them
			progUpdater.interrupt();
			mediaPlayer.release();
			if (prog != null) {
				prog.dismissManualProgress();

			}
		} catch (IllegalStateException e) {
			Log.w(TAG, "illegal state exception thrown when calling onpause");
		}

	}

	/**
	 * Creates the gesture detector. This listens for user input such as swipes
	 * and taps.
	 * 
	 * @param context
	 *            The context in which this activity is running. Used to access
	 *            system resources.
	 * @return The GestureDetector that will be used in this activity.
	 */
	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		// Create a base listener for generic gestures
		gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {

				// give audio feedback to user
				mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);

				if (gesture == Gesture.TAP) {

					// a tap will open up the menu
					Intent menuintent = new Intent(getContext(),
							AudioMenu.class);
					menuintent.putExtra(EXTRA_LAST_PLAYER_POS,
							mediaPlayer.getCurrentPosition());
					menuintent.putExtra(EXTRA_SELECTED_ID, cardId);
					menuintent.putExtra(EXTRA_SELECTED_POS, cardPos);
					Log.i(TAG, "cardPos passed to menu: " + cardPos);
					mediaPlayer.release();
					startActivity(menuintent);
				}
				if (gesture == Gesture.SWIPE_RIGHT) {
					Log.i(TAG, "swipe_right method called");
				} else if (gesture == Gesture.SWIPE_LEFT) {

					Log.i(TAG, "swipe_left method called");
				} else if (gesture == Gesture.SWIPE_DOWN) {
					Log.i(TAG, "swipe_down method called");
					//leave the below onKeyUp method to handle these events
				}
				return false;
			}
		});
		gestureDetector.setFingerListener(new GestureDetector.FingerListener() {
			@Override
			public void onFingerCountChanged(int previousCount, int currentCount) {
				// do something on finger count changes
				Log.i(TAG, "Finger count changed");
			}
		});
		gestureDetector.setScrollListener(new GestureDetector.ScrollListener() {
			@Override
			public boolean onScroll(float displacement, float delta,
					float velocity) {

				// a scroll seeks the track
				mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
				try {
					// seeking
					Log.i(TAG, "scroll detected, velocity: " + velocity);
					if (mediaPlayer.getCurrentPosition() > TIME_TO_SEEK) {
						mediaPlayer.seekTo((int) (mediaPlayer
								.getCurrentPosition() + TIME_TO_SEEK * velocity));
					}
				} catch (IllegalStateException e) {

				}

				return true;
			}
		});
		return gestureDetector;

	}

	// hacky... hopefully google will integrate the gesture class in better
	// atm011
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KEY_SWIPE_DOWN) {
			// there was a swipe down event
			Log.i(TAG, "hacky swipe_down method called");
			mAudioManager.playSoundEffect(Sounds.DISMISSED);
			peaceOut(AudioPlayer.this);
			return true;
		}
		return false;
	}

	/*
	 * Send generic motion events to the gesture detector
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onGenericMotionEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		this.dispatchTouchEvent(event);
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		progUpdater.interrupt();
		if (mediaPlayer != null)
			mediaPlayer.release();

		if (prog != null) {
			prog.dismissManualProgress();

		}
		finish();

	}

	/**
	 * Gracefully exits the activity, freeing up the resources as it goes out.
	 * 
	 * @param context
	 *            The activity context in which this is called.
	 */
	public void peaceOut(Context context) {
		progUpdater.interrupt();
		if (mediaPlayer != null)
			mediaPlayer.release();

		if (prog != null) {
			prog.dismissManualProgress();

		}

		// assume we are going back to the card selection activity
		Intent backToCardsIntent = new Intent(context, SelectCardActivity.class);
		backToCardsIntent.putExtra(EXTRA_SELECTED_POS, cardPos);
		setResult(RESULT_OK, backToCardsIntent);
		startActivity(backToCardsIntent);
		finish();
	}
}
