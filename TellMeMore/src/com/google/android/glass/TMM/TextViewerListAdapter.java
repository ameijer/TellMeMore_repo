package com.google.android.glass.TMM;

import java.util.List;

import com.google.android.glass.TMM.TextElement.Type;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;

public class TextViewerListAdapter extends ArrayAdapter<TextElement>{

	public TextViewerListAdapter(Context context, int textViewResourceId,
			List<TextElement> objects) {
		super(context, textViewResourceId, objects);
		// TODO Auto-generated constructor stub
	}

	public TextViewerListAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
	}




	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		TextElement p = getItem(position);

		if (v == null) {

			LayoutInflater vi;
			vi = LayoutInflater.from(getContext());

			if(p.getType() == Type.IMAGE){
				v = vi.inflate(R.layout.image_element, null);
			} else {
				v = vi.inflate(R.layout.text_element, null);
			}

		}



		if (p != null) {

			if(p.getType() == Type.IMAGE){
				ImageView pic = (ImageView) v.findViewById(R.id.picView);
				TextView cap = (TextView) v.findViewById(R.id.picCap);
				cap.setText(p.getText());
				pic.setImageBitmap(BitmapFactory.decodeByteArray(p.getImg(), 0, p.getImg().length));
				
			} else {
				TextView txt = (TextView) v.findViewById(R.id.textblk);
				txt.setText(p.getText());
			}

			

		}
		return v;
	}
}
