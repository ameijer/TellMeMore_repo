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


public class AudioPlayer extends Activity{
	public static final String TAG = "TMM" +", " + AudioPlayer.class.getSimpleName();
	private GestureDetector mGestureDetector;
	public static final String EXTRA_SELECTED_POS = "selected_pos";
	public static final String EXTRA_SELECTED_ID = "selected_id";
	public static final String EXTRA_PLAYER_POS = "selected_player_pos";
	public static final String EXTRA_LAST_PLAYER_POS = "last_player_pos";
	public static final int TIME_TO_SEEK = 100;
	private static final int KEY_SWIPE_DOWN = 4;
	private static final int DEFAULT_POS = 0;
	private static final String DEFAULT_ID = "null";
	//private myListener mlistener;
	private int cardPos;
	private String  cardId;
	private Context act_context; 
	private ImageView bkgrnd, stat_icon; 
	private TextView help_txt;
	private SliderView prog;
	private FrameLayout layout;
	private AudioManager mAudioManager;
	private boolean paused;
	private AudioCard thisCard;
	public static Activity player; 
	private TellMeMoreApplication app;
	MediaPlayer mediaPlayer;
	Thread progUpdater;
	int lastPos;


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

	public Context getContext(){
		return this;
	}

	private class UpdateStatusRunnable implements Runnable {

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

	private class updaterControl extends Thread {


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
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		this.dispatchTouchEvent(event);
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}

		return false;
	}

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
