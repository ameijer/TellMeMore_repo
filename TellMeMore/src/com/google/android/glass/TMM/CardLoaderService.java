package com.google.android.glass.TMM;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

//this service will load the DB with cards it downloads from the server

public class CardLoaderService extends Service{
	public static final String TAG = "TMM" +", " + CardLoaderService.class.getSimpleName();
	private TellMeMoreApplication app;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		//obtain a reference to the application singleton
		app = ((TellMeMoreApplication)this.getApplication());
		/*Parcelable toReturn = null;
		try {
			toReturn = makeDataBundle(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		sendData(this, toReturn);
		 */
		try {
			loadDBWithSamples();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Service.START_STICKY;
	}

	private void loadDBWithSamples() throws IOException{
		Server source1 = new Server("CardloaderService's sample card generator first 'server'", "none-its not a network server", System.currentTimeMillis(), System.currentTimeMillis());
		Server source2 = new Server("CardloaderService's sample card generator second 'server'", "none-its not a network server", System.currentTimeMillis(), System.currentTimeMillis());



		TextCard textCard1 = new TextCard(0, 100, "About the Author", "A. Student", "", "Tap to read more", getSampleArr1(), source1);

		
		Bitmap bmp2 = BitmapFactory.decodeResource(this.getResources(), R.raw.acmicon);
		ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
		bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
		byte[] iconArray = stream2.toByteArray();
		
		TextCard textCard2 = new TextCard(0, 99, "Read Paper Abstract", "From ACM PAUC", "A. Student and Dr. SoAndSo", "Published 1 Mar 2009",  iconArray, getSampleArr2(), source1);

		
		app.db.addCard(textCard2);
		app.db.addCard(textCard1);
	}


	private ArrayList<TextElement> getSampleArr1(){

		String text_above_pic = "A. Student was born in blah blah blah. It is a very interesting place. One of his passions has always been lightning. When he was just 6, he shot a famous photo of a lightning strike which is used on Wikipedia.";
		String text_below_pic = "Later in life, A. Student went to Africa where he photographed all manner of things. In the western sahara, he captured another famous photo of the setting african sun that won him a prize.";
		String caption1 = "\"When Clouds Attack\"";
		String caption2 = "\"A Nice Orange Sunset\"";

		ArrayList<String> textBlocks = new ArrayList<String>();
		ArrayList<String> captions = new ArrayList<String>();
		ArrayList<byte[]> pics = new ArrayList<byte[]>();

		ArrayList<TextElement> retVals = new ArrayList<TextElement>();
		TextElement e1;

		e1 = new TextElement(TextElement.Type.TEXT_, text_above_pic);
		retVals.add(e1);

		Bitmap bmp1 = BitmapFactory.decodeResource(this.getResources(), R.raw.pic1);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp1.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();

		TextElement e2 = new TextElement(TextElement.Type.IMAGE, caption1, byteArray);
		retVals.add(e2);

		TextElement e3;
			e3 = new TextElement(TextElement.Type.TEXT_, text_below_pic);
			retVals.add(e3);


		Bitmap bmp2 = BitmapFactory.decodeResource(this.getResources(), R.raw.pic2);
		ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
		bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
		byte[] byteArray2 = stream2.toByteArray();

		TextElement e4 = new TextElement(TextElement.Type.IMAGE, caption2, byteArray2);
		retVals.add(e4);
		
		return retVals;
	}
	
	
	private ArrayList<TextElement> getSampleArr2(){

		String text_above_pic = "In modern systems today, there is not really a good way to differentiate between blah and blahblah. In this paper, we present such and such novel solution. In inital testing, our solution provides several benefits while only incurring a 98% performance overhead.";
		String caption1 = "The experimental data.";
		String caption2 = "The performance results.";
		String caption3 = "The device used to make measurements.";

		ArrayList<String> textBlocks = new ArrayList<String>();
		ArrayList<String> captions = new ArrayList<String>();
		ArrayList<byte[]> pics = new ArrayList<byte[]>();

		ArrayList<TextElement> retVals = new ArrayList<TextElement>();
		TextElement e1;

		e1 = new TextElement(TextElement.Type.TEXT_, text_above_pic);
		retVals.add(e1);

		Bitmap bmp1 = BitmapFactory.decodeResource(this.getResources(), R.raw.graph_1);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp1.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();

		TextElement e2 = new TextElement(TextElement.Type.IMAGE, caption1, byteArray);
		retVals.add(e2);


		Bitmap bmp2 = BitmapFactory.decodeResource(this.getResources(), R.raw.chart2);
		ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
		bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
		byte[] byteArray2 = stream2.toByteArray();

		TextElement e4 = new TextElement(TextElement.Type.IMAGE, caption2, byteArray2);
		retVals.add(e4);
		
		Bitmap bmp3 = BitmapFactory.decodeResource(this.getResources(), R.raw.figure_3);
		ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
		bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream3);
		byte[] byteArray3 = stream3.toByteArray();

		TextElement e5 = new TextElement(TextElement.Type.IMAGE, caption3, byteArray3);
		retVals.add(e5);
		
		return retVals;
	}
	
	


	//we will use this to broadcast to the app when the cards are loaded
	//TODO
	public static void broadcastCardsLoaded(Context context, Parcelable data) {
		//Intent intent = new Intent("TextViewer");
		//intent.putExtra("data", data);
		//LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	//public synchronized TextViewerBundle makeDataBundle(int id) throws IOException{

	//byte[] toDecode;
	//make DB call using ID here, get array elements list
	//	ArrayList<TextElement> contents = this.getSampleArr();

	//TextViewerBundle toRet = new TextViewerBundle(id, contents);



	//	return toRet;



	//}



	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}





}
