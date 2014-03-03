package com.google.android.glass.TMM;

public class VideoCard extends TMMCard{

	public static final String TAG = "TMM" +", " + VideoCard.class.getSimpleName();

	private byte[] screenshot;

	private int playCount;



	public VideoCard(int handle, int id, int priority, String cardTitle) {
		super(handle, id, priority, cardTitle);

	}

	public VideoCard(int handle, int id, int priority, String cardTitle, byte[] screenshot) {
		super(handle, id, priority, cardTitle);
		this.setScreenshot(screenshot);

	}

	public VideoCard(int handle, int id, int priority, String cardTitle, byte[] screenshot, int playcount) {
		super(handle, id, priority, cardTitle);
		this.setScreenshot(screenshot);
		this.playCount = playcount;

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


}
