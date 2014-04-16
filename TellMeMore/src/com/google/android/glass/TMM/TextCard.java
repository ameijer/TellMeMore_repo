/*
 * File: TextCard.java
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
import java.util.ArrayList;


// TODO: Auto-generated Javadoc
/**
 * The Class TextCard.
 */
public class TextCard extends TMMCard implements Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4719812627361504202L;
	
	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" +", " + TextCard.class.getSimpleName();
	
	/** The ic_filename. */
	private String icon_path, ic_filename;
	
	/** The line3. */
	private String line1, line2, line3;
	
	/** The contents. */
	private ArrayList<TextElement> contents;

	/**
	 * Instantiates a new text card.
	 *
	 * @param handle the handle
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param ic_path the ic_path
	 * @param source the source
	 */
	public TextCard(int priority, String cardTitle, String ic_path, Server source) {
		super(priority, cardTitle, source, TMMCard.TEXT);
		this.icon_path = ic_path;
		int charToWipe = ic_path.lastIndexOf('/');
		ic_filename = ic_path.substring(charToWipe + 1);
	}
	
	/**
	 * Instantiates a new text card.
	 *
	 * @param handle the handle
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param source the source
	 */
	public TextCard(int priority, String cardTitle, Server source) {
		super(priority, cardTitle, source, TMMCard.TEXT);
		this.icon_path = null;
	}
	
	/**
	 * Instantiates a new text card.
	 *
	 * @param handle the handle
	 * @param id the id
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param source the source
	 */
	public TextCard(String id, int priority, String cardTitle, Server source) {
		super(id, priority, cardTitle, source, TMMCard.TEXT);
		this.icon_path = null;
		
	}
	
	/**
	 * Instantiates a new text card.
	 *
	 * @param handle the handle
	 * @param id the id
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param line1 the line1
	 * @param line2 the line2
	 * @param line3 the line3
	 * @param source the source
	 */
	public TextCard(String id, int priority, String cardTitle, String line1, String line2, String line3, Server source) {
		super(id, priority, cardTitle, source, TMMCard.TEXT);
		this.icon_path = null;
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		
	}
	
	/**
	 * Instantiates a new text card.
	 *
	 * @param handle the handle
	 * @param id the id
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param line1 the line1
	 * @param line2 the line2
	 * @param line3 the line3
	 * @param ic_path the ic_path
	 * @param source the source
	 */
	public TextCard(String id, int priority, String cardTitle, String line1, String line2, String line3, String ic_path, Server source) {
		super(id, priority, cardTitle, source, TMMCard.TEXT);
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.icon_path = ic_path;
		int charToWipe = ic_path.lastIndexOf('/');
		ic_filename = ic_path.substring(charToWipe + 1);
		
	}
	
	/**
	 * Instantiates a new text card.
	 *
	 * @param handle the handle
	 * @param id the id
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param line1 the line1
	 * @param line2 the line2
	 * @param line3 the line3
	 * @param content the content
	 * @param source the source
	 */
	public TextCard(String id, int priority, String cardTitle, String line1, String line2, String line3, ArrayList<TextElement> content, Server source) {
		super(id, priority, cardTitle, source, TMMCard.TEXT);
		this.icon_path = null;
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.setContents(content);
		
	}
	
	/**
	 * Instantiates a new text card.
	 *
	 * @param handle the handle
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param line1 the line1
	 * @param line2 the line2
	 * @param line3 the line3
	 * @param content the content
	 * @param source the source
	 */
	public TextCard(int priority, String cardTitle, String line1, String line2, String line3, ArrayList<TextElement> content, Server source) {
		super(priority, cardTitle, source, TMMCard.TEXT);
		this.icon_path = null;
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.setContents(content);
		
	}
	
	/**
	 * Instantiates a new text card.
	 *
	 * @param handle the handle
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param line1 the line1
	 * @param line2 the line2
	 * @param line3 the line3
	 * @param ic_path the ic_path
	 * @param content the content
	 * @param source the source
	 */
	public TextCard(int priority, String cardTitle, String line1, String line2, String line3, String ic_path, ArrayList<TextElement> content, Server source) {
		super(priority, cardTitle, source, TMMCard.TEXT);
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.icon_path = ic_path;
		int charToWipe = ic_path.lastIndexOf('/');
		ic_filename = ic_path.substring(charToWipe + 1);
		this.setContents(content);
		
	}
	
	/**
	 * Instantiates a new text card.
	 *
	 * @param handle the handle
	 * @param id the id
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param line1 the line1
	 * @param line2 the line2
	 * @param line3 the line3
	 * @param ic_path the ic_path
	 * @param content the content
	 * @param source the source
	 */
	public TextCard(String id, int priority, String cardTitle, String line1, String line2, String line3, String ic_path, ArrayList<TextElement> content, Server source) {
		super(id, priority, cardTitle, source, TMMCard.TEXT);
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.icon_path = ic_path;
		int charToWipe = ic_path.lastIndexOf('/');
		ic_filename = ic_path.substring(charToWipe + 1);
		this.setContents(content);
		
	}
	
	/**
	 * Gets the icon path.
	 *
	 * @return the icon path
	 */
	public String getIconPath() {
		return icon_path;
	}

	/**
	 * Sets the icon path.
	 *
	 * @param ic_path the new icon path
	 */
	public void setIconPath(String ic_path) {
		this.icon_path = ic_path;
		int charToWipe = ic_path.lastIndexOf('/');
		ic_filename = ic_path.substring(charToWipe + 1);
	}

	/**
	 * Gets the line1.
	 *
	 * @return the line1
	 */
	public String getLine1() {
		return line1;
	}

	/**
	 * Sets the line1.
	 *
	 * @param line1 the new line1
	 */
	public void setLine1(String line1) {
		this.line1 = line1;
	}

	/**
	 * Gets the line2.
	 *
	 * @return the line2
	 */
	public String getLine2() {
		return line2;
	}

	/**
	 * Sets the line2.
	 *
	 * @param line2 the new line2
	 */
	public void setLine2(String line2) {
		this.line2 = line2;
	}

	/**
	 * Gets the line3.
	 *
	 * @return the line3
	 */
	public String getLine3() {
		return line3;
	}

	/**
	 * Sets the line3.
	 *
	 * @param line3 the new line3
	 */
	public void setLine3(String line3) {
		this.line3 = line3;
	}

	/**
	 * Gets the contents.
	 *
	 * @return the contents
	 */
	public ArrayList<TextElement> getContents() {
		return contents;
	}

	/**
	 * Sets the contents.
	 *
	 * @param contents the new contents
	 */
	public void setContents(ArrayList<TextElement> contents) {
		this.contents = contents;
	}
	
	/**
	 * Checks for icon.
	 *
	 * @return true, if successful
	 */
	public boolean hasIcon(){
		if(icon_path == null || icon_path.equalsIgnoreCase("")){
			return false;
		} else return true;
	}
	
	/**
	 * Gets the ic file name.
	 *
	 * @return the ic file name
	 */
	public String getIcFileName(){
		return ic_filename;
	}
	
}
