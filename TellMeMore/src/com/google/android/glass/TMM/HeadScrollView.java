/*
 * File: HeadScrollView.java
 * Date: Apr 16, 2014
 * 
 * Original from : https://github.com/pscholl/glass_snippets/tree/master/imu_scrollview
 * Modified by: A. Meijer (atm011) and D. Prudente (dcp017)
 * 
 * Modified for ELEC429, Independent Study
 * 
 * Copyright 2014 atm011 and dcp017
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this 
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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

/**
 * The Class HeadScrollView. This view provides a scrollview that is linked to a
 * sensor listener. This allows the user to scroll up and down the view using
 * head motions, which simulates (roughly) reading down a wall of text.
 */
public class HeadScrollView extends ScrollView implements SensorEventListener {

	/** The sensor object used to obtain sensor data. */
	private Sensor mSensor;

	/** The last accuracy reading, used for sensor adjustments. */
	private int mLastAccuracy;

	/** The Sensor manager, which provides access to the sensors for this view. */
	private SensorManager mSensorManager;

	/**
	 * The sensor poll rate in microseconds. Decrease this for faster response,
	 * but more enegery usage.
	 */
	private static final int SENSOR_RATE_uS = 200000;

	/** The last known X position. */
	private static float lastX;

	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" + ", "
			+ HeadScrollView.class.getSimpleName();

	/**
	 * The Constant HEAD_MOVEMENT_DEG. This is set to the maximum degrees that a
	 * person must move their head to read the entire contents of a card,
	 * regardless of its length.
	 */
	public static final int HEAD_MOVEMENT_DEG = 50;

	/**
	 * The height of the views within this scrollview. Used for dynamically
	 * calculating the sensitivity of the view's sensors.
	 */
	private double incrementPx, childrenHeight;

	/**
	 * Instantiates a new head scroll view.
	 * 
	 * @param context
	 *            The current activity context, used to access sensors and the
	 *            screen.
	 * @param attrs
	 *            The View attribute set.
	 * @param defStyle
	 *            The defstyle of this view. Used by a superclass.
	 */
	public HeadScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * Instantiates a new head scroll view.
	 * 
	 * @param context
	 *            The current activity context, used to access sensors and the
	 *            screen.
	 * @param attrs
	 *            The View attribute set.
	 */
	public HeadScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	/**
	 * Instantiates a new head scroll view.
	 * 
	 * @param context
	 *            The current activity context, used to access sensors and the
	 *            screen.
	 */
	public HeadScrollView(Context context) {
		super(context);
		init();
	}

	/**
	 * Inits the accelerometer and maps it in the class vars.
	 */
	public void init() {
		mSensorManager = (SensorManager) getContext().getSystemService(
				Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

	}

	/**
	 * Enables scrolling of the headscroll view. This should be called only
	 * after all the child views have been drawn, so that the correct scroll
	 * amounts can be determined.
	 */
	public void enableScrolling() {
		if (childrenHeight == 0) {
			//dont freeze the view, run it on a non-ui thread
			new Thread(new Runnable() {
				public void run() {

					if (mSensor == null)
						return;
					for (int i = 0; i < getChildCount(); ++i) {
						
						//calculate the total hight of the entire view
						childrenHeight += getChildAt(i).getHeight();
						Log.d(TAG, "Child: " + i + " has height: "
								+ getChildAt(i).getHeight());
					}

					// we want the entire range to be spread evenly throughout
					// the children views
					incrementPx = childrenHeight / HEAD_MOVEMENT_DEG;
				}
			}).start();
		}
	}

	/**
	 * Activate the sensor listeners for this view.
	 */
	public void activate() {

		mSensorManager.registerListener(this, mSensor, SENSOR_RATE_uS);
	}

	/**
	 * Deactivate the sensor listeners for this view.
	 */
	public void deactivate() {
		mSensorManager.unregisterListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.hardware.SensorEventListener#onAccuracyChanged(android.hardware
	 * .Sensor, int)
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		mLastAccuracy = accuracy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.hardware.SensorEventListener#onSensorChanged(android.hardware
	 * .SensorEvent)
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] mat = new float[9], orientation = new float[3];

		if (mLastAccuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
			return;

		SensorManager.getRotationMatrixFromVector(mat, event.values);
		SensorManager.remapCoordinateSystem(mat, SensorManager.AXIS_X,
				SensorManager.AXIS_Z, mat);
		SensorManager.getOrientation(mat, orientation);

		float z = orientation[0], x = orientation[1], y = orientation[2];

		//calculate the velocity via the position-shift of the X accelerometer axis
		float diff = ((x - lastX) * 100);
		lastX = x;
		
		//scroll the view by that amount
		smoothScrollBy(0, (int) ((incrementPx * diff)));

	}

}