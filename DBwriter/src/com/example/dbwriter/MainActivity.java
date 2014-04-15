package com.example.dbwriter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

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
import org.json.JSONTokener;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.example.dbwriter.TextElement.Type;











import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity implements Replication.ChangeListener{

	public static final String TAG = "DBwriter mainactivity"; 
	public static final String EXAMPLE_CARD_SERVER = "example_card_generator";
	public static final String UUID_GETTER_PATH = "_uuids";
	private static final String DATABASE_NAME = "dbwriter_test";
	public static final String SYNC_URL = "http://192.168.1.2:5984"; 
	ArrayList<TMMCard> cardz = new ArrayList<TMMCard>();
	ArrayList<Server> servz = new ArrayList<Server>();
	private Database database;
	private Manager manager;
	JSONObject jo = null;


	private int startCBLListener(int suggestedListenPort) throws IOException, CouchbaseLiteException {

		manager = startCBLite();

		startDatabase(manager, DATABASE_NAME);

		LiteListener listener = new LiteListener(manager, suggestedListenPort);
		int port = listener.getListenPort();
		Thread thread = new Thread(listener);
		thread.start();

		return port;

	}

	protected Manager startCBLite() throws IOException {

		Manager manager = new Manager(new AndroidContext(this).getFilesDir(), Manager.DEFAULT_OPTIONS);
		return manager;
	} 

	protected void startDatabase(Manager manager, String databaseName) throws CouchbaseLiteException {
		database = manager.getDatabase(databaseName);
		database.open();
	}

	@Override
	public void changed(Replication.ChangeEvent event) {

		Replication replication = event.getSource();
		Log.d(TAG, "Replication : " + replication + " changed.");
		if (!replication.isRunning()) {
			String msg = String.format("Replicator %s not running", replication);
			Log.d(TAG, msg);
		}
		else {
			int processed = replication.getCompletedChangesCount();
			int total = replication.getChangesCount();
			String msg = String.format("Replicator processed %d / %d", processed, total);
			Log.d(TAG, msg);
		}

	}




	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		loadCards();
		//		try {
		//			startCBLListener(5984);
		//		} catch (IOException e2) {
		//			e2.printStackTrace();
		//		} catch (CouchbaseLiteException e2) {
		//			e2.printStackTrace();
		//		}


		//		//phase 1: create DB with server name
		//		Thread t1 = new Thread(new Runnable() {
		//			public void run() {
		//				try {
		//					createNewDB("http://134.82.132.99", 5984, servz.get(0).getName());
		//				} catch (IllegalStateException e) { 
		//					e.printStackTrace();
		//				} catch (IOException e) {
		//					e.printStackTrace();
		//				}
		//			}
		//		});
		//
		//		t1.start();
		//		try {
		//			t1.join();
		//		} catch (InterruptedException e1) {
		//			e1.printStackTrace();
		//		}

		//phase 1: create DB with server name
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				try {
					//createNewDB("http://134.82.132.99", 5984, servz.get(0).getName());
					getSingleAttachment("graph_1.jpg", "c629e32ea1c54b9b0840f0161007f280", "/storage/sdcard0/tmm2");
				} catch (IllegalStateException e) { 
					e.printStackTrace();
				}
			}
		});

		t1.start();
		try {
			t1.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}


		//		//phase 2.1:  get data from DB with HTTP GET: UUIDs
		//
		//		Thread t2 = new Thread(new Runnable() {
		//			public void run() {
		//				try {
		//					getUUID("http://134.82.132.99", 5984);
		//				} catch (IllegalStateException e) {
		//					e.printStackTrace();
		//				}
		//
		//				Log.i(TAG, "Beginning Sync...");
		//				startSync();
		//
		//			}
		//		});


		//local replication test: get raw JSON for everything in the DB
		//mostly a check to assure that the content is there

		//		Thread t3 = new Thread(new Runnable() {
		//			public void run() {
		//				try {
		//					jo = getEntireDbAsJSON("http://127.0.0.1", 5984, DATABASE_NAME);
		//				} catch (IllegalStateException e) {
		//					e.printStackTrace();
		//				} 
		//			}
		//		});
		//
		//		t3.start();
		//		try {
		//			t3.join();
		//		} catch (InterruptedException e) {
		//			e.printStackTrace();
		//		}
		//phase 3: add a card from the DB
		//
		//			Thread t4 = new Thread(new Runnable() {
		//			public void run() {
		//				try {
		//
		//					TMMCard temp = null;
		//					//try to add an object that doesn't yet exist, but has a valid UUID
		//					for(int i = 0; i < cardz.size(); i++){
		//						temp = cardz.get(i);
		//						temp.setuuId(getUUID("http://134.82.132.99", 5984));
		//						Log.i(TAG, "Thread t4, call addcardtoDB returns: " + addCardToDB(temp, "http://192.168.1.2", 5984, servz.get(0).getName()));
		//					}
		//
		//					//	Log.d(TAG, "Deleted card: " + temp.toString());
		//					//Log.d(TAG, "delete card from DB returs: " + deleteCardfromDB(temp,"http://192.168.1.2", 5984, servz.get(0).getName()));
		//
		//					//should throw an exception here
		//					//Log.i(TAG, "Thread t4, second addCardto DB returns: " + addCardToDB(temp, "http://192.168.1.2", 5984, servz.get(0).getName()));
		//				} catch (IllegalStateException e) { 
		//					e.printStackTrace();
		//				} catch (Exception e) {
		//					e.printStackTrace();
		//				}
		//			}
		//		});
		//
		//		t4.start();
		//		try {
		//			t4.join();
		//		} catch (InterruptedException e) {
		//			e.printStackTrace();
		//		}


		//		// Test Card parsing from JSON object
		//		Thread t6 = new Thread(new Runnable() {
		//			public void run() {
		//				Log.i(TAG,"Starting JSON retrieval and Card Parsing");
		//				JSONObject document;
		//				TMMCard result;
		//				try {
		//					// TextCard retrieval
		//					document = getDocumentAsJSON("http://134.82.132.99", 5984, "example_card_generator", "c629e32ea1c54b9b0840f01610070b68");
		//					result = convertJSONToCard(document);
		//					if (result != null)
		//						Log.i(TAG, "Card Created:\nTitle: " + result.getTitle() + 
		//								"\nID: " + result.getuuId() + "\n");
		//					
		//					// AudioCard retrieval
		//					document = getDocumentAsJSON("http://134.82.132.99", 5984, "example_card_generator", "c629e32ea1c54b9b0840f0161007151f");
		//					result = convertJSONToCard(document);
		//					if (result != null)
		//						Log.i(TAG, "Card Created:\nTitle: " + result.getTitle() + 
		//								"\nID: " + result.getuuId() + "\n");
		//					
		//					// VideoCard retrieval
		//					document = getDocumentAsJSON("http://134.82.132.99", 5984, "example_card_generator", "c629e32ea1c54b9b0840f01610072710");
		//					result = convertJSONToCard(document);
		//					if (result != null)
		//						Log.i(TAG, "Card Created:\nTitle: " + result.getTitle() + 
		//								"\nID: " + result.getuuId() + "\n");
		//					
		//				} catch (IllegalStateException e) {
		//					e.printStackTrace();
		//				} 
		//			}
		//		});
		//
		//		t6.start();
		//		try {
		//			t6.join();
		//		} catch (InterruptedException e) {
		//			e.printStackTrace();
		//		}		

		//		// Test getting entire database functionality and turn the JSON objects into cards
		//		Thread t5 = new Thread(new Runnable() {
		//			public void run() {
		//				try {
		//					JSONObject[] documents = getEntireDbAsJSON("http://134.82.132.99", 5984, "example_card_generator");
		//					for (int i = 0; i < documents.length; i++) {
		//						TMMCard result = convertJSONToCard(documents[i]);
		//						if (result != null) {
		//							String temp = "TMM";
		//							if (result instanceof AudioCard) temp = "Audio";
		//							if (result instanceof VideoCard) temp = "Video";
		//							if (result instanceof TextCard) temp = "Text";
		//							Log.i(TAG, temp + "Card Created:\nTitle: " + result.getTitle() + 
		//									"\nID: " + result.getuuId() + "\n");
		//						}
		//					}
		//				} catch (IllegalStateException e) {
		//					e.printStackTrace();
		//				} 
		//			}
		//		});
		//
		//		t5.start();
		//		try {
		//			t5.join();
		//		} catch (InterruptedException e) {
		//			e.printStackTrace();
		//		}



		//
		//		//phase 2.2: get a card from the DB that has a UUID	
		//		Thread t3 = new Thread(new Runnable() {
		//			public void run() {
		//				try {
		//					TMMCard temp = cardz.get(0);
		//					temp.setuuId(getUUID("http://192.168.1.2", 5984));
		// 
		//					//try to add an object that doesn't yet exist, but has a valid UUID
		//					Log.i(TAG, "Thread t3, call to get JSON rep returns: " + getJSONRepresentation(temp, "http://192.168.1.2", 5984, servz.get(0).getName()));
		//				} catch (IllegalStateException e) {
		//					e.printStackTrace();
		//				}
		//			}
		//		});
		// 
		//		t3.start();
		//		try {
		//			t3.join();
		//		} catch (InterruptedException e) {
		//			e.printStackTrace();
		//		}


	}

	private class AttachmentDownloader extends Thread {

		private final String uuid, filename, pathToStore; 

		public AttachmentDownloader (String uuid, String fileName, String pathToStoreAttachment){
			super();
			this.uuid = uuid;
			this.filename = fileName;
			this.pathToStore = pathToStoreAttachment;
			
		}

		@Override
		public void run() { 

			HttpClient httpclient = new DefaultHttpClient();
			HttpGet attachment = new HttpGet("http://134.82.132.99" + ":" + 5984 + "/" + "example_card_generator" + "/" + uuid + "/" + filename);

			//execute the delete and record the response
			HttpResponse response = null;
			try {
				response = httpclient.execute(attachment);
			} catch (ClientProtocolException e) {
				Log.e(TAG, "error deleting document", e);
				return;
			} catch (IOException e) {
				Log.e(TAG, "IO error delting document", e);
				return;
			}

			//get all headers		
			Header[] headers = response.getAllHeaders();
			for (Header header : headers) {
				Log.i(TAG, "Key : " + header.getName() 
						+ " ,Value : " + header.getValue());
			}
			
			Header clHead = response.getFirstHeader("Content-Length");
			Log.i(TAG, "Content-length header: " + clHead.toString());
			
			int respSize = Integer.parseInt(clHead.getValue());
			Log.i(TAG, "buffer being created to size: " + Integer.parseInt(clHead.getValue()));
			binaryAttachment = new char[respSize];
			//parse the reponse 
			BufferedReader reader = null;
			FileOutputStream save0 = null;
			try {
				save0 = new FileOutputStream(new File(pathToSave +"/" + "graph_1.jpg"));
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
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

			try {
				reader.read(binaryAttachment);
			} catch (IOException e) {
				e.printStackTrace();
			}



		}



	}

	//pathToStore does not include the filename - that will be the same as the attachment name
	private boolean getSingleAttachment(String attachmentName, String uuidOfDoc, String pathToStore){
		AttachmentDownloader dwnldr = new AttachmentDownloader(uuidOfDoc, attachmentName);

		dwnldr.run();
		try {
			dwnldr.join();
		} catch (InterruptedException e) {

			e.printStackTrace();
			return false;
		}

		return true;


	}


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
	public String getRawJSON (String URL) {
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

	//	public JSONObject getEntireDbAsJSON(String serverURLsansPort, int port, String dbName){
	//		//then we must have a good UUID
	//		// Create a new HttpClient and get Header
	//		HttpClient httpclient = new DefaultHttpClient();
	//		//
	//		HttpGet everythingGetter = new HttpGet(serverURLsansPort + ":" + port + "/" + dbName + "/_all_docs?include_docs=true");
	//
	//		//execute the put and record the response
	//		HttpResponse response = null;
	//		try {
	//			response = httpclient.execute(everythingGetter);
	//		} catch (ClientProtocolException e) {
	//
	//			Log.e(TAG, "error getting the DB as JSON", e);
	//		} catch (IOException e) {
	//			Log.e(TAG, "IO error getting the DB as JSON", e);
	//		}
	//
	//		Log.i(TAG, "server response to all database get: " + response);
	//
	//		//parse the reponse 
	//		BufferedReader reader = null;
	//		try {
	//			reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
	//		} catch (UnsupportedEncodingException e) {
	//			e.printStackTrace();
	//		} catch (IllegalStateException e) {
	//			e.printStackTrace();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//		String json = null;
	//		try {
	//			json = reader.readLine();
	//			Log.d(TAG, "Raw json string: " + json);
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		}
	//
	//		JSONObject jsonObject = null;
	//		try {
	//			jsonObject = new JSONObject(json);
	//		} catch (JSONException e1) {
	//			Log.e(TAG, "error making json object", e1);
	//		}
	//		return jsonObject;
	//
	//	}

	private void startSync(boolean deleteDBbeforeUpdate) {



		URL syncUrl;
		try {
			syncUrl = new URL(SYNC_URL + "/"+ servz.get(0).getName());

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		Replication pullReplication = database.createPullReplication(syncUrl);
		pullReplication.setCreateTarget(true);
		pullReplication.setContinuous(false);

		//Replication pushReplication = database.createPushReplication(syncUrl);
		//pushReplication.setContinuous(false);

		pullReplication.start();
		//pushReplication.start();

		pullReplication.addChangeListener(this);
		//pushReplication.addChangeListener(this);

	}



	public boolean deleteDB(String serverURLsansPort, int port, String dbName, boolean areYouSure){
		if(!areYouSure){
			Log.d(TAG, "User wasn't sure");
			return false;
		}

		HttpClient httpclient = new DefaultHttpClient();
		HttpDelete dbdeleter = new HttpDelete(serverURLsansPort + ":" + port + "/" + dbName);

		//execute the delete and record the response
		HttpResponse response = null;
		try {
			response = httpclient.execute(dbdeleter);
		} catch (ClientProtocolException e) {
			Log.e(TAG, "error deleting document", e);
			return false;
		} catch (IOException e) {
			Log.e(TAG, "IO error delting document", e);
			return false;
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

		return true;

	}

	public JSONObject getJSONRepresentation(TMMCard toCheck, String serverURLsansPort, int port, String dbName){

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
		HttpGet cardGetter = new HttpGet(serverURLsansPort + ":" + port + "/" + dbName + "/" + toCheck.getuuId());

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

	public boolean addCardToDB(TMMCard toAdd, String serverURLsansPort, int port, String dbName) throws Exception{

		//check to make sure card doesn't already exist in DB
		if(this.getJSONRepresentation(toAdd, serverURLsansPort, port, dbName) != null){
			//maybe we could update the card or something
			//I'm going to punt on this, leave it as a TODO
			throw new Exception("The card already exists in the DB, please use the update method instead");

		}

		//if we made it here, safe to assume that the card doesn't exist in the DB
		//get a UUID to use from couch
		String uuid = getUUID(serverURLsansPort, port);
		toAdd.setuuId(uuid);

		ObjectMapper mapper = new ObjectMapper();
		Log.d(TAG, "JSON'd value: " + mapper.writeValueAsString(toAdd));


		// Create a new HttpClient and put Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut cardMaker = new HttpPut(serverURLsansPort + ":" + port + "/" + dbName + "/" + uuid);
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
		uploadCardAttachments(toAdd, jsonObject, serverURLsansPort, port, dbName);

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
			return true;
		}else {
			Log.w(TAG, "CREATION FAILURE -" + reason);
			return false; 
		}

	}

	public boolean deleteCardfromDB(TMMCard todel, String serverURLsansPort, int port, String dbName){

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
		JSONObject jsonObject =  getJSONRepresentation(todel, serverURLsansPort, port, dbName);

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
		HttpDelete uuidGetter = new HttpDelete(serverURLsansPort + ":" + port + "/" + dbName + "/" + uuid + "?rev=" + revNo);

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

		return true;


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


	private boolean uploadCardAttachments(TMMCard toAdd, JSONObject jsonObject, String serverURLsansPort, int port, String dbName) throws IOException{
		//will need case statement for each subclass of TMMCard
		if(toAdd instanceof VideoCard){
			if(((VideoCard) toAdd).hasScreenshot()){
				Bitmap bmp = BitmapFactory.decodeFile(((VideoCard)toAdd).getScreenshotPath());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
				Log.d(TAG, "video card background image being uploaded: " + ((VideoCard) toAdd).getScrenshotname());
				uploadSingleAttachment(out.toByteArray(), jsonObject, serverURLsansPort, port, ((VideoCard) toAdd).getScrenshotname(), dbName, "image/jpg");
			}
			return true;
		} else if(toAdd instanceof AudioCard) {
			if(((AudioCard) toAdd).hasBackground()){
				Bitmap bmp = BitmapFactory.decodeFile(((AudioCard)toAdd).getBackgroundPath());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
				Log.d(TAG, "filename audio background image being uploaded: " + ((AudioCard) toAdd).getBackground_name());
				uploadSingleAttachment(out.toByteArray(), jsonObject, serverURLsansPort, port, ((AudioCard) toAdd).getBackground_name(), dbName, "image/jpg");
				//update the parameter
				jsonObject = getJSONRepresentation(toAdd, serverURLsansPort, port, dbName);
			}

			if(((AudioCard) toAdd).hasAudio()){
				File audio = new File(((AudioCard) toAdd).getAudioClipPath());
				FileInputStream fileInputStream = new FileInputStream(audio);
				byte[] b = new byte[(int) audio.length()];
				fileInputStream.read(b);
				fileInputStream.close();
				uploadSingleAttachment(b, jsonObject, serverURLsansPort, port, ((AudioCard) toAdd).getAudioClipName(), dbName, "audio/mpeg");
			}


			return true;
		} else if(toAdd instanceof TextCard) {
			//for a text card, first we store the icon if it exists
			if(((TextCard) toAdd).hasIcon()){
				Bitmap bmp = BitmapFactory.decodeFile(((TextCard)toAdd).getIconPath());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
				Log.d(TAG, "filename of icon image being uploaded: " + ((TextCard) toAdd).getIcFileName());
				uploadSingleAttachment(out.toByteArray(), jsonObject, serverURLsansPort, port, ((TextCard) toAdd).getIcFileName(), dbName, "image/jpg");

			}

			//now, we need to upload any images in this file
			//start by getting the JSON representation

			JSONObject respObj = getJSONRepresentation(toAdd, serverURLsansPort, port, dbName);

			for(int i = 0; i < ((TextCard) toAdd).getContents().size(); i++){
				TextElement temp = ((TextCard) toAdd).getContents().get(i);
				//we only care about uploading images
				if(temp.getType() == Type.IMAGE){
					Bitmap bmp = BitmapFactory.decodeFile(temp.getImg());
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
					Log.d(TAG, "filename of textelement image being uploaded: " + temp.getImgFilename());
					uploadSingleAttachment(out.toByteArray(), respObj, serverURLsansPort, port, temp.getImgFilename(), dbName, "image/jpg");
					//confirm revision occured, and update our rev number
					respObj = getJSONRepresentation(toAdd, serverURLsansPort, port, dbName); 
				}

			}


			return true;
		} else return false;

	}

	private boolean uploadSingleAttachment(byte[] data, JSONObject jsonObject, String serverURLsansPort, int port, String fileName, String dbName, String contentType){

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
		HttpPut attachmentAdder = new HttpPut(serverURLsansPort + ":" + port + "/" + dbName + "/" + uuid + "/" + fileName + "?rev=" + revNo);

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
			return true;
		}else {
			Log.w(TAG, "CREATION FAILURE -" + reason);
			return false; 
		}


	}



	private void loadCards() {

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
