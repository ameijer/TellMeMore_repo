package com.google.android.glass.TMM;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.glass.TMM.TextElement.Type;
import com.google.android.glass.media.Sounds;
//import com.google.android.glass.app.Card;
//import com.google.android.glass.timeline.TimelineManager;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;


@SuppressLint("ResourceAsColor")
public class TextViewer extends Activity{
	public static final String TAG = "TMM" +", " + TextViewer.class.getSimpleName();
	private GestureDetector mGestureDetector;
	public static final String EXTRA_SELECTED_POS = "selected_pos";
	public static final String EXTRA_SELECTED_ID = "selected_id";
	public static final String EXTRA_REQUESTED_NARRATION = "narration_requested";
	public static final boolean DEFAULT_NARR = false;
	public static final String EXTRA_LAST_TEXT_POS = "last_TEXT_pos";
	public static final int DEFAULT_TEXT_POS = -1;
	public static final int TIME_TO_SEEK = 100;
	private static final int KEY_SWIPE_DOWN = 4;
	private static final int DEFAULT_POS = 0;
	private static final int DEFAULT_ID = -1;
	int height;
	private TellMeMoreApplication app;
	private int cardPos;
	private String cardId;
	private Context act_context; 
	private AudioManager mAudioManager;
	private TextToSpeech mSpeech;
	private int lastPos;
	private TextCard thisCard;
	private LinearLayout mView;
	private HeadScrollView scroller;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cardPos =  getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		cardId =  getIntent().getStringExtra(EXTRA_SELECTED_ID);
		lastPos =  getIntent().getIntExtra(EXTRA_LAST_TEXT_POS, DEFAULT_TEXT_POS);
		Log.i(TAG, "this card is at position: " + cardPos);
		Log.i(TAG, "cardid passed: " + cardId);
		//obtain a reference to the application singleton
		app = ((TellMeMoreApplication)this.getApplication());

		try{
			thisCard = (TextCard) app.db.findCardById(cardId);
		} catch (ClassCastException e){
			Log.e(TAG, "Tried to cast card from DB", e);
			finish();
		}


		setContentView(R.layout.textviewer_layout);

		scroller = (HeadScrollView) findViewById(R.id.outertextList);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		this.mGestureDetector = createGestureDetector(this);
		act_context = this;

		//startService(new Intent(this, TextViewerSupportService.class));


		mView = (LinearLayout) findViewById(R.id.innertextList);

		Log.i(TAG, "View being used: " + mView);
		mView.setBackgroundColor(getResources().getColor(R.color.black));

		mSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				// Do nothing.
				mSpeech.speak("Narrator initialized", TextToSpeech.QUEUE_FLUSH, null);
			}
		});

		//	headScroll = (HeadListView) findViewById(R.id.textList);
		Log.i(TAG, "onCreate finished");
		scroller.enableScrolling();
		for(int i = 0; i < thisCard.getContents().size(); i ++){
			Log.v(TAG, "text contents of element " + i + ": " + thisCard.getContents().get(i));
			TextElement p = thisCard.getContents().get(i);
			if( p.getType() == Type.IMAGE){
				ImageView pic = new ImageView(this);
				Bitmap bmp = BitmapFactory.decodeFile(p.getImg());
				Log.d(TAG, "path to be decoded: " + p.getImg());
				Log.d(TAG, "BMP Generated: " + bmp);
				pic.setImageBitmap(bmp);
				mView.addView(pic);
				TextView cap = new TextView(this);
				cap.setText(p.getText());
				cap.setGravity(Gravity.CENTER);
				cap.setPadding(0, -10, 0, 30);
				mView.addView(cap);
			}else if( p.getType() == Type.TEXT_){
				TextView cap = new TextView(this);
				cap.setText(p.getText());
				mView.addView(cap);
			}
		}

			new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					scroller.enableScrolling();
				}
			}).start();
		}



		@Override
		public void onResume(){
			scroller.activate();
			super.onResume();
			cardPos =  getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
			cardId =  getIntent().getStringExtra(EXTRA_SELECTED_ID);
			lastPos =  getIntent().getIntExtra(EXTRA_LAST_TEXT_POS, DEFAULT_TEXT_POS);
			boolean narrate = getIntent().getBooleanExtra(EXTRA_REQUESTED_NARRATION, DEFAULT_NARR);

			//if the user wanted narration, then play the clip
			//TODO - check if this runs in a separate thread as implemented
			//if not, this will have to be encapsulated in a runnable so the user can still browse the text while 
			//it is narrating

			if(narrate && lastPos > -1){
				//		String toSpeak = customAdapter.getItem(lastPos).getText();
				//	mSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
			}
		}

		public Context getContext(){
			return this;
		}




		@Override
		public void onPause() {
			scroller.deactivate();
			super.onPause();
			mSpeech.stop();

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
						//	menuintent.putExtra(EXTRA_LAST_TEXT_POS, headScroll.getLastVisiblePosition());
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
			mSpeech.shutdown();
			finish();

		}

		public void peaceOut(Context context){
			Intent backToCardsIntent= new Intent(context, SelectCardActivity.class);
			backToCardsIntent.putExtra(EXTRA_SELECTED_POS, cardPos);
			setResult(RESULT_OK, backToCardsIntent);
			startActivity(backToCardsIntent);
			finish();
		}
	}
