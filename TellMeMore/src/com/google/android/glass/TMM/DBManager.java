package com.google.android.glass.TMM;

/*
 * File: DBManager.java
 * Author: Alexander Meijer
 * Date: Sept 5, 2013
 * Class: ELEC 602 Mobile Computing
 * Version 2.0
 * 
 * MODIFIED FOR ELEC429 SP14 A MEIJER
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.replicator.Replication;
import com.google.android.glass.TMM.TextElement.Type;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

@SuppressLint("DefaultLocale")
public class DBManager implements Replication.ChangeListener{

	private TellMeMoreApplication app; 
	private Database database;
	private Manager manager;
	private String dbName;
	private Context context;
	public static final String TAG = "TMM: DBMANAGER";
	public static final String MASTER_SERVER_URL = "http://134.82.132.99";
	public static final String LOCAL_DB_URL = "http://127.0.0.1";
	public static final String UUID_GETTER_PATH = "_uuids";
	public static final int port = 5984;
	public static final String DB_ARRAY = "rows";
	public static final String DOC_ID = "id";
	public static final String TYPE = "jsontype";
	public static final String HANDLE = "handle";
	public static final String UUID = "uuId";
	public static final String PRIORITY = "priority";
	public static final String TITLE = "title";
	public static final String SOURCE = "source";
	public static final String SERVER_NAME = "name";
	public static final String SERVER_API_INFO = "api_info";
	public static final String SERVER_FIRST_USED = "first_used";
	public static final String SERVER_LAST_USED = "last_used";
	public static final String AUDIO_BG_PATH = "backgroundPath";
	public static final String AUDIO_LENGTH = "lengthMillis";
	public static final String AUDIO_CLIP_PATH = "audioClipPath";
	public static final String TEXT_LINE1 = "line1";
	public static final String TEXT_LINE2 = "line2";
	public static final String TEXT_LINE3 = "line3";
	public static final String TEXT_CONTENTS = "contents";
	public static final String TEXT_CONTENT_TYPE = "type";
	public static final String TEXT_CONTENT_TEXT = "text";
	public static final String TEXT_CONTENT_IMG = "img";
	public static final String VIDEO_SS_PATH = "screenshotPath";
	public static final String VIDEO_PLAY_COUNT = "playCount";
	public static final String VIDEO_YOUTUBE_TAG = "yttag";

	public DBManager(Context context) throws IOException{
		this.context = context;
		app = ((TellMeMoreApplication)context.getApplicationContext());
		manager = new Manager(new AndroidContext(context).getFilesDir(), Manager.DEFAULT_OPTIONS);
	}

	public String getName() {
		return this.dbName;
	}

	public boolean open(String dbName, Context context) throws IOException, CouchbaseLiteException{
		this.dbName = dbName;
		startCBLListener(port);
		startDatabase(manager, dbName);
		this.context = context;

		//sync up database here
		startSync(false);
		return true;
	}

	private int startCBLListener(int suggestedListenPort) throws IOException, CouchbaseLiteException {

		startDatabase(manager, dbName);

		LiteListener listener = new LiteListener(manager, suggestedListenPort);
		int port = listener.getListenPort();
		Thread thread = new Thread(listener);
		thread.start();

		return port;

	}

	protected void startDatabase(Manager manager, String databaseName) throws CouchbaseLiteException {
		database = manager.getDatabase(databaseName);
		database.open();
	}


	public void close(){
		database.close();
	}


	private void startSync(boolean deleteDBbeforeUpdate) {

		if(deleteDBbeforeUpdate){
			this.deleteDB(true);
		}
		
		URL syncUrl;
		try {
			syncUrl = new URL(MASTER_SERVER_URL + ":" + port +"/"+ dbName);

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		Replication pullReplication = database.createPullReplication(syncUrl);
		pullReplication.setCreateTarget(true);
		pullReplication.setContinuous(false);

		//Replication pushReplication = database.createPushReplication(syncUrl);
		//pullReplication.setCreateTarget(true);
		//pushReplication.setContinuous(false);

		pullReplication.start();
		//pushReplication.start();

		pullReplication.addChangeListener(this);
		//pushReplication.addChangeListener(this);

	}

	//we will use this to broadcast to the app when the cards are loaded
	public static void broadcastCardsLoaded(Context context, String serverName) {
		Intent intent = new Intent("cards_loaded");
		intent.putExtra("server_used", serverName);
		context.sendBroadcast(intent);
		Log.d(TAG + "broadcast", "cards loaded broadcasted");
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void changed(Replication.ChangeEvent event) {


		Replication replication = event.getSource();
		Log.d(TAG, "Replication : " + replication + " changed.");
		if (!replication.isRunning()) {
			String msg = String.format("Replicator %s not running", replication);

			if(replication.getLastError() == null && replication.isPull()){
				//then it didn't stop on error, so we can broadcast that the database is synced up
				Log.i(TAG, "Replicator finished updating database, broadcasting notification");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {

					e1.printStackTrace();
				}
				Log.i(TAG, "Raw JSON in replication listener: " + getRawJSON("https://127.0.0.1:5984/example_card_generator"));
				Log.i(TAG, "ENTIRE DB AS OF REPLICATION COMPLETION ALERT: " + getEntireDbAsJSON());
				findCardsbyServer();
				broadcastCardsLoaded(context, dbName);
			}


			if(replication.getLastError() != null){
				Log.e(TAG, "REPLICATION FAILED IN CHANGELISTENER");
				replication.restart();
			}

			Log.d(TAG, msg);
		}
		else {
			int processed = replication.getCompletedChangesCount();
			int total = replication.getChangesCount();
			String msg = String.format("Replicator processed %d / %d", processed, total);
			Log.d(TAG, msg);
		}

	}

	/**
	 * Obtains every document in a couchDB in the form of a JSON object
	 * @param serverURLsansPort the URL of the server (without the port)
	 * @param port the port number of the server
	 * @param dbName the name of the database (in this case project)
	 * @return all documents associated with a given database
	 */
	public JSONObject[] getEntireDbAsJSON(String serverURLsansPort, int port, String dbName){
		try {
			// Get the raw JSON of all the names and IDs of each document in a database
			String json = getRawJSON(serverURLsansPort + ":" + port + "/" + dbName + "/_all_docs");

			// Obtain the UUIDs of each document in the database by JSON parsing
			JSONObject tempJSON = new JSONObject(json);
			JSONArray contents = tempJSON.getJSONArray(DB_ARRAY);
			String[] dbUUID = new String[contents.length()];
			for (int i = 0; i < contents.length(); i++) {
				dbUUID[i] = contents.getJSONObject(i).getString(DOC_ID);
			}		

			// Get the raw JSON of all the documents in a database
			JSONObject[] result = new JSONObject[dbUUID.length];
			for (int i = 0; i < dbUUID.length; i++) {
				result[i] = getDocumentAsJSON(serverURLsansPort, port, dbName, dbUUID[i]);
			}
			return result;
		} catch (JSONException e) {
			Log.e(TAG, "getEntireDbAsJSON crashed and burned");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Obtains a single document from a couchDB with a given
	 * database name and UUID
	 * @param serverURLsansPort the URL of the server (without the port)
	 * @param port the port number of the server
	 * @param dbName the name of the database (in this case project)
	 * @param UUID the ID of the requested document
	 * @return the requested document in the form of a JSON Object. Returns null if creation fails.
	 */
	public JSONObject getDocumentAsJSON(String serverURLsansPort, int port, String dbName, String UUID) {
		try {
			// Get the raw JSON of the requested document
			String json = getRawJSON(serverURLsansPort + ":" + port + "/" + dbName + "/" + UUID);

			// Convert the raw JSON string into a JSON object
			JSONObject result;
			result = new JSONObject(json);
			return result;
		} catch (JSONException e) {
			Log.e(TAG, "getDocumentAsJSON crashed and burned");
			return null;
		}
	}

	/**
	 * Retrieves an unparsed JSON string from a couchDB database
	 * @param URL the URL of the document wanted from a database
	 * @return the requested raw, unparsed JSON string. Returns null if retrieval fails.
	 */
	public String getRawJSON(String URL) {
		// Create a new HttpClient and get Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet everythingGetter = new HttpGet(URL);

		// Execute the get and record the response
		HttpResponse response = null;
		try {
			response = httpclient.execute(everythingGetter);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "error getting the DB as JSON", e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "IO error getting the DB as JSON", e);
			return null;
		}

		Log.i(TAG, "server response to all database get: " + response);

		// Parse the response into a String 
		HttpEntity resEntityGet = response.getEntity();
		String json;
		try {
			json = new String(EntityUtils.toString(resEntityGet));
		} catch (ParseException e) {
			Log.e(TAG, "Error parsing response into a string", e);
			return null;
		} catch (IOException e) {
			Log.e(TAG, "IO Error parsing response into a string", e);
			return null;
		}

		return json;
	}

	/**
	 * Creates a TMMCard object from the JSONObject
	 * @param obj the JSONObject to create the TMMCard
	 * @return the TMMCard created from parsing the JSONObject. Returns null if creation fails.
	 */
	public TMMCard convertJSONToCard (JSONObject obj) {
		TMMCard result = null;

		// Obtain type of card to be created
		String type;
		try {
			type = obj.getString(TYPE);
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(TAG,"Cast failed");
			return null;
		}

		// Create correct type of card
		if (type.equalsIgnoreCase("AUDIO"))
			result = convertJSONToAudioCard(obj);
		if (type.equalsIgnoreCase("VIDEO"))
			result = convertJSONToVideoCard(obj);
		if (type.equalsIgnoreCase("TEXT"))
			result = convertJSONToTextCard(obj);

		// Correctly put attachments in internal storage
		getAllAttachments(obj, result);

		return result;
	}

	/**
	 * Creates a VideoCard object from the JSONObject
	 * @param obj the JSONObject to create the VideoCard
	 * @return the VideoCard created from parsing the JSONObject. Returns null if creation fails.
	 */
	public VideoCard convertJSONToVideoCard (JSONObject obj) {
		try {
			// Get the Server Object by JSON parsing because Java doesn't like casting
			JSONObject JSONServer = obj.getJSONObject(SOURCE);
			Server sourceServer = new Server(JSONServer.getString(SERVER_NAME), JSONServer.getString(SERVER_API_INFO),
					Long.parseLong(JSONServer.getString(SERVER_FIRST_USED)), Long.parseLong(JSONServer.getString(SERVER_LAST_USED)));

			VideoCard result = new VideoCard(obj.getInt(HANDLE), obj.getString(UUID), obj.getInt(PRIORITY), obj.getString(TITLE),
					obj.getString(VIDEO_SS_PATH), obj.getInt(VIDEO_PLAY_COUNT), obj.getString(VIDEO_YOUTUBE_TAG), sourceServer);

			return result;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates an AudioCard object from the JSONObject
	 * @param obj the JSONObject to create the AudioCard
	 * @return the AudioCard created from parsing the JSONObject. Returns null if creation fails.
	 */
	public AudioCard convertJSONToAudioCard (JSONObject obj) {
		try {
			// Get the Server Object by JSON parsing because Java doesn't like casting
			JSONObject JSONServer = obj.getJSONObject(SOURCE);
			Server sourceServer = new Server(JSONServer.getString(SERVER_NAME), JSONServer.getString(SERVER_API_INFO),
					Long.parseLong(JSONServer.getString(SERVER_FIRST_USED)), Long.parseLong(JSONServer.getString(SERVER_LAST_USED)));

			AudioCard result = new AudioCard(obj.getInt(HANDLE), obj.getString(UUID), obj.getInt(PRIORITY), obj.getString(TITLE),
					obj.getInt(AUDIO_LENGTH), obj.getString(AUDIO_BG_PATH), obj.getString(AUDIO_CLIP_PATH), sourceServer);

			return result;
		} catch (JSONException e) {
			Log.e(TAG, "AudioCard creation crashed and burned");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates a TextCard object from the JSONObject
	 * @param obj the JSONObject to create the TextCard
	 * @return the TextCard created from parsing the JSONObject. Returns null if creation fails.
	 */
	public TextCard convertJSONToTextCard (JSONObject obj) {
		try {
			// Get array of contents for the TextCard
			JSONArray contents = obj.getJSONArray(TEXT_CONTENTS);
			ArrayList<TextElement> cardContent = new ArrayList<TextElement>();

			// Create all cardContents for the text card from JSON
			for (int i = 0; i < contents.length(); i++) {
				// Get the contentType by string parsing because Java doesn't like casting an object from obj.get to an enum
				JSONObject temp = contents.getJSONObject(i);
				Type contentType;
				if (temp.getString(TEXT_CONTENT_TYPE).equals("IMAGE"))
					contentType = Type.IMAGE;
				else
					contentType = Type.TEXT_;

				cardContent.add(new TextElement(contentType, 
						temp.getString(TEXT_CONTENT_TEXT), temp.getString(TEXT_CONTENT_IMG)));
			}

			// Get the Server Object by JSON parsing because Java doesn't like casting
			JSONObject JSONServer = obj.getJSONObject(SOURCE);
			Server sourceServer = new Server(JSONServer.getString(SERVER_NAME), JSONServer.getString(SERVER_API_INFO),
					Long.parseLong(JSONServer.getString(SERVER_FIRST_USED)), Long.parseLong(JSONServer.getString(SERVER_LAST_USED)));

			// Create actual TextCard
			TextCard result = new TextCard(obj.getInt(HANDLE), obj.getString(UUID), obj.getInt(PRIORITY), obj.getString(TITLE),
					obj.getString(TEXT_LINE1), obj.getString(TEXT_LINE2), obj.getString(TEXT_LINE3), cardContent, sourceServer);
			return result;
		} catch (JSONException e) {
			Log.i(TAG,"TextCard creation crashed and burned");
			e.printStackTrace();
			return null;
		}
	}

	private boolean getAllAttachments(JSONObject cardToGet, TMMCard cardWithNoAttachments){
		return true;
	}






	private class DBDeleter extends Thread {
		private String json = "";

		public DBDeleter(){
			super();
		}

		@Override
		public void run() { 

			HttpClient httpclient = new DefaultHttpClient();
			HttpDelete dbdeleter = new HttpDelete(LOCAL_DB_URL + ":" + port + "/" + dbName);

			//execute the delete and record the response
			HttpResponse response = null;
			try {
				response = httpclient.execute(dbdeleter);
			} catch (ClientProtocolException e) {
				Log.e(TAG, "error deleting document", e);
				return;
			} catch (IOException e) {
				Log.e(TAG, "IO error delting document", e);
				return;
			}

			//parse the reponse 
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String json = null;
			try {
				json = reader.readLine();
				Log.d(TAG, "delete DB, Raw json string: " + json);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.i(TAG, "deletedb method, server response is: " + json);

		}



	}



	public boolean deleteDB(boolean areYouSure){
		if(!areYouSure){
			Log.d(TAG, "User wasn't sure");
			return false;
		}


		DBDeleter deleter = new DBDeleter();

		deleter.start();
		try {
			deleter.join();
		} catch (InterruptedException e) {

			e.printStackTrace();
			return false;
		}
		return true;
	}








	//TODO
	public TMMCard findCardById(long id){
		return null;//placeholder to allow for compilation
	}

	public JSONObject getJSONRepresentation(TMMCard toCheck){

		if(toCheck.getuuId() == null){
			return null;
		}

		final String exampleUUID = "f2abdb8f1fb14601b9e149cd67035d8a"; 

		if(toCheck.getuuId().length() != exampleUUID.length()){
			return null;
		}

		//then we must have a good UUID
		// Create a new HttpClient and get Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet cardGetter = new HttpGet(LOCAL_DB_URL + ":" + port + "/" + dbName + "/" + toCheck.getuuId());

		//execute the put and record the response
		HttpResponse response = null;
		try {
			response = httpclient.execute(cardGetter);
		} catch (ClientProtocolException e) {

			Log.e(TAG, "error getting JSON card", e);
		} catch (IOException e) {
			Log.e(TAG, "IO error getting JSON card", e);
		}

		Log.i(TAG, "server response to card get: " + response);

		//parse the reponse 
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String json = null;
		try {
			json = reader.readLine();
			Log.d(TAG, "Raw json string: " + json);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(json);
		} catch (JSONException e1) {
			Log.e(TAG, "error making json object", e1);
		}



		String errorResult = null;
		String reason = null;
		try {
			errorResult = jsonObject.getString("error");
			reason = jsonObject.getString("reason");
			Log.d(TAG, "json representation error result is: " + errorResult);
			Log.w(TAG, "card JSON represetnation CREATION FAILURE -" + reason);
			return null;
		} catch (JSONException e) {
			Log.i(TAG, "no error code detected in response");
			errorResult = null;
			return jsonObject;

		}
	}


	//returns the message as it exists in the DB
	public synchronized boolean addCard(TMMCard toAdd) throws Exception{

		//check to make sure card doesn't already exist in DB
		if(this.getJSONRepresentation(toAdd) != null){
			//maybe we could update the card or something
			//I'm going to punt on this, leave it as a TODO
			throw new Exception("The card already exists in the DB, please use the update method instead");

		}

		//if we made it here, safe to assume that the card doesn't exist in the DB
		//get a UUID to use from couch
		String uuid = getUUID();
		toAdd.setuuId(uuid);

		ObjectMapper mapper = new ObjectMapper();
		Log.d(TAG, "JSON'd value: " + mapper.writeValueAsString(toAdd));


		// Create a new HttpClient and put Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut cardMaker = new HttpPut(LOCAL_DB_URL + ":" + port + "/" + dbName + "/" + uuid);
		cardMaker.addHeader("Content-Type", "application/json");
		cardMaker.addHeader("Accept", "application/json");
		cardMaker.setEntity(new StringEntity(mapper.writeValueAsString(toAdd)));

		//execute the put and record the response
		HttpResponse response = null;
		try {
			response = httpclient.execute(cardMaker);
		} catch (ClientProtocolException e) {

			Log.e(TAG, "error card adding", e);
		} catch (IOException e) {
			Log.e(TAG, "IO error card adding", e);
		}

		Log.i(TAG, "server response to put: " + response);

		//parse the reponse 
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String json = null;
		try {
			json = reader.readLine();
			Log.d(TAG, "Raw json string: " + json);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(json);
		} catch (JSONException e1) {
			Log.e(TAG, "error making json object", e1);
		}

		//upload attachments
		uploadCardAttachments(toAdd, jsonObject);

		String okResult = "";
		try {
			okResult = jsonObject.getString("ok");
		} catch (JSONException e) {
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
			startSync(false);
			return true;
		}else {
			Log.w(TAG, "CREATION FAILURE -" + reason);
			return false; 
		}

	}

	private String getUUID(){

		// Create a new HttpClient and get Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet uuidGetter = new HttpGet(LOCAL_DB_URL + ":" + port + "/" + UUID_GETTER_PATH);

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
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String json = null;
		try {
			json = reader.readLine();
			Log.d(TAG, "Raw json string: " + json);
		} catch (IOException e) {
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

	private boolean uploadCardAttachments(TMMCard toAdd, JSONObject jsonObject) throws IOException{
		//will need case statement for each subclass of TMMCard
		if(toAdd instanceof VideoCard){
			if(((VideoCard) toAdd).hasScreenshot()){
				Bitmap bmp = BitmapFactory.decodeFile(((VideoCard)toAdd).getScreenshotPath());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
				Log.d(TAG, "video card background image being uploaded: " + ((VideoCard) toAdd).getScrenshotname());
				uploadSingleAttachment(out.toByteArray(), jsonObject, ((VideoCard) toAdd).getScrenshotname(), "image/jpg");
			}
			return true;
		} else if(toAdd instanceof AudioCard) {
			if(((AudioCard) toAdd).hasBackground()){
				Bitmap bmp = BitmapFactory.decodeFile(((AudioCard)toAdd).getBackgroundPath());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
				Log.d(TAG, "filename audio background image being uploaded: " + ((AudioCard) toAdd).getBackground_name());
				uploadSingleAttachment(out.toByteArray(), jsonObject, ((AudioCard) toAdd).getBackground_name(), "image/jpg");
				//update the parameter
				jsonObject = getJSONRepresentation(toAdd);
			}

			if(((AudioCard) toAdd).hasAudio()){
				File audio = new File(((AudioCard) toAdd).getAudioClipPath());
				FileInputStream fileInputStream = new FileInputStream(audio);
				byte[] b = new byte[(int) audio.length()];
				fileInputStream.read(b);
				fileInputStream.close();
				uploadSingleAttachment(b, jsonObject, ((AudioCard) toAdd).getAudioClipName(), "audio/mpeg");
			}


			return true;
		} else if(toAdd instanceof TextCard) {
			//for a text card, first we store the icon if it exists
			if(((TextCard) toAdd).hasIcon()){
				Bitmap bmp = BitmapFactory.decodeFile(((TextCard)toAdd).getIconPath());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
				Log.d(TAG, "filename of icon image being uploaded: " + ((TextCard) toAdd).getIcFileName());
				uploadSingleAttachment(out.toByteArray(), jsonObject, ((TextCard) toAdd).getIcFileName(), "image/jpg");

			}

			//now, we need to upload any images in this file
			//start by getting the JSON representation

			JSONObject respObj = getJSONRepresentation(toAdd);

			for(int i = 0; i < ((TextCard) toAdd).getContents().size(); i++){
				TextElement temp = ((TextCard) toAdd).getContents().get(i);
				//we only care about uploading images
				if(temp.getType() == Type.IMAGE){
					Bitmap bmp = BitmapFactory.decodeFile(temp.getImg());
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
					Log.d(TAG, "filename of textelement image being uploaded: " + temp.getImgFilename());
					uploadSingleAttachment(out.toByteArray(), respObj, temp.getImgFilename(), "image/jpg");
					//confirm revision occured, and update our rev number
					respObj = getJSONRepresentation(toAdd); 
				}

			}


			return true;
		} else return false;

	}

	private boolean uploadSingleAttachment(byte[] data, JSONObject jsonObject, String fileName, String contentType){

		final String exRev = "1-4ce605cd9fac335e98662dd4645cd332";
		final String exUUID = "c629e32ea1c54b9b0840f0161000706e";

		if(jsonObject == null) {
			Log.w(TAG, "null jsonobject passed to uploadsingleattachmnet");
			return false;
		}
		//parse the JSONObject to get the info that we need

		Log.i(TAG, "ul single attach, raw json object passed in: " + jsonObject);
		String uuid;
		try {
			uuid = jsonObject.getString("_id");
		} catch (JSONException e3) {
			try {
				uuid = jsonObject.getString("id");
			} catch (JSONException e) {

				e.printStackTrace();
				return false;
			}

		}
		String revNo;
		try {
			revNo = jsonObject.getString("_rev");
		} catch (JSONException e2) {
			try {
				revNo = jsonObject.getString("rev");
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}

		}

		if(revNo.length() != exRev.length()){
			Log.w(TAG, "No revision found in uploadsingleattachment");
			return false;
		}

		if(uuid.length() != exUUID.length()){
			Log.w(TAG, "No UUID found in uploadsingleattachment");
			return false;
		}

		// Create a new HttpClient and put Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut attachmentAdder = new HttpPut(LOCAL_DB_URL + ":" + port + "/" + dbName + "/" + uuid + "/" + fileName + "?rev=" + revNo);

		if(contentType != null && !contentType.equalsIgnoreCase("")){
			attachmentAdder.addHeader("Content-Type", contentType);
		}

		ByteArrayEntity attachEnt = new ByteArrayEntity(data);
		attachmentAdder.setEntity(attachEnt);

		//execute the put and record the response
		HttpResponse response = null;
		try {
			response = httpclient.execute(attachmentAdder);
		} catch (ClientProtocolException e) {

			Log.e(TAG, "error attachment adding", e);
		} catch (IOException e) {
			Log.e(TAG, "IO error attachment adding", e);
		}

		Log.i(TAG, "server response to put: " + response);

		//parse the reponse 
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String json = null;
		try {
			json = reader.readLine();
			Log.d(TAG, "Raw json string: " + json);
		} catch (IOException e) {
			e.printStackTrace();
		}

		JSONObject jsonObjectresp = null;
		try {
			jsonObjectresp = new JSONObject(json);
		} catch (JSONException e1) {
			Log.e(TAG, "error making json object", e1);
		}


		String okResult = "";
		try {
			okResult = jsonObjectresp.getString("ok");
		} catch (JSONException e) {
			e.printStackTrace();
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

	private class JSONGetter extends Thread {
		private String json = "";

		public JSONGetter(){
			super();
		}

		@Override
		public void run() { 

			//then we must have a good UUID
			// Create a new HttpClient and get Header
			HttpClient httpclient = new DefaultHttpClient();
			//
			//HttpGet everythingGetter = new HttpGet(LOCAL_DB_URL + ":" + port + "/" + dbName + "/_all_docs?include_docs=true");
			HttpGet everythingGetter = new HttpGet(LOCAL_DB_URL + ":" + port + "/" + dbName);
			//execute the put and record the response
			HttpResponse response = null;
			try {
				response = httpclient.execute(everythingGetter);
			} catch (ClientProtocolException e) {

				Log.e(TAG, "error getting the DB as JSON", e);
			} catch (IOException e) {
				Log.e(TAG, "IO error getting the DB as JSON", e);
			}

			Log.i(TAG, "server response to all database get: " + response);

			//parse the reponse 
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//String json = null;
			try {
				json = reader.readLine();
				Log.d(TAG, "Raw json string: " + json);
			} catch (IOException e) {
				e.printStackTrace();
			}


		}

		public String getJson() {
			return json;
		}

	}

	public String getEntireDbAsJSON(){

		JSONGetter jsonGetter = new JSONGetter();

		jsonGetter.start();
		try {
			jsonGetter.join();
		} catch (InterruptedException e) {

			e.printStackTrace();
			return null;
		}

		Log.d(TAG, "String being returned from getEntireDBasJSON " +  jsonGetter.getJson());
		return jsonGetter.getJson();

	}


	public synchronized boolean deleteCardfromDB(TMMCard todel){

		//perform sanity checks on the UUID
		final String exUUID = "c629e32ea1c54b9b0840f0161000706e";

		if(todel.getuuId() == null) {
			Log.w(TAG, "bad UUID passed to deletecardfromdb");
			return false;
		}


		if(todel.getuuId().length() != exUUID.length()){
			Log.w(TAG, "bad UUID passed to deletecardfromdb");
			return false;
		}

		//use the UUID to get the current REV
		JSONObject jsonObject =  getJSONRepresentation(todel);

		if(jsonObject == null){
			Log.e(TAG, "no JSON retreived for the card with UUID " + todel.getuuId() + " in deletecardfromDB");
		}

		//parse the JSONObject to get the info that we need

		Log.i(TAG, "delete card from DB, card to delete info: " + jsonObject);
		String uuid;
		try {
			uuid = jsonObject.getString("_id");
		} catch (JSONException e3) {
			try {
				uuid = jsonObject.getString("id");
			} catch (JSONException e) {

				e.printStackTrace();
				return false;
			}

		}
		String revNo;
		try {
			revNo = jsonObject.getString("_rev");
		} catch (JSONException e2) {
			try {
				revNo = jsonObject.getString("rev");
			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}

		}
		HttpClient httpclient = new DefaultHttpClient();
		HttpDelete uuidGetter = new HttpDelete(LOCAL_DB_URL + ":" + port + "/" + dbName + "/" + uuid + "?rev=" + revNo);

		//execute the delete and record the response
		HttpResponse response = null;
		try {
			response = httpclient.execute(uuidGetter);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "error deleting document", e);
		} catch (IOException e) {
			Log.e(TAG, "IO error delting document", e);
		}

		//parse the reponse 
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		} catch (IllegalStateException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}
		String json = null;
		try {
			json = reader.readLine();
			Log.d(TAG, "Raw json string: " + json);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.i(TAG, "deletecardfromdb method, server response is: " + json);
		startSync(false);

		return true;



	}

	public boolean createNewDB(String dbname) throws IllegalStateException, IOException{
		// Create a new HttpClient and put Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut dbMaker = new HttpPut(LOCAL_DB_URL + ":" + port + "/" + dbname);

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
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String json = null;
		try {
			json = reader.readLine();
			Log.d(TAG, "Raw json string: " + json);
		} catch (IOException e) {
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

			e.printStackTrace();
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
			startSync(false);
			return true;
		}else {
			Log.w(TAG, "CREATION FAILURE -" + reason);
			return false; 
		}
	}

	//return ALL cards from all servers, sorted by priority
	//TODO
	public synchronized ArrayList<TMMCard> getAllCards(){
		Log.d(TAG, "get all cards");

		ArrayList<TMMCard> matches = new ArrayList<TMMCard>();


		//sort cards by priority
		Collections.sort(matches);

		return matches;
	}


	public ArrayList<TMMCard> findCardsbyServer(){

		ArrayList<TMMCard> cardlist = new ArrayList<TMMCard>();

		try {
			JSONObject[] documents = getEntireDbAsJSON(LOCAL_DB_URL, 5984, dbName);
			for (int i = 0; i < documents.length; i++) {
				TMMCard result = convertJSONToCard(documents[i]);

				//add new card to result list
				cardlist.add(result);
				if (result != null) {
					String temp = "TMM";
					if (result instanceof AudioCard) temp = "Audio";
					if (result instanceof VideoCard) temp = "Video";
					if (result instanceof TextCard) temp = "Text";
					Log.i(TAG, temp + "Card Created:\nTitle: " + result.getTitle() + 
							"\nID: " + result.getuuId() + "\n");
				}
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} 

		//sort
		Collections.sort(cardlist);
		return cardlist;


	}

}