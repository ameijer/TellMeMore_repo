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
 * Activity showing the options menu.
 */
public class TextMenu extends Activity {
	
	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" +", " + TextMenu.class.getSimpleName();
	
	/** The Constant EXTRA_PLAYER_POS. */
	public static final String EXTRA_PLAYER_POS = "selected_player_pos";
	
	/** The Constant EXTRA_SELECTED_POS. */
	public static final String EXTRA_SELECTED_POS = "selected_pos";
	
	/** The Constant EXTRA_SELECTED_ID. */
	public static final String EXTRA_SELECTED_ID = "selected_id";
	
	/** The Constant EXTRA_LAST_TEXT_POS. */
	public static final String EXTRA_LAST_TEXT_POS = "last_TEXT_pos";
	
	/** The Constant EXTRA_NARRATION_REQUESTED. */
	public static final String EXTRA_NARRATION_REQUESTED = "narration_requested";
	
	
	/** The Constant DEFAULT_TEXT_POS. */
	public static final int DEFAULT_TEXT_POS = -1;
	
	/** The Constant DEFAULT_ID. */
	private static final int DEFAULT_ID = 0;
	
	/** The Constant DEFAULT_POS. */
	private static final int DEFAULT_POS = 0;
	
	/** The Constant DEFAULT_NARR. */
	public static final boolean DEFAULT_NARR = false;
	
	/** The card id. */
	private int lastPos, cardPos, cardId; 
	
	/** The m audio manager. */
	private AudioManager mAudioManager;
    
    /* (non-Javadoc)
     * @see android.app.Activity#onAttachedToWindow()
     */
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.textmenu, menu);
        lastPos =  getIntent().getIntExtra(EXTRA_LAST_TEXT_POS, DEFAULT_TEXT_POS);
        cardPos =  getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		cardId =  getIntent().getIntExtra(EXTRA_SELECTED_ID, DEFAULT_ID);
	
        return true;
    }
   

    /* (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	 mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.narrate:
            	Log.i("TMM", "narrate text item selected");
                //stopService(new Intent(this, ScanActivity.class));
                Intent intent = new Intent(this, TextViewer.class);
                
                Log.i(TAG, "last posit passed through to new player: " + lastPos);
                intent.putExtra(EXTRA_LAST_TEXT_POS, lastPos);
                intent.putExtra(EXTRA_NARRATION_REQUESTED, true);
                intent.putExtra(EXTRA_SELECTED_POS, cardPos);
                
                
                startActivity(intent);
                return true;
            case R.id.quit_text:
            	closeTextViewer(this);
            	intent = new Intent(this, SelectCardActivity.class);
            	intent.putExtra(EXTRA_SELECTED_POS, cardPos);
            	intent.putExtra(EXTRA_LAST_TEXT_POS,  DEFAULT_TEXT_POS);
        		setResult(RESULT_OK, intent);
        		startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Close text viewer.
     *
     * @param context the context
     */
    public static void closeTextViewer(Context context) {
        Intent intent = new Intent("TextViewer");
        intent.putExtra("action", "close");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    
    /* (non-Javadoc)
     * @see android.app.Activity#onOptionsMenuClosed(android.view.Menu)
     */
    @Override
    public void onOptionsMenuClosed(Menu menu) {
    	mAudioManager.playSoundEffect(Sounds.DISMISSED);
        finish();
    }
}
