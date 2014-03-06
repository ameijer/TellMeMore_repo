package com.google.android.glass.TMM;

import java.util.ArrayList;

import android.annotation.SuppressLint;
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
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.glass.media.Sounds;
//import com.google.android.glass.app.Card;
//import com.google.android.glass.timeline.TimelineManager;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.glass.widget.SliderView;


@SuppressLint("ResourceAsColor")
public class TextViewer extends Activity{
	public static final String TAG = "TMM" +", " + TextViewer.class.getSimpleName();
	private GestureDetector mGestureDetector;
	public static final String EXTRA_SELECTED_POS = "selected_pos";
	public static final String EXTRA_SELECTED_ID = "selected_id";
	public static final String EXTRA_LAST_TEXT_POS = "last_TEXT_pos";
	//public static final String EXTRA_PLAYER_POS = "selected_player_pos";
	//public static final String EXTRA_LAST_PLAYER_POS = "last_player_pos";
	public static final int TIME_TO_SEEK = 100;
	private static final int KEY_SWIPE_DOWN = 4;
	private static final int DEFAULT_POS = 0;
	private static final int DEFAULT_ID = 0;
	//private myListener mlistener;
	private long cardPos, cardId;
	private Context act_context; 
	//private ImageView bkgrnd, stat_icon; 
	//private TextView help_txt;
	//private SliderView prog;
	//private FrameLayout layout;
	private AudioManager mAudioManager;
	//private boolean paused;
	//public static Activity player; 
	//MediaPlayer mediaPlayer;
	//Thread progUpdater;
	private int lastPos;
	private ArrayList<TextElement> toShow;
	private ListView mView;
	private TextViewerListAdapter customAdapter;
	private HeadListView headScroll;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cardPos =  getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		cardId =  getIntent().getIntExtra(EXTRA_SELECTED_ID, DEFAULT_ID);
		//lastPos =  getIntent().getIntExtra(EXTRA_LAST_PLAYER_POS, DEFAULT_POS);
		Log.i(TAG, "this card is at position: " + cardPos);
		//setContentView(R.layout.audio_player_layout);
		//bkgrnd = (ImageView)findViewById(R.id.background_audio_activity);
		//stat_icon = (ImageView)findViewById(R.id.status_icon);
		//help_txt = (TextView)findViewById(R.id.audio_activity_helper);
		//prog = (SliderView) findViewById(R.id.prog);
		//layout = (FrameLayout) findViewById(R.id.audioPlayerFrame);
		Log.i(TAG, "cardid passed: " + cardId);
		//player = this;
		//TODO - replace with DB calls
		TextCard thisCard = (TextCard) SelectCardActivity.getTestCards(10)[(int) cardPos];
		//if(thisCard.getBackground() == null){
			//set a black background
			//	layout.setBackgroundColor(getResources().getColor(R.color.black));
		//} else {
		//	bkgrnd.setImageBitmap(BitmapFactory.decodeByteArray(thisCard.getBackground(), 0, thisCard.getBackground().length));
		//}
		//stat_icon.setImageResource(R.drawable.ic_pause);
		//TODO
		//help_txt.setText("tap to pause");
		setContentView(R.layout.textviewer_layout);
		mView = (ListView) findViewById(R.id.textList);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		
		
		//		int resID=getResources().getIdentifier("powerpointdemo", "raw", getPackageName());
		//		if(resID == 0){
		//			Log.e(TAG, "sound resource not found");
		//			
		//		}
		//mediaPlayer = MediaPlayer.create(this,R.raw.powerpointdemo);
		Log.i(TAG, "lastPos as read by audioplayer: " + lastPos);
		if(lastPos > 0){
			//	mediaPlayer.seekTo(lastPos);

		}
		//	mediaPlayer.start();

		LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("TextViewer"));
		//	//prog.startProgress(/*mediaPlayer.getDuration()*/ 30000);
		//	progUpdater = new updaterControl();
		//	progUpdater.start();


		this.mGestureDetector = createGestureDetector(this);
		act_context = this;

		startService(new Intent(this, TextViewerSupportService.class));
		customAdapter = new TextViewerListAdapter(this, toShow);

		Log.i(TAG, "Textviewadapter customadapter: " + customAdapter);
		Log.i(TAG, "View being used: " + mView);
		mView.setAdapter(customAdapter);
		mView.setBackgroundColor(getResources().getColor(R.color.black_trans));
		
		
		
		headScroll = (HeadListView) findViewById(R.id.textList);
		Log.i(TAG, "onCreate finished");
		

	}


	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "onReceive called in textviewer");
			TextViewerBundle bundl = (TextViewerBundle) intent.getParcelableExtra("data");
			toShow = bundl.getElems();
			if(toShow.size() < 1) {
				Log.i(TAG, "NOTHING TO SHOW IN TEXTVIEW");
				peaceOut(getContext());
			}

			cardId = bundl.getId();
			// get data from the table by the ListAdapter
			customAdapter.addContent(toShow);
			
			for(int i = 0; i < toShow.size(); i ++){
				Log.v(TAG, "text contents of element " + i + ": " + toShow.get(i));
			}
		}
	};


	@Override
	public void onResume(){
		headScroll.activate();
		super.onResume();
		cardPos =  getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		cardId =  getIntent().getIntExtra(EXTRA_SELECTED_ID, DEFAULT_ID);
		//lastPos =  getIntent().getIntExtra(EXTRA_LAST_PLAYER_POS, DEFAULT_POS);

		try {
			//	mediaPlayer.seekTo(lastPos);
			//	mediaPlayer.start();

		} catch (IllegalStateException e){
			//	mediaPlayer = MediaPlayer.create(this,R.raw.powerpointdemo);
			//	mediaPlayer.seekTo(lastPos);
			//	mediaPlayer.start();
			Log.w(TAG, "onresume illegal state exception caught, initing new mediaplayer");
		}

		//help_txt.setText("tap to pause");
		//stat_icon.setImageResource(R.drawable.ic_pause);

		//mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

	//	public void onCompletion(MediaPlayer mp) {

	//		peaceOut(act_context);
//
	//	}
	//});

}

public Context getContext(){
	return this;
}




@Override
public void onPause() {
	headScroll.deactivate();
	super.onPause();
	//stat_icon.setImageResource(R.color.black);
	//help_txt.setText(R.string.null_string);
	//progUpdater.interrupt();

	try {
		//		progUpdater.interrupt();
		//	mediaPlayer.release();
		//		if(prog != null){
		////			prog.dismissManualProgress();

		//		}
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

				Intent menuintent = new Intent(getContext(), TextMenu.class);
			//	menuintent.putExtra(EXTRA_LAST_PLAYER_POS, mediaPlayer.getCurrentPosition());
				menuintent.putExtra(EXTRA_SELECTED_ID, cardId);
				menuintent.putExtra(EXTRA_SELECTED_POS, cardPos);
				Log.i(TAG, "cardPos passed to menu: " + cardPos);
				//		mediaPlayer.release();
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
				//	if(mediaPlayer.getCurrentPosition() > TIME_TO_SEEK){
				//			mediaPlayer.seekTo((int) (mediaPlayer.getCurrentPosition() +  TIME_TO_SEEK * velocity ));
				//		}
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
		peaceOut(TextViewer.this);
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
	//progUpdater.interrupt();
	//	if(mediaPlayer != null)
	//		mediaPlayer.release();

	//if(prog != null){
	//		prog.dismissManualProgress();

	//}
	finish();

}

public void peaceOut(Context context){
	//progUpdater.interrupt();
	//	if(mediaPlayer != null)
	//		mediaPlayer.release();

	//	if(prog != null){
	//		prog.dismissManualProgress();
	//
	//	}
	Intent backToCardsIntent= new Intent(context, SelectCardActivity.class);
	backToCardsIntent.putExtra(EXTRA_SELECTED_POS, cardPos);
	setResult(RESULT_OK, backToCardsIntent);
	startActivity(backToCardsIntent);
	finish();
}
}
