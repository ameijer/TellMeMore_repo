package com.google.android.glass.TMM;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import android.os.Parcel;
import android.os.Parcelable;

public class TextElement implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3730074042585803658L;

	public enum Type {
		TEXT_, IMAGE;
	}

	private Type type;
	private String text;
	private String img_path;
	private int imgSize;

	public TextElement(Type type, String text){
		this.setType(type);
		this.setText(text);
		imgSize = 0;
	}

	public TextElement(Type type, String caption, String img){
		this.setType(type);
		this.setText(caption);
		img_path = img;

	}

	public String getImg() {
		return img_path;
	}

	public void setImg(String img) {
		this.img_path = img;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Type getType() {
		return type;
	}
	
	@Override
	public String toString(){
		if(type == Type.IMAGE){
			return "Image, with caption: " + text;
		} else {
			return "Text block, contents: " + text;
		}
	}

	public void setType(Type type) {
		this.type = type;
	}

}
