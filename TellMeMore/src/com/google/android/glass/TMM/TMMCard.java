package com.google.android.glass.TMM;

public abstract class TMMCard implements Comparable<TMMCard>{
	public static final String TAG = "TMM" +", " + TMMCard.class.getSimpleName();
	
	//lets us sort the cards in the scroll view by priority
	private int priority;
	
	private String title;
	
	//stores position in the array
		private int handle;
		
		//the DB id
		private long id;
		
	
	
	
	
	public TMMCard(int handle, int id, int priority, String cardTitle){
		this.handle= handle;
		this.id=id;
		this.priority = priority;
		this.title = cardTitle;
	}
	
	
	
	public long getId() {
		return id;
	}



	public void setId(long id) {
		this.id = id;
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
	
	
	
}
