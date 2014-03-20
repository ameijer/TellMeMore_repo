package com.google.android.glass.TMM;

import java.io.Serializable;

public class VideoCard extends TMMCard implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6169847664604436124L;

	public static final String TAG = "TMM" +", " + VideoCard.class.getSimpleName();

	private byte[] screenshot;

	private int playCount;
	private String YTtag; 



	public VideoCard(int handle, int priority, String cardTitle, Server source) {
		super(handle, priority, cardTitle, source);

	}

	public VideoCard(int handle, int priority, String cardTitle, String ytId, Server source) {
		super(handle, priority, cardTitle, source);
		YTtag = ytId;

	}

	public VideoCard(int handle, int id, int priority, String cardTitle, byte[] screenshot, Server source) {
		super(handle, priority, cardTitle, source);
		this.setScreenshot(screenshot);

	}

	public VideoCard(int handle, int id, int priority, String cardTitle, byte[] screenshot, int playcount, Server source) {
		super(handle, priority, cardTitle, source);
		this.setScreenshot(screenshot);
		this.playCount = playcount;

	}
	
	
	public VideoCard(int handle, int id, int priority, String cardTitle, byte[] screenshot, String youTubeTag, Server source) {
		super(handle, priority, cardTitle, source);
		this.setScreenshot(screenshot);
		this.setYTtag(youTubeTag);

	}

	public VideoCard(int handle, int id, int priority, String cardTitle, byte[] screenshot, int playcount, String youTubeTag, Server source) {
		super(handle, priority, cardTitle, source);
		this.setScreenshot(screenshot);
		this.playCount = playcount;
		this.setYTtag(youTubeTag);

	}

	public byte[] getScreenshot() {
		return screenshot;
	}

	public void setScreenshot(byte[] screenshot) {
		this.screenshot = screenshot;
	}

	public int getPlayCount() {
		return playCount;
	}

	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}

	public String getYTtag() {
		return YTtag;
	}

	public void setYTtag(String yTtag) {
		YTtag = yTtag;
	}


}
