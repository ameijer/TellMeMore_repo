/*
 * File: TMMCard.java
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
 * The Class TMMCard. This abstract class must be extended by all types of cards used in this application. It contains the minimum amount of interaction required for a card to function in the application.
 */
public abstract class TMMCard implements Comparable<TMMCard>, Serializable{
	
	/** The Constant serialVersionUID. Used for serialization of this object. */
	private static final long serialVersionUID = 2159979972374818761L;
	
	/** The Constant TEXT. Used for the JSON parsing of cards. */
	public static final String TEXT = "TEXT";
	
	/** The Constant AUDIO. Used for the JSON parsing of cards. */
	public static final String AUDIO = "AUDIO";
	
	/** The Constant VIDEO. Used for the JSON parsing of cards.*/
	public static final String VIDEO = "VIDEO";
	
	/** The specific type of the card, required when recreating card objects from JSON in HTTP responses. Can take 1 of 3 values: TEXT, AUDIO, and VIDEO. */
	final private String jsontype;

	/** The Constant TAG. Used in the debug Logger*/
	public static final String TAG = "TMM" +", " + TMMCard.class.getSimpleName();

	/** The priority of the card. This allows the user to order the cards in the list used to select the card of interest. */
	private int priority;

	/** The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.*/
	private String title; 

	/** The database identifier of the card. */
	private String uuid;
	
	/** The source, A {@link Server} object with info about where this card came from */
	private Server source = new Server("NO SERVER ASSIGNED", "NO SERVER ASSIGNED", 0, 0); //initialized to a default placeholder object



	/**
	 * Instantiates a new TMM card if the UUID is known used when creating cards from DB query results.
	 *
	 * @param uuid The unique UUID of the card. This must be obtained from the DB that this card resides on. 
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param source Information about the {@link Server} that this audio card originated from.
	 * @param type The type of the card, either AUDIO, VIDEO, or TEXT
	 */
	public TMMCard(String uuid, int priority, String cardTitle, Server source, String type){
		this.uuid=uuid;
		this.jsontype = type;
		this.priority = priority;
		this.title = cardTitle;
		
		if(source != null){
			this.source = source;
		}
	}

	/**
	 * Instantiates a new TMM card, without setting the UUID of the card. Used when new card objects are created from scratch.
	 *
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param source Information about the {@link Server} that this audio card originated from.
	 * @param type The type of the card, either AUDIO, VIDEO, or TEXT
	 */
	public TMMCard(int priority, String cardTitle, Server source, String type){
		this.uuid= "";
		this.priority = priority;
		this.jsontype=type;
		this.title = cardTitle;
		
		if(source != null){
			this.source = source;
		}
		
	}


	/**
	 * Gets the uuid of the card. This can be used to directly reference the card in the DB.
	 *
	 * @return The String UUID of the card. 
	 */
	public String getuuId() {
		return uuid;
	}



	/**
	 * Sets the uuid of the card. This must be obtained from the DB that this card resides on. 
	 *
	 * @param id The new UUID of the card. 
	 */
	public void setuuId(String id) {
		this.uuid = id;
	}



	/**
	 * Gets the priority of the card.
	 *
	 * @return The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (postion 0)
	 */
	public int getPriority() {
		return priority;
	}



	/**
	 * Sets the priority. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (postion 0)
	 *
	 * @param priority The new priority of the card. 
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}



	/**
	 * Gets the card's title.
	 *
	 * @return The card's title.
	 */
	public String getTitle() {
		return title;
	}



	/**
	 * Sets the title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 *
	 * @param title The card's new title.
	 */
	public void setTitle(String title) {
		this.title = title;
	}



	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(TMMCard arg0) {
		if(priority > arg0.getPriority()){
			return 1;
		} else if(priority < arg0.getPriority()){
			return -1;
		} else return 0;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return "TMM Card: " + title + " with DB id: " + uuid;
	}

	/**
	 * Gets the source of this card as a {@link Server} object. This object contains information about the server that this audio card originated from.
	 *
	 * @return A {@link Server} object containing information about the source of this card.  
	 */
	
	public Server getSource() {
		return source;
	}

	/**
	 * Sets the source object. This object contains information about the server that this audio card originated from.
	 *
	 * @param source The new {@link Server} object to set as the card's source. 
	 */
	public void setSource(Server source) {
		this.source = source;
	}

	/**
	 * Gets the JSON type of this card. Used in the translation of this card to/from JSON. 
	 *
	 * @return THe type of this card - either TEXT, AUDIO, or VIDEO.
	 */
	public String getJsontype() {
		return jsontype;
	}



}
