package com.google.android.glass.TMM;

import android.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;

public class AudioCard extends TMMCard{
	public static final String TAG = "TMM" +", " + AudioCard.class.getSimpleName();
	//length of clip in milliseconds
	private int lengthMillis;

	//number of times this clip has been played
	private int numPlays;
	
	private byte[] background;


	public AudioCard(int handle, int id, int priority, String cardTitle) {
		super(handle, id, priority, cardTitle);
		
		this.background = null;
	}
	
	public AudioCard(int handle, int id, int priority, String cardTitle, int length, byte[] background) {
		super(handle, id, priority, cardTitle);
		this.lengthMillis = length;
		this.background = background;
	}
	
	public AudioCard(int handle, int id, int priority, String cardTitle, byte[] background) {
		super(handle, id, priority, cardTitle);
		this.background = background;
	}

	public int getNumPlays() {
		return numPlays;
	}

	public void setNumPlays(int numPlays) {
		this.numPlays = numPlays;
	}

	public int getLengthMillis() {
		return lengthMillis;
	}

	public void setLengthMillis(int lengthMillis) {
		this.lengthMillis = lengthMillis;
	}

	public byte[] getBackground() {
		return background;
	}

	public void setBackground(byte[] background) {
		this.background = background;
	}


}
