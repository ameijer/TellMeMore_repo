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
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
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

/**
 * The Class DBManager. This class provides a friendly API for
 * retrieving/storing {@link TMMCard} objects from the NoSQL database.
 * 
 * Note that some of the methods in this class make network accesses, which
 * should be done asynchronously. Since we only make calls to a DB on localhost,
 * we should not have any network latency so we simply block on the network
 * threads. If this class is used with an actual remote DB (it can be configured
 * to do this quite easily), then this approach could cause major freezing due
 * to network failure/latency. Care should then be exercised to account for the
 * latency in such a situation.
 * 
 */
@SuppressLint("DefaultLocale")
public class DBManager implements Replication.ChangeListener {

	/** The NoSQL database: an instance of couchbase lite. */
	private Database database;

	/**
	 * The NoSQL database manager. Keeps databases in order and controls access.
	 */
	private Manager manager;

	/**
	 * The database name. This is most often what is passed through to this
	 * manager and governs the scope of the database.
	 */
	private String dbName;

	/** The Constant TAG. Used for the Android debug logger. */
	public static final String TAG = "TMM: DBMANAGER";

	/**
	 * The Constant MASTER_SERVER_URL. This URL is for the server containing all
	 * the cards to synchronize with.
	 */
	public static final String MASTER_SERVER_URL = "http://134.82.132.99";

	/**
	 * The Constant LOCAL_DB_URL. This contains the IP of the local database,
	 * almost always localhost.
	 */
	public static final String LOCAL_DB_URL = "http://127.0.0.1";

	/**
	 * The Constant UUID_GETTER_PATH. The URL path to retreive a UUID for a new
	 * card when it is going to be added to the database.
	 */
	public static final String UUID_GETTER_PATH = "_uuids";

	/**
	 * The port number that the NoSQL DBs are running on. For good consistency,
	 * this should be the same on the local and remote databases.
	 */
	public static final int port = 5984;

	/** The JSON field identifier for the items in the database. */
	public static final String DB_ARRAY = "rows";

	/** The JSON field identifier for the id of a document item on the DB. */
	public static final String DOC_ID = "id";

	/**
	 * The JSON field identifier for the type of card contained in the JSON
	 * document.
	 */
	public static final String TYPE = "jsontype";

	/** The JSON field identifier for the UUID of an item from the database. */
	public static final String UUID = "uuId";

	/** The JSON field identifier for the priority of the JSON document. */
	public static final String PRIORITY = "priority";

	/**
	 * The JSON field identifier for the title of the card JSON document in the
	 * database.
	 */
	public static final String TITLE = "title";

	/**
	 * The the JSON field identifier for the source server of the card JSON
	 * document from the database.
	 */
	public static final String SOURCE = "source";

	/**
	 * The JSON field identifier for the name of the JSON server object from the
	 * database.
	 */
	public static final String SERVER_NAME = "name";

	/**
	 * The JSON field identifier for the API info of the server item in the
	 * database.
	 */
	public static final String SERVER_API_INFO = "api_info";

	/**
	 * The JSON field identifier for the time of first use of the JSON server
	 * object from the database.
	 */
	public static final String SERVER_FIRST_USED = "first_used";

	/**
	 * The JSON field identifier for the time of last use of the JSON server
	 * object from the database.
	 */
	public static final String SERVER_LAST_USED = "last_used";

	/**
	 * The JSON field identifier for the length of the audio clip of the JSON
	 * audiocard document.
	 */
	public static final String AUDIO_LENGTH = "lengthMillis";

	/**
	 * The JSON field identifier for the path to the audio file contained in the
	 * JSON audiocard document.
	 */
	public static final String AUDIO_CLIP_PATH = "audioClipPath";

	/**
	 * The JSON field identifier for the background image file path contained in
	 * the audiocard JSON document.
	 */
	public static final String AUDIO_BACKGROUND_PATH = "backgroundPath";

	/**
	 * The JSON field identifier for the icon image filename contained in the
	 * textcard JSON document.
	 */
	public static final String TEXT_CARD_IC_NAME = "icFileName";

	/**
	 * The JSON field identifier for the path of the icon image file for the
	 * textcard contained in the JSON document.
	 */
	public static final String TEXT_CARD_IC_PATH = "iconPath";

	/**
	 * The JSON field identifier for the first (top) line of text in the
	 * textcard contained in the JSON document.
	 */
	public static final String TEXT_LINE1 = "line1";

	/**
	 * The JSON field identifier for the second (middle) line of text in the
	 * textcard contained in the JSON document.
	 */
	public static final String TEXT_LINE2 = "line2";

	/**
	 * The JSON field identifier for the third (bottom) line of text in the
	 * textcard contained in the JSON document.
	 */
	public static final String TEXT_LINE3 = "line3";

	/**
	 * The JSON field identifier for the array of {@link TextElement} JSON
	 * objects contained in the textcard of this JSON document.
	 */
	public static final String TEXT_CONTENTS = "contents";

	/**
	 * The JSON field identifier for the JSON representation of the textelement
	 * class. This indicates the type of element, either TEXT or IMAGE.
	 */
	public static final String TEXT_CONTENT_TYPE = "type";

	/**
	 * The JSON field identifier for the JSON representation of the textelement
	 * class. This is used to retrieve the textual content of the textelement
	 * from the JSON document it is contained in.
	 */
	public static final String TEXT_CONTENT_TEXT = "text";

	/**
	 * The JSON field identifier for the JSON representation of the textelement
	 * class. This is used to retrive the image path from the textelement, if it
	 * exists.
	 */
	public static final String TEXT_CONTENT_IMG = "img";

	/**
	 * The the JSON field identifier for the path of the screenshot image file
	 * for the video card contained in the JSON document, if it exists.
	 */
	public static final String VIDEO_SS_PATH = "screenshotPath";

	/**
	 * The the JSON field identifier for the play count field in the videocard
	 * JSON document.
	 */
	public static final String VIDEO_PLAY_COUNT = "playCount";

	/**
	 * TThe the JSON field identifier for the youtube tag of the videocard
	 * contained in the JSON document.
	 */
	public static final String VIDEO_YOUTUBE_TAG = "yttag";

	/**
	 * Boolean flag used to indicate when the database has finished its
	 * synchronization process with the master.
	 */
	private boolean synced;

	/**
	 * Checks if the database currently open in the manager is synchronized with
	 * the master database.
	 * 
	 * @return true, if the database has completed its synchronization.
	 */
	public boolean isSynced() {
		return synced;
	}

	/**
	 * Instantiates a new DB manager.
	 * 
	 * @param context
	 *            The application context. Used to obtain a writable private
	 *            directory.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public DBManager(Context context) throws IOException {

		// open a NoSQL manager
		manager = new Manager(new AndroidContext(context).getFilesDir(),
				Manager.DEFAULT_OPTIONS);
	}

	/**
	 * Gets the name of the currently open database contained in this manager.
	 * 
	 * @return THe name of the database. Note that this is usually the project
	 *         name and will NOT be a URL.
	 */
	public String getName() {
		return this.dbName;
	}

	/**
	 * Opens the database with the name specified, and synchronizes it. The
	 * isSynced() function should be used to check for the completion of the
	 * synchronization before using the newly opened database. If the database
	 * with the specified name does not exist, then it will be created.
	 * 
	 * @param dbName
	 *            The name of the database to open (if it exists) or create if
	 *            it does not exist.
	 * @param context
	 *            The current context to use for the opening of the database.
	 * @return true, if the synchronization starts with no apparent problems.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws CouchbaseLiteException
	 *             Signals that an exception with the local NoSQL DB has been
	 *             thrown during initialization and/or sync.
	 */
	public boolean open(String dbName, Context context) throws IOException,
	CouchbaseLiteException {

		// set the synced flag, assume un-updated database
		this.synced = false;
		this.dbName = dbName;

		// start a local listener for the app to access the local DB
		startCBLListener(port);
		startDatabase(manager, dbName);

		// sync up database
		startSync(false);
		return true;
	}

	/**
	 * Starts a local Couchbase lite NoSQL DB listening on the specified port.
	 * 
	 * @param suggestedListenPort
	 *            The number of the suggested port to listen on.
	 * @return The port that the DB is listening on.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws CouchbaseLiteException
	 *             Signals that an exception with the local NoSQL DB has been
	 *             thrown during initialization.
	 */
	private int startCBLListener(int suggestedListenPort) throws IOException,
	CouchbaseLiteException {

		startDatabase(manager, dbName);

		LiteListener listener = new LiteListener(manager, suggestedListenPort);
		int port = listener.getListenPort();
		Thread thread = new Thread(listener);
		thread.start();

		return port;

	}

	/**
	 * Starts database. Used as a wrapper for other CBL opening methods.
	 * 
	 * @param manager
	 *            the manager
	 * @param databaseName
	 *            the database name
	 * @throws CouchbaseLiteException
	 *             the couchbase lite exception
	 */
	protected void startDatabase(Manager manager, String databaseName)
			throws CouchbaseLiteException {
		database = manager.getDatabase(databaseName);
		database.open();
	}

	/**
	 * Closes the database.
	 */
	public void close() {
		database.close();
	}

	/**
	 * Starts the background synchronization of the local database in either one
	 * or two directions.
	 * 
	 * @param pushLocalChanges
	 *            Set to true to push changes made to the local database to the
	 *            master database.
	 */
	private void startSync(boolean pushLocalChanges) {

		// form target sync URL
		URL syncUrl;
		try {
			syncUrl = new URL(MASTER_SERVER_URL + ":" + port + "/" + dbName);

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		// start pulling changes from the master
		Replication pullReplication = database.createPullReplication(syncUrl);
		pullReplication.setCreateTarget(true);

		// do not sync continuously. This can be changed in the future to allow
		// real-time updates to be pushed
		pullReplication.setContinuous(false);
		Replication pushReplication = null;

		// only push changes if specified
		if (pushLocalChanges) {
			pushReplication = database.createPushReplication(syncUrl);
			pullReplication.setCreateTarget(true);
			pushReplication.setContinuous(false);
		}

		// start the synchronizer(s)
		pullReplication.start();

		if (pushLocalChanges) {
			pushReplication.start();
		}

		pullReplication.addChangeListener(this);

		if (pushLocalChanges) {
			pushReplication.addChangeListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.couchbase.lite.replicator.Replication.ChangeListener#changed(com.
	 * couchbase.lite.replicator.Replication.ChangeEvent)
	 */
	@SuppressLint("DefaultLocale")
	@Override
	public void changed(Replication.ChangeEvent event) {

		Replication replication = event.getSource();
		Log.d(TAG, "Replication : " + replication + " changed.");
		if (!replication.isRunning()) {
			String msg = String
					.format("Replicator %s not running", replication);

			// if the replication is not running and has no last error, then we
			// assume a successful, completed sync
			if (replication.getLastError() == null && replication.isPull()) {

				this.synced = true;
			}

			// try to restart if there is a problem.
			if (replication.getLastError() != null) {
				Log.e(TAG, "REPLICATION FAILED IN CHANGELISTENER");
				replication.restart();
			}

			Log.d(TAG, msg);
		} else {

			// log replication updated for user/developer
			int processed = replication.getCompletedChangesCount();
			int total = replication.getChangesCount();
			String msg = String.format("Replicator processed %d / %d",
					processed, total);
			Log.d(TAG, msg);
		}

	}

	/**
	 * Obtains every document in a couchDB in the form of a JSON object.
	 * 
	 * @param serverURLsansPort
	 *            The URL of the server (without the port).
	 * @param port
	 *            The port number of the server.
	 * @param dbName
	 *            The name of the database (in this case project). NOT A URL
	 * @return Array of JSONObjects containing all documents associated with a
	 *         given database.
	 */
	public JSONObject[] getEntireDbAsJSON(String serverURLsansPort, int port,
			String dbName) {
		try {
			// Get the raw JSON of all the names and IDs of each document in a
			// database
			String json = getRawJSON(serverURLsansPort + ":" + port + "/"
					+ dbName + "/_all_docs");

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
				result[i] = getDocumentAsJSON(serverURLsansPort, port, dbName,
						dbUUID[i]);
			}
			return result;
		} catch (JSONException e) {
			Log.e(TAG, "getEntireDbAsJSON crashed and burned");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Obtains a single document from a couchDB with a given database name and
	 * UUID.
	 * 
	 * @param serverURLsansPort
	 *            The URL of the server (without the port).
	 * @param port
	 *            The port number of the server.
	 * @param dbName
	 *            The name of the database (in this case project).
	 * @param UUID
	 *            The String UUID of the requested document.
	 * @return The requested document in the form of a JSON Object. Returns null
	 *         if creation fails.
	 */
	public JSONObject getDocumentAsJSON(String serverURLsansPort, int port,
			String dbName, String UUID) {
		try {
			// Get the raw JSON of the requested document
			String json = getRawJSON(serverURLsansPort + ":" + port + "/"
					+ dbName + "/" + UUID);

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
	 * The Class RawJSONGetter. This runnable is used to perform asynchronous
	 * HTTP gets for JSON in the local DB. After the run() method completes
	 * (i.e. the class is joined()) then the retrieved data is stored in the
	 * class member vars and can be retrieved via the getter(s) provided.
	 * 
	 * @author atm011
	 */
	private class RawJSONGetter extends Thread {

		/** The json string returned from the HTTP GET call to the database. */
		private String json = "";

		/** The url to access for the raw JSON data. */
		private final String url;

		/**
		 * Instantiates a new raw JOSN getter, which will retrieve the JSON at
		 * the specified URL.
		 * 
		 * @param URL
		 *            The address of the JSON to retrieve.
		 */
		public RawJSONGetter(String URL) {
			super();
			url = URL;
		}

		/*
		 * (non-Javadoc)
		 * 
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
				return;
			} catch (SSLPeerUnverifiedException e1) {
				Log.i(TAG, "Peerunverified exception caught");

			} catch (IOException e) {

				Log.e(TAG, "IO error getting the DB as JSON", e);
				return;
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
		 * Gets the json retrieved by this class. Will return the null string
		 * until the run() method of the object has been called.
		 * 
		 * @return The string containing the JSON retrieved by the class, if any
		 *         exists at the specified URL.
		 */
		public String getJson() {
			return json;
		}

	}

	/**
	 * Retrieves an unparsed JSON string from a couchDB database.
	 * 
	 * @param URL
	 *            The URL of the document wanted from the database.
	 * @return The requested raw, unparsed JSON string. Returns null if
	 *         retrieval fails.
	 */
	public String getRawJSON(String URL) {

		// create an asynchronous getter at the target URL
		RawJSONGetter raw = new RawJSONGetter(URL);

		raw.start();
		try {
			// wait until the task completes. It is OK to join here since we are
			// using a local DB with known retrieval times.
			// If this is to be used with an actual remote DB, then this join()
			// is a poor choice and should be replaced with other non-blocking
			// measures
			raw.join();
		} catch (InterruptedException e) {

			e.printStackTrace();
			return null;
		}

		Log.d(TAG, "String being returned from getRawJSON" + raw.getJson());
		return raw.getJson();

	}

	/**
	 * Creates a {@link TMMCard} object from the JSONObject.
	 * 
	 * @param obj
	 *            The JSONObject to create the {@link TMMCard} from.
	 * @return The {@link TMMCard} created from parsing the JSONObject. Returns
	 *         null if creation fails.
	 */
	public TMMCard convertJSONToCard(JSONObject obj) {
		TMMCard result = null;

		// Obtain type of card to be created
		String type;
		try {
			type = obj.getString(TYPE);
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(TAG, "Cast failed");
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
	 * Creates a {@link VideoCard} object from the JSONObject. A helper method
	 * for the above convertJSONToCard() method.
	 * 
	 * @param obj
	 *            The JSONObject to create the {@link VideoCard} .
	 * @return The {@link VideoCard} created from parsing the JSONObject.
	 *         Returns null if creation fails.
	 */
	private VideoCard convertJSONToVideoCard(JSONObject obj) {
		try {
			// Get the Server Object by JSON parsing because Java doesn't like
			// casting
			JSONObject JSONServer = obj.getJSONObject(SOURCE);
			Server sourceServer = new Server(JSONServer.getString(SERVER_NAME),
					JSONServer.getString(SERVER_API_INFO),
					Long.parseLong(JSONServer.getString(SERVER_FIRST_USED)),
					Long.parseLong(JSONServer.getString(SERVER_LAST_USED)));

			// call the huge constructor using fields obtained from JSON
			VideoCard result = new VideoCard(obj.getString(UUID),
					obj.getInt(PRIORITY), obj.getString(TITLE),
					obj.getInt(VIDEO_PLAY_COUNT),
					obj.getString(VIDEO_YOUTUBE_TAG), sourceServer);

			if (!obj.getString(VIDEO_SS_PATH).equalsIgnoreCase("null")) {
				// then this video has a screenshot
				result.setScreenshot(obj.getString(VIDEO_SS_PATH));
			}

			return result;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates an {@link AudioCard} object from the JSONObject. A helper method
	 * for the above convertJSONToCard() method.
	 * 
	 * @param obj
	 *            The JSONObject to create the {@link AudioCard}.
	 * @return The {@link AudioCard} created from parsing the JSONObject.
	 *         Returns null if creation fails.
	 */
	private AudioCard convertJSONToAudioCard(JSONObject obj) {
		try {
			// Get the Server Object by JSON parsing because Java doesn't like
			// casting
			JSONObject JSONServer = obj.getJSONObject(SOURCE);
			Server sourceServer = new Server(JSONServer.getString(SERVER_NAME),
					JSONServer.getString(SERVER_API_INFO),
					Long.parseLong(JSONServer.getString(SERVER_FIRST_USED)),
					Long.parseLong(JSONServer.getString(SERVER_LAST_USED)));
			// call the huge constructor using fields obtained from JSON
			AudioCard result = new AudioCard(obj.getString(UUID),
					obj.getInt(PRIORITY), obj.getString(TITLE),
					obj.getInt(AUDIO_LENGTH),
					obj.getString(AUDIO_BACKGROUND_PATH),
					obj.getString(AUDIO_CLIP_PATH), sourceServer);

			if (!obj.getString(AUDIO_BACKGROUND_PATH).equalsIgnoreCase("null")) {
				// then this audio card has a background we need to store and
				// retrieve
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
	 * Creates a {@link TextCard} object from the JSONObject. A helper method
	 * for the above convertJSONToCard() method.
	 * 
	 * @param obj
	 *            The JSONObject to create the {@link TextCard}.
	 * @return The {@link TextCard} created from parsing the JSONObject. Returns
	 *         null if creation fails.
	 */
	private TextCard convertJSONToTextCard(JSONObject obj) {
		try {

			// Get array of contents for the TextCard
			JSONArray contents = obj.getJSONArray(TEXT_CONTENTS);
			ArrayList<TextElement> cardContent = new ArrayList<TextElement>();

			// Create all cardContents for the text card from JSON
			for (int i = 0; i < contents.length(); i++) {

				// Get the contentType by string parsing because Java doesn't
				// like casting an object from obj.get to an enum
				JSONObject temp = contents.getJSONObject(i);
				Type contentType;
				if (temp.getString(TEXT_CONTENT_TYPE).equals("IMAGE"))
					contentType = Type.IMAGE;
				else
					contentType = Type.TEXT_;

				cardContent.add(new TextElement(contentType, temp
						.getString(TEXT_CONTENT_TEXT), temp
						.getString(TEXT_CONTENT_IMG)));
			}

			// Get the Server Object by JSON parsing because Java doesn't like
			// casting
			JSONObject JSONServer = obj.getJSONObject(SOURCE);
			Server sourceServer = new Server(JSONServer.getString(SERVER_NAME),
					JSONServer.getString(SERVER_API_INFO),
					Long.parseLong(JSONServer.getString(SERVER_FIRST_USED)),
					Long.parseLong(JSONServer.getString(SERVER_LAST_USED)));

			// Create actual TextCard
			TextCard result = new TextCard(obj.getString(UUID),
					obj.getInt(PRIORITY), obj.getString(TITLE),
					obj.getString(TEXT_LINE1), obj.getString(TEXT_LINE2),
					obj.getString(TEXT_LINE3), cardContent, sourceServer);

			// check if this textcard has an icon
			Log.d(TAG,
					"icon path resolved to be: "
							+ obj.getString(TEXT_CARD_IC_PATH));
			if (!obj.getString(TEXT_CARD_IC_PATH).equalsIgnoreCase("null")) {
				result.setIconPath(obj.getString(TEXT_CARD_IC_PATH));
			}

			return result;
		} catch (JSONException e) {
			Log.i(TAG, "TextCard creation crashed and burned");
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Downloads local copies of all the attachments for the card passed to this
	 * method.
	 * 
	 * @param cardWithNoAttachments
	 *            The {@link TMMCard} object with all attachment paths set, but
	 *            no actual files at those paths.
	 * @return true, if successful in obtaining all the attachments for the
	 *         card.
	 */
	private boolean getAllAttachments(TMMCard cardWithNoAttachments) {

		// call helpers depending on the type of card
		if (cardWithNoAttachments instanceof VideoCard) {
			return downloadVideoCardAttachments((VideoCard) cardWithNoAttachments);
		} else if (cardWithNoAttachments instanceof AudioCard) {
			return downloadAudioCardAttachments((AudioCard) cardWithNoAttachments);
		} else if (cardWithNoAttachments instanceof TextCard) {
			return downloadTextCardAttachments((TextCard) cardWithNoAttachments);
		} else
			return false; // error, the TMM card must be one of the above

	}

	/**
	 * Download all attachments for specified {@link VideoCard} object.
	 * Attachments will be saved at paths designated in parameter object. This
	 * is a helper method for the above getAllAttachments() method.
	 * 
	 * @param cardToDl
	 *            The {@link VideoCard} object with paths for attachments set,
	 *            but no actual attachment files at those paths.
	 * @return true, if all attachments for the card downloaded successfully.
	 */
	private boolean downloadVideoCardAttachments(VideoCard cardToDl) {
		// video card is easy - only one attachment, the screenshot

		// make sure that it has a valid screenshot...
		if (cardToDl.getScreenshotPath() == null
				|| cardToDl.getScreenshotPath().equalsIgnoreCase("")) {
			return true;
		}
		Log.i(TAG,
				"Videocard screenshot attachment path : "
						+ cardToDl.getScreenshotPath());

		// call the single attachment getter on the only attachment for this
		// card type
		return getSingleAttachment(
				cardToDl.getScrenshotname(),
				cardToDl.getuuId(),
				cardToDl.getScreenshotPath().substring(0,
						cardToDl.getScreenshotPath().lastIndexOf('/')));

	}

	/**
	 * Download all attachments for specified {@link AudioCard} object.
	 * Attachments will be saved at paths designated in parameter object. This
	 * is a helper method for the above getAllAttachments() method.
	 * 
	 * @param cardToDl
	 *            The {@link AudioCard} object with paths for attachments set,
	 *            but no actual attachment files at those paths.
	 * @return true, if all attachments for the card downloaded successfully.
	 */
	private boolean downloadAudioCardAttachments(AudioCard cardToDl) {
		// audio card has 2 attachments, the mp3 and the background if there is
		// one
		// make sure that it has a audio clip
		if (cardToDl.getAudioClipPath() != null
				&& !cardToDl.getAudioClipPath().equalsIgnoreCase("")) {
			if (getSingleAttachment(
					cardToDl.getAudioClipName(),
					cardToDl.getuuId(),
					cardToDl.getAudioClipPath().substring(0,
							cardToDl.getAudioClipPath().lastIndexOf('/'))) == false) {
				return false;
			}
		}

		// get the background image attachment if it exists
		if (!(cardToDl.getBackgroundPath() == null || !cardToDl
				.getBackgroundPath().equalsIgnoreCase(""))) {
			Log.i(TAG,
					"audiocard background path: "
							+ cardToDl.getBackgroundPath());
			if (getSingleAttachment(
					cardToDl.getBackground_name(),
					cardToDl.getuuId(),
					cardToDl.getBackgroundPath().substring(0,
							cardToDl.getBackgroundPath().lastIndexOf('/'))) == false) {
				return false;
			}
		}
		return true;

	}

	/**
	 * Download all attachments for specified {@link TextCard} object.
	 * Attachments will be saved at paths designated in parameter object. This
	 * is a helper method for the above getAllAttachments() method.
	 * 
	 * @param cardToDl
	 *            The {@link TextCard} object with paths for attachments set,
	 *            but no actual attachment files at those paths.
	 * @return true, if all attachments for the card downloaded successfully.
	 */
	private boolean downloadTextCardAttachments(TextCard cardToDl) {
		// first, if there is an icon, then DL it
		if (cardToDl.getIconPath() != null
				&& !cardToDl.getIconPath().equalsIgnoreCase("")) {
			if (getSingleAttachment(
					cardToDl.getIcFileName(),
					cardToDl.getuuId(),
					cardToDl.getIconPath().substring(0,
							cardToDl.getIconPath().lastIndexOf('/'))) == false) {
				return false;
			}
		}

		// now get all the textelements that contain images
		for (int i = 0; i < cardToDl.getContents().size(); i++) {
			if (cardToDl.getContents().get(i).getType() == Type.IMAGE
					&& cardToDl.getContents().get(i).getImg() != null
					&& !cardToDl.getContents().get(i).getImg()
					.equalsIgnoreCase("")) {
				if (getSingleAttachment(
						cardToDl.getContents().get(i).getImgFilename(),
						cardToDl.getuuId(),
						cardToDl.getContents()
						.get(i)
						.getImg()
						.substring(
								0,
								cardToDl.getContents().get(i).getImg()
								.lastIndexOf('/'))) == false) {
					return false;
				}
			}
		}
		return true;

	}

	/**
	 * The Class AttachmentDownloader. This runnable is used to perform
	 * asynchronous HTTP gets for binary attachment data in the local DB. It
	 * writes the retrieved data to the file path specified, so by the time the
	 * runnable is joined the file is ready to access locally.
	 * 
	 * @author atm011
	 */
	private class AttachmentDownloader extends Thread {

		/** The UUID of the parent document of the attachment. */
		private final String uuid;

		/** The name of the attachment to download, NOT the path. */
		private final String filename;

		/** The local filesystem path to store the downloaded attachment file. */
		private final String pathToStore;

		/**
		 * Instantiates a new attachment downloader.
		 * 
		 * @param uuid
		 *            The UUID of the parent document of the attachment.
		 * @param fileName
		 *            The name of the attachment to download, NOT the path.
		 * @param pathToStoreAttachment
		 *            The local filesystem path to store the downloaded
		 *            attachment file.
		 */
		public AttachmentDownloader(String uuid, String fileName,
				String pathToStoreAttachment) {
			super();
			this.uuid = uuid;
			this.filename = fileName;
			this.pathToStore = pathToStoreAttachment;

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {

			HttpClient httpclient = new DefaultHttpClient();
			HttpGet attachment = new HttpGet(LOCAL_DB_URL + ":" + 5984 + "/"
					+ dbName + "/" + uuid + "/" + filename);

			// execute the delete and record the response
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

			// write the attachment to file
			FileOutputStream save0 = null;

			try {
				save0 = new FileOutputStream(new File(pathToStore + "/"
						+ filename));
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

	/**
	 * Gets the single attachment. Essentially an API wrapper for the attachment
	 * downloader runnable. This methods returns with the attachment in a known
	 * state - completely downloaded and saved to the local filesystem.
	 * 
	 * @param attachmentName
	 *            The name of the attachment to download, NOT the path.
	 * @param uuidOfDoc
	 *            The UUID of the parent document of the attachment.
	 * @param pathToStore
	 *            The local filesystem path to store the downloaded attachment
	 *            file.
	 * @return true, if the attachment was downloaded successfully.
	 */
	private boolean getSingleAttachment(String attachmentName,
			String uuidOfDoc, String pathToStore) {
		AttachmentDownloader dwnldr = new AttachmentDownloader(uuidOfDoc,
				attachmentName, pathToStore);

		dwnldr.start();
		try {

			// wait until the task completes. It is OK to join here since we are
			// using a local DB with known retrieval times.
			// If this is to be used with an actual remote DB, then this join()
			// is a poor choice and should be replaced with other non-blocking
			// measures
			dwnldr.join();
		} catch (InterruptedException e) {

			e.printStackTrace();
			return false;
		}

		return true;

	}

	/**
	 * The Class DBDeleter. This runnable is used to perform asynchronous HTTP
	 * DELETEs to wipe the database currently open in the manager.
	 * 
	 * @author atm011
	 */
	private class DBDeleter extends Thread {

		/**
		 * Instantiates a new DB deleter.
		 */
		public DBDeleter() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {

			HttpClient httpclient = new DefaultHttpClient();
			HttpDelete dbdeleter = new HttpDelete(LOCAL_DB_URL + ":" + port
					+ "/" + dbName);

			// execute the delete and record the response
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

			// parse the reponse
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent(), "UTF-8"));
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
	 * Deletes the database currently open in this db manager.
	 * 
	 * @param areYouSure
	 *            Just to check that you know what you are doing
	 * @return true, if the DB was successfully deleted.
	 */
	public boolean deleteDB(boolean areYouSure) {
		if (!areYouSure) {
			Log.d(TAG, "User wasn't sure");
			return false;
		}

		DBDeleter deleter = new DBDeleter();

		deleter.start();
		try {
			// wait until the task completes. It is OK to join here since we are
			// using a local DB with known access times.
			// If this is to be used with an actual remote DB, then this join()
			// is a poor choice and should be replaced with other non-blocking
			// measures
			deleter.join();
		} catch (InterruptedException e) {

			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Find a card in the database by UUID.
	 * 
	 * @param uuid
	 *            The UUID of the card to retrieve. If it is not an exact match
	 *            to an actual UUID, then the call will fail.
	 * @return The {@link TMMCard} with the specified UUID retrieved from the
	 *         database.
	 */
	public TMMCard findCardById(String uuid) {
		String rawobj = this.getRawJSON(LOCAL_DB_URL + ":" + port + "/"
				+ dbName + "/" + uuid);

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
	 * Gets the JSON representation of the specified TMMCard. The TMMCard must
	 * have a valid UUID for a valid JSONObject to be retrieved.
	 * 
	 * @param toCheck
	 *            The card to obtain the JSON representation from the DB of.
	 * @return The JSONOBject of the specified TMMCard, if the card can be found
	 *         in the DB.
	 */
	public JSONObject getJSONRepresentation(TMMCard toCheck) {

		// perform sanity checks on the UUID
		if (toCheck.getuuId() == null) {
			return null;
		}

		final String exampleUUID = "f2abdb8f1fb14601b9e149cd67035d8a";

		if (toCheck.getuuId().length() != exampleUUID.length()) {
			return null;
		}

		// then we must have a good UUID
		// Create a new HttpClient and get Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet cardGetter = new HttpGet(LOCAL_DB_URL + ":" + port + "/"
				+ dbName + "/" + toCheck.getuuId());

		// execute the put and record the response
		HttpResponse response = null;
		try {
			response = httpclient.execute(cardGetter);
		} catch (ClientProtocolException e) {

			Log.e(TAG, "error getting JSON card", e);
		} catch (IOException e) {
			Log.e(TAG, "IO error getting JSON card", e);
		}

		Log.i(TAG, "server response to card get: " + response);

		// parse the reponse
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent(), "UTF-8"));
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
			// convert to JSONObject
			jsonObject = new JSONObject(json);
		} catch (JSONException e1) {
			Log.e(TAG, "error making json object", e1);
		}

		String errorResult = null;
		String reason = null;
		try {

			// check to make sure the JSONObject wasn't an error object
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

	/**
	 * Adds a {@link TMMCard} to the database.
	 * 
	 * @param toAdd
	 *            The {@link TMMCard} to add to the database.
	 * @return true, if the card was successfully added.
	 * @throws IOException
	 *             Thrown if the is an IO problem while adding the card.
	 * @throws JsonMappingException
	 *             Thrown if there is a problem mapping the card object to a
	 *             JSON string to store in the DB.
	 * @throws JsonGenerationException
	 *             Thrown if there is a problem generating the JSON form of the
	 *             card.
	 */
	public synchronized boolean addCard(TMMCard toAdd)
			throws JsonGenerationException, JsonMappingException, IOException {

		// get a UUID to use from couch
		String uuid = getUUID();
		toAdd.setuuId(uuid);

		ObjectMapper mapper = new ObjectMapper();
		Log.d(TAG, "JSON'd value: " + mapper.writeValueAsString(toAdd));

		// Create a new HttpClient and put Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut cardMaker = new HttpPut(LOCAL_DB_URL + ":" + port + "/"
				+ dbName + "/" + uuid);
		cardMaker.addHeader("Content-Type", "application/json");
		cardMaker.addHeader("Accept", "application/json");
		cardMaker.setEntity(new StringEntity(mapper.writeValueAsString(toAdd)));

		// execute the put and record the response
		HttpResponse response = null;
		try {
			response = httpclient.execute(cardMaker);
		} catch (ClientProtocolException e) {

			Log.e(TAG, "error card adding", e);
		} catch (IOException e) {
			Log.e(TAG, "IO error card adding", e);
		}

		Log.i(TAG, "server response to put: " + response);

		// parse the reponse
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent(), "UTF-8"));
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

		// upload attachments
		uploadCardAttachments(toAdd, jsonObject);

		String okResult = "";
		try {
			okResult = jsonObject.getString("ok");
		} catch (JSONException e) {
			Log.e(TAG, "DB reports creation did not go well...", e);
		}

		// check for error condition
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

		if (okResult.equalsIgnoreCase("true")) {
			startSync(true);
			return true;
		} else {
			Log.w(TAG, "CREATION FAILURE -" + reason);
			return false;
		}

	}

	/**
	 * Gets a new uuid from the database to use for the addition of new material
	 * to the DB.
	 * 
	 * @return A string containing a valid UUID from the database.
	 */
	private String getUUID() {

		// Create a new HttpClient and get Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet uuidGetter = new HttpGet(LOCAL_DB_URL + ":" + port + "/"
				+ UUID_GETTER_PATH);

		// execute the put and record the response
		HttpResponse response = null;
		try {
			response = httpclient.execute(uuidGetter);
		} catch (ClientProtocolException e) {

			Log.e(TAG, "error creating DB", e);
		} catch (IOException e) {
			Log.e(TAG, "IO error creating DB", e);
		}

		Log.i(TAG, "server response to uuid get: " + response);

		// parse the reponse
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent(), "UTF-8"));
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

		// extract the UUID from the JSON object that is returned from the call
		String uuidToRet = "";
		try {
			uuidToRet = uuids.getString(0);
		} catch (JSONException e) {

			e.printStackTrace();
		}

		Log.i(TAG, "Returning UUID: " + uuidToRet);
		return uuidToRet;

	}

	/**
	 * Upload all card attachments for a single {@link TMMCard}.
	 * 
	 * @param toAdd
	 *            The card containing valid paths to the files to be uploaded.
	 * @param jsonObject
	 *            The JSON object returned when the card was added to the
	 *            database. This is used to revise and access the card in the DB
	 *            to add the attachments.
	 * @return true, if all of the card's attachments were uploaded successfully
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private boolean uploadCardAttachments(TMMCard toAdd, JSONObject jsonObject)
			throws IOException {
		// will need case statement for each subclass of TMMCard
		if (toAdd instanceof VideoCard) {
			if (((VideoCard) toAdd).hasScreenshot()) {

				// write binary byte array of the attachment to the server
				Bitmap bmp = BitmapFactory.decodeFile(((VideoCard) toAdd)
						.getScreenshotPath());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
				Log.d(TAG, "video card background image being uploaded: "
						+ ((VideoCard) toAdd).getScrenshotname());
				uploadSingleAttachment(out.toByteArray(), jsonObject,
						((VideoCard) toAdd).getScrenshotname(), "image/jpg");
			}
			return true;
		} else if (toAdd instanceof AudioCard) {
			if (((AudioCard) toAdd).hasBackground()) {
				Bitmap bmp = BitmapFactory.decodeFile(((AudioCard) toAdd)
						.getBackgroundPath());
				// write binary byte array of the attachment to the server
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
				Log.d(TAG, "filename audio background image being uploaded: "
						+ ((AudioCard) toAdd).getBackground_name());
				uploadSingleAttachment(out.toByteArray(), jsonObject,
						((AudioCard) toAdd).getBackground_name(), "image/jpg");
				// update the parameter
				jsonObject = getJSONRepresentation(toAdd);
			}

			if (((AudioCard) toAdd).hasAudio()) {
				File audio = new File(((AudioCard) toAdd).getAudioClipPath());
				FileInputStream fileInputStream = new FileInputStream(audio);
				// write binary byte array of the attachment to the server
				byte[] b = new byte[(int) audio.length()];
				fileInputStream.read(b);
				fileInputStream.close();
				uploadSingleAttachment(b, jsonObject,
						((AudioCard) toAdd).getAudioClipName(), "audio/mpeg");
			}

			return true;
		} else if (toAdd instanceof TextCard) {
			// for a text card, first we store the icon if it exists
			if (((TextCard) toAdd).hasIcon()) {
				Bitmap bmp = BitmapFactory.decodeFile(((TextCard) toAdd)
						.getIconPath());
				// write binary byte array of the attachment to the server
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
				Log.d(TAG, "filename of icon image being uploaded: "
						+ ((TextCard) toAdd).getIcFileName());
				uploadSingleAttachment(out.toByteArray(), jsonObject,
						((TextCard) toAdd).getIcFileName(), "image/jpg");

			}

			// now, we need to upload any images in this file
			// start by getting the JSON representation

			JSONObject respObj = getJSONRepresentation(toAdd);

			// go through all the elements of this Textcard, uploading images
			// when necessary.
			for (int i = 0; i < ((TextCard) toAdd).getContents().size(); i++) {
				TextElement temp = ((TextCard) toAdd).getContents().get(i);
				// we only care about uploading images
				if (temp.getType() == Type.IMAGE) {
					Bitmap bmp = BitmapFactory.decodeFile(temp.getImg());
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
					Log.d(TAG, "filename of textelement image being uploaded: "
							+ temp.getImgFilename());
					uploadSingleAttachment(out.toByteArray(), respObj,
							temp.getImgFilename(), "image/jpg");
					// confirm revision occurred, and update our rev number
					respObj = getJSONRepresentation(toAdd);
				}

			}

			return true;
		} else
			return false;

	}

	/**
	 * Upload single attachment to the JSONObject passed in.
	 * 
	 * @param data
	 *            The binary representation of the attachment to store in the
	 *            database.
	 * @param jsonObject
	 *            The JSON Object containing the identifier and revision number
	 *            of the document to which the attachment will be uploaded.
	 * @param fileName
	 *            The attachment's filename, which is what it will be called
	 *            after it is attached.
	 * @param contentType
	 *            The MIME-type, useful for browsers wishing to view the
	 *            content.
	 * @return true, if the attachment is successfully uploaded
	 */
	private boolean uploadSingleAttachment(byte[] data, JSONObject jsonObject,
			String fileName, String contentType) {

		// extract relevant information (UUID and rev no.)
		final String exRev = "1-4ce605cd9fac335e98662dd4645cd332";
		final String exUUID = "c629e32ea1c54b9b0840f0161000706e";

		if (jsonObject == null) {
			Log.w(TAG, "null jsonobject passed to uploadsingleattachmnet");
			return false;
		}
		// parse the JSONObject to get the info that we need

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

		// sanity checks
		if (revNo.length() != exRev.length()) {
			Log.w(TAG, "No revision found in uploadsingleattachment");
			return false;
		}

		if (uuid.length() != exUUID.length()) {
			Log.w(TAG, "No UUID found in uploadsingleattachment");
			return false;
		}

		// Create a new HttpClient and put Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut attachmentAdder = new HttpPut(LOCAL_DB_URL + ":" + port + "/"
				+ dbName + "/" + uuid + "/" + fileName + "?rev=" + revNo);

		if (contentType != null && !contentType.equalsIgnoreCase("")) {
			attachmentAdder.addHeader("Content-Type", contentType);
		}

		ByteArrayEntity attachEnt = new ByteArrayEntity(data);
		attachmentAdder.setEntity(attachEnt);

		// execute the put and record the response
		HttpResponse response = null;
		try {
			response = httpclient.execute(attachmentAdder);
		} catch (ClientProtocolException e) {

			Log.e(TAG, "error attachment adding", e);
		} catch (IOException e) {
			Log.e(TAG, "IO error attachment adding", e);
		}

		Log.i(TAG, "server response to put: " + response);

		// parse the reponse
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent(), "UTF-8"));
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

		// check for error condition
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

		if (okResult.equalsIgnoreCase("true")) {
			return true;
		} else {
			Log.w(TAG, "CREATION FAILURE -" + reason);
			return false;
		}
	}

	/**
	 * The Class JSONGetter. This runnable is used to perform asynchronous HTTP
	 * gets for JSON in the local DB. After the run() method completes (i.e. the
	 * class is joined()) then the retrieved data is stored in the class member
	 * vars and can be retrieved via the getter(s) provided.
	 * 
	 * @author atm011
	 */
	private class JSONGetter extends Thread {

		/** The JSON string containing the JSOn item retreived. */
		private String json = "";

		/**
		 * Instantiates a new JSON getter.
		 */
		public JSONGetter() {
			super();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {

			// then we must have a good UUID
			// Create a new HttpClient and get Header
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet everythingGetter = new HttpGet(LOCAL_DB_URL + ":" + port
					+ "/" + dbName);
			// execute the put and record the response
			HttpResponse response = null;
			try {
				response = httpclient.execute(everythingGetter);
			} catch (ClientProtocolException e) {

				Log.e(TAG, "error getting the DB as JSON", e);
			} catch (IOException e) {
				Log.e(TAG, "IO error getting the DB as JSON", e);
			}

			Log.i(TAG, "server response to all database get: " + response);

			// parse the reponse
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// String json = null;
			try {
				json = reader.readLine();
				Log.d(TAG, "Raw json string: " + json);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		/**
		 * Gets the JSON object retrieved by this method.
		 * 
		 * @return A string containing the JSON representation of the object
		 *         retrieved by this method.
		 */
		public String getJson() {
			return json;
		}

	}

	/**
	 * Get the entire contents of the currently open database as a JSON string.
	 * 
	 * @return A string containing the JSON representation of all the documents
	 *         in the currently open DB.
	 */
	public String getEntireDbAsJSON() {

		JSONGetter jsonGetter = new JSONGetter();

		jsonGetter.start();
		try {
			jsonGetter.join();
		} catch (InterruptedException e) {

			e.printStackTrace();
			return null;
		}

		Log.d(TAG,
				"String being returned from getEntireDBasJSON "
						+ jsonGetter.getJson());
		return jsonGetter.getJson();

	}

	/**
	 * Delete the specified card from the database. In order for a successful
	 * deletion to occur, the specified card MUST have a valid UUID, as that is
	 * how the DB copy is accessed and deleted.
	 * 
	 * @param todel
	 *            The {@link TMMCard} object to delete from the database.
	 * @return true, if the specified card was successfully deleted from the
	 *         database.
	 */
	public synchronized boolean deleteCardfromDB(TMMCard todel) {

		// perform sanity checks on the UUID
		final String exUUID = "c629e32ea1c54b9b0840f0161000706e";

		if (todel.getuuId() == null) {
			Log.w(TAG, "bad UUID passed to deletecardfromdb");
			return false;
		}

		if (todel.getuuId().length() != exUUID.length()) {
			Log.w(TAG, "bad UUID passed to deletecardfromdb");
			return false;
		}

		// use the UUID to get the current REV
		JSONObject jsonObject = getJSONRepresentation(todel);

		if (jsonObject == null) {
			Log.e(TAG,
					"no JSON retreived for the card with UUID "
							+ todel.getuuId() + " in deletecardfromDB");
		}

		// parse the JSONObject to get the info that we need

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
		HttpDelete uuidGetter = new HttpDelete(LOCAL_DB_URL + ":" + port + "/"
				+ dbName + "/" + uuid + "?rev=" + revNo);

		// execute the delete and record the response
		HttpResponse response = null;
		try {
			response = httpclient.execute(uuidGetter);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "error deleting document", e);
		} catch (IOException e) {
			Log.e(TAG, "IO error delting document", e);
		}

		// parse the reponse
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent(), "UTF-8"));
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

		// push changes to remote server
		startSync(true);

		return true;

	}

	/**
	 * Creates a new database with the specified name locally, then synchronizes
	 * the new database with the remote repository.
	 * 
	 * @param dbname
	 *            The name of the database to create.
	 * @return true, if the database has been created successfully.
	 * @throws IllegalStateException
	 *             Thrown if the input stream cannot be read properly. s * @throws
	 *             IOException Signals that an I/O exception has occurred.
	 */
	public boolean createNewDB(String dbname) throws IllegalStateException,
	IOException {
		// Create a new HttpClient and put Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut dbMaker = new HttpPut(LOCAL_DB_URL + ":" + port + "/" + dbname);

		// execute the put and record the response
		HttpResponse response = null;
		try {
			response = httpclient.execute(dbMaker);
		} catch (ClientProtocolException e) {

			Log.e(TAG, "error creating DB", e);
		} catch (IOException e) {
			Log.e(TAG, "IO error creating DB", e);
		}

		Log.i(TAG, "server response to put: " + response);

		// parse the reponse
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent(), "UTF-8"));
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

		// check for any errors in creation
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

		// make sure the DB was created properly
		if (okResult.equalsIgnoreCase("true")) {
			startSync(true);
			return true;
		} else {
			Log.w(TAG, "CREATION FAILURE -" + reason);
			return false;
		}
	}

	// TODO
	/**
	 * Gets ALL cards from all servers, sorted by priority.
	 * 
	 * @return The arrayList of {@link TMMCard} objects that consists of every
	 *         card from every database maintained locally by the app.
	 */
	public synchronized ArrayList<TMMCard> getAllCards() {
		Log.d(TAG, "get all cards");

		ArrayList<TMMCard> matches = new ArrayList<TMMCard>();

		// sort cards by priority
		Collections.sort(matches);

		return matches;
	}

	/**
	 * Retrieve all the cards from the currently open database in the DBManager
	 * object.
	 * 
	 * @return The arraylist of all available {@link TMMCard} objects for the
	 *         currently open database.
	 */
	public ArrayList<TMMCard> findCardsbyServer() {

		ArrayList<TMMCard> cardlist = new ArrayList<TMMCard>();

		try {

			// get everything
			JSONObject[] documents = getEntireDbAsJSON(LOCAL_DB_URL, 5984,
					dbName);
			for (int i = 0; i < documents.length; i++) {
				TMMCard result = convertJSONToCard(documents[i]);

				// add new card to result list
				cardlist.add(result);
				if (result != null) {
					String temp = "TMM";
					if (result instanceof AudioCard)
						temp = "Audio";
					if (result instanceof VideoCard)
						temp = "Video";
					if (result instanceof TextCard)
						temp = "Text";
					Log.i(TAG,
							temp + "Card Created:\nTitle: " + result.getTitle()
							+ "\nID: " + result.getuuId() + "\n");
				}
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}

		// sort the list by priority
		Collections.sort(cardlist);
		return cardlist;

	}

}