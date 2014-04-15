

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
	
	private String background_path, background_name;
	
	private String audioClipPath, audioClipName;
	


	public AudioCard(int handle, int priority, String cardTitle, String path, Server source) {
		super(handle, priority, cardTitle, source, TMMCard.AUDIO);
		
		this.background_path = null;
		this.audioClipPath = path;
		int charToWipe = audioClipPath.lastIndexOf('/');
		setAudioClipName(audioClipPath.substring(charToWipe + 1));
	}
	
	public AudioCard(int handle, String id, int priority, String cardTitle, int length, String backgroundPath, Server source) {
		super(handle, id, priority, cardTitle, source,  TMMCard.AUDIO);
		this.lengthMillis = length;
		this.background_path = backgroundPath;
		this.audioClipPath = null;
		int charToWipe = background_path.lastIndexOf('/');
		setBackground_name(background_path.substring(charToWipe + 1));
	}
	
	public AudioCard(int handle, String id, int priority, String cardTitle, String backgroundPath, Server source) {
		super(handle, id, priority, cardTitle, source, TMMCard.AUDIO);
		this.background_path = backgroundPath;
		this.audioClipPath = null;
		int charToWipe = background_path.lastIndexOf('/');
		setBackground_name(background_path.substring(charToWipe + 1));
	}
	
	public AudioCard(int handle, String id, int priority, String cardTitle, int length, String backgroundPath, String contentPath, Server source) {
		super(handle, id, priority, cardTitle, source, TMMCard.AUDIO);
		this.lengthMillis = length;
		this.background_path = backgroundPath;
		this.audioClipPath = contentPath;
		int charToWipe = audioClipPath.lastIndexOf('/');
		setAudioClipName(audioClipPath.substring(charToWipe + 1));
		charToWipe = background_path.lastIndexOf('/');
		setBackground_name(background_path.substring(charToWipe + 1));
	}
	
	public AudioCard(int handle, String id, int priority, String cardTitle, String backgroundPath, String contentPath, Server source) {
		super(handle, id, priority, cardTitle, source, TMMCard.AUDIO);
		this.background_path = backgroundPath;
		this.audioClipPath = contentPath;
		int charToWipe = audioClipPath.lastIndexOf('/');
		setAudioClipName(audioClipPath.substring(charToWipe + 1));
		charToWipe = background_path.lastIndexOf('/');
		setBackground_name(background_path.substring(charToWipe + 1));
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
	
	public boolean hasBackground(){
		if(background_path == null || background_path.equalsIgnoreCase("")){
			return false;
		} else return true;
	}
	
	public boolean hasAudio(){
		if(audioClipPath == null || audioClipPath.equalsIgnoreCase("")){
			return false;
		} else return true;
	}

	public void setLengthMillis(int lengthMillis) {
		this.lengthMillis = lengthMillis;
	}

	public String getBackgroundPath() {
		return background_path;
	}

	public void setBackgroundPath(String backgroundPath) {
		this.background_path = backgroundPath;
		
		int charToWipe = background_path.lastIndexOf('/');
		setBackground_name(background_path.substring(charToWipe + 1));
	}

	public String getAudioClipPath() {
		return audioClipPath;
	}

	public void setAudioClip(String audioClipPath) {
		this.audioClipPath = audioClipPath;
		int charToWipe = audioClipPath.lastIndexOf('/');
		setAudioClipName(audioClipPath.substring(charToWipe + 1));
	}

	public String getAudioClipName() {
		return audioClipName;
	}

	private void setAudioClipName(String audioClipName) {
		this.audioClipName = audioClipName;
	}

	public String getBackground_name() {
		return background_name;
	}

	private void setBackground_name(String background_name) {
		this.background_name = background_name;
	}


}
