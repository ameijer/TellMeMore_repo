/*
 * File: Server.java
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
 * The Class Server.
 */
public class Server implements Serializable{

	/** The Constant serialVersionUID. Used for serialization of this object. */
	private static final long serialVersionUID = -5012970894877388655L;
	
	/** The name. */
	private String name;
	
	/** The AP i_info. */
	private String API_info;
	
	/** The id. */
	private long first_used, last_used, id;
	
	/**
	 * Instantiates a new server.
	 *
	 * @param name the name
	 * @param aPI_info the a p i_info
	 * @param first_used the first_used
	 * @param last_used the last_used
	 */
	public Server(String name, String aPI_info, long first_used, long last_used) {
		super();
		this.setName(name);
		setAPI_info(aPI_info);
		this.setFirst_used(first_used);
		this.setLast_used(last_used);
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the AP i_info.
	 *
	 * @return the aPI_info
	 */
	public String getAPI_info() {
		return API_info;
	}

	/**
	 * Sets the AP i_info.
	 *
	 * @param aPI_info the aPI_info to set
	 */
	public void setAPI_info(String aPI_info) {
		API_info = aPI_info;
	}

	/**
	 * Gets the first_used.
	 *
	 * @return the first_used
	 */
	public long getFirst_used() {
		return first_used;
	}

	/**
	 * Sets the first_used.
	 *
	 * @param first_used the first_used to set
	 */
	public void setFirst_used(long first_used) {
		this.first_used = first_used;
	}

	/**
	 * Gets the last_used.
	 *
	 * @return the last_used
	 */
	public long getLast_used() {
		return last_used;
	}

	/**
	 * Sets the last_used.
	 *
	 * @param last_used the last_used to set
	 */
	public void setLast_used(long last_used) {
		this.last_used = last_used;
	}

	/**
	 * Sets the id.
	 *
	 * @param int1 the new id
	 */
	public void setId(int int1) {
		this.id = int1;
		
	}
	
	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public long getId(){
		return id;
	}

}
