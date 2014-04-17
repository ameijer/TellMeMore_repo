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
 * This card is used to present context-relevant audio information to the user, usually realted to what they 
 * are learning about at that time. 
 *
 * @author atm011
 * @version 1.0
 */
public class AudioCard extends TMMCard implements Serializable{
	
	/** The Constant serialVersionUID. Used for serialization of this object. */
	private static final long serialVersionUID = 367690893029671570L;
	
	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" +", " + AudioCard.class.getSimpleName();

	/** The length of clip in milliseconds. Could be displayed to the user at a later date. */
	private int lengthMillis;

	/** The number of times this clip has been played. Could be retained by system for later use/analysis*/
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
	 * @param path The path to the audio file to play if the user chooses to interact with this card. The supported file formats are those supported by Android's class {@link MediaPlayer}
	 * @param source Information about the {@link Server} that this audio card originated from.
	 */
	public AudioCard(int priority, String cardTitle, String path, Server source) {
		super(priority, cardTitle, source, TMMCard.AUDIO);
		
		this.background_path = null;
		this.audioClipPath = path;
		
		//set the clip filename automatically, to keep the name + path consistent
		int charToWipe = audioClipPath.lastIndexOf('/');
		setAudioClipName(audioClipPath.substring(charToWipe + 1));
	}
	
	/**
	 * Instantiates a new audio card without a specific audio source file.
	 *
	 * @param id The unique UUID of the card. This must be obtained from the DB that this card resides on. 
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param length The length of the audio clip in milliseconds. Could be used for the UI.  
	 * @param backgroundPath The path of an image to use as the background to the audio player. If no background is specified the system defaults to a black background. 
	 * @param source Information about the {@link Server} that this audio card originated from.
	 */
	public AudioCard(String id, int priority, String cardTitle, int length, String backgroundPath, Server source) {
		super(id, priority, cardTitle, source,  TMMCard.AUDIO);
		this.lengthMillis = length;
		this.background_path = backgroundPath;
		this.audioClipPath = null;
		
		//set the background filename automatically, to keep the name + path consistent
		int charToWipe = background_path.lastIndexOf('/');
		setBackground_name(background_path.substring(charToWipe + 1));
	}
	
	/**
	 * Instantiates a new audio card without a specific audio source file or a length of that file. 
	 *
	 * @param id The unique UUID of the card. This must be obtained from the DB that this card resides on. 
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param backgroundPath The path of an image to use as the background to the audio player. If no background is specified the system defaults to a black background.
	 * @param source Information about the {@link Server} that this audio card originated from.
	 */
	public AudioCard(String id, int priority, String cardTitle, String backgroundPath, Server source) {
		super(id, priority, cardTitle, source, TMMCard.AUDIO);
		this.background_path = backgroundPath;
		this.audioClipPath = null;
		
		//set the background filename automatically, to keep the name + path consistent
		int charToWipe = background_path.lastIndexOf('/');
		setBackground_name(background_path.substring(charToWipe + 1));
	}
	
	/**
	 * Instantiates a new audio card using the most commonly available attributes.
	 *
	 * @param id The unique UUID of the card. This must be obtained from the DB that this card resides on. 
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param length The length of the audio clip in milliseconds. Could be used for the UI. 
	 * @param backgroundPath The path of an image to use as the background to the audio player. If no background is specified the system defaults to a black background.
	 * @param contentPath The path to the audio file to play if the user chooses to interact with this card. The supported file formats are those supported by Android's class {@link MediaPlayer}
	 * @param source Information about the {@link Server} that this audio card originated from.
	 */
	public AudioCard(String id, int priority, String cardTitle, int length, String backgroundPath, String contentPath, Server source) {
		super(id, priority, cardTitle, source, TMMCard.AUDIO);
		this.lengthMillis = length;
		this.background_path = backgroundPath;
		this.audioClipPath = contentPath;
		
		//set filenames automatically from pathnames, to maintain consistency
		int charToWipe = audioClipPath.lastIndexOf('/');
		setAudioClipName(audioClipPath.substring(charToWipe + 1));
		charToWipe = background_path.lastIndexOf('/');
		setBackground_name(background_path.substring(charToWipe + 1));
	}
	
	/**
	 * Instantiates a new audio card.
	 *
	 * @param id The unique UUID of the card. This must be obtained from the DB that this card resides on. 
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param backgroundPath The path of an image to use as the background to the audio player. If no background is specified the system defaults to a black background.
	 * @param contentPath The path to the audio file to play if the user chooses to interact with this card. The supported file formats are those supported by Android's class {@link MediaPlayer}
	 * @param source Information about the {@link Server} that this audio card originated from.
	 */
	public AudioCard(String id, int priority, String cardTitle, String backgroundPath, String contentPath, Server source) {
		super(id, priority, cardTitle, source, TMMCard.AUDIO);
		this.background_path = backgroundPath;
		this.audioClipPath = contentPath;
		
		//set filenames automatically from pathnames, to maintain consistency
		int charToWipe = audioClipPath.lastIndexOf('/');
		setAudioClipName(audioClipPath.substring(charToWipe + 1));
		charToWipe = background_path.lastIndexOf('/');
		setBackground_name(background_path.substring(charToWipe + 1));
	}

	/**
	 * Gets the number of times that this clip has been played. 
	 *
	 * @return The number of times that this clip has been played, as referenced by this card object.
	 */
	public int getNumPlays() {
		return numPlays;
	}

	/**
	 * Sets the number of times that this clip has been played for this Audiocard object. 
	 *
	 * @param numPlays The new number of times that this clip has been played. 
	 */
	public void setNumPlays(int numPlays) {
		this.numPlays = numPlays;
	}

	/**
	 * Gets the length of the audio clip in milliseconds. Could be used for the UI. 
	 *
	 * @return The length of the audio clip in milliseconds.
	 */
	public int getLengthMillis() {
		return lengthMillis;
	}
	
	/**
	 * Checks if this AudioCard contains a background image to be displayed to the user. 
	 *
	 * @return true, if there is a non-null background image to be displayed to the user. 
	 */
	public boolean hasBackground(){
		if(background_path == null || background_path.equalsIgnoreCase("")){
			return false;
		} else return true;
	}
	
	/**
	 * Checks if this AudioCard contains an audioclip that the user can listen to/interact with. 
	 *
	 * @return true, if there is a non-null audio clip contained by this card. 
	 */
	public boolean hasAudio(){
		if(audioClipPath == null || audioClipPath.equalsIgnoreCase("")){
			return false;
		} else return true;
	}

	/**
	 * Sets the length of the audioclip contained in this card. 
	 *
	 * @param lengthMillis The new length of the clip (in milliseconds) to be set. 
	 */
	public void setLengthMillis(int lengthMillis) {
		this.lengthMillis = lengthMillis;
	}

	/**
	 * Gets the background image path, if it exists.
	 *
	 * @return A string contain the filesystem path to the background image if it exists. 
	 */
	public String getBackgroundPath() {
		return background_path;
	}

	/**
	 * Sets the path to the background image for this card. 
	 *
	 * @param backgroundPath the new background path
	 */
	public void setBackgroundPath(String backgroundPath) {
		this.background_path = backgroundPath;
		
		//set the background file name automatically from the path - this will keep things consistent
		int charToWipe = background_path.lastIndexOf('/');
		setBackground_name(background_path.substring(charToWipe + 1));
	}

	/**
	 * Gets the audio clip path of the target file to play.
	 *
	 * @return The path to the audio file to play if the user chooses to interact with this card. The supported file formats are those supported by Android's class {@link MediaPlayer}
	 */
	public String getAudioClipPath() {
		return audioClipPath;
	}

	/**
	 * Sets the path to the audio file to play when the user interacts with this card. 
	 *
	 * @param audioClipPath The path of the new audio file to associate with this card. 
	 */
	public void setAudioClip(String audioClipPath) {
		this.audioClipPath = audioClipPath;
		
		//set the audio clip file name automatically from the path - this will keep things consistent
		int charToWipe = audioClipPath.lastIndexOf('/');
		setAudioClipName(audioClipPath.substring(charToWipe + 1));
	}

	/**
	 * Gets the name of the audio file to play when the user interacts with this card. 
	 *
	 * @return The path of the audio file associated with this card. 
	 */
	public String getAudioClipName() {
		return audioClipName;
	}

	/**
	 * Sets the audio clip name. Users should not call this method directly to maintain consistency between the audio clip path and its filename. 
	 *
	 * @param audioClipName The name of the audio clip to set.
	 */
	private void setAudioClipName(String audioClipName) {
		this.audioClipName = audioClipName;
	}

	/**
	 * Gets the name of the image file used for the background of the audio player.
	 *
	 * @return The name of the background image file. 
	 */
	public String getBackground_name() {
		return background_name;
	}

	/**
	 * Sets the background_name. Users should not call this method directly to maintain consistency between the background image path and its filename. 
	 *
	 * @param background_name The new name of the background image file to set.
	 */
	private void setBackground_name(String background_name) {
		this.background_name = background_name;
	}


}
