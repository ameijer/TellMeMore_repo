package com.google.android.glass.TMM;

public class TextCard extends TMMCard{
	public static final String TAG = "TMM" +", " + TextCard.class.getSimpleName();
	private byte[] icon;
	private String line1, line2, line3;

	public TextCard(int handle, int id, int priority, String cardTitle, byte[] icon_img) {
		super(handle, id, priority, cardTitle);
		this.icon = icon_img;
	}
	
	public TextCard(int handle, int id, int priority, String cardTitle) {
		super(handle, id, priority, cardTitle);
		this.icon = null;
		
	}
	public TextCard(int handle, int id, int priority, String cardTitle, String line1, String line2, String line3) {
		super(handle, id, priority, cardTitle);
		this.icon = null;
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		
	}
	
	public TextCard(int handle, int id, int priority, String cardTitle, String line1, String line2, String line3, byte[] icon_img) {
		super(handle, id, priority, cardTitle);
		this.icon = null;
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.icon = icon_img;
		
	}
	
	public byte[] getIcon() {
		return icon;
	}

	public void setIcon(byte[] icon) {
		this.icon = icon;
	}

	public String getLine1() {
		return line1;
	}

	public void setLine1(String line1) {
		this.line1 = line1;
	}

	public String getLine2() {
		return line2;
	}

	public void setLine2(String line2) {
		this.line2 = line2;
	}

	public String getLine3() {
		return line3;
	}

	public void setLine3(String line3) {
		this.line3 = line3;
	}
	
}