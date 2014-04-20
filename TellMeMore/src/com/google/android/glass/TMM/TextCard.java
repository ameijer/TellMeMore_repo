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

/**
 * The Class TextCard. This card contains information representing a single card
 * in the TellMeMore app. This card is used to present context-relevant
 * textual/image information to the user, usually related to what they are
 * learning about at that time.
 * 
 * @author atm011
 * @version 1.0
 */
public class TextCard extends TMMCard implements Serializable {

	/** The Constant serialVersionUID. Used for serialization of this object. */
	private static final long serialVersionUID = -4719812627361504202L;

	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" + ", "
			+ TextCard.class.getSimpleName();

	/** The path to the icon image file for this text card, if there is one. */
	private String icon_path;

	/** The name of the icon image file, if there is one for this textcard. */
	private String ic_filename;

	/**
	 * The lines of text used in the TextCard when it is presented to the user
	 * in the {@link SelectCardActivity}. Usually these are used for summaries
	 * or subtitles.
	 */
	private String line1, line2, line3;

	/**
	 * The arraylist of {@link TextElement} objects that comprise the content of
	 * this textcard.
	 */
	private ArrayList<TextElement> contents;

	/**
	 * Instantiates a new text card, with an icon but without any content.
	 * 
	 * @param priority
	 *            The priority of the card. This is used to order the cards in
	 *            the {@link SelectCardActivity}, with higher priority cards
	 *            located at the beginning of the sequence (position 0)
	 * @param cardTitle
	 *            The card's title. This is displayed prominently and is usually
	 *            the largest text on the card. For best visibility, this should
	 *            be less than 20 characters long.
	 * @param ic_path
	 *            The path to the icon image file to use for this card.
	 * @param source
	 *            Information about the {@link Server} that this card originated
	 *            from.
	 */
	public TextCard(int priority, String cardTitle, String ic_path,
			Server source) {
		super(priority, cardTitle, source, TMMCard.TEXT);
		this.icon_path = ic_path;

		// set icon file name automatically, for consistency
		int charToWipe = ic_path.lastIndexOf('/');
		ic_filename = ic_path.substring(charToWipe + 1);
	}

	/**
	 * Instantiates a new text card. This is the minimal constructor for this
	 * card requiring.
	 * 
	 * @param priority
	 *            The priority of the card. This is used to order the cards in
	 *            the {@link SelectCardActivity}, with higher priority cards
	 *            located at the beginning of the sequence (position 0)
	 * @param cardTitle
	 *            The card's title. This is displayed prominently and is usually
	 *            the largest text on the card. For best visibility, this should
	 *            be less than 20 characters long.
	 * @param source
	 *            Information about the {@link Server} that this card originated
	 *            from.
	 */
	public TextCard(int priority, String cardTitle, Server source) {
		super(priority, cardTitle, source, TMMCard.TEXT);
		this.icon_path = null;
	}

	/**
	 * Instantiates a new text card with an ID obtained for the database.
	 * 
	 * @param id
	 *            The unique UUID of the card. This must be obtained from the DB
	 *            that the card resides on.
	 * @param priority
	 *            The priority of the card. This is used to order the cards in
	 *            the {@link SelectCardActivity}, with higher priority cards
	 *            located at the beginning of the sequence (position 0)
	 * @param cardTitle
	 *            The card's title. This is displayed prominently and is usually
	 *            the largest text on the card. For best visibility, this should
	 *            be less than 20 characters long.
	 * @param source
	 *            Information about the {@link Server} that this card originated
	 *            from.
	 */
	public TextCard(String id, int priority, String cardTitle, Server source) {
		super(id, priority, cardTitle, source, TMMCard.TEXT);
		this.icon_path = null;

	}

	/**
	 * Instantiates a new text card with no icon or content.
	 * 
	 * @param id
	 *            The unique UUID of the card. This must be obtained from the DB
	 *            that the card resides on.
	 * @param priority
	 *            The priority of the card. This is used to order the cards in
	 *            the {@link SelectCardActivity}, with higher priority cards
	 *            located at the beginning of the sequence (position 0)
	 * @param cardTitle
	 *            The card's title. This is displayed prominently and is usually
	 *            the largest text on the card. For best visibility, this should
	 *            be less than 20 characters long.
	 * @param line1
	 *            The top subtitle/summary line of text.
	 * @param line2
	 *            The middle subtitle/summary line of text.
	 * @param line3
	 *            The bottom subtitle/summary line of text.
	 * @param source
	 *            Information about the {@link Server} that this card originated
	 *            from.
	 */
	public TextCard(String id, int priority, String cardTitle, String line1,
			String line2, String line3, Server source) {
		super(id, priority, cardTitle, source, TMMCard.TEXT);
		this.icon_path = null;
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;

	}

	/**
	 * Instantiates a new text card.
	 * 
	 * @param id
	 *            The unique UUID of the card. This must be obtained from the DB
	 *            that the card resides on.
	 * @param priority
	 *            The priority of the card. This is used to order the cards in
	 *            the {@link SelectCardActivity}, with higher priority cards
	 *            located at the beginning of the sequence (position 0)
	 * @param cardTitle
	 *            The card's title. This is displayed prominently and is usually
	 *            the largest text on the card. For best visibility, this should
	 *            be less than 20 characters long.
	 * @param line1
	 *            The top subtitle/summary line of text.
	 * @param line2
	 *            The middle subtitle/summary line of text.
	 * @param line3
	 *            The bottom subtitle/summary line of text.
	 * @param ic_path
	 *            The path to the icon image file to use for this card.
	 * @param source
	 *            Information about the {@link Server} that this card originated
	 *            from.
	 */
	public TextCard(String id, int priority, String cardTitle, String line1,
			String line2, String line3, String ic_path, Server source) {
		super(id, priority, cardTitle, source, TMMCard.TEXT);
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.icon_path = ic_path;

		// set icon file name automatically, for consistency
		int charToWipe = ic_path.lastIndexOf('/');
		ic_filename = ic_path.substring(charToWipe + 1);

	}

	/**
	 * Instantiates a new text card.
	 * 
	 * @param id
	 *            The unique UUID of the card. This must be obtained from the DB
	 *            that the card resides on.
	 * @param priority
	 *            The priority of the card. This is used to order the cards in
	 *            the {@link SelectCardActivity}, with higher priority cards
	 *            located at the beginning of the sequence (position 0)
	 * @param cardTitle
	 *            The card's title. This is displayed prominently and is usually
	 *            the largest text on the card. For best visibility, this should
	 *            be less than 20 characters long.
	 * @param line1
	 *            The top subtitle/summary line of text.
	 * @param line2
	 *            The middle subtitle/summary line of text.
	 * @param line3
	 *            The bottom subtitle/summary line of text.
	 * @param content
	 *            The ArrayList of {@link TextElement} objects that contain the
	 *            textual/visual content of the card.
	 * @param source
	 *            Information about the {@link Server} that this card originated
	 *            from.
	 */
	public TextCard(String id, int priority, String cardTitle, String line1,
			String line2, String line3, ArrayList<TextElement> content,
			Server source) {
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
	 * @param priority
	 *            The priority of the card. This is used to order the cards in
	 *            the {@link SelectCardActivity}, with higher priority cards
	 *            located at the beginning of the sequence (position 0)
	 * @param cardTitle
	 *            The card's title. This is displayed prominently and is usually
	 *            the largest text on the card. For best visibility, this should
	 *            be less than 20 characters long.
	 * @param line1
	 *            The top subtitle/summary line of text.
	 * @param line2
	 *            The middle subtitle/summary line of text.
	 * @param line3
	 *            The bottom subtitle/summary line of text.
	 * @param content
	 *            The ArrayList of {@link TextElement} objects that contain the
	 *            textual/visual content of the card.
	 * @param source
	 *            Information about the {@link Server} that this card originated
	 *            from.
	 */
	public TextCard(int priority, String cardTitle, String line1, String line2,
			String line3, ArrayList<TextElement> content, Server source) {
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
	 * @param priority
	 *            The priority of the card. This is used to order the cards in
	 *            the {@link SelectCardActivity}, with higher priority cards
	 *            located at the beginning of the sequence (position 0)
	 * @param cardTitle
	 *            The card's title. This is displayed prominently and is usually
	 *            the largest text on the card. For best visibility, this should
	 *            be less than 20 characters long.
	 * @param line1
	 *            The top subtitle/summary line of text.
	 * @param line2
	 *            The middle subtitle/summary line of text.
	 * @param line3
	 *            The bottom subtitle/summary line of text.
	 * @param ic_path
	 *            The path to the icon image file to use for this card.
	 * @param content
	 *            The ArrayList of {@link TextElement} objects that contain the
	 *            textual/visual content of the card.
	 * @param source
	 *            Information about the {@link Server} that this card originated
	 *            from.
	 */
	public TextCard(int priority, String cardTitle, String line1, String line2,
			String line3, String ic_path, ArrayList<TextElement> content,
			Server source) {
		super(priority, cardTitle, source, TMMCard.TEXT);
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.icon_path = ic_path;

		// set icon file name automatically, for consistency
		int charToWipe = ic_path.lastIndexOf('/');
		ic_filename = ic_path.substring(charToWipe + 1);
		this.setContents(content);

	}

	/**
	 * Instantiates a new text card with the most amount of information
	 * contained in it upon creation.
	 * 
	 * @param id
	 *            The unique UUID of the card. This must be obtained from the DB
	 *            that the card resides on.
	 * @param priority
	 *            The priority of the card. This is used to order the cards in
	 *            the {@link SelectCardActivity}, with higher priority cards
	 *            located at the beginning of the sequence (position 0)
	 * @param cardTitle
	 *            The card's title. This is displayed prominently and is usually
	 *            the largest text on the card. For best visibility, this should
	 *            be less than 20 characters long.
	 * @param line1
	 *            The top subtitle/summary line of text.
	 * @param line2
	 *            The middle subtitle/summary line of text.
	 * @param line3
	 *            The bottom subtitle/summary line of text.
	 * @param ic_path
	 *            The path to the icon image file to use for this card.
	 * @param content
	 *            The ArrayList of {@link TextElement} objects that contain the
	 *            textual/visual content of the card.
	 * @param source
	 *            Information about the {@link Server} that this card originated
	 *            from.
	 */
	public TextCard(String id, int priority, String cardTitle, String line1,
			String line2, String line3, String ic_path,
			ArrayList<TextElement> content, Server source) {
		super(id, priority, cardTitle, source, TMMCard.TEXT);
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.icon_path = ic_path;

		// set icon file name automatically, for consistency
		int charToWipe = ic_path.lastIndexOf('/');
		ic_filename = ic_path.substring(charToWipe + 1);

		this.setContents(content);

	}

	/**
	 * Gets the path to the icon image file of the card.
	 * 
	 * @return The path to the icon image file of the card, including filename.
	 */
	public String getIconPath() {
		return icon_path;
	}

	/**
	 * Sets the path to the icon image file of the card.
	 * 
	 * @param ic_path
	 *            The path to the new icon image to use for this file, including
	 *            the filename.
	 */
	public void setIconPath(String ic_path) {
		this.icon_path = ic_path;

		// set icon file name automatically, for consistency
		int charToWipe = ic_path.lastIndexOf('/');
		ic_filename = ic_path.substring(charToWipe + 1);
	}

	/**
	 * Gets the topmost summary/subtitle line of text of the card.
	 * 
	 * @return The contents of the topmost line of the card.
	 */
	public String getLine1() {
		return line1;
	}

	/**
	 * Sets the topmost summary/subtitle line of text of the card.
	 * 
	 * @param line1
	 *            The new contents of the topmost line of the card to set.
	 */
	public void setLine1(String line1) {
		this.line1 = line1;
	}

	/**
	 * Gets the middle summary/subtitle line of text of the card.
	 * 
	 * @return The contents of the middle line of the card.
	 */
	public String getLine2() {
		return line2;
	}

	/**
	 * Sets the middle summary/subtitle line of text of the card..
	 * 
	 * @param line2
	 *            The new contents of the middle line of the card to set.
	 */
	public void setLine2(String line2) {
		this.line2 = line2;
	}

	/**
	 * Gets the bottom summary/subtitle line of text of the card.
	 * 
	 * @return The contents of the bottom line of the card.
	 */
	public String getLine3() {
		return line3;
	}

	/**
	 * Sets the bottom summary/subtitle line of text of the card.
	 * 
	 * @param line3
	 *            The new contents of the bottom line of the card to set.
	 */
	public void setLine3(String line3) {
		this.line3 = line3;
	}

	/**
	 * Gets the TextElements that consist of the content of the card.
	 * 
	 * @return The ArrayList of TextElements which are the contents of this
	 *         card.
	 */
	public ArrayList<TextElement> getContents() {
		return contents;
	}

	/**
	 * Sets the contents of this card to the Arraylist of TextElements
	 * specified.
	 * 
	 * @param contents
	 *            The new arraylist of contents to set as the card's contents.
	 */
	public void setContents(ArrayList<TextElement> contents) {
		this.contents = contents;
	}

	/**
	 * Checks for the existence of an icon image file.
	 * 
	 * @return true, if there is a non-null icon image file path.
	 */
	public boolean hasIcon() {
		if (icon_path == null || icon_path.equalsIgnoreCase("")
				|| icon_path.equalsIgnoreCase("null")) {
			return false;
		} else
			return true;
	}

	/**
	 * Gets the filename of the icon image file. Note that this matches the last
	 * part of the icon image file path.
	 * 
	 * @return The name of the icon image file.
	 */
	public String getIcFileName() {
		return ic_filename;
	}

}
