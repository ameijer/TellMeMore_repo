/*
 * File: VideoCard.java
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
package com.example.dbwriter;

import java.io.Serializable;

/**
 * The Class VideoCard. This card contains information representing a single
 * card in the TellMeMore app. This card is used to present context-relevant
 * video information to the user, usually related to what they are learning
 * about at that time.
 * 
 * @author atm011
 * @version 1.0
 */
public class VideoCard extends TMMCard implements Serializable{

	/** The Constant serialVersionUID. Used for serialization of this object. */
	private static final long serialVersionUID = 6169847664604436124L;

	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" +", " + VideoCard.class.getSimpleName();

	/** The path of the image to be used as the video screenshot for this card. */
	private String screenshot_path;
	
	/** The name of the screenshot image associated with the VideoCard */
	private String screnshotname;

	/** The play count of the video. This could be retrieved from youtube, or could be a local counter. */
	private int playCount;
	
	/** The YouTube video tag serving as the source of the video in this card. */
	private String YTtag; 



	/**
	 * Instantiates a new video card, with no source video or screenshot.
	 *
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param source  Information about the {@link Server} that this audio card originated from.
	 */
	public VideoCard(int priority, String cardTitle, Server source) {
		super(priority, cardTitle, source, TMMCard.VIDEO);

	}

	/**
	 * Instantiates a new video card, with a specific video ID tag but no screenshot.
	 *
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param ytId The youtube ID tag of the video to play. Found at the end of the video URL.
	 * @param source Information about the {@link Server} that this audio card originated from.
	 */
	public VideoCard(int priority, String cardTitle, String ytId, Server source) {
		super(priority, cardTitle, source, TMMCard.VIDEO);
		YTtag = ytId;

	}

	/**
	 * Instantiates a new video card.
	 *
	 * @param id The unique UUID of the card. This must be obtained from the DB that this card resides on.
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param ss_path The path to the screenshot image for this video card. 
	 * @param source Information about the {@link Server} that this audio card originated from.
	 */
	public VideoCard(String id, int priority, String cardTitle, String ss_path, Server source) {
		super(id, priority, cardTitle, source, TMMCard.VIDEO);
		screenshot_path = ss_path;
		
		//set the screenshot name automatically from the path to maintain consistency between the two
		int charToWipe = screenshot_path.lastIndexOf('/');
		setScrenshotname(screenshot_path.substring(charToWipe + 1));

	}

	/**
	 * Instantiates a new video card.
	 *
	 * @param id The unique UUID of the card. This must be obtained from the DB that this card resides on.
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param ss_path The path to the screenshot image for this video card. 
	 * @param playcount The playcount of the video, obtained either via the API or an application-specific counter
	 * @param source Information about the {@link Server} that this audio card originated from.
	 */
	public VideoCard(String id, int priority, String cardTitle, String ss_path, int playcount, Server source) {
		super(id, priority, cardTitle, source, TMMCard.VIDEO);
		this.screenshot_path = ss_path;
		this.playCount = playcount;
		
		//set the screenshot name automatically from the path to maintain consistency between the two
		int charToWipe = screenshot_path.lastIndexOf('/');
		setScrenshotname(screenshot_path.substring(charToWipe + 1));

	}
	
	
	/**
	 * Instantiates a new video card.
	 *
	 * @param id The unique UUID of the card. This must be obtained from the DB that this card resides on.
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param ss_path The path to the screenshot image for this video card. 
	 * @param youTubeTag The youtube ID tag of the video to play. Found at the end of the video URL.
	 * @param source Information about the {@link Server} that this audio card originated from.
	 */
	public VideoCard(String id, int priority, String cardTitle, String ss_path, String youTubeTag, Server source) {
		super(id, priority, cardTitle, source, TMMCard.VIDEO);
		this.screenshot_path = ss_path;
		this.setYTtag(youTubeTag);
		
		//set the screenshot name automatically from the path to maintain consistency between the two
		int charToWipe = screenshot_path.lastIndexOf('/');
		setScrenshotname(screenshot_path.substring(charToWipe + 1));

	}

	/**
	 * Instantiates a new video card.
	 *
	 * @param id The unique UUID of the card. This must be obtained from the DB that this card resides on.
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param ss_path The path to the screenshot image for this video card. 
	 * @param playcount The playcount of the video, obtained either via the API or an application-specific counter
	 * @param youTubeTag The youtube ID tag of the video to play. Found at the end of the video URL.
	 * @param source Information about the {@link Server} that this audio card originated from.
	 */
	public VideoCard(String id, int priority, String cardTitle, String ss_path, int playcount, String youTubeTag, Server source) {
		super(id, priority, cardTitle, source, TMMCard.VIDEO);
		screenshot_path = ss_path;
		this.playCount = playcount;
		this.setYTtag(youTubeTag);
		
		//set the screenshot name automatically from the path to maintain consistency between the two
		int charToWipe = screenshot_path.lastIndexOf('/');
		setScrenshotname(screenshot_path.substring(charToWipe + 1));

	}
	
	/**
	 * Instantiates a new video card.
	 *
	 * @param id The unique UUID of the card. This must be obtained from the DB that this card resides on.
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param playcount The playcount of the video, obtained either via the API or an application-specific counter
	 * @param youTubeTag The youtube ID tag of the video to play. Found at the end of the video URL.
	 * @param source Information about the {@link Server} that this audio card originated from.
	 */
	public VideoCard(String id, int priority, String cardTitle, int playcount, String youTubeTag, Server source) {
		super(id, priority, cardTitle, source, TMMCard.VIDEO);
		this.playCount = playcount;
		this.setYTtag(youTubeTag);

	}
	

	/**
	 * Gets the path of the screenshot image for the VideoCard.
	 *
	 * @return The path of the screenshot image. 
	 */
	public String getScreenshotPath() {
		return screenshot_path;
	}

	/**
	 * Sets the path of the screenshot image for the card. 
	 *
	 * @param screenshotPath The path of the new screenshot image to set.
	 */
	public void setScreenshot(String screenshotPath) {
		this.screenshot_path = screenshotPath;
		
		//set the screenshot name automatically from the path to maintain consistency between the two
		int charToWipe = screenshot_path.lastIndexOf('/');
		setScrenshotname(screenshot_path.substring(charToWipe + 1));
	}

	/**
	 * Gets the play count of the video contained in the card.
	 *
	 * @return The number of times the video on this card has been played.
	 */
	public int getPlayCount() {
		return playCount;
	}

	/**
	 * Sets the play count of the video contained in the card.
	 *
	 * @param playCount The number to set the new play counter to. 
	 */
	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}

	/**
	 * Gets the youtube ID tag of the video to play.
	 *
	 * @return The youtube ID tag of the video to play.
	 */
	public String getYTtag() {
		return YTtag;
	}

	/**
	 * Sets the youtube ID tag of the video contained in the card. 
	 *
	 * @param yTtag The new Youtube tag to set for the VideoCard. 
	 */
	public void setYTtag(String yTtag) {
		YTtag = yTtag;
	}
	
	/**
	 * Checks for the existence of a screenshot contained within this card. 
	 *
	 * @return true, if there is a screenshot for the video contained in the card. 
	 */
	public boolean hasScreenshot(){
		if(screenshot_path == null || screenshot_path.equalsIgnoreCase("")){
			return false;
		} else return true;
	}

	/**
	 * Gets the filename of the screenshot image. 
	 *
	 * @return The name of the screenshot image file. 
	 */
	public String getScrenshotname() {
		return screnshotname;
	}

	/**
	 * Sets the filename of the screenshot image. Users should not access this method directly to maintain consistency between the pathname and the filename. 
	 *
	 * @param screnshotname The new name of the screenshot image file. 
	 */
	private void setScrenshotname(String screnshotname) {
		this.screnshotname = screnshotname;
	}


}
