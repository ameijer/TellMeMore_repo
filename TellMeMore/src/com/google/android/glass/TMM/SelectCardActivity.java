package com.google.android.glass.TMM;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollView;
import com.google.glass.widget.SliderView;

      
public class SelectCardActivity extends Activity implements GestureDetector.BaseListener{
	public static final String NUM_CARDS = "num_cards";
	public static final String EXTRA_INITIAL_VALUE = "initial_value";
	public static final String EXTRA_SELECTED_ID = "selected_id";
	public static final String EXTRA_SELECTED_POS = "selected_pos";
	public static final String CARDS_READY_KEY = "cards_ready";
	private static final int DEFAULT_POS = 0;
	private static final int KEY_SWIPE_DOWN = 4;
	public static final String TAG = "TMM" +", " + SelectCardActivity.class.getSimpleName();
	private int lastCard;
	private boolean hasCards;

	private static TMMCard[] cardArr;
	private AudioManager mAudioManager;

	private TellMeMoreApplication app;
	private GestureDetector mDetector;
	private CardScrollView mView;
	private SelectCardScrollAdapter mAdapter;
	private SliderView mIndeterm;

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
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("cards_loaded"));
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

	private void registerListener(){
		mDetector = new GestureDetector(this).setBaseListener(this);
	}

	// handler for received Intents for the "my-event" event 
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Extract data included in the Intent
			String serverName = intent.getStringExtra("server_used");
			Log.d(TAG, "Got message: " + serverName);

			//retreive cards from the target server, since they have ostensibly been loaded
			ArrayList<TMMCard> cardz = app.db.findCardsbyServer(serverName);

			//TODO
			//ugly, needs further research into options in this area
			cardArr = new TMMCard[cardz.size()];
			for(int i = 0; i < cardz.size(); i++){
				cardArr[i] = cardz.get(i);
			}
			hasCards = true;
			enableCardScroll();
	
			//mView.setSelection(lastCard);
			//registerListener();
		}
	};

	private void enableCardScroll(){
		registerListener();
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
		mView.activate();mAdapter = new SelectCardScrollAdapter(
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
		mView.activate();
	}

	@Override
	public void onResume() {
		super.onResume();
		if(hasCards){
			mView.activate();
			//mView.setSelection(getIntent().getIntExtra(EXTRA_INITIAL_VALUE, 0));
		}
	}

	@Override
	public void onPause() {
		if(hasCards){
			super.onPause();
			mView.deactivate();
		}
	}

	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		return mDetector.onMotionEvent(event);
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
			finish();
			return true;
		}
		return false;
	}

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

			int id =  cardArr[mView.getSelectedItemPosition()].getId();
			resultIntent.putExtra(EXTRA_SELECTED_ID,id);
			resultIntent.putExtra(EXTRA_SELECTED_POS, mView.getSelectedItemPosition());
			setResult(RESULT_OK, resultIntent);
			mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
			startActivity(resultIntent);
			Log.d(TAG, "after gesture handled, cardId sent to viewer/player activity is: " + id);
			Log.i(TAG, "finishing gesture handling");
			finish();
			return true;
		}
		return false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		hasCards = false;
		//finish();

	}
}
