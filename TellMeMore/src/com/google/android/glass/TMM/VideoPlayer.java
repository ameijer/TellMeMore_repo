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
//import com.google.android.glass.app.Card;
//import com.google.android.glass.timeline.TimelineManager;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

// TODO: Auto-generated Javadoc
/**
 * The Class VideoPlayer.
 */
public class VideoPlayer extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener{
	
	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" +", " + VideoPlayer.class.getSimpleName();
	
	/** The m gesture detector. */
	private GestureDetector mGestureDetector;
	
	/** The Constant EXTRA_SELECTED_POS. */
	public static final String EXTRA_SELECTED_POS = "selected_pos";
	
	/** The Constant EXTRA_SELECTED_ID. */
	public static final String EXTRA_SELECTED_ID = "selected_id";
	
	/** The Constant DEFAULT_POS. */
	private static final int DEFAULT_POS = 0;
	
	/** The Constant DEFAULT_ID. */
	private static final int DEFAULT_ID = 0;
	
	/** The Constant KEY_SWIPE_DOWN. */
	private static final int KEY_SWIPE_DOWN = 4;
	
	/** The mlistener. */
	private myListener mlistener;
	
	/** The card pos. */
	private int cardPos;
	
	/** The card id. */
	private String cardId;
	
	/** The act_context. */
	private Context act_context; 
	
	/** The app. */
	private TellMeMoreApplication app;
	
	/** The this card. */
	private VideoCard thisCard;
	
	/** The m audio manager. */
	private AudioManager mAudioManager;
	
	/* (non-Javadoc)
	 * @see com.google.android.youtube.player.YouTubeBaseActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (TellMeMoreApplication)getApplication();
		
		
		
		cardPos =  getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		cardId =  getIntent().getStringExtra(EXTRA_SELECTED_ID);
		Log.i(TAG, "this card is at position: " + cardPos);
		Log.d(TAG, "Cardid received by videoplayer: " + cardId);
		try{
			thisCard = (VideoCard) app.db.findCardById(cardId);
		} catch (ClassCastException e){
			Log.e(TAG, "Tried to cast card from DB", e);
			finish();
		}
		setContentView(R.layout.video_player_layout);
		//Declare YouTubePlayerView
		YouTubePlayerView ytpv = (YouTubePlayerView)findViewById(R.id.youtubeplayer);
		//Initialize YouTubePlayerView using DeveloperKey obtained from YouTubeAPIDemo
		ytpv.initialize("AIzaSyC6MVQReF82uBRdqYJMgisaSMqM1i4dJIM", this);
		mlistener = new myListener();
		mlistener.setContext(this);
		this.mGestureDetector = createGestureDetector(this);
		act_context = this;
		Log.i(TAG, "onCreate finished");

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

	}

	/* (non-Javadoc)
	 * @see com.google.android.youtube.player.YouTubePlayer.OnInitializedListener#onInitializationFailure(com.google.android.youtube.player.YouTubePlayer.Provider, com.google.android.youtube.player.YouTubeInitializationResult)
	 */
	@Override
	public void onInitializationFailure(Provider arg0,
			YouTubeInitializationResult arg1) {
		// TODO Auto-generated method stub

		Toast.makeText(this, "YouTubePlayer.onInitializationFailure(): " + arg1.toString(), Toast.LENGTH_LONG).show(); 
	}

	/* (non-Javadoc)
	 * @see com.google.android.youtube.player.YouTubePlayer.OnInitializedListener#onInitializationSuccess(com.google.android.youtube.player.YouTubePlayer.Provider, com.google.android.youtube.player.YouTubePlayer, boolean)
	 */
	@Override
	public void onInitializationSuccess(Provider arg0, YouTubePlayer arg1,
			boolean arg2) {
		//Set class variable player to current initialized player in order to be used inside the whole class
		///this.player = player;
		//Make a list of cues that is used to be passed to the player because the getTag method returns two extra characters
		// so we use the substring to chop those two off.
		List<String> cue = new ArrayList<String>();

		cue.add(thisCard.getYTtag());

		//Play YouTube videos in the cue.
		arg1.loadVideos(cue);
		arg1.setPlaybackEventListener(mlistener);

	}

	/**
	 * Creates the gesture detector.
	 *
	 * @param context the context
	 * @return the gesture detector
	 */
	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		//Create a base listener for generic gestures
		gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
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
				} else if(gesture == Gesture.SWIPE_DOWN){
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
			public boolean onScroll(float displacement, float delta, float velocity) {
				// do something on scrolling
				Log.i(TAG, "scroll detected");
				return true;
			}
		});
		return gestureDetector;

	}
	/*
	 * Send generic motion events to the gesture detector
	 */
	/* (non-Javadoc)
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

	//hacky... hopefully google will integrate the gesture class in better
	//atm011
	/* (non-Javadoc)
	 * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if (keyCode == KEY_SWIPE_DOWN)
		{
			// there was a swipe down event
			Log.i(TAG, "hacky swipe_down method called");
			mAudioManager.playSoundEffect(Sounds.DISMISSED);
			peaceOut(VideoPlayer.this);
			return true;
		}
		return false;
	}

	/**
	 * The listener interface for receiving my events.
	 * The class that is interested in processing a my
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addmyListener<code> method. When
	 * the my event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see myEvent
	 */
	private class myListener implements YouTubePlayer.PlaybackEventListener {

		/** The context. */
		private Context context;
		
		/* (non-Javadoc)
		 * @see com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener#onBuffering(boolean)
		 */
		@Override
		public void onBuffering(boolean arg0) {
			return;

		}

		/* (non-Javadoc)
		 * @see com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener#onPaused()
		 */
		@Override
		public void onPaused() {
			Log.i(TAG, "onPause called");

		}

		/* (non-Javadoc)
		 * @see com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener#onPlaying()
		 */
		@Override
		public void onPlaying() {
			return;

		}

		/* (non-Javadoc)
		 * @see com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener#onSeekTo(int)
		 */
		@Override
		public void onSeekTo(int arg0) {
			return;

		}

		/* (non-Javadoc)
		 * @see com.google.android.youtube.player.YouTubePlayer.PlaybackEventListener#onStopped()
		 */
		@Override
		public void onStopped() {

			Log.i(TAG, "onStopped listener called, returning to card selection activity, card number: " + cardPos);
			peaceOut(context);

		}

		/**
		 * Sets the context.
		 *
		 * @param context the new context
		 */
		public void setContext(Context context) {
			this.context = context;
		}
	}

	/**
	 * Peace out.
	 *
	 * @param context the context
	 */
	private void peaceOut(Context context){
		Intent backToCardsIntent= new Intent(context, SelectCardActivity.class);
		backToCardsIntent.putExtra(EXTRA_SELECTED_POS, cardPos);
		setResult(RESULT_OK, backToCardsIntent);
		startActivity(backToCardsIntent);
		finish();
	}
}
