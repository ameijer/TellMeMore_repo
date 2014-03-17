package com.google.android.glass.TMM;

import com.google.android.glass.media.Sounds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Activity showing the options menu.
 */
public class StartMenuActivity extends Activity {
	public static final String TAG = "TMM" +", " + StartMenuActivity.class.getSimpleName();
	private AudioManager mAudioManager;
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        openOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        inflater.inflate(R.menu.tmm_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.start_scan_item:
            	Log.i("TMM", "start scan menu item selected");
                //stopService(new Intent(this, ScanActivity.class));
                stopService(new Intent(StartMenuActivity.this, TMMService.class));
                Intent intent = new Intent(this, ScanActivity.class);
                startActivity(intent);
                return true;
                
            case R.id.disable_tooltip_item:
            	stopService(new Intent(StartMenuActivity.this, TMMService.class));
            	finish();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        // Nothing else to do, closing the activity.
    	//mAudioManager.playSoundEffect(Sounds.DISMISSED);
        finish();
    }
}
