package com.example.dbwriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	public static final String TAG = "DBwriter mainactivity";
	public static final String EXAMPLE_CARD_SERVER = "example_card_generator";
	public static final String UUID_GETTER_PATH = "_uuids";
	ArrayList<TMMCard> cardz = new ArrayList<TMMCard>();
	ArrayList<Server> servz = new ArrayList<Server>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		loadCards();

		//phase 1: create DB with server name
		new Thread(new Runnable() {
			public void run() {
				try {
					createNewDB("http://192.168.1.2", 5984, servz.get(0).getName());
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();


		//phase 2: store some cards on it, no attachments
		try {
			addCardToDB(cardz.get(0), servz.get(0).getName());
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}





	}

	public boolean addCardToDB(TMMCard toAdd, String serverURLsansPort, int port, String dbName) throws JsonGenerationException, JsonMappingException, IOException{

		//get a UUID to use from couch
		String uuid = getUUID(serverURLsansPort, port);

		ObjectMapper mapper = new ObjectMapper();
		Log.d(TAG, "JSON'd value: " + mapper.writeValueAsString(toAdd));


		return true; //placeholder
	}

	private String getUUID(String serverURLsansPort, int port){
		
		// Create a new HttpClient and get Header
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet uuidGetter = new HttpGet(serverURLsansPort + ":" + port + "/" + UUID_GETTER_PATH);

				//execute the put and record the response
				HttpResponse response = null;
				try {
					response = httpclient.execute(uuidGetter);
				} catch (ClientProtocolException e) {

					Log.e(TAG, "error creating DB", e);
				} catch (IOException e) {
					Log.e(TAG, "IO error creating DB", e);
				}

				Log.i(TAG, "server response to uuid get: " + response);

				//parse the reponse 
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String json = null;
				try {
					json = reader.readLine();
					Log.d(TAG, "Raw json string: " + json);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				JSONObject jsonObject = null;
				try {
					jsonObject = new JSONObject(json);
				} catch (JSONException e1) {
					Log.e(TAG, "error making json object", e1);
				}
				JSONArray uuids = new JSONArray();
				try {
					 uuids = jsonObject.getJSONArray("uuids");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				String uuidToRet="";
				try {
					uuidToRet = uuids.getString(0);
				} catch (JSONException e) {
					
					e.printStackTrace();
				}
				
				Log.i(TAG, "Returning UUID: " + uuidToRet);
				return uuidToRet;
				
	}


	public boolean createNewDB(String serverURLsansPort, int port, String dbname) throws IllegalStateException, IOException{
		// Create a new HttpClient and put Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut dbMaker = new HttpPut(serverURLsansPort + ":" + port + "/" + dbname);

		//execute the put and record the response
		HttpResponse response = null;
		try {
			response = httpclient.execute(dbMaker);
		} catch (ClientProtocolException e) {

			Log.e(TAG, "error creating DB", e);
		} catch (IOException e) {
			Log.e(TAG, "IO error creating DB", e);
		}

		Log.i(TAG, "server response to put: " + response);

		//parse the reponse 
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String json = null;
		try {
			json = reader.readLine();
			Log.d(TAG, "Raw json string: " + json);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(json);
		} catch (JSONException e1) {
			Log.e(TAG, "error making json object", e1);
		}

		String okResult = "";
		try {
			okResult = jsonObject.getString("ok");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.i(TAG, "DB reports creation did not go well...");
		}

		String errorResult = null;
		String reason = null;
		try {
			errorResult = jsonObject.getString("error");
			reason = jsonObject.getString("reason");
		} catch (JSONException e) {
			Log.i(TAG, "no error code dtected in response");
			errorResult = null;
		}

		Log.d(TAG, "ok result is: " + okResult);
		Log.d(TAG, "error result is: " + errorResult);

		if(okResult.equalsIgnoreCase("true")){
			return true;
		}else {
			Log.w(TAG, "CREATION FAILURE -" + reason);
			return false; 
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}



	private void loadCards() {
		try {
			Thread.sleep(7000);
		} catch (InterruptedException e3) {
			e3.printStackTrace();
		}
		Server source1 = new Server(EXAMPLE_CARD_SERVER, "none-its not a network server", System.currentTimeMillis(), System.currentTimeMillis());
		//Server source2 = new Server("CardloaderService's sample card generator second 'server'", "none-its not a network server", System.currentTimeMillis(), System.currentTimeMillis());
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmm");
		TextCard textCard1 = new TextCard(0, 100, "About the Author", "A. Student", "", "Tap to read more", getSampleArr1(), source1);

		//			Bitmap bmp2 = BitmapFactory.decodeResource(contexts[0].getResources(), R.raw.acmicon);
		//			ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
		//			bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
		//			byte[] iconArray = stream2.toByteArray();

		File file0 = new File(dir, "acmicon.jpg");

		//manually write the audio file to the external to emulate it being downloaded
		InputStream fIn0 = getBaseContext().getResources().openRawResource(R.raw.acmicon);
		byte[] buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

		FileOutputStream save0;
		try {
			save0 = new FileOutputStream(file0);
			save0.write(buffer0);
			save0.flush();
			save0.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException in second part");

		} catch (IOException e) {
			Log.e(TAG, "IOException in second part");
		}


		TextCard textCard2 = new TextCard(0, 99, "Read Paper Abstract", "From ACM PAUC", "A. Student and Dr. XYZ", "Published 1 Mar 2009",  file0.getAbsolutePath(), getSampleArr2(), source1);

		Log.d(TAG, "text card 2 added, returned: " + cardz.add(textCard2));
		Log.d(TAG, "text card 1 added, returned: " + cardz.add(textCard1));

		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmm/powerpointdemo.mp3";

		dir.mkdirs();

		File file = new File(dir, "powerpointdemo.mp3");

		//manually write the audio file to the external to emulate it being downloaded
		InputStream fIn = getBaseContext().getResources().openRawResource(R.raw.powerpointdemo);
		byte[] buffer = null;
		try {
			int size = fIn.available();
			buffer = new byte[size];
			fIn.read(buffer);
			fIn.close();
		} catch (IOException e2) {
			Log.e(TAG, "IOException first part");

		}

		FileOutputStream save;
		try {
			save = new FileOutputStream(file);
			save.write(buffer);
			save.flush();
			save.close();
		} catch (FileNotFoundException e2) {
			Log.e(TAG, "FileNotFoundException in second part");

		} catch (IOException e1) {
			Log.e(TAG, "IOException in second part");

		}    

		AudioCard audioCard1 = new AudioCard(0, 97, "Hear A Student Narration", file.getAbsolutePath(), source1);
		Log.d(TAG, "audio card 1 added, returned: " + cardz.add(audioCard1));

		VideoCard videoCard1 = new VideoCard(0, 90, "Watch the Experiment", "wtnI3kyCnmA", source1);
		VideoCard videoCard2 = new VideoCard(0, 89, "View the presentation", "cn5mMJiPYmw", source1);
		Log.d(TAG, "video card 1 added, returned: " + cardz.add(videoCard1));
		Log.d(TAG, "video card 2 added, returned: " + cardz.add(videoCard2));

		Log.d(TAG, "server source 1 added, returned: " + servz.add(source1));
	}



	private ArrayList<TextElement> getSampleArr1(){

		String text_above_pic = "A. Student was born in blah blah blah. It is a very interesting place. One of his passions has always been lightning. When he was just 6, he shot a famous photo of a lightning strike which is used on Wikipedia.";
		String text_below_pic = "Later in life, A. Student went to Africa where he photographed all manner of things. In the western sahara, he captured another famous photo of the setting african sun that won him a prize.";
		String caption1 = "\"When Clouds Attack\"";
		String caption2 = "\"A Nice Orange Sunset\"";

		ArrayList<TextElement> retVals = new ArrayList<TextElement>();
		TextElement e1;

		e1 = new TextElement(TextElement.Type.TEXT_, text_above_pic);
		retVals.add(e1);








		//Bitmap bmp1 = BitmapFactory.decodeResource(this.getResources(), R.raw.pic1);
		//ByteArrayOutputStream stream = new ByteArrayOutputStream();
		//bmp1.compress(Bitmap.CompressFormat.PNG, 100, stream);
		//byte[] byteArray = stream.toByteArray();

		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmm");
		dir.mkdirs();

		File file = new File(dir, "pic1.jpg");

		//manually write the audio file to the external to emulate it being downloaded
		InputStream fIn = getBaseContext().getResources().openRawResource(R.raw.pic1);
		byte[] buffer = null;
		try {
			int size = fIn.available();
			buffer = new byte[size];
			fIn.read(buffer);
			fIn.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

		FileOutputStream save;
		try {
			save = new FileOutputStream(file);
			save.write(buffer);
			save.flush();
			save.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException in second part");

		} catch (IOException e) {
			Log.e(TAG, "IOException in second part");

		}



		TextElement e2 = new TextElement(TextElement.Type.IMAGE, caption1, file.getAbsolutePath());
		retVals.add(e2);

		TextElement e3;
		e3 = new TextElement(TextElement.Type.TEXT_, text_below_pic);
		retVals.add(e3);


		//		Bitmap bmp2 = BitmapFactory.decodeResource(this.getResources(), R.raw.pic2);
		//		ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
		//		bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
		//		byte[] byteArray2 = stream2.toByteArray();


		File file2 = new File(dir, "pic2.jpg");

		//manually write the audio file to the external to emulate it being downloaded
		InputStream fIn2 = getBaseContext().getResources().openRawResource(R.raw.pic2);
		byte[] buffer2 = null;
		try {
			int size2 = fIn2.available();
			buffer2 = new byte[size2];
			fIn2.read(buffer2);
			fIn2.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

		FileOutputStream save2;
		try {
			save2 = new FileOutputStream(file2);
			save2.write(buffer2);
			save2.flush();
			save2.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException in second part");

		} catch (IOException e) {
			Log.e(TAG, "IOException in second part");

		}


		TextElement e4 = new TextElement(TextElement.Type.IMAGE, caption2, file2.getAbsolutePath());
		retVals.add(e4);

		return retVals;
	}


	private ArrayList<TextElement> getSampleArr2(){

		String text_above_pic = "In modern systems today, there is not really a good way to differentiate between blah and blahblah. In this paper, we present such and such novel solution. In inital testing, our solution provides several benefits while only incurring a 98% performance overhead.";
		String caption1 = "The experimental data.";
		String caption2 = "The performance results.";
		String caption3 = "The device used to make measurements.";

		ArrayList<TextElement> retVals = new ArrayList<TextElement>();
		TextElement e1;

		e1 = new TextElement(TextElement.Type.TEXT_, text_above_pic);
		retVals.add(e1);
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmm");
		//		Bitmap bmp1 = BitmapFactory.decodeResource(this.getResources(), R.raw.graph_1);
		//		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		//		bmp1.compress(Bitmap.CompressFormat.PNG, 100, stream);
		//		byte[] byteArray = stream.toByteArray();

		File file2 = new File(dir, "graph_1.jpg");

		//manually write the audio file to the external to emulate it being downloaded
		InputStream fIn2 = getBaseContext().getResources().openRawResource(R.raw.graph_1);
		byte[] buffer2 = null;
		try {
			int size2 = fIn2.available();
			buffer2 = new byte[size2];
			fIn2.read(buffer2);
			fIn2.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

		FileOutputStream save2;
		try {
			save2 = new FileOutputStream(file2);
			save2.write(buffer2);
			save2.flush();
			save2.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException in second part");

		} catch (IOException e) {
			Log.e(TAG, "IOException in second part");

		}

		TextElement e2 = new TextElement(TextElement.Type.IMAGE, caption1, file2.getAbsolutePath());
		retVals.add(e2);


		//		Bitmap bmp2 = BitmapFactory.decodeResource(this.getResources(), R.raw.chart2);
		//		ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
		//		bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
		//		byte[] byteArray2 = stream2.toByteArray();

		File file3 = new File(dir, "chart2.png");

		//manually write the audio file to the external to emulate it being downloaded
		InputStream fIn3 = getBaseContext().getResources().openRawResource(R.raw.chart2);
		byte[] buffer3 = null;
		try {
			int size3 = fIn3.available();
			buffer3 = new byte[size3];
			fIn3.read(buffer3);
			fIn3.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

		FileOutputStream save3;
		try {
			save3 = new FileOutputStream(file3);
			save3.write(buffer3);
			save3.flush();
			save3.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException in second part");

		} catch (IOException e) {
			Log.e(TAG, "IOException in second part");

		}

		TextElement e4 = new TextElement(TextElement.Type.IMAGE, caption2, file3.getAbsolutePath());
		retVals.add(e4);

		//		Bitmap bmp3 = BitmapFactory.decodeResource(this.getResources(), R.raw.figure_3);
		//		ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
		//		bmp3.compress(Bitmap.CompressFormat.PNG, 100, stream3);
		//		byte[] byteArray3 = stream3.toByteArray();

		File file4 = new File(dir, "figure_3.png");

		//manually write the audio file to the external to emulate it being downloaded
		InputStream fIn4 = getBaseContext().getResources().openRawResource(R.raw.figure_3);
		byte[] buffer4 = null;
		try {
			int size4 = fIn4.available();
			buffer4 = new byte[size4];
			fIn4.read(buffer4);
			fIn4.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

		FileOutputStream save4;
		try {
			save4 = new FileOutputStream(file4);
			save4.write(buffer4);
			save4.flush();
			save4.close();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "FileNotFoundException in second part");

		} catch (IOException e) {
			Log.e(TAG, "IOException in second part");

		}

		TextElement e5 = new TextElement(TextElement.Type.IMAGE, caption3, file4.getAbsolutePath());
		retVals.add(e5);

		return retVals;
	}




}
