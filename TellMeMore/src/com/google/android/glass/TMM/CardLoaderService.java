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
	
	
	
	
	/////////////////////

	//unused, we will store in DB as JDO and not our own binary blob implementation
	public static final String caption1 = "Caption for photo 1";
	public static final String caption2 = "Caption for photo 2";
	public static final String photo_sentinel = "@@";
	public static final String caption_sentinel = "}{";

	public static final String[] text_above_pic = new String[]{"Arma virumque cano, Troiae qui primus ab oris", "Italiam, fato profugus, Laviniaque venit", "litora, multum ille et terris iactatus et alto", "vi superum saevae memorem Iunonis ob iram;", "multa quoque et bello passus, dum conderet urbem,", "inferretque deos Latio, genus unde Latinum,", "Albanique patres, atque altae moenia Romae."};
	public static final String[] text_below_pic = new String[]{ "Musa, mihi causas memora, quo numine laeso,", "quidve dolens, regina deum tot volvere casus", "insignem pietate virum, tot adire labores", "impulerit. Tantaene animis caelestibus irae?"};

	////////////////


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
		loadDBWithSamples();
		return 0;
	}
	
	private void loadDBWithSamples(){
		Server source1 = new Server("CardloaderService's sample card generator first 'server'", "none-its not a network server", System.currentTimeMillis(), System.currentTimeMillis());
		Server source2 = new Server("CardloaderService's sample card generator second 'server'", "none-its not a network server", System.currentTimeMillis(), System.currentTimeMillis());
		
		
		
		TextCard textCard1 = TextCard(0, 100, "About the author", "Name: A. Student", String line2, String line3, ArrayList<TextElement> content, Server source)
		
		
		
		
	}

	
	private void getSampleArr1(){
		ArrayList<String> textBlocks = new ArrayList<String>();
		ArrayList<String> captions = new ArrayList<String>();
		ArrayList<byte[]> pics = new ArrayList<byte[]>();

		ArrayList<TextElement> retVals = new ArrayList<TextElement>();
		TextElement e1;
		for(int i = 0; i < text_above_pic.length; i++){
			e1 = new TextElement(TextElement.Type.TEXT_, text_above_pic[i]);
			retVals.add(e1);
		}



		Bitmap bmp1 = BitmapFactory.decodeResource(this.getResources(), R.raw.pic1);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp1.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();

		TextElement e2 = new TextElement(TextElement.Type.IMAGE, caption1, byteArray);
		retVals.add(e2);

		TextElement e3;
		for(int i = 0; i < text_below_pic.length; i++){
			 e3 = new TextElement(TextElement.Type.TEXT_, text_below_pic[i]);
			retVals.add(e3);
		}

		Bitmap bmp2 = BitmapFactory.decodeResource(this.getResources(), R.raw.pic2);
		ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
		bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
		byte[] byteArray2 = stream2.toByteArray();

		TextElement e4 = new TextElement(TextElement.Type.IMAGE, caption2, byteArray2);
		retVals.add(e4);
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

	private ArrayList<TextElement> getSampleArr(){
		ArrayList<String> textBlocks = new ArrayList<String>();
		ArrayList<String> captions = new ArrayList<String>();
		ArrayList<byte[]> pics = new ArrayList<byte[]>();

		ArrayList<TextElement> retVals = new ArrayList<TextElement>();
		TextElement e1;
		for(int i = 0; i < text_above_pic.length; i++){
			e1 = new TextElement(TextElement.Type.TEXT_, text_above_pic[i]);
			retVals.add(e1);
		}



		Bitmap bmp1 = BitmapFactory.decodeResource(this.getResources(), R.raw.pic1);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp1.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();

		TextElement e2 = new TextElement(TextElement.Type.IMAGE, caption1, byteArray);
		retVals.add(e2);

		TextElement e3;
		for(int i = 0; i < text_below_pic.length; i++){
			 e3 = new TextElement(TextElement.Type.TEXT_, text_below_pic[i]);
			retVals.add(e3);
		}

		Bitmap bmp2 = BitmapFactory.decodeResource(this.getResources(), R.raw.pic2);
		ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
		bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
		byte[] byteArray2 = stream2.toByteArray();

		TextElement e4 = new TextElement(TextElement.Type.IMAGE, caption2, byteArray2);
		retVals.add(e4);

		for(int k = 0; k < textBlocks.size(); k++){
			Log.i(TAG, "contents of text block: " + k + ": " + textBlocks.get(k));
		}

		for(int k = 0; k < textBlocks.size(); k++){
			Log.i(TAG, "contents of caption : " + k + ": " + captions.get(k));
		}

		return retVals;


	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}





}
