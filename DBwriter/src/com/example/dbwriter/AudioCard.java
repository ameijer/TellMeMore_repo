

package com.example.dbwriter;

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
	
	private String background_path;
	
	private String audioClipPath;


	public AudioCard(int handle, int priority, String cardTitle, String path, Server source) {
		super(handle, priority, cardTitle, source);
		
		this.background_path = null;
		this.audioClipPath = path;
	}
	
	public AudioCard(int handle, int id, int priority, String cardTitle, int length, String backgroundPath, Server source) {
		super(handle, id, priority, cardTitle, source);
		this.lengthMillis = length;
		this.background_path = backgroundPath;
		this.audioClipPath = null;
	}
	
	public AudioCard(int handle, int id, int priority, String cardTitle, String backgroundPath, Server source) {
		super(handle, id, priority, cardTitle, source);
		this.background_path = backgroundPath;
		this.audioClipPath = null;
	}
	
	public AudioCard(int handle, int id, int priority, String cardTitle, int length, String backgroundPath, String contentPath, Server source) {
		super(handle, id, priority, cardTitle, source);
		this.lengthMillis = length;
		this.background_path = backgroundPath;
		this.audioClipPath = contentPath;
	}
	
	public AudioCard(int handle, int id, int priority, String cardTitle, String backgroundPath, String contentPath, Server source) {
		super(handle, id, priority, cardTitle, source);
		this.background_path = backgroundPath;
		this.audioClipPath = contentPath;
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

	public String getBackgroundPath() {
		return background_path;
	}

	public void setBackgroundPath(String backgroundPath) {
		this.background_path = backgroundPath;
	}

	public String getAudioClipPath() {
		return audioClipPath;
	}

	public void setAudioClip(String audioClipPath) {
		this.audioClipPath = audioClipPath;
	}


}
