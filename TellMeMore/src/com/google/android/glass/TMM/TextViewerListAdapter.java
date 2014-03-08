package com.google.android.glass.TMM;

import java.util.ArrayList;
import java.util.List;

import com.google.android.glass.TMM.TextElement.Type;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;

public class TextViewerListAdapter extends BaseAdapter{

	private ArrayList<TextElement> elems;
	private Context context;
	
	public TextViewerListAdapter(Context context,
			ArrayList<TextElement> objects) {
		if (objects != null){
		elems = objects;
		} else {
			elems = new ArrayList<TextElement>();
		}
		this.context = context;
	}

	 @Override
     public TextElement getItem(int position) {
         return elems.get(position);
     }

	 public boolean addContent(ArrayList<TextElement> elements){
		 this.elems = elements;
		 notifyDataSetChanged();
		 return true;
	 }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		TextElement p = getItem(position);

		if (v == null) {

			LayoutInflater vi;
			vi = LayoutInflater.from(context);

			if(p.getType() == Type.IMAGE){
				v = vi.inflate(R.layout.image_element, null);
				v.setBackgroundResource(R.color.black);
			} else {
				v = vi.inflate(R.layout.text_element, null);
				v.setBackgroundResource(R.color.black);
			}

		}



		if (p != null) {

			if(p.getType() == Type.IMAGE){
				ImageView pic = (ImageView) v.findViewById(R.id.picView);
				//TextView cap = (TextView) v.findViewById(R.id.picCap);
				//cap.setText(p.getText());
				pic.setImageBitmap(BitmapFactory.decodeByteArray(p.getImg(), 0, p.getImg().length));
				
			} else {
				TextView txt = (TextView) v.findViewById(R.id.textblk);
				txt.setText(p.getText());
			}

			

		}
		return v;
	}
	
	@Override
	public int getCount() {
	
		return elems.size();
	}

	@Override
	public long getItemId(int position) {
		
		return position;
	}
}
