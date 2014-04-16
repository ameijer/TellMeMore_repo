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
//import com.google.android.glass.app.Card;
//import com.google.android.glass.timeline.TimelineManager;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.glass.widget.SliderView;


// TODO: Auto-generated Javadoc
/**
 * The Class AudioPlayer.
 */
public class AudioPlayer extends Activity{
	
	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" +", " + AudioPlayer.class.getSimpleName();
	
	/** The m gesture detector. */
	private GestureDetector mGestureDetector;
	
	/** The Constant EXTRA_SELECTED_POS. */
	public static final String EXTRA_SELECTED_POS = "selected_pos";
	
	/** The Constant EXTRA_SELECTED_ID. */
	public static final String EXTRA_SELECTED_ID = "selected_id";
	
	/** The Constant EXTRA_PLAYER_POS. */
	public static final String EXTRA_PLAYER_POS = "selected_player_pos";
	
	/** The Constant EXTRA_LAST_PLAYER_POS. */
	public static final String EXTRA_LAST_PLAYER_POS = "last_player_pos";
	
	/** The Constant TIME_TO_SEEK. */
	public static final int TIME_TO_SEEK = 100;
	
	/** The Constant KEY_SWIPE_DOWN. */
	private static final int KEY_SWIPE_DOWN = 4;
	
	/** The Constant DEFAULT_POS. */
	private static final int DEFAULT_POS = 0;
	
	/** The Constant DEFAULT_ID. */
	private static final String DEFAULT_ID = "null";
	//private myListener mlistener;
	/** The card pos. */
	private int cardPos;
	
	/** The card id. */
	private String  cardId;
	
	/** The act_context. */
	private Context act_context; 
	
	/** The stat_icon. */
	private ImageView bkgrnd, stat_icon; 
	
	/** The help_txt. */
	private TextView help_txt;
	
	/** The prog. */
	private SliderView prog;
	
	/** The layout. */
	private FrameLayout layout;
	
	/** The m audio manager. */
	private AudioManager mAudioManager;
	
	/** The paused. */
	private boolean paused;
	
	/** The this card. */
	private AudioCard thisCard;
	
	/** The player. */
	public static Activity player; 
	
	/** The app. */
	private TellMeMoreApplication app;
	
	/** The media player. */
	MediaPlayer mediaPlayer;
	
	/** The prog updater. */
	Thread progUpdater;
	
	/** The last pos. */
	int lastPos;


	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cardPos =  getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		cardId =  getIntent().getStringExtra(EXTRA_SELECTED_ID);
		lastPos =  getIntent().getIntExtra(EXTRA_LAST_PLAYER_POS, DEFAULT_POS);
		Log.i(TAG, "this card is at position: " + cardPos);
		setContentView(R.layout.audio_player_layout);
		bkgrnd = (ImageView)findViewById(R.id.background_audio_activity);
		stat_icon = (ImageView)findViewById(R.id.status_icon);
		help_txt = (TextView)findViewById(R.id.audio_activity_helper);
		prog = (SliderView) findViewById(R.id.prog);
		layout = (FrameLayout) findViewById(R.id.audioPlayerFrame);
		Log.i(TAG, "cardid passed: " + cardId);
		player = this;

 
		//obtain a reference to the application singleton
		app = ((TellMeMoreApplication)this.getApplication());

		try{
			thisCard = (AudioCard) app.db.findCardById(cardId);
		} catch (ClassCastException e){
			Log.e(TAG, "Tried to cast card from DB", e);
			finish();
		}

		if(thisCard.getBackgroundPath() == null || thisCard.getBackgroundPath().equalsIgnoreCase("")){
			//set a black background
			layout.setBackgroundColor(getResources().getColor(R.color.black));
		} else {
			bkgrnd.setImageBitmap(BitmapFactory.decodeFile(thisCard.getBackgroundPath()));
		}
		stat_icon.setImageResource(R.drawable.ic_pause);
		help_txt.setText("tap to pause");

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		mediaPlayer = MediaPlayer.create(this,R.raw.powerpointdemo);
		Log.i(TAG, "lastPos as read by audioplayer: " + lastPos);
		if(lastPos > 0){
			mediaPlayer.seekTo(lastPos);

		}
		mediaPlayer.start();

		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("AudioPlayer"));
		//prog.startProgress(/*mediaPlayer.getDuration()*/ 30000);
		progUpdater = new updaterControl();
		progUpdater.start();


		this.mGestureDetector = createGestureDetector(this);
		act_context = this;

		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mp) {

				peaceOut(act_context);

			}
		});




		Log.i(TAG, "onCreate finished");

	}


	/** The broadcast receiver. */
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action= intent.getStringExtra("action");
			if(action.equals("close")) {
				Log.i(TAG, "CLOSE ORDER RECEIVED");
				//peaceOut(getContext());
				mediaPlayer.release();
				finish();
			}
		}
	};


	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume(){
		super.onResume();
		cardPos =  getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		cardId =  getIntent().getStringExtra(EXTRA_SELECTED_ID);
		lastPos =  getIntent().getIntExtra(EXTRA_LAST_PLAYER_POS, DEFAULT_POS);

		try {
			mediaPlayer.seekTo(lastPos); 
			mediaPlayer.start();

		} catch (IllegalStateException e){
			mediaPlayer = MediaPlayer.create(this,R.raw.powerpointdemo);
			mediaPlayer.seekTo(0);
			mediaPlayer.start();
			Log.w(TAG, "onresume illegal state exception caught, initing new mediaplayer");
		}

		help_txt.setText("tap to pause");
		stat_icon.setImageResource(R.drawable.ic_pause);

		if(progUpdater != null ){

			if(!progUpdater.isAlive()){
				progUpdater.start();
			}
		} else {
			progUpdater = new updaterControl();
			progUpdater.start();
		}

		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			public void onCompletion(MediaPlayer mp) {

				peaceOut(act_context);

			}
		});

	}

	/**
	 * Gets the context.
	 *
	 * @return the context
	 */
	public Context getContext(){
		return this;
	}

	/**
	 * The Class UpdateStatusRunnable.
	 */
	private class UpdateStatusRunnable implements Runnable {

		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			/*
			 * Code you want to run on the thread goes here
			 */ 
			try {
				float portionDone = (float) ((double) mediaPlayer.getCurrentPosition() / (double) mediaPlayer.getDuration());

				prog.setManualProgress(portionDone, true);
			}catch (IllegalStateException e){
				return;
			}
		}

	}

	/**
	 * The Class updaterControl.
	 */
	private class updaterControl extends Thread {


		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				while(mediaPlayer.isPlaying()){
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						return;
					}

					//Log.i(TAG, "updater control thread woken up, updating progress bar");
					AudioPlayer.this.runOnUiThread(new UpdateStatusRunnable());


				}
			} catch (IllegalStateException e) {
				return;
			}


		}

	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
		stat_icon.setImageResource(R.color.black);
		help_txt.setText(R.string.null_string);
		progUpdater.interrupt();

		try {
			progUpdater.interrupt();
			mediaPlayer.release();
			if(prog != null){
				prog.dismissManualProgress();

			}
		}catch (IllegalStateException e){
			Log.w(TAG, "illegal state exception thrown when calling onpuase");
		}

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
				mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
				if (gesture == Gesture.TAP){

					Intent menuintent = new Intent(getContext(), AudioMenu.class);
					menuintent.putExtra(EXTRA_LAST_PLAYER_POS, mediaPlayer.getCurrentPosition());
					menuintent.putExtra(EXTRA_SELECTED_ID, cardId);
					menuintent.putExtra(EXTRA_SELECTED_POS, cardPos);
					Log.i(TAG, "cardPos passed to menu: " + cardPos);
					mediaPlayer.release();
					startActivity(menuintent);
				} if (gesture == Gesture.SWIPE_RIGHT) {
					// do something on right (forward) swipe
					Log.i(TAG, "swipe_right method called");
					//mAudioManager.playSoundEffect(Sounds.DISMISSED);
					//peaceOut(act_context);
				} else if (gesture == Gesture.SWIPE_LEFT) {
					// do something on left (backwards) swipe
					Log.i(TAG, "swipe_left method called");
					//mAudioManager.playSoundEffect(Sounds.);

					//if(mediaPlayer.isPlaying()){
					//	mediaPlayer.seek(mediaPlayer.getCurrentPosition() + TIME_TO_SEEK);
					//}


					//peaceOut(act_context);
				} else if(gesture == Gesture.SWIPE_DOWN){
					Log.i(TAG, "swipe_down method called");
					mAudioManager.playSoundEffect(Sounds.DISMISSED);
					peaceOut(act_context);
					finish();
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

				mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
				try{
					//seeking 
					//	if(velocity > 0){
					Log.i(TAG, "scroll detected, velocity: " + velocity);
					if(mediaPlayer.getCurrentPosition() > TIME_TO_SEEK){
						mediaPlayer.seekTo((int) (mediaPlayer.getCurrentPosition() +  TIME_TO_SEEK * velocity ));
					}
				} catch (IllegalStateException e){

				}

				return true;
			}
		});
		return gestureDetector;

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
			peaceOut(AudioPlayer.this);
			return true;
		}
		return false;
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

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		progUpdater.interrupt();
		if(mediaPlayer != null)
			mediaPlayer.release();

		if(prog != null){
			prog.dismissManualProgress();

		}
		finish();

	}

	/**
	 * Peace out.
	 *
	 * @param context the context
	 */
	public void peaceOut(Context context){
		progUpdater.interrupt();
		if(mediaPlayer != null)
			mediaPlayer.release();

		if(prog != null){
			prog.dismissManualProgress();

		}
		Intent backToCardsIntent= new Intent(context, SelectCardActivity.class);
		backToCardsIntent.putExtra(EXTRA_SELECTED_POS, cardPos);
		setResult(RESULT_OK, backToCardsIntent);
		startActivity(backToCardsIntent);
		finish();
	}
}
