/*
 * File: VideoPlayer.java
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

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

/**
 * The Class VideoPlayer. This is an activity that is started in response to a
 * user selecting a video card from the list presented by the app. It handles
 * the streaming of YouTube videos and deals with user input during video
 * playback.
 */
public class VideoPlayer extends YouTubeBaseActivity implements
		YouTubePlayer.OnInitializedListener {

	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" + ", "
			+ VideoPlayer.class.getSimpleName();

	/** The gesturedetector used by this activity to handle user input. */
	private GestureDetector mGestureDetector;

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
	 * The Constant DEFAULT_POS. Used if the position of the card in the
	 * selectcardactivity cannot be obtained from the intent.
	 */
	private static final int DEFAULT_POS = 0;

	/**
	 * The Constant KEY_SWIPE_DOWN. Used to handle the backwards compatibility
	 * for the swipe down action used to dismiss views in glass.
	 */
	private static final int KEY_SWIPE_DOWN = 4;

	/**
	 * The mlistener. Used to interact with the youtube player view that lies at
	 * the heart of this activity.
	 */
	private myListener mlistener;

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
	 * The Context in which this activity is running in. Used for accessing
	 * system resources.
	 */
	private Context act_context;

	/**
	 * Reference to the parent application, which contains the database with the
	 * card information.
	 */
	private TellMeMoreApplication app;

	/**
	 * A reference to the video card containing all the information used in this
	 * activity.
	 */
	private VideoCard thisCard;

	/**
	 * The audio manager used to provide audio feedback to the user in response
	 * to user actions.
	 */
	private AudioManager mAudioManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.youtube.player.YouTubeBaseActivity#onCreate(android
	 * .os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// obtain a reference to the application object, for later DB accesses
		app = (TellMeMoreApplication) getApplication();

		// get intent information
		cardPos = getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		cardId = getIntent().getStringExtra(EXTRA_SELECTED_ID);

		Log.i(TAG, "this card is at position: " + cardPos);
		Log.d(TAG, "Cardid received by videoplayer: " + cardId);
		try {
			// using the UUID passed through the intent, find the card in the DB
			thisCard = (VideoCard) app.db.findCardById(cardId);
		} catch (ClassCastException e) {
			Log.e(TAG, "Tried to cast card from DB", e);
			finish();
		}
		setContentView(R.layout.video_player_layout);

		// Declare YouTubePlayerView
		YouTubePlayerView ytpv = (YouTubePlayerView) findViewById(R.id.youtubeplayer);

		// Initialize YouTubePlayerView using DeveloperKey obtained from
		// YouTubeAPIDemo
		ytpv.initialize("AIzaSyC6MVQReF82uBRdqYJMgisaSMqM1i4dJIM", this);

		// set up listeners/detecetors
		mlistener = new myListener();
		mlistener.setContext(this);
		this.mGestureDetector = createGestureDetector(this);
		act_context = this;
		Log.i(TAG, "onCreate finished");

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.youtube.player.YouTubePlayer.OnInitializedListener
	 * #onInitializationFailure
	 * (com.google.android.youtube.player.YouTubePlayer.Provider,
	 * com.google.android.youtube.player.YouTubeInitializationResult)
	 */
	@Override
	public void onInitializationFailure(Provider arg0,
			YouTubeInitializationResult arg1) {

		Toast.makeText(this,
				"YouTube Player initialization failure: " + arg1.toString(),
				Toast.LENGTH_LONG).show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.google.android.youtube.player.YouTubePlayer.OnInitializedListener
	 * #onInitializationSuccess
	 * (com.google.android.youtube.player.YouTubePlayer.Provider,
	 * com.google.android.youtube.player.YouTubePlayer, boolean)
	 */
	@Override
	public void onInitializationSuccess(Provider arg0, YouTubePlayer arg1,
			boolean arg2) {
		// Set class variable player to current initialized player in order to
		// be used inside the whole class
		// /this.player = player;
		// Make a list of cues that is used to be passed to the player because
		// the getTag method returns two extra characters
		// so we use the substring to chop those two off.
		List<String> cue = new ArrayList<String>();

		cue.add(thisCard.getYTtag());

		// Play YouTube videos in the cue.
		arg1.loadVideos(cue);
		arg1.setPlaybackEventListener(mlistener);

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

				if (gesture == Gesture.SWIPE_RIGHT) {

					// do something on right (forward) swipe
					mAudioManager.playSoundEffect(Sounds.DISMISSED);
					peaceOut(act_context);
				} else if (gesture == Gesture.SWIPE_LEFT) {

					// do something on left (backwards) swipe
					mAudioManager.playSoundEffect(Sounds.DISMISSED);
					peaceOut(act_context);
				} else if (gesture == Gesture.SWIPE_DOWN) {
					mAudioManager.playSoundEffect(Sounds.DISMISSED);
					peaceOut(act_context);
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

				// do something on scrolling
				Log.i(TAG, "scroll detected");
				return true;
			}
		});
		return gestureDetector;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onGenericMotionEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		/*
		 * Send generic motion events to the gesture detector
		 */
		this.dispatchTouchEvent(event);
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}

		return false;
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
			peaceOut(VideoPlayer.this);
			return true;
		}
		return false;
	}

	/**
	 * The listener interface for receiving my events. The class that is
	 * interested in processing a my event implements this interface, and the
	 * object created with that class is registered with a component using the
	 * component's <code>addmyListener<code> method. When
	 * the my event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see myEvent
	 */
	private class myListener implements YouTubePlayer.PlaybackEventListener {

		/** The context. */
		private Context context;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener
		 * #onBuffering(boolean)
		 */
		@Override
		public void onBuffering(boolean arg0) {
			return;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener
		 * #onPaused()
		 */
		@Override
		public void onPaused() {
			Log.i(TAG, "onPause called");

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener
		 * #onPlaying()
		 */
		@Override
		public void onPlaying() {
			return;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener
		 * #onSeekTo(int)
		 */
		@Override
		public void onSeekTo(int arg0) {
			return;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener
		 * #onStopped()
		 */
		@Override
		public void onStopped() {

			Log.i(TAG,
					"onStopped listener called, returning to card selection activity, card number: "
							+ cardPos);
			peaceOut(context);

		}

		/**
		 * Sets the context of this listener class.
		 * 
		 * @param context
		 *            The context in which this class is operating in.
		 */
		public void setContext(Context context) {
			this.context = context;
		}
	}

	/**
	 * Gracefully exits the activity, freeing up the resources as it goes out.
	 * 
	 * @param context
	 *            The activity context in which this is called.
	 */
	private void peaceOut(Context context) {
		Intent backToCardsIntent = new Intent(context, SelectCardActivity.class);
		backToCardsIntent.putExtra(EXTRA_SELECTED_POS, cardPos);
		setResult(RESULT_OK, backToCardsIntent);
		startActivity(backToCardsIntent);
		finish();
	}
}
