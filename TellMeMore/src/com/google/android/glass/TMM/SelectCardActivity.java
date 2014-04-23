/*
 * File: SelectCardActivity.java
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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollView;
import com.google.glass.widget.SliderView;

 
/**
 * The Class SelectCardActivity.
 */
public class SelectCardActivity extends Activity implements GestureDetector.BaseListener{
	
	/** The Constant NUM_CARDS. */
	public static final String NUM_CARDS = "num_cards";
	
	/** The Constant EXTRA_INITIAL_VALUE. */
	public static final String EXTRA_INITIAL_VALUE = "initial_value";
	
	/** The Constant EXTRA_SELECTED_ID. */
	public static final String EXTRA_SELECTED_ID = "selected_id";
	
	/** The Constant EXTRA_SELECTED_POS. */
	public static final String EXTRA_SELECTED_POS = "selected_pos";
	
	/** The Constant CARDS_READY_KEY. */
	public static final String CARDS_READY_KEY = "cards_ready";
	
	/** The Constant DEFAULT_POS. */
	private static final int DEFAULT_POS = 0;
	
	/** The Constant KEY_SWIPE_DOWN. */
	private static final int KEY_SWIPE_DOWN = 4;
	
	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" +", " + SelectCardActivity.class.getSimpleName();
	
	/** The last card. */
	private int lastCard;
	
	/** The has cards. */
	private boolean hasCards;

	/** The card arr. */
	private static TMMCard[] cardArr;
	
	/** The m audio manager. */
	private AudioManager mAudioManager;

	/** The app. */
	private TellMeMoreApplication app;
	
	/** The m detector. */
	private GestureDetector mDetector;
	
	/** The m view. */
	private CardScrollView mView;
	
	/** The m adapter. */
	private SelectCardScrollAdapter mAdapter;
	
	/** The m indeterm. */
	private SliderView mIndeterm;

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "oncreate called");
		super.onCreate(savedInstanceState);
		app = ((TellMeMoreApplication)this.getApplication());
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		lastCard = getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		hasCards = getIntent().getBooleanExtra(CARDS_READY_KEY, true);
		Log.d(TAG, "Cards are ready: " + hasCards);
		// Register mMessageReceiver to receive messages.
		registerReceiver(mMessageReceiver, new IntentFilter("cards_loaded"));
		if(hasCards){
			enableCardScroll();
		} else { 
			//we are waiting for cards to be obtained
			// display a progress bar for the user 
			setContentView(R.layout.waiting_for_cards_layout);
			mIndeterm = (SliderView) findViewById(R.id.indeterm_slider);
			mIndeterm.startIndeterminate();
		}
	}

	/**
	 * Register listener.
	 */
	private void registerListener(){
		mDetector = new GestureDetector(this).setBaseListener(this);
	}

	// handler for received Intents for the "my-event" event 
	/** The m message receiver. */
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		
		/* (non-Javadoc)
		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		 */
		@Override 
		public void onReceive(Context context, Intent intent) {
			// Extract data included in the Intent
			String serverName = intent.getStringExtra("server_used");
			Log.d(TAG, "Got message: " + serverName);
			Log.i(TAG, "json representation of DB: " + app.db.getEntireDbAsJSON());
			//retreive cards from the target server, since they have ostensibly been loaded
			ArrayList<TMMCard> cardz = app.db.findCardsbyServer();

			//TODO
			//ugly, needs further research into options in this area
			cardArr = new TMMCard[cardz.size()];
			for(int i = 0; i < cardz.size(); i++){
				cardArr[i] = cardz.get(i);
			}
			hasCards = true;
			enableCardScroll();
	
			
			//registerListener();
		}
	};

	/**
	 * Enable card scroll.
	 */
	private void enableCardScroll(){
		registerListener();
		if(mView == null){
		mAdapter = new SelectCardScrollAdapter(
				this, cardArr.length, cardArr );


		
		mView = new CardScrollView(this) {
			@Override
			public final boolean dispatchGenericFocusedEvent(MotionEvent event) {
				if (mDetector.onMotionEvent(event)) {
					return true;
				}
				return super.dispatchGenericFocusedEvent(event);
			}
		};

		mView.deactivate();
		mView.setHorizontalScrollBarEnabled(true);

		mView.setAdapter(mAdapter);

		Log.i(TAG, "trying to start with card at postion: " + lastCard);
		setContentView(mView);

		mAdapter = new SelectCardScrollAdapter(
				this, cardArr.length, cardArr );


		mView = new CardScrollView(this) {
			@Override
			public final boolean dispatchGenericFocusedEvent(MotionEvent event) {
				if (mDetector.onMotionEvent(event)) {
					return true;
				}
				return super.dispatchGenericFocusedEvent(event);
			}
		};
		mView.setHorizontalScrollBarEnabled(true);

		mView.setAdapter(mAdapter);

		mView.setBackgroundColor(getResources().getColor(R.color.black));
		Log.i(TAG, "trying to start with card at postion: " + lastCard);
		
		setContentView(mView);
		mView.activate();
		}
		mView.setSelection(lastCard);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(mMessageReceiver, new IntentFilter("cards_loaded"));
		if(hasCards){
			mView.activate();
			//mView.setSelection(getIntent().getIntExtra(EXTRA_INITIAL_VALUE, 0));
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(mMessageReceiver);
		
		if(hasCards){
			
			mView.deactivate();
			
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onGenericMotionEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		return mDetector.onMotionEvent(event);
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
			finish();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.google.android.glass.touchpad.GestureDetector.BaseListener#onGesture(com.google.android.glass.touchpad.Gesture)
	 */
	@Override
	public boolean onGesture(Gesture gesture) {
		if (gesture == Gesture.TAP) {
			Intent resultIntent;
			if(cardArr[mView.getSelectedItemPosition()] instanceof VideoCard){
				resultIntent= new Intent(this, com.google.android.glass.TMM.VideoPlayer.class);
				// resultIntent.putExtra("placeholder", "ytv://PNGMWZ1XJvI");



			} else if(cardArr[mView.getSelectedItemPosition()] instanceof AudioCard) {

				resultIntent= new Intent(this, AudioPlayer.class);

			} else {//textcard

				resultIntent= new Intent(this, TextViewer.class);
			}

			String id =  cardArr[mView.getSelectedItemPosition()].getuuId();
			resultIntent.putExtra(EXTRA_SELECTED_ID,id);
			resultIntent.putExtra(EXTRA_SELECTED_POS, mView.getSelectedItemPosition());
			setResult(RESULT_OK, resultIntent);
			mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
			finish();
			startActivity(resultIntent);
			Log.d(TAG, "after gesture handled, cardId sent to viewer/player activity is: " + id);
			Log.i(TAG, "finishing gesture handling");
			
			return true;
		}
		return false;
	}
	
	

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		try{
		unregisterReceiver(mMessageReceiver);
		} catch(IllegalArgumentException e){
			Log.i(TAG, "tried to unregister already unregistered receiver in onDestroy()");
		}
		hasCards = false;
		

	}
}
