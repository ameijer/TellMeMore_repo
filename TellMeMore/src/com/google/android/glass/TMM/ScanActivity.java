package com.google.android.glass.TMM;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;

//import android.view.GestureDetector;
//import android.view.GestureDetector.OnGestureListener;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

//import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Button;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;

/* Import ZBar Class files */
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;

public class ScanActivity extends Activity 
{
	public static final String TAG = "TMM" +", " + ScanActivity.class.getSimpleName();
	public static char uniqueId;
	public static final String TARGET_SERVER_KEY = "target_server";
	public static final String EXAMPLE_CARD_SERVER = "example_card_generator";
	public static final String CARDS_READY_KEY = "cards_ready";
	private GestureDetector mGestureDetector;
	
	private static final int KEY_SWIPE_DOWN = 4;
	protected static final String url = "URL";
	private TellMeMoreApplication app;

	private Camera mCamera;
	private CameraPreview mPreview;
	private Handler autoFocusHandler;
	private AudioManager mAudioManager;
	Button scanButton;

	ImageScanner scanner;

	private boolean previewing = true;

	static {
		System.loadLibrary("iconv");
	} 

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = (TellMeMoreApplication) this.getApplication();
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		setContentView(R.layout.scan_activity_layout);
		Log.i(TAG, "onCreateCalled");

		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		autoFocusHandler = new Handler();




		//For some reason, right after launching from the "ok, glass" menu the camera is locked
		//Try 3 times to grab the camera, with a short delay in between.
		for(int i=0; i < 3; i++)
		{
			mCamera = getCameraInstance();
			if(mCamera != null) break;

			//Toast.makeText(this, "Couldn't lock camera, trying again in 1 second", Toast.LENGTH_SHORT).show();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(mCamera == null)
		{
			Toast.makeText(this, "Camera cannot be locked", Toast.LENGTH_SHORT).show();
			finish();
		}

		/* Instance barcode scanner */
		createScanner();

		scanQR();
	}


	//temporary debug code
	//atm011
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if (keyCode == KEY_SWIPE_DOWN)
		{
		
			//when we start the select card activity from here, we are going to want to download/update the new cards, so 
			//tell the selectcardactivity that the cards arent ready yet
			
			
			// there was a swipe down event
			Log.i(TAG, "hacky swipe_down method called");
			mAudioManager.playSoundEffect(Sounds.DISMISSED);
			
			//start the next activity
			startCardDownload(EXAMPLE_CARD_SERVER);
			Intent intent = new Intent(this, SelectCardActivity.class);
			intent.putExtra(CARDS_READY_KEY, false);
			//Intent intent= new Intent(context, OpenYouTubePlayerActivity.class);
			//Uri myUri = Uri.parse("ytv://eneEmDtSvzI");
			//intent.setData(myUri);
			startActivity(intent);
			finish();
			return true;
		}
		return false;
	}


	public void createScanner() {
		scanner = new ImageScanner();
		scanner.setConfig(0, Config.X_DENSITY, 3);
		scanner.setConfig(0, Config.Y_DENSITY, 3);
	}

	public void scanQR() {
		mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
		FrameLayout preview = (FrameLayout)findViewById(R.id.cameraPreview);
		preview.addView(mPreview);
	}

	public void onPause() {
		super.onPause();
		releaseCamera();
	}

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
		Camera c = null;
		try {
			c = Camera.open();
			Log.d(TAG, "getCamera = " + c);
		} catch (Exception e){
			Log.d(TAG, e.toString());
		}
		return c;
	}

	private void releaseCamera() {
		if (mCamera != null) {
			previewing = false;
			mCamera.setPreviewCallback(null);
			mCamera.release();
			mCamera = null;
		}
	}

	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (previewing)
				mCamera.autoFocus(autoFocusCB);
		}
	};

	PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();

			Image barcode = new Image(size.width, size.height, "Y800");
			barcode.setData(data);

			int result = scanner.scanImage(barcode);

			if (result != 0) {
				previewing = false;
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();

				String text = "";
				SymbolSet syms = scanner.getResults();
				for (Symbol sym : syms) {
					text = sym.getData();
					startCardDownload(text);
					break;
				}
				mAudioManager.playSoundEffect(Sounds.SUCCESS);
				// Add in integration stuff to go to SelectCardActivity
				Context context = getApplicationContext();
				//mTimelineManager = TimelineManager.from(context);
				//Card initCard = new Card(context);
				//if (uniqueId < 0x41){
				//	uniqueId = 0x41;
				//}
				//String testText = "You learned about " + uniqueId;
				//uniqueId++;
				//String testFootnote = "Tap to revisit";
				//initCard.setText(testText);
				//initCard.setFootnote(testFootnote);

				//note-no menu or pending intents supported, google is working on this 
				//mTimelineManager.insert(initCard);

				//start the next activity
				Intent intent = new Intent(context, SelectCardActivity.class);

				//Intent intent= new Intent(context, OpenYouTubePlayerActivity.class);
				//Uri myUri = Uri.parse("ytv://eneEmDtSvzI");
				//intent.setData(myUri);
				startActivity(intent);
				finish();


				//                    // Return to the calling activity with the result
				//                    Intent resultIntent = new Intent();
				//                    resultIntent.putExtra(ScanActivity.url, text);
				//                    setResult(Activity.RESULT_OK, resultIntent);
				//                    finish();
			}
		}
	};

	void startCardDownload(String url)
	{
		
		//start the card download service
		//TODO -perform sanity check on the scanned text
		Intent intent = new Intent(this, CardLoaderService.class);
		intent.putExtra(TARGET_SERVER_KEY, url);
		
        startService(intent);
		//Toast t = Toast.makeText(getApplicationContext(), url, Toast.LENGTH_SHORT);
		//t.show();
		return;
	}

	// Mimic continuous auto-focusing
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};

}