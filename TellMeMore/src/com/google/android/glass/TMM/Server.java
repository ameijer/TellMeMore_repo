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

/**
 * The Class Server. This class is designed to contain information about the
 * source of a card, which is assumed to be a server somewhere.
 */
public class Server implements Serializable {

	/** The Constant serialVersionUID. Used for serialization of this object. */
	private static final long serialVersionUID = -5012970894877388655L;

	/** The name of the server. */
	private String name;

	/** The api info, such as auth information. */
	private String API_info;

	/** Various number describing the use times and ID of the server. */
	private long first_used, last_used, id;

	/**
	 * Instantiates a new server object.
	 * 
	 * @param name
	 *            The server's name.
	 * @param aPI_info
	 *            The API information for this server if such information
	 *            exists.
	 * @param first_used
	 *            The UNIX time that the server was first used.
	 * @param last_used
	 *            The UNIX time that the server was last used.
	 */
	public Server(String name, String aPI_info, long first_used, long last_used) {
		super();
		this.setName(name);
		setAPI_info(aPI_info);
		this.setFirst_used(first_used);
		this.setLast_used(last_used);
	}

	/**
	 * Gets the server's name.
	 * 
	 * @return The server's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the server's name.
	 * 
	 * @param name
	 *            The new name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the API info for this server, which may include auth information.
	 * 
	 * @return The string containing API info for this server.
	 */
	public String getAPI_info() {
		return API_info;
	}

	/**
	 * Sets the api info for this server.
	 * 
	 * @param aPI_info
	 *            The String of new API information to set for the server
	 *            object.
	 */
	public void setAPI_info(String aPI_info) {
		API_info = aPI_info;
	}

	/**
	 * Gets the UNIX time of this server's first use by the app.
	 * 
	 * @return The long containing the UNIX time of the first server use.
	 */
	public long getFirst_used() {
		return first_used;
	}

	/**
	 * Sets the UNIX time of this server's first use by the app.
	 * 
	 * @param first_used
	 *            The new UNIX time of this server's first use to set.
	 */
	public void setFirst_used(long first_used) {
		this.first_used = first_used;
	}

	/**
	 * Gets the UNIX time of this server's last use by the app.
	 * 
	 * @return The UNIX time of this server's last use.
	 */
	public long getLast_used() {
		return last_used;
	}

	/**
	 * Sets the UNIX time of this server's last use by the app.
	 * 
	 * @param last_used
	 *            The new UNIX time of this server's last use to set.
	 */
	public void setLast_used(long last_used) {
		this.last_used = last_used;
	}

	/**
	 * Sets the id of the server.
	 * 
	 * @param int1
	 *            THe new ID to set as the server's ID.
	 */
	public void setId(int int1) {
		this.id = int1;

	}

	/**
	 * Gets the server's id.
	 * 
	 * @return The ID of the server.
	 */
	public long getId() {
		return id;
	}

}
