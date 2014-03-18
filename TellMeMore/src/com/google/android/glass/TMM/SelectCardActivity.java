package com.google.android.glass.TMM;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.widget.CardScrollView;
import com.google.android.glass.TMM.VideoPlayer;


public class SelectCardActivity extends Activity implements GestureDetector.BaseListener{
	public static final String NUM_CARDS = "num_cards";
	public static final String EXTRA_INITIAL_VALUE = "initial_value";
	public static final String EXTRA_SELECTED_ID = "selected_id";
	public static final String EXTRA_SELECTED_POS = "selected_pos";
	private static final int DEFAULT_POS = 0;
	private static final int KEY_SWIPE_DOWN = 4;
	public static final String TAG = "TMM" +", " + SelectCardActivity.class.getSimpleName();
	private static final int DEFAULT_NUM_CARDS = 10;
	private int lastCard;
	TMMCard[] cardArr;
	private AudioManager mAudioManager;

	private GestureDetector mDetector;
	private CardScrollView mView;
	private SelectCardScrollAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		lastCard = getIntent().getIntExtra(EXTRA_SELECTED_POS, DEFAULT_POS);
		cardArr = getTestCards(10);
		mAdapter = new SelectCardScrollAdapter(
				this, cardArr.length /*getIntent().getIntExtra(NUM_CARDS, DEFAULT_NUM_CARDS)*/, cardArr );


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
		//		Object id = mView.getItemForChildAt(lastCard);
		//		if (id != null){
		//			mView.setIdSelection(id);
		//		}
		mView.activate();
		//mView.setSelection(lastCard);


		mDetector = new GestureDetector(this).setBaseListener(this);
	}

	public static TMMCard[] getTestCards(int num) {

		TMMCard[] toReturn = new TMMCard[num];
		char uniqueId = 0x41;
		boolean flag = false;
		for(int i = 0; i < num; i++){



			//if num = 10...


			uniqueId++;
			if(i%3 == 0){
				//0, 3, 6, 9
				//create text card
				//	toReturn[i] = new TextCard
				if(flag){
					toReturn[i] = new TextCard(i, i, i, "Text Card title: Card " + uniqueId, "Summary info line 1", "Summary info line 2", "Summary info line 3", null);
					flag = !flag;

				} else {
					byte[] junkBytes = new byte[]{0x34, 0x41};

					toReturn[i] = new TextCard(i, i, i, "Text Card title: Card " + uniqueId, "Summary info line 1", "Summary info line 2", "Summary info line 3", junkBytes, null);
					flag= !flag;
				}
			} else if(i%2 == 0){
				//2, 4, 8
				//create video card
				toReturn[i] = new VideoCard(i, i, "Video Card title: Card " + uniqueId, null);
			} else {
				//1, 5, 7
				//create audio card
				toReturn[i] = new AudioCard(i,  i, "Audio Card title: Card " + uniqueId, "path/to/file", null);
			}


		}




		return toReturn;
	}

	@Override
	public void onResume() {
		super.onResume();
		mView.activate();
		mView.setSelection(getIntent().getIntExtra(EXTRA_INITIAL_VALUE, 0));
	}

	@Override
	public void onPause() {
		super.onPause();
		mView.deactivate();
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
				//TODO
				//Context context = getApplicationContext();
				//CharSequence text = "Text support not yet implmented";
				//int duration = Toast.LENGTH_LONG;

				//Toast toast = Toast.makeText(context, text, duration);
				//toast.show();
				//startService(new Intent(this, TextViewerSupportService.class));
				resultIntent= new Intent(this, TextViewer.class);
			}

			resultIntent.putExtra(EXTRA_SELECTED_ID, cardArr[mView.getSelectedItemPosition()].getId());
			resultIntent.putExtra(EXTRA_SELECTED_POS, mView.getSelectedItemPosition());
			setResult(RESULT_OK, resultIntent);
			mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
			startActivity(resultIntent);
			Log.i(TAG, "finishing gesture handling");
			finish();
			return true;
		}
		return false;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		finish();

	}
}
