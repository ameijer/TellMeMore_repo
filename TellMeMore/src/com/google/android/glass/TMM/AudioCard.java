/*
 * File: AudioCard.java
 * Date: Apr 16, 2014
 * Authors: A. Meijer (atm011) and D. Prudente (dcp017)
 * 
 * Written for ELEC429, Independent Study
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

import java.io.Serializable;


/**
 * The Class AudioCard. This card contains information representing a single card in the TellMeMore app. 
 * This card is used to present context-relevant audio information to the user
 *
 * @author atm011
 */
public class AudioCard extends TMMCard implements Serializable{
	
	/** The Constant serialVersionUID. Used for serialization of this object. */
	private static final long serialVersionUID = 367690893029671570L;
	
	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" +", " + AudioCard.class.getSimpleName();

	/** length of clip in milliseconds. */
	private int lengthMillis;

	/**number of times this clip has been played. */
	private int numPlays;
	
	/** Information about the image to serve as the background */
	private String background_path, background_name;
	
	/** The audio clip name and path information. */
	private String audioClipPath, audioClipName;
	


	/**
	 * Instantiates a new audio card. This is the 'minimal' constructor for this class, requiring the least amount of information.
	 *
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param path the path
	 * @param source the source
	 */
	public AudioCard(int priority, String cardTitle, String path, Server source) {
		super(priority, cardTitle, source, TMMCard.AUDIO);
		
		this.background_path = null;
		this.audioClipPath = path;
		int charToWipe = audioClipPath.lastIndexOf('/');
		setAudioClipName(audioClipPath.substring(charToWipe + 1));
	}
	
	/**
	 * Instantiates a new audio card.
	 *
	 * @param handle the handle
	 * @param id the id
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param length the length
	 * @param backgroundPath the background path
	 * @param source the source
	 */
	public AudioCard(String id, int priority, String cardTitle, int length, String backgroundPath, Server source) {
		super(id, priority, cardTitle, source,  TMMCard.AUDIO);
		this.lengthMillis = length;
		this.background_path = backgroundPath;
		this.audioClipPath = null;
		int charToWipe = background_path.lastIndexOf('/');
		setBackground_name(background_path.substring(charToWipe + 1));
	}
	
	/**
	 * Instantiates a new audio card.
	 *
	 * @param handle the handle
	 * @param id the id
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param backgroundPath the background path
	 * @param source the source
	 */
	public AudioCard(String id, int priority, String cardTitle, String backgroundPath, Server source) {
		super(id, priority, cardTitle, source, TMMCard.AUDIO);
		this.background_path = backgroundPath;
		this.audioClipPath = null;
		int charToWipe = background_path.lastIndexOf('/');
		setBackground_name(background_path.substring(charToWipe + 1));
	}
	
	/**
	 * Instantiates a new audio card.
	 *
	 * @param handle the handle
	 * @param id the id
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param length the length
	 * @param backgroundPath the background path
	 * @param contentPath the content path
	 * @param source the source
	 */
	public AudioCard(String id, int priority, String cardTitle, int length, String backgroundPath, String contentPath, Server source) {
		super(id, priority, cardTitle, source, TMMCard.AUDIO);
		this.lengthMillis = length;
		this.background_path = backgroundPath;
		this.audioClipPath = contentPath;
		int charToWipe = audioClipPath.lastIndexOf('/');
		setAudioClipName(audioClipPath.substring(charToWipe + 1));
		charToWipe = background_path.lastIndexOf('/');
		setBackground_name(background_path.substring(charToWipe + 1));
	}
	
	/**
	 * Instantiates a new audio card.
	 *
	 * @param handle the handle
	 * @param id the id
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param backgroundPath the background path
	 * @param contentPath the content path
	 * @param source the source
	 */
	public AudioCard(String id, int priority, String cardTitle, String backgroundPath, String contentPath, Server source) {
		super(id, priority, cardTitle, source, TMMCard.AUDIO);
		this.background_path = backgroundPath;
		this.audioClipPath = contentPath;
		int charToWipe = audioClipPath.lastIndexOf('/');
		setAudioClipName(audioClipPath.substring(charToWipe + 1));
		charToWipe = background_path.lastIndexOf('/');
		setBackground_name(background_path.substring(charToWipe + 1));
	}

	/**
	 * Gets the num plays.
	 *
	 * @return the num plays
	 */
	public int getNumPlays() {
		return numPlays;
	}

	/**
	 * Sets the num plays.
	 *
	 * @param numPlays the new num plays
	 */
	public void setNumPlays(int numPlays) {
		this.numPlays = numPlays;
	}

	/**
	 * Gets the length millis.
	 *
	 * @return the length millis
	 */
	public int getLengthMillis() {
		return lengthMillis;
	}
	
	/**
	 * Checks for background.
	 *
	 * @return true, if successful
	 */
	public boolean hasBackground(){
		if(background_path == null || background_path.equalsIgnoreCase("")){
			return false;
		} else return true;
	}
	
	/**
	 * Checks for audio.
	 *
	 * @return true, if successful
	 */
	public boolean hasAudio(){
		if(audioClipPath == null || audioClipPath.equalsIgnoreCase("")){
			return false;
		} else return true;
	}

	/**
	 * Sets the length millis.
	 *
	 * @param lengthMillis the new length millis
	 */
	public void setLengthMillis(int lengthMillis) {
		this.lengthMillis = lengthMillis;
	}

	/**
	 * Gets the background path.
	 *
	 * @return the background path
	 */
	public String getBackgroundPath() {
		return background_path;
	}

	/**
	 * Sets the background path.
	 *
	 * @param backgroundPath the new background path
	 */
	public void setBackgroundPath(String backgroundPath) {
		this.background_path = backgroundPath;
		
		int charToWipe = background_path.lastIndexOf('/');
		setBackground_name(background_path.substring(charToWipe + 1));
	}

	/**
	 * Gets the audio clip path.
	 *
	 * @return the audio clip path
	 */
	public String getAudioClipPath() {
		return audioClipPath;
	}

	/**
	 * Sets the audio clip.
	 *
	 * @param audioClipPath the new audio clip
	 */
	public void setAudioClip(String audioClipPath) {
		this.audioClipPath = audioClipPath;
		int charToWipe = audioClipPath.lastIndexOf('/');
		setAudioClipName(audioClipPath.substring(charToWipe + 1));
	}

	/**
	 * Gets the audio clip name.
	 *
	 * @return the audio clip name
	 */
	public String getAudioClipName() {
		return audioClipName;
	}

	/**
	 * Sets the audio clip name.
	 *
	 * @param audioClipName the new audio clip name
	 */
	private void setAudioClipName(String audioClipName) {
		this.audioClipName = audioClipName;
	}

	/**
	 * Gets the background_name.
	 *
	 * @return the background_name
	 */
	public String getBackground_name() {
		return background_name;
	}

	/**
	 * Sets the background_name.
	 *
	 * @param background_name the new background_name
	 */
	private void setBackground_name(String background_name) {
		this.background_name = background_name;
	}


}
