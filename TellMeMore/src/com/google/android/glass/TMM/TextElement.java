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

/**
 * The Class TextElement. This is used for holding the contents of a text card.
 * This class can either contain a picture and a caption or only text.
 */
public class TextElement implements Serializable {

	/** The Constant serialVersionUID. Used for serialization of this object. */
	private static final long serialVersionUID = 3730074042585803658L;

	/**
	 * The Enum Type. Used to indicate if the textelement contains text or
	 * visual content (images)
	 */
	public enum Type {

		/** The text enum vale. */
		TEXT_,
		/** The image enum value. */
		IMAGE;
	}

	/** The Type of the textelement, either Type.IMAGE or Type.TEXT. */
	private Type type;

	/**
	 * The text contained in this text element. This is either a caption to an
	 * image or the element content itself.
	 */
	private String text;

	/**
	 * If this textelement contains an image, this is the name of it, including
	 * the extension.
	 */
	private String imgFilename;

	/**
	 * If this textelement contains an image, this is the path of that image
	 * used to retrieve and display that image.
	 */
	private String img_path;

	/**
	 * Instantiates a new text element, without an image path.
	 * 
	 * @param type
	 *            Either IMAGE or TEXT, depending on the desired content of the
	 *            element.
	 * @param text
	 *            The text to be used in the card, either as a picture caption
	 *            or the content itself.
	 */
	public TextElement(Type type, String text) {
		this.setType(type);
		this.setText(text);
	}

	/**
	 * Instantiates a new text element with an image path.
	 * 
	 * @param type
	 *            Either IMAGE or TEXT, depending on the desired content of the
	 *            element.
	 * @param caption
	 *            The text to be used in the caption of the picture pointed to
	 *            for this element.
	 * @param img
	 *            The path of the image to serve as the source image for this
	 *            textelement.
	 */
	public TextElement(Type type, String caption, String img) {
		this.setType(type);
		this.setText(caption);
		img_path = img;

		// set the filename of the image automatically, to keep the name and
		// path consistent
		int charToWipe = img_path.lastIndexOf('/');
		setImgFilename(img_path.substring(charToWipe + 1));

	}

	/**
	 * Gets the path of the image contained in this textelement, if there is a
	 * corresponding image.
	 * 
	 * @return The string containing the path of the source image for this
	 *         element.
	 */
	public String getImg() {
		return img_path;
	}

	/**
	 * Sets the image path of the source image for this element.
	 * 
	 * @param img
	 *            The the path of the new image to set in the texetelement.
	 */
	public void setImg(String img) {
		this.img_path = img;

		// set the filename of the image automatically, to keep the name and
		// path consistent
		int charToWipe = img_path.lastIndexOf('/');
		setImgFilename(img_path.substring(charToWipe + 1));
	}

	/**
	 * Gets the text contained in this textelement. This could be either a
	 * picture caption or the full content of this textelement.
	 * 
	 * @return The string containing the relevent text for this textelement.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the text contained in the textelement.This could be either a picture
	 * caption or the full content of this textelement.
	 * 
	 * @param text
	 *            The string of new text to set as the content of the
	 *            textelement.
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Gets the type of this textelement, either IMAGE or TEXT.
	 * 
	 * @return The {@link Type} of the textelement.
	 */
	public Type getType() {
		return type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (type == Type.IMAGE) {
			return "Image, with caption: " + text;
		} else {
			return "Text block, contents: " + text;
		}
	}

	/**
	 * Sets the Type of this textelement, either IMAGE or TEXT.
	 * 
	 * @param type
	 *            The type of this textelement to set.
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Gets the filename of the image contained in this textelement, if one
	 * exists.
	 * 
	 * @return The name of the image file being used by this textelement.
	 */
	public String getImgFilename() {
		return imgFilename;
	}

	/**
	 * Sets the img filename. Should not be called by users directly to preserve
	 * consistency
	 * 
	 * @param imgFilename
	 *            The new image filename to set.
	 */
	private void setImgFilename(String imgFilename) {
		this.imgFilename = imgFilename;
	}

}
