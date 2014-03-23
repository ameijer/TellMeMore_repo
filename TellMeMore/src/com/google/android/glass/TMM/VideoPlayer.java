package com.google.android.glass.TMM;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.glass.media.Sounds;
//import com.google.android.glass.app.Card;
//import com.google.android.glass.timeline.TimelineManager;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerView;

public class VideoPlayer extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener{
	public static final String TAG = "TMM" +", " + VideoPlayer.class.getSimpleName();
	private GestureDetector mGestureDetector;
	public static final String EXTRA_SELECTED_POS = "selected_pos";
	public static final String EXTRA_SELECTED_ID = "selected_id";
	private static final int DEFAULT_POS = 0;
	private static final int DEFAULT_ID = 0;
	private static final int KEY_SWIPE_DOWN = 4;
	private myListener mlistener;
	private int cardPos, cardId;
	private Context act_context; 
	private TellMeMoreApplication app;
	private VideoCard thisCard;
	private AudioManager mAudioManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (TellMeMoreApplication)getApplication();
		
		
		
		cardPos =  getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		cardId =  getIntent().getIntExtra(EXTRA_SELECTED_ID, DEFAULT_ID);
		Log.i(TAG, "this card is at position: " + cardPos);
		Log.d(TAG, "Cardid received by videoplayer: " + cardId);
		try{
			thisCard = (VideoCard) app.db.findCardById(cardId);
		} catch (ClassCastException e){
			Log.e(TAG, "Tried to cast card from DB", e);
			finish();
		}
		setContentView(R.layout.video_player_layout);
		//Declare YouTubePlayerView
		YouTubePlayerView ytpv = (YouTubePlayerView)findViewById(R.id.youtubeplayer);
		//Initialize YouTubePlayerView using DeveloperKey obtained from YouTubeAPIDemo
		ytpv.initialize("AIzaSyC6MVQReF82uBRdqYJMgisaSMqM1i4dJIM", this);
		mlistener = new myListener();
		mlistener.setContext(this);
		this.mGestureDetector = createGestureDetector(this);
		act_context = this;
		Log.i(TAG, "onCreate finished");

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

	}

	@Override
	public void onInitializationFailure(Provider arg0,
			YouTubeInitializationResult arg1) {
		// TODO Auto-generated method stub

		Toast.makeText(this, "YouTubePlayer.onInitializationFailure(): " + arg1.toString(), Toast.LENGTH_LONG).show(); 
	}

	@Override
	public void onInitializationSuccess(Provider arg0, YouTubePlayer arg1,
			boolean arg2) {
		//Set class variable player to current initialized player in order to be used inside the whole class
		///this.player = player;
		//Make a list of cues that is used to be passed to the player because the getTag method returns two extra characters
		// so we use the substring to chop those two off.
		List<String> cue = new ArrayList<String>();

		cue.add(thisCard.getYTtag());

		//Play YouTube videos in the cue.
		arg1.loadVideos(cue);
		arg1.setPlaybackEventListener(mlistener);

	}

	private GestureDetector createGestureDetector(Context context) {
		GestureDetector gestureDetector = new GestureDetector(context);
		//Create a base listener for generic gestures
		gestureDetector.setBaseListener( new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				
				if (gesture == Gesture.SWIPE_RIGHT) {
					// do something on right (forward) swipe
					mAudioManager.playSoundEffect(Sounds.DISMISSED);
					peaceOut(act_context);
				} else if (gesture == Gesture.SWIPE_LEFT) {
					// do something on left (backwards) swipe
					mAudioManager.playSoundEffect(Sounds.DISMISSED);
					peaceOut(act_context);
				} else if(gesture == Gesture.SWIPE_DOWN){
					mAudioManager.playSoundEffect(Sounds.DISMISSED);
					peaceOut(act_context);
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
				// do something on scrolling
				Log.i(TAG, "scroll detected");
				return true;
			}
		});
		return gestureDetector;

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
			peaceOut(VideoPlayer.this);
			return true;
		}
		return false;
	}

	private class myListener implements YouTubePlayer.PlaybackEventListener {

		private Context context;
		@Override
		public void onBuffering(boolean arg0) {
			return;

		}

		@Override
		public void onPaused() {
			Log.i(TAG, "onPause called");

		}

		@Override
		public void onPlaying() {
			return;

		}

		@Override
		public void onSeekTo(int arg0) {
			return;

		}

		@Override
		public void onStopped() {

			Log.i(TAG, "onStopped listener called, returning to card selection activity, card number: " + cardPos);
			peaceOut(context);

		}

		public void setContext(Context context) {
			this.context = context;
		}
	}

	private void peaceOut(Context context){
		Intent backToCardsIntent= new Intent(context, SelectCardActivity.class);
		backToCardsIntent.putExtra(EXTRA_SELECTED_POS, cardPos);
		setResult(RESULT_OK, backToCardsIntent);
		startActivity(backToCardsIntent);
		finish();
	}
}
