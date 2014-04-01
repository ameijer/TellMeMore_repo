package com.example.dbwriter;

import java.io.Serializable;

public class Server implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5012970894877388655L;
	
	private String name;
	private String API_info;
	private long first_used, last_used, id;
	
	public Server(String name, String aPI_info, long first_used, long last_used) {
		super();
		this.setName(name);
		setAPI_info(aPI_info);
		this.setFirst_used(first_used);
		this.setLast_used(last_used);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the aPI_info
	 */
	public String getAPI_info() {
		return API_info;
	}

	/**
	 * @param aPI_info the aPI_info to set
	 */
	public void setAPI_info(String aPI_info) {
		API_info = aPI_info;
	}

	/**
	 * @return the first_used
	 */
	public long getFirst_used() {
		return first_used;
	}

	/**
	 * @param first_used the first_used to set
	 */
	public void setFirst_used(long first_used) {
		this.first_used = first_used;
	}

	/**
	 * @return the last_used
	 */
	public long getLast_used() {
		return last_used;
	}

	/**
	 * @param last_used the last_used to set
	 */
	public void setLast_used(long last_used) {
		this.last_used = last_used;
	}

	public void setId(int int1) {
		this.id = int1;
		
	}
	public long getId(){
		return id;
	}

}
