/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Modified by D. Prudente and A. Meijer for ELEC 429
 * 
 */

package com.google.android.glass.TMM;

import com.google.android.glass.widget.CardScrollAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

// TODO: Auto-generated Javadoc
/**
 * Adapter for the {@link CardSrollView} inside {@link SelectValueActivity}.
 */
public class SelectCardScrollAdapter extends CardScrollAdapter {

	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" +", " + SelectCardScrollAdapter.class.getSimpleName();
	
	/** The m context. */
	private final Context mContext;
	
	/** The m count. */
	private final int mCount;
	
	/** The card arr. */
	private TMMCard[] cardArr;

	/**
	 * Instantiates a new select card scroll adapter.
	 *
	 * @param context the context
	 * @param count the count
	 * @param content the content
	 */
	public SelectCardScrollAdapter(Context context, int count, TMMCard[] content) {
		mContext = context;
		mCount = count;
		cardArr = content;
	}

	/* (non-Javadoc)
	 * @see com.google.android.glass.widget.CardScrollAdapter#getCount()
	 */
	@Override
	public int getCount() {
		return mCount;
	}

	/* (non-Javadoc)
	 * @see com.google.android.glass.widget.CardScrollAdapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return Integer.valueOf(position);
	}

	/* (non-Javadoc)
	 * @see com.google.android.glass.widget.CardScrollAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//Log.i(TAG, "cardarr[position] instance of audiocard?: " + (cardArr[position] instanceof AudioCard));

		if(cardArr[position] instanceof AudioCard){
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.audio_card, parent);
			}

			final ImageView img = (ImageView) convertView.findViewById(R.id.background_audio);
			Log.i(TAG, "Audiocard has background: " + ((AudioCard)cardArr[position]).hasBackground());
			Log.i(TAG, " background: " + ((AudioCard)cardArr[position]).getBackgroundPath());
			if(!((AudioCard)cardArr[position]).hasBackground() ){
				img.setBackgroundResource(R.drawable.redbmp);
			} else {
				Bitmap bmp = BitmapFactory.decodeFile(((AudioCard) cardArr[position]).getBackgroundPath());
				if (bmp != null){
					img.setImageBitmap(bmp);
				} 

			}
			final TextView view = (TextView) convertView.findViewById(R.id.audio_title);
			view.setText(cardArr[position].getTitle());

			return convertView;

		} else if(cardArr[position] instanceof VideoCard){

			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.video_card, parent);
			}

			final ImageView img = (ImageView) convertView.findViewById(R.id.background_video);
			if(((VideoCard)cardArr[position]).getScreenshotPath() == null || ((VideoCard)cardArr[position]).getScreenshotPath().equalsIgnoreCase("")){
				img.setBackgroundResource(R.drawable.blue);
			}

			final TextView view = (TextView) convertView.findViewById(R.id.video_title);
			view.setText(cardArr[position].getTitle());

			return convertView;
		} else {//instanceof textcard
			//Textcard is a good default anyway...

			if(((TextCard) cardArr[position]).getIconPath() == null || ((TextCard) cardArr[position]).getIconPath().equalsIgnoreCase("")){//no icon specified
				if (convertView == null) {
					convertView = LayoutInflater.from(mContext).inflate(R.layout.text_card_no_icon, parent);
				}

				final TextView view = (TextView) convertView.findViewById(R.id.text_title);
				view.setText(cardArr[position].getTitle());

				final TextView summary1 = (TextView) convertView.findViewById(R.id.summary_1);
				summary1.setText(((TextCard)cardArr[position]).getLine1());

				final TextView summary2= (TextView) convertView.findViewById(R.id.summary_2);
				summary2.setText(((TextCard)cardArr[position]).getLine2());

				final TextView summary3 = (TextView) convertView.findViewById(R.id.summary_3);
				summary3.setText(((TextCard)cardArr[position]).getLine3());

				return convertView;
			} else { //there is an icon for this card

				if (convertView == null) {
					convertView = LayoutInflater.from(mContext).inflate(R.layout.text_card, parent);
				}

				final TextView view = (TextView) convertView.findViewById(R.id.text_title);
				view.setText(cardArr[position].getTitle());

				final TextView summary1 = (TextView) convertView.findViewById(R.id.summary_1);
				summary1.setText(((TextCard)cardArr[position]).getLine1());

				final TextView summary2= (TextView) convertView.findViewById(R.id.summary_2);
				summary2.setText(((TextCard)cardArr[position]).getLine2());

				final TextView summary3 = (TextView) convertView.findViewById(R.id.summary_3);
				summary3.setText(((TextCard)cardArr[position]).getLine3());

				final ImageView ic = (ImageView) convertView.findViewById(R.id.text_icon);
				Bitmap bmp = BitmapFactory.decodeFile(((TextCard)cardArr[position]).getIconPath());
				if (bmp != null){
					ic.setImageBitmap(bmp);
				} else {
					Log.d(TAG, "Using default icon");
					ic.setBackgroundResource(R.drawable.unknown_user);
				}



				return convertView;
			}

		}

	}

	/* (non-Javadoc)
	 * @see com.google.android.glass.widget.CardScrollAdapter#getPosition(java.lang.Object)
	 */
	@Override
	public int getPosition(Object id) {
		if (id instanceof Integer) {
			int idInt = (Integer) id;
			if (idInt >= 0 && idInt < mCount) {
				return idInt;
			}
		}
		return AdapterView.INVALID_POSITION;

	}
}
