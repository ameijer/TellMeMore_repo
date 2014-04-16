/*
 * File: CardLoaderService.java
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.couchbase.lite.CouchbaseLiteException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

// TODO: Auto-generated Javadoc
//this service will load the DB with cards it downloads from the server

/**
 * The Class CardLoaderService.
 */
public class CardLoaderService extends Service{
	
	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM" +", " + CardLoaderService.class.getSimpleName();
	
	/** The app. */
	private TellMeMoreApplication app;
	
	/** The Constant TARGET_SERVER_KEY. */
	public static final String TARGET_SERVER_KEY = "target_server";
	
	/** The Constant EXAMPLE_CARD_SERVER. */
	public static final String EXAMPLE_CARD_SERVER = "example_card_generator";
	
	/** The target server. */
	private String targetServer;


	/* (non-Javadoc)
	 * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {



		//obtain a reference to the application singleton
		app = ((TellMeMoreApplication)this.getApplication());
		if (intent != null){
			targetServer = intent.getStringExtra(TARGET_SERVER_KEY);
		}
		Log.d(TAG, "target server retreived from intent: " + targetServer);


		if(targetServer == null){
			Log.e(TAG, "no target server retreived! Using example server");
			targetServer = EXAMPLE_CARD_SERVER;
		}

		if(targetServer.equalsIgnoreCase(EXAMPLE_CARD_SERVER)){
			Log.d(TAG, "Cardloader service is using DB: " + app.db);

			try {
				app.db.open(EXAMPLE_CARD_SERVER, this);
				new cardsLoadedBroadcaster().execute(this);

				Log.i(TAG, "CONTENTS OF ENTIRE DB IN CARDLOADERSERVICE FOLLOWS: " + app.db.getEntireDbAsJSON());
			} catch (IOException e) {

				e.printStackTrace();
			} catch (CouchbaseLiteException e) {
				e.printStackTrace();
			}

			//new loadDBWithSamplesTask().execute(this);
			//broadcastCardsLoaded(this, targetServer);
		} else { //we have an actual target
			//TODO
			//check DB for already existing cards
			//if there aren't any already in the DB, then download it
			//etc etc
			try {
				app.db.open(targetServer, this);
				new cardsLoadedBroadcaster().execute(this);
			} catch (IOException e) {

				e.printStackTrace();
			} catch (CouchbaseLiteException e) {
				e.printStackTrace();
			}

			//once cards are stored in the DB, alert the selectcardactivity
			//broadcastCardsLoaded(this, targetServer);

		}


		return Service.START_STICKY;
	}

	/**
	 * The Class cardsLoadedBroadcaster.
	 */
	private class cardsLoadedBroadcaster extends AsyncTask<Context, Integer, Long> {
		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		protected Long doInBackground(Context... contexts) {
			while(!app.db.isSynced()){
				Log.i(TAG, "cardsloaded broadcaster, issynched reports: " + app.db.isSynced());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e3) {
					Log.e(TAG, "broadcast sleeper thread was interrupted", e3);
					
				}
			}
			
			broadcastCardsLoaded(contexts[0], targetServer);


			return (long) 1;


		}
		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		protected void onProgressUpdate(Integer... progress) {

		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		protected void onPostExecute(Long result) {

		}
	}






	/**
	 * The Class loadDBWithSamplesTask.
	 */
	private class loadDBWithSamplesTask extends AsyncTask<Context, Integer, Long> {
		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		protected Long doInBackground(Context... contexts) {

			try {
				Thread.sleep(3000);
			} catch (InterruptedException e3) {
				e3.printStackTrace();
			}
			broadcastCardsLoaded(contexts[0], targetServer);
			//			Server source1 = new Server(EXAMPLE_CARD_SERVER, "none-its not a network server", System.currentTimeMillis(), System.currentTimeMillis());
			//			//Server source2 = new Server("CardloaderService's sample card generator second 'server'", "none-its not a network server", System.currentTimeMillis(), System.currentTimeMillis());
			//			File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmm");
			//			TextCard textCard1 = new TextCard(0, 100, "About the Author", "A. Student", "", "Tap to read more", getSampleArr1(), source1);
			//
			//			//			Bitmap bmp2 = BitmapFactory.decodeResource(contexts[0].getResources(), R.raw.acmicon);
			//			//			ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
			//			//			bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
			//			//			byte[] iconArray = stream2.toByteArray();
			//
			//			File file0 = new File(dir, "acmicon.jpg");
			//
			//			//manually write the audio file to the external to emulate it being downloaded
			//			InputStream fIn0 = getBaseContext().getResources().openRawResource(R.raw.acmicon);
			//			byte[] buffer0 = null;
			//			try {
			//				int size0 = fIn0.available();
			//				buffer0 = new byte[size0];
			//				fIn0.read(buffer0);
			//				fIn0.close();
			//			} catch (IOException e) {
			//				Log.e(TAG, "IOException first part");
			//
			//			}
			//

			//
			//
			//			TextCard textCard2 = new TextCard(0, 99, "Read Paper Abstract", "From ACM PAUC", "A. Student and Dr. XYZ", "Published 1 Mar 2009",  file0.getAbsolutePath(), getSampleArr2(), source1);
			//
			//			try {
			//				Log.d(TAG, "text card 2 added, returned: " + app.db.addCard(textCard2));
			//				Log.d(TAG, "text card 1 added, returned: " + app.db.addCard(textCard1));
			//			} catch (IOException e1) {
			//
			//				Log.e(TAG, "IOexception adding sample text cards", e1);
			//			} catch (Exception e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			}
			//
			//			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmm/powerpointdemo.mp3";
			//
			//			dir.mkdirs();
			//
			//			File file = new File(dir, "powerpointdemo.mp3");
			//
			//			//manually write the audio file to the external to emulate it being downloaded
			//			InputStream fIn = getBaseContext().getResources().openRawResource(R.raw.powerpointdemo);
			//			byte[] buffer = null;
			//			try {
			//				int size = fIn.available();
			//				buffer = new byte[size];
			//				fIn.read(buffer);
			//				fIn.close();
			//			} catch (IOException e2) {
			//				Log.e(TAG, "IOException first part");
			//
			//			}
			//
			//			FileOutputStream save;
			//			try {
			//				save = new FileOutputStream(file);
			//				save.write(buffer);
			//				save.flush();
			//				save.close();
			//			} catch (FileNotFoundException e2) {
			//				Log.e(TAG, "FileNotFoundException in second part");
			//
			//			} catch (IOException e1) {
			//				Log.e(TAG, "IOException in second part");
			//
			//			}    
			//
			//			AudioCard audioCard1 = new AudioCard(0, 97, "Hear A Student Narration", file.getAbsolutePath(), source1);
			//			try{
			//				Log.d(TAG, "audio card 1 added, returned: " + app.db.addCard(audioCard1));
			//			} catch (IOException e1) {
			//
			//				Log.e(TAG, "IOexception adding sample audio card", e1);
			//			} catch (Exception e) {
			//				// TODO Auto-generated catch block
			//				e.printStackTrace();
			//			}
			//
			//			VideoCard videoCard1 = new VideoCard(0, 90, "Watch the Experiment", "wtnI3kyCnmA", source1);
			//			VideoCard videoCard2 = new VideoCard(0, 89, "View the presentation", "cn5mMJiPYmw", source1);
			//			try{
			//				Log.d(TAG, "video card 1 added, returned: " + app.db.addCard(videoCard1));
			//				try {
			//					Log.d(TAG, "video card 2 added, returned: " + app.db.addCard(videoCard2));
			//				} catch (Exception e) {
			//					// TODO Auto-generated catch block
			//					e.printStackTrace();
			//				}
			//
			//			
			//			} catch (IOException e1) {
			//
			//				Log.e(TAG, "IOexception adding sample video cards", e1);
			//			} catch (Exception e1) {
			//				// TODO Auto-generated catch block
			//				e1.printStackTrace();
			//			}
			//
			//			//app.db.addServer(source2);
			//
			//			//notify any waiting activities that we have finished loading the cards
			//			//Server source1 = new Server(EXAMPLE_CARD_SERVER, "none-its not a network server", System.currentTimeMillis(), System.currentTimeMillis());
			//			broadcastCardsLoaded(contexts[0], source1.getName());

			return (long) 1;


		}
		
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		protected void onProgressUpdate(Integer... progress) {

		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		protected void onPostExecute(Long result) {

		}
	}
	//
	//
	//
	//	private ArrayList<TextElement> getSampleArr1(){
	//
	//		String text_above_pic = "A. Student was born in blah blah blah. It is a very interesting place. One of his passions has always been lightning. When he was just 6, he shot a famous photo of a lightning strike which is used on Wikipedia.";
	//		String text_below_pic = "Later in life, A. Student went to Africa where he photographed all manner of things. In the western sahara, he captured another famous photo of the setting african sun that won him a prize.";
	//		String caption1 = "\"When Clouds Attack\"";
	//		String caption2 = "\"A Nice Orange Sunset\"";
	//
	//		ArrayList<TextElement> retVals = new ArrayList<TextElement>();
	//		TextElement e1;
	//
	//		e1 = new TextElement(TextElement.Type.TEXT_, text_above_pic);
	//		retVals.add(e1);
	//
	//
	//
	//
	//
	//
	//
	//
	//		//Bitmap bmp1 = BitmapFactory.decodeResource(this.getResources(), R.raw.pic1);
	//		//ByteArrayOutputStream stream = new ByteArrayOutputStream();
	//		//bmp1.compress(Bitmap.CompressFormat.PNG, 100, stream);
	//		//byte[] byteArray = stream.toByteArray();
	//
	//		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmm");
	//		dir.mkdirs();
	//
	//		File file = new File(dir, "pic1.jpg");
	//
	//		//manually write the audio file to the external to emulate it being downloaded
	//		InputStream fIn = getBaseContext().getResources().openRawResource(R.raw.pic1);
	//		byte[] buffer = null;
	//		try {
	//			int size = fIn.available();
	//			buffer = new byte[size];
	//			fIn.read(buffer);
	//			fIn.close();
	//		} catch (IOException e) {
	//			Log.e(TAG, "IOException first part");
	//
	//		}
	//
	//		FileOutputStream save;
	//		try {
	//			save = new FileOutputStream(file);
	//			save.write(buffer);
	//			save.flush();
	//			save.close();
	//		} catch (FileNotFoundException e) {
	//			Log.e(TAG, "FileNotFoundException in second part");
	//
	//		} catch (IOException e) {
	//			Log.e(TAG, "IOException in second part");
	//
	//		}
	//
	//
	//
	//		TextElement e2 = new TextElement(TextElement.Type.IMAGE, caption1, file.getAbsolutePath());
	//		retVals.add(e2);
	//
	//		TextElement e3;
	//		e3 = new TextElement(TextElement.Type.TEXT_, text_below_pic);
	//		retVals.add(e3);
	//
	//
	//		//		Bitmap bmp2 = BitmapFactory.decodeResource(this.getResources(), R.raw.pic2);
	//		//		ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
	//		//		bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
	//		//		byte[] byteArray2 = stream2.toByteArray();
	//
	//
	//		File file2 = new File(dir, "pic2.jpg");
	//
	//		//manually write the audio file to the external to emulate it being downloaded
	//		InputStream fIn2 = getBaseContext().getResources().openRawResource(R.raw.pic2);
	//		byte[] buffer2 = null;
	//		try {
	//			int size2 = fIn2.available();
	//			buffer2 = new byte[size2];
	//			fIn2.read(buffer2);
	//			fIn2.close();
	//		} catch (IOException e) {
	//			Log.e(TAG, "IOException first part");
	//
	//		}
	//
	//		FileOutputStream save2;
	//		try {
	//			save2 = new FileOutputStream(file2);
	//			save2.write(buffer2);
	//			save2.flush();
	//			save2.close();
	//		} catch (FileNotFoundException e) {
	//			Log.e(TAG, "FileNotFoundException in second part");
	//
	//		} catch (IOException e) {
	//			Log.e(TAG, "IOException in second part");
	//
	//		}
	//
	//
	//		TextElement e4 = new TextElement(TextElement.Type.IMAGE, caption2, file2.getAbsolutePath());
	//		retVals.add(e4);
	//
	//		return retVals;
	//	}
	//
	//
	//	private ArrayList<TextElement> getSampleArr2(){
	//
	//		String text_above_pic = "In modern systems today, there is not really a good way to differentiate between blah and blahblah. In this paper, we present such and such novel solution. In inital testing, our solution provides several benefits while only incurring a 98% performance overhead.";
	//		String caption1 = "The experimental data.";
	//		String caption2 = "The performance results.";
	//		String caption3 = "The device used to make measurements.";
	//
	//		ArrayList<TextElement> retVals = new ArrayList<TextElement>();
	//		TextElement e1;
	//
	//		e1 = new TextElement(TextElement.Type.TEXT_, text_above_pic);
	//		retVals.add(e1);
	//		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmm");
	//		//		Bitmap bmp1 = BitmapFactory.decodeResource(this.getResources(), R.raw.graph_1);
	//		//		ByteArrayOutputStream stream = new ByteArrayOutputStream();
	//		//		bmp1.compress(Bitmap.CompressFormat.PNG, 100, stream);
	//		//		byte[] byteArray = stream.toByteArray();
	//
	//		File file2 = new File(dir, "graph_1.jpg");
	//
	//		//manually write the audio file to the external to emulate it being downloaded
	//		InputStream fIn2 = getBaseContext().getResources().openRawResource(R.raw.graph_1);
	//		byte[] buffer2 = null;
	//		try {
	//			int size2 = fIn2.available();
	//			buffer2 = new byte[size2];
	//			fIn2.read(buffer2);
	//			fIn2.close();
	//		} catch (IOException e) {
	//			Log.e(TAG, "IOException first part");
	//
	//		}
	//
	//		FileOutputStream save2;
	//		try {
	//			save2 = new FileOutputStream(file2);
	//			save2.write(buffer2);
	//			save2.flush();
	//			save2.close();
	//		} catch (FileNotFoundException e) {
	//			Log.e(TAG, "FileNotFoundException in second part");
	//
	//		} catch (IOException e) {
	//			Log.e(TAG, "IOException in second part");
	//
	//		}
	//
	//		TextElement e2 = new TextElement(TextElement.Type.IMAGE, caption1, file2.getAbsolutePath());
	//		retVals.add(e2);
	//
	//
	//		//		Bitmap bmp2 = BitmapFactory.decodeResource(this.getResources(), R.raw.chart2);
	//		//		ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
	//		//		bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
	//		//		byte[] byteArray2 = stream2.toByteArray();
	//
	//		File file3 = new File(dir, "chart2.png");
	//
	//		//manually write the audio file to the external to emulate it being downloaded
	//		InputStream fIn3 = getBaseContext().getResources().openRawResource(R.raw.chart2);
	//		byte[] buffer3 = null;
	//		try {
	//			int size3 = fIn3.available();
	//			buffer3 = new byte[size3];
	//			fIn3.read(buffer3);
	//			fIn3.close();
	//		} catch (IOException e) {
	//			Log.e(TAG, "IOException first part");
	//
	//		}
	//
	//		FileOutputStream save3;
	//		try {
	//			save3 = new FileOutputStream(file3);
	//			save3.write(buffer3);
	//			save3.flush();
	//			save3.close();
	//		} catch (FileNotFoundException e) {
	//			Log.e(TAG, "FileNotFoundException in second part");
	//
	//		} catch (IOException e) {
	//			Log.e(TAG, "IOException in second part");
	//
	//		}
	//
	//		TextElement e4 = new TextElement(TextElement.Type.IMAGE, caption2, file3.getAbsolutePath());
	//		retVals.add(e4);
	//
	//		//		Bitmap bmp3 = BitmapFactory.decodeResource(this.getResources(), R.raw.figure_3);
	//		//		ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
	//		//		bmp3.compress(Bitmap.CompressFormat.PNG, 100, stream3);
	//		//		byte[] byteArray3 = stream3.toByteArray();
	//
	//		File file4 = new File(dir, "figure_3.png");
	//
	//		//manually write the audio file to the external to emulate it being downloaded
	//		InputStream fIn4 = getBaseContext().getResources().openRawResource(R.raw.figure_3);
	//		byte[] buffer4 = null;
	//		try {
	//			int size4 = fIn4.available();
	//			buffer4 = new byte[size4];
	//			fIn4.read(buffer4);
	//			fIn4.close();
	//		} catch (IOException e) {
	//			Log.e(TAG, "IOException first part");
	//
	//		}
	//
	//		FileOutputStream save4;
	//		try {
	//			save4 = new FileOutputStream(file4);
	//			save4.write(buffer4);
	//			save4.flush();
	//			save4.close();
	//		} catch (FileNotFoundException e) {
	//			Log.e(TAG, "FileNotFoundException in second part");
	//
	//		} catch (IOException e) {
	//			Log.e(TAG, "IOException in second part");
	//
	//		}
	//
	//		TextElement e5 = new TextElement(TextElement.Type.IMAGE, caption3, file4.getAbsolutePath());
	//		retVals.add(e5);
	//
	//		return retVals;
	//	}




	//we will use this to broadcast to the app when the cards are loaded
	/**
	 * Broadcast cards loaded.
	 *
	 * @param context the context
	 * @param serverName the server name
	 */
	public static void broadcastCardsLoaded(Context context, String serverName) {
		Intent intent = new Intent("cards_loaded");
		intent.putExtra("server_used", serverName);
		context.sendBroadcast(intent);
		Log.d(TAG + "broadcast", "cards loaded broadcasted");
	}


	/* (non-Javadoc)
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}





}
