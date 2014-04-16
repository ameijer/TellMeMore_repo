/*
 * File: DBManager.java
 * Date: Sept 5, 2013
 * Originally written for ELEC 602 Mobile Computing by A. Meijer
 * 
 * 
 * Modified for ELEC429, Independent Study by D. Prudente and A. Meijer
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.Header;
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
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

// TODO: Auto-generated Javadoc
/**
 * The Class DBManager.
 */
@SuppressLint("DefaultLocale")
public class DBManager implements Replication.ChangeListener{

	/** The database. */
	private Database database;
	
	/** The manager. */
	private Manager manager;
	
	/** The db name. */
	private String dbName;
	
	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM: DBMANAGER";
	
	/** The Constant MASTER_SERVER_URL. */
	public static final String MASTER_SERVER_URL = "http://134.82.132.99";
	
	/** The Constant LOCAL_DB_URL. */
	public static final String LOCAL_DB_URL = "http://127.0.0.1";
	
	/** The Constant UUID_GETTER_PATH. */
	public static final String UUID_GETTER_PATH = "_uuids";
	
	/** The Constant port. */
	public static final int port = 5984;
	
	/** The Constant DB_ARRAY. */
	public static final String DB_ARRAY = "rows";
	
	/** The Constant DOC_ID. */
	public static final String DOC_ID = "id";
	
	/** The Constant TYPE. */
	public static final String TYPE = "jsontype";
	
	/** The Constant HANDLE. */
	public static final String HANDLE = "handle";
	
	/** The Constant UUID. */
	public static final String UUID = "uuId";
	
	/** The Constant PRIORITY. */
	public static final String PRIORITY = "priority";
	
	/** The Constant TITLE. */
	public static final String TITLE = "title";
	
	/** The Constant SOURCE. */
	public static final String SOURCE = "source";
	
	/** The Constant SERVER_NAME. */
	public static final String SERVER_NAME = "name";
	
	/** The Constant SERVER_API_INFO. */
	public static final String SERVER_API_INFO = "api_info";
	
	/** The Constant SERVER_FIRST_USED. */
	public static final String SERVER_FIRST_USED = "first_used";
	
	/** The Constant SERVER_LAST_USED. */
	public static final String SERVER_LAST_USED = "last_used";
	
	/** The Constant AUDIO_BG_PATH. */
	public static final String AUDIO_BG_PATH = "backgroundPath";
	
	/** The Constant AUDIO_LENGTH. */
	public static final String AUDIO_LENGTH = "lengthMillis";
	
	/** The Constant AUDIO_CLIP_PATH. */
	public static final String AUDIO_CLIP_PATH = "audioClipPath";
	
	/** The Constant AUDIO_BACKGROUND_PATH. */
	public static final String AUDIO_BACKGROUND_PATH = "backgroundPath";
	
	/** The Constant TEXT_CARD_IC_NAME. */
	public static final String TEXT_CARD_IC_NAME = "icFileName";
	
	/** The Constant TEXT_CARD_IC_PATH. */
	public static final String TEXT_CARD_IC_PATH = "iconPath";
	
	/** The Constant TEXT_LINE1. */
	public static final String TEXT_LINE1 = "line1";
	
	/** The Constant TEXT_LINE2. */
	public static final String TEXT_LINE2 = "line2";
	
	/** The Constant TEXT_LINE3. */
	public static final String TEXT_LINE3 = "line3";
	
	/** The Constant TEXT_CONTENTS. */
	public static final String TEXT_CONTENTS = "contents";
	
	/** The Constant TEXT_CONTENT_TYPE. */
	public static final String TEXT_CONTENT_TYPE = "type";
	
	/** The Constant TEXT_CONTENT_TEXT. */
	public static final String TEXT_CONTENT_TEXT = "text";
	
	/** The Constant TEXT_CONTENT_IMG. */
	public static final String TEXT_CONTENT_IMG = "img";
	
	/** The Constant VIDEO_SS_PATH. */
	public static final String VIDEO_SS_PATH = "screenshotPath";
	
	/** The Constant VIDEO_PLAY_COUNT. */
	public static final String VIDEO_PLAY_COUNT = "playCount";
	
	/** The Constant VIDEO_YOUTUBE_TAG. */
	public static final String VIDEO_YOUTUBE_TAG = "yttag";
	
	/** The synced. */
	private boolean synced;
	
	/** The context. */
	private Context context;
	
	/** The app. */
	private TellMeMoreApplication app;

	/**
	 * Checks if is synced.
	 *
	 * @return true, if is synced
	 */
	public boolean isSynced(){
		return synced;
	}
	
	/**
	 * Instantiates a new DB manager.
	 *
	 * @param context the context
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public DBManager(Context context) throws IOException{
		this.context = context;
		app = ((TellMeMoreApplication)context.getApplicationContext());
		manager = new Manager(new AndroidContext(context).getFilesDir(), Manager.DEFAULT_OPTIONS);
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.dbName;
	}

	/**
	 * Open.
	 *
	 * @param dbName the db name
	 * @param context the context
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws CouchbaseLiteException the couchbase lite exception
	 */
	public boolean open(String dbName, Context context) throws IOException, CouchbaseLiteException{
		this.synced = false;
		this.dbName = dbName;
		startCBLListener(port);
		startDatabase(manager, dbName);
		this.context = context;

		//sync up database here
		startSync(false);
		return true;
	}

	/**
	 * Start cbl listener.
	 *
	 * @param suggestedListenPort the suggested listen port
	 * @return the int
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws CouchbaseLiteException the couchbase lite exception
	 */
	private int startCBLListener(int suggestedListenPort) throws IOException, CouchbaseLiteException {

		startDatabase(manager, dbName);

		LiteListener listener = new LiteListener(manager, suggestedListenPort);
		int port = listener.getListenPort();
		Thread thread = new Thread(listener);
		thread.start();

		return port;

	}

	/**
	 * Start database.
	 *
	 * @param manager the manager
	 * @param databaseName the database name
	 * @throws CouchbaseLiteException the couchbase lite exception
	 */
	protected void startDatabase(Manager manager, String databaseName) throws CouchbaseLiteException {
		database = manager.getDatabase(databaseName);
		database.open();
	}


	/**
	 * Close.
	 */
	public void close(){
		database.close();
	}


	/**
	 * Start sync.
	 *
	 * @param deleteDBbeforeUpdate the delete d bbefore update
	 */
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

	/* (non-Javadoc)
	 * @see com.couchbase.lite.replicator.Replication.ChangeListener#changed(com.couchbase.lite.replicator.Replication.ChangeEvent)
	 */
	@SuppressLint("DefaultLocale")
	@Override
	public void changed(Replication.ChangeEvent event) {


		Replication replication = event.getSource();
		Log.d(TAG, "Replication : " + replication + " changed.");
		if (!replication.isRunning()) {
			String msg = String.format("Replicator %s not running", replication);

			if(replication.getLastError() == null && replication.isPull()){
				//then it didn't stop on error, so we can broadcast that the database is synced up
				//Log.i(TAG, "Replicator finished updating database, broadcasting notification");
				//				try {
				//					Thread.sleep(5000);
				//				} catch (InterruptedException e1) {
				//
				//					e1.printStackTrace();
				//				}
				Log.i(TAG, "Raw JSON in replication listener: " + getRawJSON("http://127.0.0.1:5984/example_card_generator"));
				Log.i(TAG, "ENTIRE DB AS OF REPLICATION COMPLETION ALERT: " + getEntireDbAsJSON());
				this.synced = true;
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
	 * Obtains every document in a couchDB in the form of a JSON object.
	 *
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
	 * database name and UUID.
	 *
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
	 * The Class RawJSONGetter.
	 */
	private class RawJSONGetter extends Thread {
		
		/** The json. */
		private String json = "";
		
		/** The url. */
		private final String url;

		/**
		 * Instantiates a new raw json getter.
		 *
		 * @param URL the url
		 */
		public RawJSONGetter(String URL){
			super();
			url = URL;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() { 
			// Create a new HttpClient and get Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet everythingGetter = new HttpGet(url);

			// Execute the get and record the response
			HttpResponse response = null;
			try { 
				response = httpclient.execute(everythingGetter);
			} catch (ClientProtocolException e) {
				Log.e(TAG, "error getting the DB as JSON", e);
				return ;
			} catch (SSLPeerUnverifiedException e1){
				Log.i(TAG, "Peerunverified exception caught");

			}	catch (IOException e) {

				Log.e(TAG, "IO error getting the DB as JSON", e);
				return ;
			}

			Log.i(TAG, "server response to all database get: " + response);

			// Parse the response into a String 
			HttpEntity resEntityGet = response.getEntity();

			try {
				json = new String(EntityUtils.toString(resEntityGet));
			} catch (ParseException e) {
				Log.e(TAG, "Error parsing response into a string", e);
				return;
			} catch (IOException e) {
				Log.e(TAG, "IO Error parsing response into a string", e);
				return;
			}

		}

		/**
		 * Gets the json.
		 *
		 * @return the json
		 */
		public String getJson() {
			return json;
		}

	}




	/**
	 * Retrieves an unparsed JSON string from a couchDB database.
	 *
	 * @param URL the URL of the document wanted from a database
	 * @return the requested raw, unparsed JSON string. Returns null if retrieval fails.
	 */
	public String getRawJSON(String URL) {
		RawJSONGetter raw = new RawJSONGetter(URL);

		raw.start();
		try {
			raw.join();
		} catch (InterruptedException e) {

			e.printStackTrace();
			return null;
		}

		Log.d(TAG, "String being returned from getRawJSON" +  raw.getJson());
		return raw.getJson();

	}

	/**
	 * Creates a TMMCard object from the JSONObject.
	 *
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
		getAllAttachments(result);

		return result;
	}

	/**
	 * Creates a VideoCard object from the JSONObject.
	 *
	 * @param obj the JSONObject to create the VideoCard
	 * @return the VideoCard created from parsing the JSONObject. Returns null if creation fails.
	 */
	public VideoCard convertJSONToVideoCard (JSONObject obj) {
		try {
			// Get the Server Object by JSON parsing because Java doesn't like casting
			JSONObject JSONServer = obj.getJSONObject(SOURCE);
			Server sourceServer = new Server(JSONServer.getString(SERVER_NAME), JSONServer.getString(SERVER_API_INFO),
					Long.parseLong(JSONServer.getString(SERVER_FIRST_USED)), Long.parseLong(JSONServer.getString(SERVER_LAST_USED)));

			VideoCard result = new VideoCard(obj.getString(UUID), obj.getInt(PRIORITY), obj.getString(TITLE), obj.getInt(VIDEO_PLAY_COUNT), obj.getString(VIDEO_YOUTUBE_TAG), sourceServer);

			if(!obj.getString(VIDEO_SS_PATH).equalsIgnoreCase("null")){
				//then this video has a screenshot
				result.setScreenshot(obj.getString(VIDEO_SS_PATH));
			}
			
			return result;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates an AudioCard object from the JSONObject.
	 *
	 * @param obj the JSONObject to create the AudioCard
	 * @return the AudioCard created from parsing the JSONObject. Returns null if creation fails.
	 */
	public AudioCard convertJSONToAudioCard (JSONObject obj) {
		try {
			// Get the Server Object by JSON parsing because Java doesn't like casting
			JSONObject JSONServer = obj.getJSONObject(SOURCE);
			Server sourceServer = new Server(JSONServer.getString(SERVER_NAME), JSONServer.getString(SERVER_API_INFO),
					Long.parseLong(JSONServer.getString(SERVER_FIRST_USED)), Long.parseLong(JSONServer.getString(SERVER_LAST_USED)));

			AudioCard result = new AudioCard(obj.getString(UUID), obj.getInt(PRIORITY), obj.getString(TITLE),
					obj.getInt(AUDIO_LENGTH), obj.getString(AUDIO_BG_PATH), obj.getString(AUDIO_CLIP_PATH), sourceServer);

			if(!obj.getString(AUDIO_BACKGROUND_PATH).equalsIgnoreCase("null")){
				result.setBackgroundPath(obj.getString(AUDIO_BACKGROUND_PATH));
			}

			return result;
		} catch (JSONException e) {
			Log.e(TAG, "AudioCard creation crashed and burned");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates a TextCard object from the JSONObject.
	 *
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
			TextCard result = new TextCard(obj.getString(UUID), obj.getInt(PRIORITY), obj.getString(TITLE),
					obj.getString(TEXT_LINE1), obj.getString(TEXT_LINE2), obj.getString(TEXT_LINE3), cardContent, sourceServer);
			//check if this textcard has an icon
			Log.d(TAG, "icon path resolved to be: " + obj.getString(TEXT_CARD_IC_PATH));
			if(!obj.getString(TEXT_CARD_IC_PATH).equalsIgnoreCase("null")){
				result.setIconPath(obj.getString(TEXT_CARD_IC_PATH));	
			} 

			return result;
		} catch (JSONException e) {
			Log.i(TAG,"TextCard creation crashed and burned");
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * Gets the all attachments.
	 *
	 * @param cardWithNoAttachments the card with no attachments
	 * @return the all attachments
	 */
	private boolean getAllAttachments(TMMCard cardWithNoAttachments){
		if(cardWithNoAttachments instanceof VideoCard){
			return downloadVideoCardAttachments((VideoCard) cardWithNoAttachments);
		} else if(cardWithNoAttachments instanceof AudioCard) {
			return downloadAudioCardAttachments((AudioCard) cardWithNoAttachments);
		} else if(cardWithNoAttachments instanceof TextCard) {
			return downloadTextCardAttachments((TextCard) cardWithNoAttachments);
		} else return false; //error, the TMM card must be one of the above


	}

	/**
	 * Download video card attachments.
	 *
	 * @param cardToDl the card to dl
	 * @return true, if successful
	 */
	private boolean downloadVideoCardAttachments(VideoCard cardToDl){
		//video card is easy - only one attachment, the screenshot

		//make sure that it has a valid screenshot...
		if(cardToDl.getScreenshotPath() == null || cardToDl.getScreenshotPath().equalsIgnoreCase("")){
			return true;
		}
		Log.i(TAG, "Videocard screenshot attachment path : " + cardToDl.getScreenshotPath());
		return getSingleAttachment(cardToDl.getScrenshotname(), cardToDl.getuuId(), cardToDl.getScreenshotPath().substring(0, cardToDl.getScreenshotPath().lastIndexOf('/')));

	}

	/**
	 * Download audio card attachments.
	 *
	 * @param cardToDl the card to dl
	 * @return true, if successful
	 */
	private boolean downloadAudioCardAttachments(AudioCard cardToDl){
		//audio card has 2 attachments, the mp3 and the background if there is one
		//make sure that it has a audio clip
		if(cardToDl.getAudioClipPath() != null && !cardToDl.getAudioClipPath().equalsIgnoreCase("")){
			if(getSingleAttachment(cardToDl.getAudioClipName(), cardToDl.getuuId(), cardToDl.getAudioClipPath().substring(0, cardToDl.getAudioClipPath().lastIndexOf('/'))) == false){
				return false;
			}
		}
		
		
		if(!(cardToDl.getBackgroundPath() == null  || !cardToDl.getBackgroundPath().equalsIgnoreCase(""))){
			Log.i(TAG, "audiocard background path: " + cardToDl.getBackgroundPath());
			if(getSingleAttachment(cardToDl.getBackground_name(), cardToDl.getuuId(), cardToDl.getBackgroundPath().substring(0, cardToDl.getBackgroundPath().lastIndexOf('/'))) == false){
				return false;
			} 
		}
		return true;
		
	}

	/**
	 * Download text card attachments.
	 *
	 * @param cardToDl the card to dl
	 * @return true, if successful
	 */
	private boolean downloadTextCardAttachments(TextCard cardToDl){
		//first, if there is an icon, then DL it
		if(cardToDl.getIconPath() != null && !cardToDl.getIconPath().equalsIgnoreCase("")){
			if(getSingleAttachment(cardToDl.getIcFileName(), cardToDl.getuuId(), cardToDl.getIconPath().substring(0, cardToDl.getIconPath().lastIndexOf('/'))) == false){
				return false;
			} 
		}
		
		//now get all the textelements that contain images
		for(int i = 0; i < cardToDl.getContents().size(); i++){
			if(cardToDl.getContents().get(i).getType() == Type.IMAGE && cardToDl.getContents().get(i).getImg() != null && !cardToDl.getContents().get(i).getImg().equalsIgnoreCase("")){
				if(getSingleAttachment(cardToDl.getContents().get(i).getImgFilename(), cardToDl.getuuId(), cardToDl.getContents().get(i).getImg().substring(0, cardToDl.getContents().get(i).getImg().lastIndexOf('/'))) == false){
					return false;
				} 
			}
		}
		return true;
		
	}

	/**
	 * The Class AttachmentDownloader.
	 */
	private class AttachmentDownloader extends Thread {

		/** The path to store. */
		private final String uuid, filename, pathToStore; 

		/**
		 * Instantiates a new attachment downloader.
		 *
		 * @param uuid the uuid
		 * @param fileName the file name
		 * @param pathToStoreAttachment the path to store attachment
		 */
		public AttachmentDownloader (String uuid, String fileName, String pathToStoreAttachment){
			super();
			this.uuid = uuid;
			this.filename = fileName;
			this.pathToStore = pathToStoreAttachment;

		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() { 

			HttpClient httpclient = new DefaultHttpClient();
			HttpGet attachment = new HttpGet(LOCAL_DB_URL + ":" + 5984 + "/" + dbName + "/" + uuid + "/" + filename);

			//execute the delete and record the response
			HttpResponse response = null;
			try {
				response = httpclient.execute(attachment);
			} catch (ClientProtocolException e) {
				Log.e(TAG, "error retrieving document attachment", e);
				return;
			} catch (IOException e) {
				Log.e(TAG, "IO error retrieving document attachment", e);
				return;
			}
		

			//write the attachment to file
			FileOutputStream save0 = null;

			try {
				save0 = new FileOutputStream(new File(pathToStore +"/" + filename));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			try {
				response.getEntity().writeTo(save0);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}



	}

	//pathToStore does not include the filename - that will be the same as the attachment name
	/**
	 * Gets the single attachment.
	 *
	 * @param attachmentName the attachment name
	 * @param uuidOfDoc the uuid of doc
	 * @param pathToStore the path to store
	 * @return the single attachment
	 */
	private boolean getSingleAttachment(String attachmentName, String uuidOfDoc, String pathToStore){
		AttachmentDownloader dwnldr = new AttachmentDownloader(uuidOfDoc, attachmentName, pathToStore);

		dwnldr.start();
		try {
			dwnldr.join();
		} catch (InterruptedException e) {

			e.printStackTrace();
			return false;
		}

		return true;


	}




	/**
	 * The Class DBDeleter.
	 */
	private class DBDeleter extends Thread {

		/**
		 * Instantiates a new DB deleter.
		 */
		public DBDeleter(){
			super();
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
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



	/**
	 * Delete db.
	 *
	 * @param areYouSure the are you sure
	 * @return true, if successful
	 */
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

	/**
	 * Find card by id.
	 *
	 * @param uuid the uuid
	 * @return the TMM card
	 */
	public TMMCard findCardById(String uuid){
		String rawobj = this.getRawJSON(LOCAL_DB_URL + ":" + port + "/" + dbName + "/" + uuid);

		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(rawobj);
		} catch (JSONException e1) {
			Log.e(TAG, "error making json object", e1);
			return null;
		}

		TMMCard toRet = this.convertJSONToCard(jsonObject);
		return toRet;
	}

	/**
	 * Gets the JSON representation.
	 *
	 * @param toCheck the to check
	 * @return the JSON representation
	 */
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
	/**
	 * Adds the card.
	 *
	 * @param toAdd the to add
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public synchronized boolean addCard(TMMCard toAdd) throws Exception{

		//check to make sure card doesn't already exist in DB
		if(this.getJSONRepresentation(toAdd) != null){
			//maybe we could update the card or something

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

	/**
	 * Gets the uuid.
	 *
	 * @return the uuid
	 */
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

	/**
	 * Upload card attachments.
	 *
	 * @param toAdd the to add
	 * @param jsonObject the json object
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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

	/**
	 * Upload single attachment.
	 *
	 * @param data the data
	 * @param jsonObject the json object
	 * @param fileName the file name
	 * @param contentType the content type
	 * @return true, if successful
	 */
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

	/**
	 * The Class JSONGetter.
	 */
	private class JSONGetter extends Thread {
		
		/** The json. */
		private String json = "";

		/**
		 * Instantiates a new JSON getter.
		 */
		public JSONGetter(){
			super();
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
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

		/**
		 * Gets the json.
		 *
		 * @return the json
		 */
		public String getJson() {
			return json;
		}

	}

	/**
	 * Gets the entire db as json.
	 *
	 * @return the entire db as json
	 */
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


	/**
	 * Delete cardfrom db.
	 *
	 * @param todel the todel
	 * @return true, if successful
	 */
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

	/**
	 * Creates the new db.
	 *
	 * @param dbname the dbname
	 * @return true, if successful
	 * @throws IllegalStateException the illegal state exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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
	/**
	 * Gets the all cards.
	 *
	 * @return the all cards
	 */
	public synchronized ArrayList<TMMCard> getAllCards(){
		Log.d(TAG, "get all cards");

		ArrayList<TMMCard> matches = new ArrayList<TMMCard>();


		//sort cards by priority
		Collections.sort(matches);

		return matches;
	}


	/**
	 * Find cardsby server.
	 *
	 * @return the array list
	 */
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