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
package com.google.android.glass.TMM;

import java.io.Serializable;

// TODO: Auto-generated Javadoc
/**
 * The Class VideoCard.
 */
public class VideoCard extends TMMCard implements Serializable{

	/** The Constant serialVersionUID. Used for serialization of this object. */
	private static final long serialVersionUID = 6169847664604436124L;

	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" +", " + VideoCard.class.getSimpleName();

	/** The screnshotname. */
	private String screenshot_path, screnshotname;

	/** The play count. */
	private int playCount;
	
	/** The Y ttag. */
	private String YTtag; 



	/**
	 * Instantiates a new video card.
	 *
	 * @param handle the handle
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param source the source
	 */
	public VideoCard(int priority, String cardTitle, Server source) {
		super(priority, cardTitle, source, TMMCard.VIDEO);

	}

	/**
	 * Instantiates a new video card.
	 *
	 * @param handle the handle
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param ytId the yt id
	 * @param source the source
	 */
	public VideoCard(int priority, String cardTitle, String ytId, Server source) {
		super(priority, cardTitle, source, TMMCard.VIDEO);
		YTtag = ytId;

	}

	/**
	 * Instantiates a new video card.
	 *
	 * @param handle the handle
	 * @param id the id
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param ss_path the ss_path
	 * @param source the source
	 */
	public VideoCard(String id, int priority, String cardTitle, String ss_path, Server source) {
		super(id, priority, cardTitle, source, TMMCard.VIDEO);
		screenshot_path = ss_path;
		int charToWipe = screenshot_path.lastIndexOf('/');
		setScrenshotname(screenshot_path.substring(charToWipe + 1));

	}

	/**
	 * Instantiates a new video card.
	 *
	 * @param handle the handle
	 * @param id the id
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param ss_path the ss_path
	 * @param playcount the playcount
	 * @param source the source
	 */
	public VideoCard(String id, int priority, String cardTitle, String ss_path, int playcount, Server source) {
		super(id, priority, cardTitle, source, TMMCard.VIDEO);
		this.screenshot_path = ss_path;
		this.playCount = playcount;
		int charToWipe = screenshot_path.lastIndexOf('/');
		setScrenshotname(screenshot_path.substring(charToWipe + 1));

	}
	
	
	/**
	 * Instantiates a new video card.
	 *
	 * @param handle the handle
	 * @param id the id
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param ss_path the ss_path
	 * @param youTubeTag the you tube tag
	 * @param source the source
	 */
	public VideoCard(String id, int priority, String cardTitle, String ss_path, String youTubeTag, Server source) {
		super(id, priority, cardTitle, source, TMMCard.VIDEO);
		this.screenshot_path = ss_path;
		this.setYTtag(youTubeTag);
		int charToWipe = screenshot_path.lastIndexOf('/');
		setScrenshotname(screenshot_path.substring(charToWipe + 1));

	}

	/**
	 * Instantiates a new video card.
	 *
	 * @param handle the handle
	 * @param id the id
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param ss_path the ss_path
	 * @param playcount the playcount
	 * @param youTubeTag the you tube tag
	 * @param source the source
	 */
	public VideoCard(String id, int priority, String cardTitle, String ss_path, int playcount, String youTubeTag, Server source) {
		super(id, priority, cardTitle, source, TMMCard.VIDEO);
		screenshot_path = ss_path;
		this.playCount = playcount;
		this.setYTtag(youTubeTag);
		int charToWipe = screenshot_path.lastIndexOf('/');
		setScrenshotname(screenshot_path.substring(charToWipe + 1));

	}
	
	/**
	 * Instantiates a new video card.
	 *
	 * @param handle the handle
	 * @param id the id
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param playcount the playcount
	 * @param youTubeTag the you tube tag
	 * @param source the source
	 */
	public VideoCard(String id, int priority, String cardTitle, int playcount, String youTubeTag, Server source) {
		super(id, priority, cardTitle, source, TMMCard.VIDEO);
		this.playCount = playcount;
		this.setYTtag(youTubeTag);

	}
	

	/**
	 * Gets the screenshot path.
	 *
	 * @return the screenshot path
	 */
	public String getScreenshotPath() {
		return screenshot_path;
	}

	/**
	 * Sets the screenshot.
	 *
	 * @param screenshotPath the new screenshot
	 */
	public void setScreenshot(String screenshotPath) {
		this.screenshot_path = screenshotPath;
		int charToWipe = screenshot_path.lastIndexOf('/');
		setScrenshotname(screenshot_path.substring(charToWipe + 1));
	}

	/**
	 * Gets the play count.
	 *
	 * @return the play count
	 */
	public int getPlayCount() {
		return playCount;
	}

	/**
	 * Sets the play count.
	 *
	 * @param playCount the new play count
	 */
	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}

	/**
	 * Gets the y ttag.
	 *
	 * @return the y ttag
	 */
	public String getYTtag() {
		return YTtag;
	}

	/**
	 * Sets the y ttag.
	 *
	 * @param yTtag the new y ttag
	 */
	public void setYTtag(String yTtag) {
		YTtag = yTtag;
	}
	
	/**
	 * Checks for screenshot.
	 *
	 * @return true, if successful
	 */
	public boolean hasScreenshot(){
		if(screenshot_path == null || screenshot_path.equalsIgnoreCase("")){
			return false;
		} else return true;
	}

	/**
	 * Gets the screnshotname.
	 *
	 * @return the screnshotname
	 */
	public String getScrenshotname() {
		return screnshotname;
	}

	/**
	 * Sets the screnshotname.
	 *
	 * @param screnshotname the new screnshotname
	 */
	private void setScrenshotname(String screnshotname) {
		this.screnshotname = screnshotname;
	}


}
