package com.google.android.glass.TMM;

import java.io.Serializable;
import java.util.ArrayList;

public class TextCard extends TMMCard implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4719812627361504202L;
	public static final String TAG = "TMM" +", " + TextCard.class.getSimpleName();
	private String icon_path, ic_filename;
	private String line1, line2, line3;
	private ArrayList<TextElement> contents;

	public TextCard(int handle, int priority, String cardTitle, String ic_path, Server source) {
		super(handle, priority, cardTitle, source);
		this.icon_path = ic_path;
		int charToWipe = ic_path.lastIndexOf('/');
		ic_filename = ic_path.substring(charToWipe + 1);
	}
	
	public TextCard(int handle, int priority, String cardTitle, Server source) {
		super(handle, priority, cardTitle, source);
		this.icon_path = null;
	}
	
	public TextCard(int handle, String id, int priority, String cardTitle, Server source) {
		super(handle, id, priority, cardTitle, source);
		this.icon_path = null;
		
	}
	public TextCard(int handle, String id, int priority, String cardTitle, String line1, String line2, String line3, Server source) {
		super(handle, id, priority, cardTitle, source);
		this.icon_path = null;
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		
	}
	
	public TextCard(int handle, String id, int priority, String cardTitle, String line1, String line2, String line3, String ic_path, Server source) {
		super(handle, id, priority, cardTitle, source);
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.icon_path = ic_path;
		int charToWipe = ic_path.lastIndexOf('/');
		ic_filename = ic_path.substring(charToWipe + 1);
		
	}
	
	public TextCard(int handle,String id, int priority, String cardTitle, String line1, String line2, String line3, ArrayList<TextElement> content, Server source) {
		super(handle, id, priority, cardTitle, source);
		this.icon_path = null;
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.setContents(content);
		
	}
	
	public TextCard(int handle, int priority, String cardTitle, String line1, String line2, String line3, ArrayList<TextElement> content, Server source) {
		super(handle, priority, cardTitle, source);
		this.icon_path = null;
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.setContents(content);
		
	}
	
	public TextCard(int handle, int priority, String cardTitle, String line1, String line2, String line3, String ic_path, ArrayList<TextElement> content, Server source) {
		super(handle, priority, cardTitle, source);
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.icon_path = ic_path;
		int charToWipe = ic_path.lastIndexOf('/');
		ic_filename = ic_path.substring(charToWipe + 1);
		this.setContents(content);
		
	}
	
	public TextCard(int handle, String id, int priority, String cardTitle, String line1, String line2, String line3, String ic_path, ArrayList<TextElement> content, Server source) {
		super(handle, id, priority, cardTitle, source);
		this.line1 = line1;
		this.line2 = line2;
		this.line3 = line3;
		this.icon_path = ic_path;
		int charToWipe = ic_path.lastIndexOf('/');
		ic_filename = ic_path.substring(charToWipe + 1);
		this.setContents(content);
		
	}
	
	public String getIconPath() {
		return icon_path;
	}

	public void setIconPath(String ic_path) {
		this.icon_path = ic_path;
		int charToWipe = ic_path.lastIndexOf('/');
		ic_filename = ic_path.substring(charToWipe + 1);
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

	public ArrayList<TextElement> getContents() {
		return contents;
	}

	public void setContents(ArrayList<TextElement> contents) {
		this.contents = contents;
	}
	
	public boolean hasIcon(){
		if(icon_path == null || icon_path.equalsIgnoreCase("")){
			return false;
		} else return true;
	}
	
	public String getIcFileName(){
		return ic_filename;
	}
	
}
