package com.example.dbwriter;

import java.io.Serializable;

public class VideoCard extends TMMCard implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6169847664604436124L;

	public static final String TAG = "TMM" +", " + VideoCard.class.getSimpleName();

	private String screenshot_path, screnshotname;

	private int playCount;
	private String YTtag; 



	public VideoCard(int handle, int priority, String cardTitle, Server source) {
		super(handle, priority, cardTitle, source);

	}

	public VideoCard(int handle, int priority, String cardTitle, String ytId, Server source) {
		super(handle, priority, cardTitle, source);
		YTtag = ytId;

	}

	public VideoCard(int handle, String id, int priority, String cardTitle, String ss_path, Server source) {
		super(handle, id, priority, cardTitle, source);
		screenshot_path = ss_path;
		int charToWipe = screenshot_path.lastIndexOf('/');
		setScrenshotname(screenshot_path.substring(charToWipe + 1));

	}

	public VideoCard(int handle, String id, int priority, String cardTitle, String ss_path, int playcount, Server source) {
		super(handle, id, priority, cardTitle, source);
		this.screenshot_path = ss_path;
		this.playCount = playcount;
		int charToWipe = screenshot_path.lastIndexOf('/');
		setScrenshotname(screenshot_path.substring(charToWipe + 1));

	}
	
	
	public VideoCard(int handle, String id, int priority, String cardTitle, String ss_path, String youTubeTag, Server source) {
		super(handle, id, priority, cardTitle, source);
		this.screenshot_path = ss_path;
		this.setYTtag(youTubeTag);
		int charToWipe = screenshot_path.lastIndexOf('/');
		setScrenshotname(screenshot_path.substring(charToWipe + 1));

	}

	public VideoCard(int handle, String id, int priority, String cardTitle, String ss_path, int playcount, String youTubeTag, Server source) {
		super(handle, id, priority, cardTitle, source);
		screenshot_path = ss_path;
		this.playCount = playcount;
		this.setYTtag(youTubeTag);
		int charToWipe = screenshot_path.lastIndexOf('/');
		setScrenshotname(screenshot_path.substring(charToWipe + 1));

	}

	public String getScreenshotPath() {
		return screenshot_path;
	}

	public void setScreenshot(String screenshotPath) {
		this.screenshot_path = screenshotPath;
		int charToWipe = screenshot_path.lastIndexOf('/');
		setScrenshotname(screenshot_path.substring(charToWipe + 1));
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
	
	public boolean hasScreenshot(){
		if(screenshot_path == null || screenshot_path.equalsIgnoreCase("")){
			return false;
		} else return true;
	}

	public String getScrenshotname() {
		return screnshotname;
	}

	private void setScrenshotname(String screnshotname) {
		this.screnshotname = screnshotname;
	}


}
