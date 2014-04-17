/*
 * File: AudioMenu.java
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

/**
 * 
 * Activity showing the options menu for the audio player activity in the
 * application.
 * 
 */
public class AudioMenu extends Activity {

	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" + ", "
			+ AudioMenu.class.getSimpleName();

	/**
	 * The Constant EXTRA_PLAYER_POS. This is used for intent extra passing.
	 * Designed to map to the last position of the player within the file, so
	 * that it can be resumed properly.
	 */
	public static final String EXTRA_PLAYER_POS = "selected_player_pos";

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
	 * The Constant EXTRA_LAST_PLAYER_POS. This is used for intent extra
	 * passing. This maps to the last known progress of the player through the
	 * audio clip, can be used to restart the player in a specific state.
	 */
	public static final String EXTRA_LAST_PLAYER_POS = "last_player_pos";

	/**
	 * The Constant DEFAULT_POS. Used to obtain a numeric position of the player
	 * within the audio file, in the event that one cannot be obtained from the
	 * intent
	 */
	private static final int DEFAULT_POS = 0;

	/**
	 * The positions of the card in the array and the position of the player
	 * within the audio file when the intent was launched.
	 */
	private int lastPos, cardPos;

	/**
	 * The UUID of the audio card which is serving as the source for this audio
	 * player activity.
	 */
	private String cardId;

	/**
	 * The audio manager for this Activity. Used in this Menu class to provide
	 * audio feedback to user actions/choices.
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
		inflater.inflate(R.menu.audiomenu, menu);

		// get intent attributes to pass back to next activity
		lastPos = getIntent().getIntExtra(EXTRA_LAST_PLAYER_POS, DEFAULT_POS);
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
		// provide some audio feedback to the user
		mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);

		// Handle item selection.
		switch (item.getItemId()) {
		case R.id.resume_audio:
			Log.i("TMM", "resume audio menu item selected");

			// if the user just wants to resume, we go right back to the
			// audioplayer
			Intent intent = new Intent(this, AudioPlayer.class);
			Log.i(TAG, "last posit passed through to new player: " + lastPos);

			// pass relevant information back to the audioplayer activity
			// through the intent
			intent.putExtra(EXTRA_LAST_PLAYER_POS, lastPos);
			intent.putExtra(EXTRA_SELECTED_POS, cardPos);
			intent.putExtra(EXTRA_SELECTED_ID, cardId);

			// start the audioplayer again
			startActivity(intent);
			return true;
		case R.id.reset_audio:

			// kill the current audio player
			closeAudioPlayer(this);
			intent = new Intent(this, AudioPlayer.class);

			// reset = 0 ms resume position
			intent.putExtra(EXTRA_LAST_PLAYER_POS, 0);
			intent.putExtra(EXTRA_SELECTED_POS, cardPos);
			intent.putExtra(EXTRA_SELECTED_ID, cardId);

			// start the new audioplayer that has been reset
			startActivity(intent);
			return true;
		case R.id.quit_audio:
			// kill the current audio player
			closeAudioPlayer(this);

			// go back to the selectcardactivity so the user can choose another
			// card
			intent = new Intent(this, SelectCardActivity.class);
			intent.putExtra(EXTRA_SELECTED_POS, cardPos);
			intent.putExtra(EXTRA_SELECTED_ID, cardId);
			setResult(RESULT_OK, intent);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Close the audio player. Used when a new, reset audio player is going to
	 * be used or the user has finished with the activity.
	 * 
	 * @param context
	 *            The {@link Context} to close the audioplayer in.
	 */
	public static void closeAudioPlayer(Context context) {
		Intent intent = new Intent("AudioPlayer");
		intent.putExtra("action", "close");

		// keep the broadcasted intent local, the whole system does not need to
		// know abou this
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsMenuClosed(android.view.Menu)
	 */
	@Override
	public void onOptionsMenuClosed(Menu menu) {

		// play the glass-specific dismissal sound.
		mAudioManager.playSoundEffect(Sounds.DISMISSED);

		// assume the user wants to resume
		Intent intent = new Intent(this, AudioPlayer.class);
		Log.i(TAG, "last posit passed through to new player: " + lastPos);
		intent.putExtra(EXTRA_LAST_PLAYER_POS, lastPos);
		intent.putExtra(EXTRA_SELECTED_POS, cardPos);
		intent.putExtra(EXTRA_SELECTED_ID, cardId);
		startActivity(intent);

		// kill the Audio menu activity
		finish();
	}
}
