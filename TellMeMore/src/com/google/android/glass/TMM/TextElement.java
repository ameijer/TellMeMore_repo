/*
 * File: TextElement.java
 * Date: Apr 16, 2014
 * Authors: A. Meijer (atm011) and D. Prudente (dcp017)
 * 
 * Written for ELEC429, Independent Study
 * 
 * Copyright 2014 atm011 and dcp017
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this 
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors 
 * may be used to endorse or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.google.android.glass.TMM;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import android.os.Parcel;
import android.os.Parcelable;

// TODO: Auto-generated Javadoc
/**
 * The Class TextElement.
 */
public class TextElement implements Serializable{
	
	/** The Constant serialVersionUID. Used for serialization of this object. */
	private static final long serialVersionUID = 3730074042585803658L;

	/**
	 * The Enum Type.
	 */
	public enum Type {
		
		/** The text. */
		TEXT_, 
 /** The image. */
 IMAGE;
	}

	/** The type. */
	private Type type;
	
	/** The img filename. */
	private String text, imgFilename;
	
	/** The img_path. */
	private String img_path;
	
	/** The img size. */
	private int imgSize;

	/**
	 * Instantiates a new text element.
	 *
	 * @param type the type
	 * @param text the text
	 */
	public TextElement(Type type, String text){
		this.setType(type);
		this.setText(text);
		imgSize = 0;
	}

	/**
	 * Instantiates a new text element.
	 *
	 * @param type the type
	 * @param caption the caption
	 * @param img the img
	 */
	public TextElement(Type type, String caption, String img){
		this.setType(type);
		this.setText(caption);
		img_path = img;
		int charToWipe = img_path.lastIndexOf('/');
		setImgFilename(img_path.substring(charToWipe + 1));

	}

	/**
	 * Gets the img.
	 *
	 * @return the img
	 */
	public String getImg() {
		return img_path;
	}

	/**
	 * Sets the img.
	 *
	 * @param img the new img
	 */
	public void setImg(String img) {
		this.img_path = img;
		int charToWipe = img_path.lastIndexOf('/');
		setImgFilename(img_path.substring(charToWipe + 1));
	}

	/**
	 * Gets the text.
	 *
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text.
	 *
	 * @param text the new text
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public Type getType() {
		return type;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		if(type == Type.IMAGE){
			return "Image, with caption: " + text;
		} else {
			return "Text block, contents: " + text;
		}
	}

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Gets the img filename.
	 *
	 * @return the img filename
	 */
	public String getImgFilename() {
		return imgFilename;
	}

	/**
	 * Sets the img filename.
	 *
	 * @param imgFilename the new img filename
	 */
	private void setImgFilename(String imgFilename) {
		this.imgFilename = imgFilename;
	}

}
