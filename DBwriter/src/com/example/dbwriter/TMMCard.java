package com.example.dbwriter;

import java.io.Serializable;

public abstract class TMMCard implements Comparable<TMMCard>, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2159979972374818761L;

	public static final String TAG = "TMM" +", " + TMMCard.class.getSimpleName();

	//lets us sort the cards in the scroll view by priority
	private int priority;

	private String title;

	//stores position in the array
	private int handle;

	//the DB id
	private String uuid;
	
	private Server source = new Server("NO SERVER ASSIGNED", "NO SERVER ASSIGNED", 0, 0);




	//only for use when creating card from DB
	public TMMCard(int handle, String uuid, int priority, String cardTitle, Server source){
		this.handle= handle;
		this.uuid=uuid;
		this.priority = priority;
		this.title = cardTitle;
		
		if(source != null){
			this.source = source;
		}
	}

	//only for use when creating card from DB
	public TMMCard(int handle,  int priority, String cardTitle, Server source){
		this.handle= handle;
		this.uuid= "";
		this.priority = priority;
		this.title = cardTitle;
		
		if(source != null){
			this.source = source;
		}
		
	}


	public String getuuId() {
		return uuid;
	}



	public void setuuId(String id) {
		this.uuid = id;
	}

	public int getHandle() {
		return handle;
	}



	public void setHandle(int handle) {
		this.handle = handle;
	}



	public int getPriority() {
		return priority;
	}



	public void setPriority(int priority) {
		this.priority = priority;
	}



	public String getTitle() {
		return title;
	}



	public void setTitle(String title) {
		this.title = title;
	}



	@Override
	public int compareTo(TMMCard arg0) {
		if(priority > arg0.getPriority()){
			return 1;
		} else if(priority < arg0.getPriority()){
			return -1;
		} else return 0;
	}
	
	@Override
	public String toString(){
		return "TMM Card: " + title + " with DB id: " + uuid;
	}

	public Server getSource() {
		return source;
	}

	public void setSource(Server source) {
		this.source = source;
	}



}
