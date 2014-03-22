package com.google.android.glass.TMM;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import android.os.Parcel;
import android.os.Parcelable;

public class TextElement implements Parcelable, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3730074042585803658L;

	public enum Type {
		TEXT_, IMAGE;
	}

	private Type type;
	private String text;
	private byte[] img;
	private int imgSize;

	public TextElement(Type type, String text){
		this.setType(type);
		this.setText(text);
		imgSize = 0;
	}

	public TextElement(Type type, String caption, byte[] img){
		this.setType(type);
		this.setText(caption);
		this.setImg(img);
		imgSize = img.length;

	}

	// example constructor that takes a Parcel and gives you an object populated with it's values
	public TextElement(Parcel in) throws UnsupportedEncodingException{
		byte[] enum_asStringBytes = new byte[5];
		in.readByteArray(enum_asStringBytes);
		type = Type.valueOf(new String(enum_asStringBytes, "UTF-8"));
		text = in.readString();
		imgSize = in.readInt();

		img = new byte[imgSize];
		if(imgSize > 0){
			in.readByteArray(img);
		}

	}


	// this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
	public static final Parcelable.Creator<TextElement> CREATOR = new Parcelable.Creator<TextElement>() {
		public TextElement createFromParcel(Parcel in) {
			try {
				return new TextElement(in);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}

		}

		public TextElement[] newArray(int size) {
			return new TextElement[size];
		}
	};





	public byte[] getImg() {
		return img;
	}

	public void setImg(byte[] img) {
		this.img = img;
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

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		try {
			dest.writeByteArray(type.name().getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dest.writeString(text);
		dest.writeInt(imgSize);
		if (imgSize > 0){
			dest.writeByteArray(img);
		}

	}
}
