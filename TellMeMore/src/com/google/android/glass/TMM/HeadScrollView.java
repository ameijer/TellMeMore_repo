package com.google.android.glass.TMM;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ScrollView;


public class HeadScrollView extends ScrollView implements SensorEventListener {

	private Sensor mSensor;
	private int mLastAccuracy;
	private SensorManager mSensorManager;
	private static final int SENSOR_RATE_uS = 200000;
	private static float lastX;
	public static final String TAG = "TMM" +", " + HeadScrollView.class.getSimpleName();
	//private static int position;
	public static final int HEAD_MOVEMENT_DEG = 50;
	private double incrementPx, childrenHeight;

	public HeadScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public HeadScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HeadScrollView(Context context) {
		super(context);
		init();
	}

	public void init() {
		mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);



	}
	
	public void enableScrolling(){
		if(childrenHeight == 0){
			new Thread(new Runnable() {
				public void run() {
			
					if (mSensor == null)
						return;
					for(int i=0; i< getChildCount(); ++i) {
						childrenHeight += getChildAt(i).getHeight();
						Log.d(TAG, "Child: " + i + " has height: " + getChildAt(i).getHeight());
					}

					//we want the entire range to be spread evenly throughout the children views 
					incrementPx = childrenHeight / HEAD_MOVEMENT_DEG;
				}
			}).start();
		}
	}

	public void activate() {

		

		mSensorManager.registerListener(this, mSensor, SENSOR_RATE_uS);
	}

	public void deactivate() {
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		mLastAccuracy = accuracy;
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] mat = new float[9],
				orientation = new float[3];

		if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
			return;

		SensorManager.getRotationMatrixFromVector(mat, event.values);
		SensorManager.remapCoordinateSystem(mat, SensorManager.AXIS_X, SensorManager.AXIS_Z, mat);
		SensorManager.getOrientation(mat, orientation);

		float z = orientation[0], x = orientation[1], y = orientation[2];


		float diff =  ((x - lastX) * 100); 
		lastX = x;
		//position += diff;
		//Log.d(TAG, "Scrolling text view by: " + (int) ((incrementPx * x)) + " pixels");
		//Log.d(TAG, "X sensor value reads: " + x);
		//Log.d(TAG, "Increment value reads: " + incrementPx);
		smoothScrollBy(0, (int) ((incrementPx * diff)));

	}

}