/*
 * File: TextMenu.java
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

import com.google.android.glass.media.Sounds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

// TODO: Auto-generated Javadoc
/**
 * Activity showing the options menu for the TextViewer activity. Provides the
 * user with at minimum a way to exit the activity programmatically without using
 * gestures.
 */
public class TextMenu extends Activity {

	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" + ", "
			+ TextMenu.class.getSimpleName();

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
	 * The Constant EXTRA_LAST_TEXT_POS. Used to alert the narrator what section
	 * to narrate.
	 */
	public static final String EXTRA_LAST_TEXT_POS = "last_TEXT_pos";

	/**
	 * The Constant EXTRA_REQUESTED_NARRATION. Used to retrieve a narration
	 * request from the menu intent.
	 */
	public static final String EXTRA_NARRATION_REQUESTED = "narration_requested";

	/**
	 * The Constant DEFAULT_TEXT_POS. The default value to retrieve for the
	 * narrator - -1 is an error so nothing will be played.
	 */
	public static final int DEFAULT_TEXT_POS = -1;


	/**
	 * The Constant DEFAULT_POS. Used if the position of the card in the
	 * {@link SelectCardActivity} cannot be obtained from the intent.
	 */
	private static final int DEFAULT_POS = 0;

	/**
	 * The Constant DEFAULT_NARR. Supplies the default narration setting to
	 * retrieve from the intent.
	 */
	public static final boolean DEFAULT_NARR = false;

	/**
	 * The last pos selected before the user launched a menu. Designed to
	 * provide a way to do 'selective narration' of only segments of all the
	 * text.
	 */
	private int lastPos;
	
	/**
	 * The position of the card within the cardscroll view from which the user
	 * launched this audioplayer activity.
	 */
	private int cardPos;
	
	/**
	 * The UUID of the audio card which is serving as the source for this textviewer activity.
	 */
	private String cardId;

	/** Boolean flag to indicate if the user has selected a narration at the options menu. This is mostly used to govern the onmenuclosed behavior. */
	private boolean isNarrating = false;

	/**
	 * The audio manager used to provide audio feedback to the user in response
	 * to user actions.
	 */
	private AudioManager mAudioManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onAttachedToWindow()
	 */
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		openOptionsMenu();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.textmenu, menu);
		
		//recover intent data 
		lastPos = getIntent()
				.getIntExtra(EXTRA_LAST_TEXT_POS, DEFAULT_TEXT_POS);
		cardPos = getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		cardId = getIntent().getStringExtra(EXTRA_SELECTED_ID);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
		// Handle item selection.
		switch (item.getItemId()) {
		case R.id.narrate:
			Log.i("TMM", "narrate text item selected");
			Intent intent = new Intent(this, TextViewer.class);

			Log.i(TAG, "last posit passed through to new player: " + lastPos);
			intent.putExtra(EXTRA_LAST_TEXT_POS, lastPos);
			intent.putExtra(EXTRA_SELECTED_ID, cardId);
			intent.putExtra(EXTRA_NARRATION_REQUESTED, true);
			intent.putExtra(EXTRA_SELECTED_POS, cardPos);

			isNarrating = true;
			startActivity(intent);
			return true;
		case R.id.quit_text:
			closeTextViewer(this);

			intent = new Intent(this, SelectCardActivity.class);
			intent.putExtra(EXTRA_SELECTED_POS, cardPos);
			intent.putExtra(EXTRA_SELECTED_ID, cardId);
			intent.putExtra(EXTRA_LAST_TEXT_POS, DEFAULT_TEXT_POS);
			setResult(RESULT_OK, intent);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Broadcasts an intent to close the textviewer activity quickly so that the user may return directly to the selectcardactivty directly from the menu activity
	 * 
	 * @param context
	 *            The activty's context, used to send a broadcast. 
	 */
	public static void closeTextViewer(Context context) {
		Intent intent = new Intent("TextViewer");

		intent.putExtra("action", "close");
		
		//use localbroadcastmanager, the whole system doesn't need to know about this 
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsMenuClosed(android.view.Menu)
	 */
	@Override
	public void onOptionsMenuClosed(Menu menu) {
		if (!isNarrating) {
			mAudioManager.playSoundEffect(Sounds.DISMISSED);
		}
		finish();
	}
}
