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
public class AudioMenu extends Activity {
	public static final String TAG = "TMM" +", " + AudioMenu.class.getSimpleName();
	public static final String EXTRA_PLAYER_POS = "selected_player_pos";
	public static final String EXTRA_SELECTED_POS = "selected_pos";
	public static final String EXTRA_SELECTED_ID = "selected_id";
	public static final String EXTRA_LAST_PLAYER_POS = "last_player_pos";
	private static final int DEFAULT_ID = 0;
	private static final int DEFAULT_POS = 0;
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
        inflater.inflate(R.menu.audiomenu, menu);
        lastPos =  getIntent().getIntExtra(EXTRA_LAST_PLAYER_POS, DEFAULT_POS);
        cardPos =  getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		cardId =  getIntent().getIntExtra(EXTRA_SELECTED_ID, DEFAULT_ID);
	
        return true;
    }
   

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	 mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.resume_audio:
            	Log.i("TMM", "resume audio menu item selected");
                //stopService(new Intent(this, ScanActivity.class));
                Intent intent = new Intent(this, AudioPlayer.class);
                
                Log.i(TAG, "last posit passed through to new player: " + lastPos);
                intent.putExtra(EXTRA_LAST_PLAYER_POS, lastPos);
                intent.putExtra(EXTRA_SELECTED_POS, cardPos);
                startActivity(intent);
                return true;
            case R.id.reset_audio:
            	closeAudioPlayer(this);
            	 intent = new Intent(this, AudioPlayer.class);
                 
                 //reset = 0 ms resume position
                 intent.putExtra(EXTRA_LAST_PLAYER_POS, 0);
                 intent.putExtra(EXTRA_SELECTED_POS, cardPos);
                 startActivity(intent);
            	return true;
            case R.id.quit_audio:
            	closeAudioPlayer(this);
            	intent = new Intent(this, SelectCardActivity.class);
            	intent.putExtra(EXTRA_SELECTED_POS, cardPos);
        		setResult(RESULT_OK, intent);
        		startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static void closeAudioPlayer(Context context) {
        Intent intent = new Intent("AudioPlayer");
        intent.putExtra("action", "close");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    
    @Override
    public void onOptionsMenuClosed(Menu menu) {
    	mAudioManager.playSoundEffect(Sounds.DISMISSED);
        // assume the user wants to resume
    	Intent intent = new Intent(this, AudioPlayer.class);
        
        Log.i(TAG, "last posit passed through to new player: " + lastPos);
        intent.putExtra(EXTRA_LAST_PLAYER_POS, lastPos);
        intent.putExtra(EXTRA_SELECTED_POS, cardPos);
        startActivity(intent);
        finish();
    }
}
