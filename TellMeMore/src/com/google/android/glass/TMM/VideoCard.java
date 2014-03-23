package com.google.android.glass.TMM;

import java.io.Serializable;

public class VideoCard extends TMMCard implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6169847664604436124L;

	public static final String TAG = "TMM" +", " + VideoCard.class.getSimpleName();

	private String screenshot_path;

	private int playCount;
	private String YTtag; 



	public VideoCard(int handle, int priority, String cardTitle, Server source) {
		super(handle, priority, cardTitle, source);

	}

	public VideoCard(int handle, int priority, String cardTitle, String ytId, Server source) {
		super(handle, priority, cardTitle, source);
		YTtag = ytId;

	}

	public VideoCard(int handle, int id, int priority, String cardTitle, String ss_path, Server source) {
		super(handle, priority, cardTitle, source);
		screenshot_path = ss_path;

	}

	public VideoCard(int handle, int id, int priority, String cardTitle, String ss_path, int playcount, Server source) {
		super(handle, priority, cardTitle, source);
		this.screenshot_path = ss_path;
		this.playCount = playcount;

	}
	
	
	public VideoCard(int handle, int id, int priority, String cardTitle, String ss_path, String youTubeTag, Server source) {
		super(handle, priority, cardTitle, source);
		this.screenshot_path = ss_path;
		this.setYTtag(youTubeTag);

	}

	public VideoCard(int handle, int id, int priority, String cardTitle, String ss_path, int playcount, String youTubeTag, Server source) {
		super(handle, priority, cardTitle, source);
		screenshot_path = ss_path;
		this.playCount = playcount;
		this.setYTtag(youTubeTag);

	}

	public String getScreenshotPath() {
		return screenshot_path;
	}

	public void setScreenshot(String screenshotPath) {
		this.screenshot_path = screenshotPath;
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
