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

// TODO: Auto-generated Javadoc
/**
 * The Class TMMCard.
 */
public abstract class TMMCard implements Comparable<TMMCard>, Serializable{
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2159979972374818761L;
	
	/** The Constant TEXT. */
	public static final String TEXT = "TEXT";
	
	/** The Constant AUDIO. */
	public static final String AUDIO = "AUDIO";
	
	/** The Constant VIDEO. */
	public static final String VIDEO = "VIDEO";
	
	/** The jsontype. */
	final private String jsontype;

	/** The Constant TAG. Used in the debug Logger*/
	public static final String TAG = "TMM" +", " + TMMCard.class.getSimpleName();

	//lets us sort the cards in the scroll view by priority
	/** The priority. */
	private int priority;

	/** The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.*/
	private String title; 

	//the DB id
	/** The uuid. */
	private String uuid;
	
	/** The source. */
	private Server source = new Server("NO SERVER ASSIGNED", "NO SERVER ASSIGNED", 0, 0);




	//only for use when creating card from DB
	/**
	 * Instantiates a new TMM card.
	 *
	 * @param uuid the uuid
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param source the source
	 * @param type the type
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

	//only for use when creating card from DB
	/**
	 * Instantiates a new TMM card.
	 *
	 * @param priority The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (position 0)
	 * @param cardTitle The card's title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 * @param source the source
	 * @param type the type
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
	 * Gets the uu id.
	 *
	 * @return the uu id
	 */
	public String getuuId() {
		return uuid;
	}



	/**
	 * Sets the uu id.
	 *
	 * @param id the new uu id
	 */
	public void setuuId(String id) {
		this.uuid = id;
	}



	/**
	 * Gets the priority.
	 *
	 * @return The priority of the card. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (postion 0)
	 */
	public int getPriority() {
		return priority;
	}



	/**
	 * Sets the priority. This is used to order the cards in the {@link SelectCardActivity}, with higher priority cards located at the beginning of the sequence (postion 0)
	 *
	 * @param priority the new priority
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}



	/**
	 * Gets the title.
	 *
	 * @return The card's title.
	 */
	public String getTitle() {
		return title;
	}



	/**
	 * Sets the title. This is displayed prominently and is usually the largest text on the card. For best visibility, this should be less than 20 characters long.
	 *
	 * @param title The card's title.
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
	 * Gets the source.
	 *
	 * @return the source
	 */
	public Server getSource() {
		return source;
	}

	/**
	 * Sets the source.
	 *
	 * @param source the new source
	 */
	public void setSource(Server source) {
		this.source = source;
	}

	/**
	 * Gets the jsontype.
	 *
	 * @return the jsontype
	 */
	public String getJsontype() {
		return jsontype;
	}



}
