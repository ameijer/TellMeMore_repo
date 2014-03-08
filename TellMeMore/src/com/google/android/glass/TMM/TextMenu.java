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
 * Activity showing the options menu.
 */
public class TextMenu extends Activity {
	public static final String TAG = "TMM" +", " + TextMenu.class.getSimpleName();
	public static final String EXTRA_PLAYER_POS = "selected_player_pos";
	public static final String EXTRA_SELECTED_POS = "selected_pos";
	public static final String EXTRA_SELECTED_ID = "selected_id";
	public static final String EXTRA_LAST_TEXT_POS = "last_TEXT_pos";
	public static final String EXTRA_NARRATION_REQUESTED = "narration_requested";
	public static final int DEFAULT_TEXT_POS = -1;
	private static final int DEFAULT_ID = 0;
	private static final int DEFAULT_POS = 0;
	public static final boolean DEFAULT_NARR = false;
	private int lastPos, cardPos, cardId; 
	private AudioManager mAudioManager;
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
    }

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

    public static void closeTextViewer(Context context) {
        Intent intent = new Intent("TextViewer");
        intent.putExtra("action", "close");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    
    @Override
    public void onOptionsMenuClosed(Menu menu) {
    	mAudioManager.playSoundEffect(Sounds.DISMISSED);
        // assume the user wants to resume
    	Intent intent = new Intent(this, AudioPlayer.class);
        
        Log.i(TAG, "last posit passed through to new player: " + lastPos);
        intent.putExtra(EXTRA_LAST_TEXT_POS, lastPos);
        intent.putExtra(EXTRA_SELECTED_POS, cardPos);
        startActivity(intent);
        finish();
    }
}
