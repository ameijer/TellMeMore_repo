/*
 * File: TextViewer.java
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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.glass.TMM.TextElement.Type;
import com.google.android.glass.media.Sounds;
//import com.google.android.glass.app.Card;
//import com.google.android.glass.timeline.TimelineManager;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

@SuppressLint("ResourceAsColor")
/**
 * The Class TextViewer. This is an activity that is started in response to a
 * user selecting a text card from the list presented by the app. It handles
 * the rendering of text and graphics and deals with user input for the textviewer.
 */
public class TextViewer extends Activity {

	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" + ", "
			+ TextViewer.class.getSimpleName();

	/**
	 * The geturedetector for this activity, used to handle user input during
	 * the time that this activity is in focus.
	 */
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
	 * The Constant EXTRA_REQUESTED_NARRATION. Used to retrieve a narration
	 * request from the menu intent.
	 */
	public static final String EXTRA_REQUESTED_NARRATION = "narration_requested";

	/**
	 * The Constant DEFAULT_NARR. Supplies the default narration setting to
	 * retrieve from the intent.
	 */
	public static final boolean DEFAULT_NARR = false;

	/**
	 * The Constant EXTRA_LAST_TEXT_POS. Used to alert the narrator what section
	 * to narrate.
	 */
	public static final String EXTRA_LAST_TEXT_POS = "last_TEXT_pos";

	/**
	 * The Constant DEFAULT_TEXT_POS. The default value to retreive for the
	 * narrator - -1 is an error so nothing will be played.
	 */
	public static final int DEFAULT_TEXT_POS = -1;

	/**
	 * The Constant KEY_SWIPE_DOWN. Used to handle the backwards compatibility
	 * for the swipe down action used to dismiss views in glass.
	 */
	private static final int KEY_SWIPE_DOWN = 4;

	/**
	 * The Constant DEFAULT_POS. Used if the position of the card in the
	 * selectcardactivity cannot be obtained from the intent.
	 */
	private static final int DEFAULT_POS = 0;

	/**
	 * Reference to the parent application, which contains the database with the
	 * card information.
	 */
	private TellMeMoreApplication app;

	/**
	 * The position of the card within the cardscroll view from which the user
	 * launched this textviewer activity.
	 */
	private int cardPos;

	/**
	 * The UUID of the audio card which is serving as the source for this
	 * textviewer activity.
	 */
	private String cardId;

	/**
	 * The Context in which this activity is running in. Used for accessing
	 * system resources.
	 */
	private Context act_context;

	/**
	 * The audio manager used to provide audio feedback to the user in response
	 * to user actions.
	 */
	private AudioManager mAudioManager;

	/** The narrator object used to narrate the text in this activity. */
	private TextToSpeech mSpeech;

	/**
	 * The last pos selected before the user launched a menu. Designed to
	 * provide a way to do 'selective narration' of only segments of all the
	 * text.
	 */
	private int lastPos;
	
	private boolean narrate = false;

	/**
	 * A reference to the text card containing all the information used in this
	 * activity.
	 */
	private TextCard thisCard;

	/**
	 * The main view for this layout. This will contain all the textviews as
	 * children as they are added from the card.
	 */
	private LinearLayout mView;

	/**
	 * The headscrollview allowing the user to scroll up and down the list of
	 * text.
	 */
	private HeadScrollView scroller;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get relevant information from intent
		cardPos = getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		cardId = getIntent().getStringExtra(EXTRA_SELECTED_ID);
		lastPos = getIntent()
				.getIntExtra(EXTRA_LAST_TEXT_POS, DEFAULT_TEXT_POS);
		Log.i(TAG, "this card is at position: " + cardPos);
		Log.i(TAG, "cardid passed: " + cardId);

		// obtain a reference to the application singleton
		app = ((TellMeMoreApplication) this.getApplication());

		try {
			// find the card with the info for this activity
			thisCard = (TextCard) app.db.findCardById(cardId);
		} catch (ClassCastException e) {
			Log.e(TAG, "Tried to cast card from DB", e);
			finish();
		}

		setContentView(R.layout.textviewer_layout);

		scroller = (HeadScrollView) findViewById(R.id.outertextList);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		this.mGestureDetector = createGestureDetector(this);
		act_context = this;

		mView = (LinearLayout) findViewById(R.id.innertextList);

		Log.i(TAG, "View being used: " + mView);
		mView.setBackgroundColor(getResources().getColor(R.color.black));

		// initialize narrator
		mSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {

				if(narrate){
				// Alert the user to the presence of the narrator
				mSpeech.speak("Narrator Ready", TextToSpeech.QUEUE_FLUSH, null);
				}
			}
		});

		Log.i(TAG, "onCreate finished");
		scroller.enableScrolling();

		// add the list of textelements to the scrollview
		for (int i = 0; i < thisCard.getContents().size(); i++) {
			Log.v(TAG, "text contents of element " + i + ": "
					+ thisCard.getContents().get(i));
			TextElement p = thisCard.getContents().get(i);

			// add image
			if (p.getType() == Type.IMAGE) {

				// note: this clayout can be done via XML file for better
				// looking/more elaborate layouts
				ImageView pic = new ImageView(this);
				Bitmap bmp = BitmapFactory.decodeFile(p.getImg());
				Log.d(TAG, "path to be decoded: " + p.getImg());
				Log.d(TAG, "BMP Generated: " + bmp);
				pic.setImageBitmap(bmp);
				mView.addView(pic);
				TextView cap = new TextView(this);
				cap.setText(p.getText());
				cap.setGravity(Gravity.CENTER);
				cap.setPadding(0, -10, 0, 30);
				mView.addView(cap);

				// add text
			} else if (p.getType() == Type.TEXT_) {
				TextView cap = new TextView(this);
				cap.setText(p.getText());
				mView.addView(cap);
			}
		}

		// pause before we enable scrolling for 2 seconds
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				scroller.enableScrolling();
			}
		}).start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	public void onResume() {
		scroller.activate();
		super.onResume();

		// retrieve intent data
		cardPos = getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		cardId = getIntent().getStringExtra(EXTRA_SELECTED_ID);
		lastPos = getIntent()
				.getIntExtra(EXTRA_LAST_TEXT_POS, DEFAULT_TEXT_POS);
	 narrate = getIntent().getBooleanExtra(
				EXTRA_REQUESTED_NARRATION, DEFAULT_NARR);

		// if the user wanted narration, then play the clip
		// if not, this will have to be encapsulated in a runnable so the user
		// can still browse the text while
		// it is narrating

		if (narrate && lastPos > -1) {
			// String toSpeak = customAdapter.getItem(lastPos).getText();
			// mSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	/**
	 * Gets the context that this activity is operating in. Used here to provide
	 * inner classes easy access
	 * 
	 * @return The Context of this activty.
	 */
	public Context getContext() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	public void onPause() {
		scroller.deactivate();
		super.onPause();
		mSpeech.stop();

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
				mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
				if (gesture == Gesture.TAP) {

					Intent menuintent = new Intent(getContext(), TextMenu.class);
					menuintent.putExtra(EXTRA_SELECTED_ID, cardId);
					menuintent.putExtra(EXTRA_SELECTED_POS, cardPos);

					Log.i(TAG, "cardPos passed to menu: " + cardPos);

					startActivity(menuintent);
				}
				if (gesture == Gesture.SWIPE_RIGHT) {
					// do something on right (forward) swipe
					Log.i(TAG, "swipe_right method called");

				} else if (gesture == Gesture.SWIPE_LEFT) {
					// do something on left (backwards) swipe
					Log.i(TAG, "swipe_left method called");
				} else if (gesture == Gesture.SWIPE_DOWN) {
					Log.i(TAG, "swipe_down method called");
					//leave this to the onKeyUp method below
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
			peaceOut(TextViewer.this);
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onGenericMotionEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		this.dispatchTouchEvent(event);
		if (mGestureDetector != null) {
			/*
			 * Send generic motion events to the gesture detector
			 */
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
		mSpeech.shutdown();
		finish();

	}

	/**
	 * Gracefully exits the activity, freeing up the resources as it goes out.
	 * 
	 * @param context
	 *            The activity context in which this is called.
	 */
	public void peaceOut(Context context) {
		Intent backToCardsIntent = new Intent(context, SelectCardActivity.class);
		backToCardsIntent.putExtra(EXTRA_SELECTED_POS, cardPos);
		setResult(RESULT_OK, backToCardsIntent);
		startActivity(backToCardsIntent);
		finish();
	}
}
