package com.google.android.glass.TMM;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class TextViewerBundle implements Parcelable{

		private int id;
		private ArrayList<TextElement> elems;
		

		
		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public ArrayList<TextElement> getElems() {
			return elems;
		}
		public void setElems(ArrayList<TextElement> elems) {
			this.elems = elems;
		}
		
		public TextViewerBundle(Parcel in){
			id = in.readInt();
			in.readTypedList(elems, TextElement.CREATOR);
		}
		public TextViewerBundle(int id, ArrayList<TextElement> contents){
			this.id = id;
			elems = contents;
		}
		
		
		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public void writeToParcel(Parcel dest, int flags) {
			// TODO Auto-generated method stub
			dest.writeInt(id);
			dest.writeTypedList(elems);
			
		}
		
		public static final Parcelable.Creator<TextViewerBundle> CREATOR = 
	            new Parcelable.Creator<TextViewerBundle>() {
	        public TextViewerBundle createFromParcel(Parcel in) {
	            return new TextViewerBundle(in);
	        }

	        public TextViewerBundle[] newArray(int size) {
	            return new TextViewerBundle[size];
	        }
	    };

		
		
}
