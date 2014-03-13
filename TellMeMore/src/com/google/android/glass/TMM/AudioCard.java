

package com.google.android.glass.TMM;

import java.io.Serializable;


/**
 * @author alex
 *
 */
public class AudioCard extends TMMCard implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 367690893029671570L;
	public static final String TAG = "TMM" +", " + AudioCard.class.getSimpleName();
	//length of clip in milliseconds
	private int lengthMillis;

	//number of times this clip has been played
	private int numPlays;
	
	private byte[] background;
	
	private byte[] audioClip;


	public AudioCard(int handle, int id, int priority, String cardTitle) {
		super(handle, id, priority, cardTitle);
		
		this.background = null;
		this.audioClip = null;
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
	
	public AudioCard(int handle, int id, int priority, String cardTitle, int length, byte[] background, byte[] content) {
		super(handle, id, priority, cardTitle);
		this.lengthMillis = length;
		this.background = background;
		this.audioClip = content;
	}
	
	public AudioCard(int handle, int id, int priority, String cardTitle, byte[] background, byte[] content) {
		super(handle, id, priority, cardTitle);
		this.background = background;
		this.audioClip = content;
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

	public byte[] getAudioClip() {
		return audioClip;
	}

	public void setAudioClip(byte[] audioClip) {
		this.audioClip = audioClip;
	}


}
