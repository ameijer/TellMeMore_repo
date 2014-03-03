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

/**
 * Adapter for the {@link CardSrollView} inside {@link SelectValueActivity}.
 */
public class SelectCardScrollAdapter extends CardScrollAdapter {

	public static final String TAG = "TMM" +", " + SelectCardScrollAdapter.class.getSimpleName();
	private final Context mContext;
	private final int mCount;
	private TMMCard[] cardArr;

	public SelectCardScrollAdapter(Context context, int count, TMMCard[] content) {
		mContext = context;
		mCount = count;
		cardArr = content;
	}

	@Override
	public int getCount() {
		return mCount;
	}

	@Override
	public Object getItem(int position) {
		return Integer.valueOf(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//Log.i(TAG, "cardarr[position] instance of audiocard?: " + (cardArr[position] instanceof AudioCard));

		if(cardArr[position] instanceof AudioCard){
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.audio_card, parent);
			}

			final ImageView img = (ImageView) convertView.findViewById(R.id.background_audio);
			if(((AudioCard)cardArr[position]).getBackground() == null){
				img.setBackgroundResource(R.drawable.redbmp);
			}
			final TextView view = (TextView) convertView.findViewById(R.id.audio_title);
			view.setText(cardArr[position].getTitle());

			return setItemOnCard(this, convertView);

		} else if(cardArr[position] instanceof VideoCard){

			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.video_card, parent);
			}

			final ImageView img = (ImageView) convertView.findViewById(R.id.background_video);
			if(((VideoCard)cardArr[position]).getScreenshot() == null){
				img.setBackgroundResource(R.drawable.blue);
			}

			final TextView view = (TextView) convertView.findViewById(R.id.video_title);
			view.setText(cardArr[position].getTitle());

			return setItemOnCard(this, convertView);
		} else {//instanceof textcard
			//Textcard is a good default anyway...

			if(((TextCard) cardArr[position]).getIcon() == null){//no icon specified
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

				return setItemOnCard(this, convertView);
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
				Bitmap bmp = BitmapFactory.decodeByteArray(((TextCard)cardArr[position]).getIcon(), 0, ((TextCard)cardArr[position]).getIcon().length);
				if (bmp != null){
					ic.setImageBitmap(bmp);
				} else {
					Log.d(TAG, "Using default icon");
					ic.setBackgroundResource(R.drawable.unknown_user);
				}



				return setItemOnCard(this, convertView);
			}

		}

	}

	@Override
	public int findIdPosition(Object id) {
		if (id instanceof Integer) {
			int idInt = (Integer) id;
			if (idInt >= 0 && idInt < mCount) {
				return idInt;
			}
		}
		return AdapterView.INVALID_POSITION;
	}

	@Override
	public int findItemPosition(Object item) {
		return findIdPosition(item);
	}
}
