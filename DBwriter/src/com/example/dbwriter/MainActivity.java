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
import android.os.Looper;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String TAG = "DBwriter mainactivity";
	public static final String EXAMPLE_CARD_SERVER = "example_card_generator";
	public static final String POSTER_CARD_SERVER = "tellmemore_poster";
	public static final String UUID_GETTER_PATH = "_uuids";
	public static final String SYNC_URL = "http://192.168.1.2:5984";
	private Context context;
	/**
	 * The Constant MASTER_SERVER_URL. This URL is for the server containing all
	 * the cards to synchronize with.
	 */
	public static final String MASTER_SERVER_URL = "http://134.82.132.99";

	private static Button deleteExample, deletePoster, makeExample, makePoster;
	ArrayList<TMMCard> cardz = new ArrayList<TMMCard>();
	ArrayList<Server> servz = new ArrayList<Server>();

	JSONObject jo = null;

	public void findViews() {
		deleteExample = (Button) findViewById(R.id.deleteExampleDB);
		deletePoster = (Button) findViewById(R.id.deletePosterDB);
		makeExample = (Button) findViewById(R.id.makeExampleDB);
		makePoster = (Button) findViewById(R.id.makePosterDB);

	}

	public void setListeners() {
		deleteExample.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				Thread t1 = new Thread(new Runnable() {
					public void run() {
						try {
							Looper.prepare();
							boolean result = deleteDB(MASTER_SERVER_URL, 5984,
									EXAMPLE_CARD_SERVER, true);
							Toast.makeText(context,
									"deleted example DB, result is: " + result,
									Toast.LENGTH_LONG).show();
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

			}
		});

		deletePoster.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				Thread t1 = new Thread(new Runnable() {
					public void run() {
						try {
							boolean result = false;
							Looper.prepare();
							result = deleteDB(MASTER_SERVER_URL, 5984,
									POSTER_CARD_SERVER, true);
							Toast.makeText(context,
									"deleted poster DB, result is: " + result,
									Toast.LENGTH_LONG).show();
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

			}
		});

		makeExample.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				Thread t1 = new Thread(new Runnable() {
					public void run() {
						try {
							createNewDB(MASTER_SERVER_URL, 5984,
									EXAMPLE_CARD_SERVER);
						} catch (IllegalStateException e) {
							e.printStackTrace();
						} catch (IOException e) {
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

				loadExampleCards();

				// phase 2: add the cards from the DB

				Thread t4 = new Thread(new Runnable() {
					public void run() {
						try {

							TMMCard temp = null;
							// try to add an object that doesn't yet exist, but
							// has a valid UUID
							for (int i = 0; i < cardz.size(); i++) {
								temp = cardz.get(i);
								temp.setuuId(getUUID(MASTER_SERVER_URL, 5984));
								Log.i(TAG,
										"Thread t4, call addcardtoDB returns: "
												+ addCardToDB(temp,
														MASTER_SERVER_URL,
														5984,
														EXAMPLE_CARD_SERVER));
							}

						} catch (IllegalStateException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

				t4.start();
				try {
					t4.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Toast.makeText(
						context,
						"created example DB, server returns: "
								+ getEntireDbAsJSON(EXAMPLE_CARD_SERVER),
								Toast.LENGTH_LONG).show();

			}
		});
		makePoster.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				Thread t1 = new Thread(new Runnable() {
					public void run() {
						try {
							createNewDB(MASTER_SERVER_URL, 5984,
									POSTER_CARD_SERVER);
						} catch (IllegalStateException e) {
							e.printStackTrace();
						} catch (IOException e) {
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

				loadPosterCards();

				// phase 2: add the cards from the DB

				Thread t4 = new Thread(new Runnable() {
					public void run() {
						try {

							TMMCard temp = null;
							// try to add an object that doesn't yet exist, but
							// has a valid UUID
							Log.i(TAG,
									"about to add to DB in thread, cardz.size is: "
											+ cardz.size());
							for (int i = 0; i < cardz.size(); i++) {
								temp = cardz.get(i);
								temp.setuuId(getUUID(MASTER_SERVER_URL, 5984));
								Log.i(TAG,
										"Thread t4, call addcardtoDB returns: "
												+ addCardToDB(temp,
														MASTER_SERVER_URL,
														5984,
														POSTER_CARD_SERVER));
							}

						} catch (IllegalStateException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

				t4.start();
				try {
					t4.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Toast.makeText(
						context,
						"created poster DB, server returns: "
								+ getEntireDbAsJSON(POSTER_CARD_SERVER),
								Toast.LENGTH_LONG).show();

			}

		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		findViews();
		setListeners();

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
	 * 
	 * @param serverURLsansPort
	 *            the URL of the server (without the port)
	 * @param port
	 *            the port number of the server
	 * @param dbName
	 *            the name of the database (in this case project)
	 * @return all documents associated with a given database
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
	 * The Class JSONGetter. This runnable is used to perform asynchronous HTTP
	 * gets for JSON in the local DB. After the run() method completes (i.e. the
	 * class is joined()) then the retrieved data is stored in the class member
	 * vars and can be retrieved via the getter(s) provided.
	 * 
	 * @author atm011
	 */
	private class JSONGetter extends Thread {

		/** The JSON string containing the JSOn item retrieved. */
		private String json = "";
		private String dbName;

		/**
		 * Instantiates a new JSON getter.
		 */
		public JSONGetter(String name) {
			super();
			dbName = name;
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
			HttpGet everythingGetter = new HttpGet(MASTER_SERVER_URL + ":"
					+ 5984 + "/" + dbName);
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
	public String getEntireDbAsJSON(String name) {

		JSONGetter jsonGetter = new JSONGetter(name);

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
	 * Obtains a single document from a couchDB with a given database name and
	 * UUID
	 * 
	 * @param serverURLsansPort
	 *            the URL of the server (without the port)
	 * @param port
	 *            the port number of the server
	 * @param dbName
	 *            the name of the database (in this case project)
	 * @param UUID
	 *            the ID of the requested document
	 * @return the requested document in the form of a JSON Object. Returns null
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
	 * Retrieves an unparsed JSON string from a couchDB database
	 * 
	 * @param URL
	 *            the URL of the document wanted from a database
	 * @return the requested raw, unparsed JSON string. Returns null if
	 *         retrieval fails.
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

	public boolean deleteDB(String serverURLsansPort, int port, String dbName,
			boolean areYouSure) {
		if (!areYouSure) {
			Log.d(TAG, "User wasn't sure");
			return false;
		}

		HttpClient httpclient = new DefaultHttpClient();
		HttpDelete dbdeleter = new HttpDelete(serverURLsansPort + ":" + port
				+ "/" + dbName);

		// execute the delete and record the response
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

		return true;

	}

	public JSONObject getJSONRepresentation(TMMCard toCheck,
			String serverURLsansPort, int port, String dbName) {

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
		HttpGet cardGetter = new HttpGet(serverURLsansPort + ":" + port + "/"
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

	public boolean addCardToDB(TMMCard toAdd, String serverURLsansPort,
			int port, String dbName) throws Exception {

		// check to make sure card doesn't already exist in DB
		if (this.getJSONRepresentation(toAdd, serverURLsansPort, port, dbName) != null) {
			// maybe we could update the card or something
			// I'm going to punt on this, leave it as a TODO
			throw new Exception(
					"The card already exists in the DB, please use the update method instead");

		}

		// if we made it here, safe to assume that the card doesn't exist in the
		// DB
		// get a UUID to use from couch
		String uuid = getUUID(serverURLsansPort, port);
		toAdd.setuuId(uuid);

		ObjectMapper mapper = new ObjectMapper();
		Log.d(TAG, "JSON'd value: " + mapper.writeValueAsString(toAdd));

		// Create a new HttpClient and put Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut cardMaker = new HttpPut(serverURLsansPort + ":" + port + "/"
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
		uploadCardAttachments(toAdd, jsonObject, serverURLsansPort, port,
				dbName);

		String okResult = "";
		try {
			okResult = jsonObject.getString("ok");
		} catch (JSONException e) {
			// e.printStackTrace();
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

		if (okResult.equalsIgnoreCase("true")) {
			return true;
		} else {
			Log.w(TAG, "CREATION FAILURE -" + reason);
			return false;
		}

	}

	public boolean deleteCardfromDB(TMMCard todel, String serverURLsansPort,
			int port, String dbName) {

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
		JSONObject jsonObject = getJSONRepresentation(todel, serverURLsansPort,
				port, dbName);

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
		HttpDelete uuidGetter = new HttpDelete(serverURLsansPort + ":" + port
				+ "/" + dbName + "/" + uuid + "?rev=" + revNo);

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

		return true;

	}

	private String getUUID(String serverURLsansPort, int port) {

		// Create a new HttpClient and get Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet uuidGetter = new HttpGet(serverURLsansPort + ":" + port + "/"
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

		String uuidToRet = "";
		try {
			uuidToRet = uuids.getString(0);
		} catch (JSONException e) {

			e.printStackTrace();
		}

		Log.i(TAG, "Returning UUID: " + uuidToRet);
		return uuidToRet;

	}

	private boolean uploadCardAttachments(TMMCard toAdd, JSONObject jsonObject,
			String serverURLsansPort, int port, String dbName)
					throws IOException {
		// will need case statement for each subclass of TMMCard
		if (toAdd instanceof VideoCard) {
			if (((VideoCard) toAdd).hasScreenshot()) {
				Bitmap bmp = BitmapFactory.decodeFile(((VideoCard) toAdd)
						.getScreenshotPath());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
				Log.d(TAG, "video card background image being uploaded: "
						+ ((VideoCard) toAdd).getScrenshotname());
				uploadSingleAttachment(out.toByteArray(), jsonObject,
						serverURLsansPort, port,
						((VideoCard) toAdd).getScrenshotname(), dbName,
						"image/jpg");
			}
			return true;
		} else if (toAdd instanceof AudioCard) {
			if (((AudioCard) toAdd).hasBackground()) {
				Log.i(TAG, "Audiocard attachment found, path: "
						+ ((AudioCard) toAdd).getBackgroundPath());
				Bitmap bmp = BitmapFactory.decodeFile(((AudioCard) toAdd)
						.getBackgroundPath());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
				Log.d(TAG, "filename audio background image being uploaded: "
						+ ((AudioCard) toAdd).getBackground_name());
				uploadSingleAttachment(out.toByteArray(), jsonObject,
						serverURLsansPort, port,
						((AudioCard) toAdd).getBackground_name(), dbName,
						"image/jpg");
				// update the parameter
				jsonObject = getJSONRepresentation(toAdd, serverURLsansPort,
						port, dbName);
			}

			if (((AudioCard) toAdd).hasAudio()) {
				File audio = new File(((AudioCard) toAdd).getAudioClipPath());
				FileInputStream fileInputStream = new FileInputStream(audio);
				byte[] b = new byte[(int) audio.length()];
				fileInputStream.read(b);
				fileInputStream.close();
				uploadSingleAttachment(b, jsonObject, serverURLsansPort, port,
						((AudioCard) toAdd).getAudioClipName(), dbName,
						"audio/mpeg");
			}

			return true;
		} else if (toAdd instanceof TextCard) {
			// for a text card, first we store the icon if it exists
			if (((TextCard) toAdd).hasIcon()) {
				Bitmap bmp = BitmapFactory.decodeFile(((TextCard) toAdd)
						.getIconPath());
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
				Log.d(TAG, "filename of icon image being uploaded: "
						+ ((TextCard) toAdd).getIcFileName());
				uploadSingleAttachment(out.toByteArray(), jsonObject,
						serverURLsansPort, port,
						((TextCard) toAdd).getIcFileName(), dbName, "image/jpg");

			}

			// now, we need to upload any images in this file
			// start by getting the JSON representation

			JSONObject respObj = getJSONRepresentation(toAdd,
					serverURLsansPort, port, dbName);

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
							serverURLsansPort, port, temp.getImgFilename(),
							dbName, "image/jpg");
					// confirm revision occured, and update our rev number
					respObj = getJSONRepresentation(toAdd, serverURLsansPort,
							port, dbName);
				}

			}

			return true;
		} else
			return false;

	}

	private boolean uploadSingleAttachment(byte[] data, JSONObject jsonObject,
			String serverURLsansPort, int port, String fileName, String dbName,
			String contentType) {

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
		HttpPut attachmentAdder = new HttpPut(serverURLsansPort + ":" + port
				+ "/" + dbName + "/" + uuid + "/" + fileName + "?rev=" + revNo);

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

	public boolean createNewDB(String serverURLsansPort, int port, String dbname)
			throws IllegalStateException, IOException {
		// Create a new HttpClient and put Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut dbMaker = new HttpPut(serverURLsansPort + ":" + port + "/"
				+ dbname);

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

	private void loadExampleCards() {
		cardz = new ArrayList<TMMCard>();
		Server source1 = new Server(EXAMPLE_CARD_SERVER,
				"none-its not a network server", System.currentTimeMillis(),
				System.currentTimeMillis());
		// Server source2 = new
		// Server("CardloaderService's sample card generator second 'server'",
		// "none-its not a network server", System.currentTimeMillis(),
		// System.currentTimeMillis());
		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/tmm");
		TextCard textCard1 = new TextCard(100, "About the Author",
				"A. Student", "", "Tap to read more", getSampleArr1(), source1);

		// Bitmap bmp2 =
		// BitmapFactory.decodeResource(contexts[0].getResources(),
		// R.raw.acmicon);
		// ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
		// bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
		// byte[] iconArray = stream2.toByteArray();

		File file0 = new File(dir, "acmicon.jpg");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.acmicon);
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

		TextCard textCard2 = new TextCard(99, "Read Paper Abstract",
				"From ACM PAUC", "A. Student and Dr. XYZ",
				"Published 1 Mar 2009", file0.getAbsolutePath(),
				getSampleArr2(), source1);

		Log.d(TAG, "text card 2 added, returned: " + cardz.add(textCard2));
		Log.d(TAG, "text card 1 added, returned: " + cardz.add(textCard1));

		String path = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/tmm/powerpointdemo.mp3";

		dir.mkdirs();

		File file = new File(dir, "powerpointdemo.mp3");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn = getBaseContext().getResources().openRawResource(
				R.raw.powerpointdemo);
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

		AudioCard audioCard1 = new AudioCard(97, "Hear A Student Narration",
				file.getAbsolutePath(), source1);
		Log.d(TAG, "audio card 1 added, returned: " + cardz.add(audioCard1));

		File file11 = new File(dir, "chartreading.mp3");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn11 = getBaseContext().getResources().openRawResource(
				R.raw.chartreading);
		byte[] buffer11 = null;
		try {
			int size11 = fIn11.available();
			buffer11 = new byte[size11];
			fIn11.read(buffer11);
			fIn11.close();
		} catch (IOException e2) {
			Log.e(TAG, "IOException first part");

		}

		FileOutputStream save11;
		try {
			save11 = new FileOutputStream(file11);
			save11.write(buffer11);
			save11.flush();
			save11.close();
		} catch (FileNotFoundException e2) {
			Log.e(TAG, "FileNotFoundException in second part");

		} catch (IOException e1) {
			Log.e(TAG, "IOException in second part");

		}

		File file12 = new File(dir, "chartreadbkgrnd.png");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn12 = getBaseContext().getResources().openRawResource(
				R.raw.chartreadbkgrnd);
		byte[] buffer12 = null;
		try {
			int size12 = fIn12.available();
			buffer12 = new byte[size12];
			fIn12.read(buffer12);
			fIn12.close();
		} catch (IOException e2) {
			Log.e(TAG, "IOException first part");

		}

		FileOutputStream save12;
		try {
			save12 = new FileOutputStream(file12);
			save12.write(buffer12);
			save12.flush();
			save12.close();
		} catch (FileNotFoundException e2) {
			Log.e(TAG, "FileNotFoundException in second part");

		} catch (IOException e1) {
			Log.e(TAG, "IOException in second part");

		}

		AudioCard audioCard2 = new AudioCard(96, "Focus: Immigration Chart",
				file11.getAbsolutePath(), source1);
		audioCard2.setBackgroundPath(file12.getPath());

		Log.d(TAG, "audio card 2 added, returned: " + cardz.add(audioCard2));

		VideoCard videoCard1 = new VideoCard(90, "Watch the Experiment",
				"wtnI3kyCnmA", source1);
		File file9 = new File(dir, "reactor.png");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn9 = getBaseContext().getResources().openRawResource(
				R.raw.reactor);
		byte[] buffer9 = null;
		try {
			int size9 = fIn9.available();
			buffer9 = new byte[size9];
			fIn9.read(buffer9);
			fIn9.close();
		} catch (IOException e2) {
			Log.e(TAG, "IOException first part");

		}

		FileOutputStream save9;
		try {
			save9 = new FileOutputStream(file9);
			save9.write(buffer9);
			save9.flush();
			save9.close();
		} catch (FileNotFoundException e2) {
			Log.e(TAG, "FileNotFoundException in second part");

		} catch (IOException e1) {
			Log.e(TAG, "IOException in second part");

		}

		videoCard1.setScreenshot(file9.getPath());
		VideoCard videoCard2 = new VideoCard(89, "View the presentation",
				"cn5mMJiPYmw", source1);
		Log.d(TAG, "video card 1 added, returned: " + cardz.add(videoCard1));
		Log.d(TAG, "video card 2 added, returned: " + cardz.add(videoCard2));

		Log.d(TAG, "server source 1 added, returned: " + servz.add(source1));
	}

	private ArrayList<TextElement> getSampleArr1() {

		String text_above_pic = "A. Student was born in blah blah blah. It is a very interesting place. One of his passions has always been lightning. When he was just 6, he shot a famous photo of a lightning strike which is used on Wikipedia.";
		String text_below_pic = "Later in life, A. Student went to Africa where he photographed all manner of things. In the western sahara, he captured another famous photo of the setting african sun that won him a prize.";
		String caption1 = "\"When Clouds Attack\"";
		String caption2 = "\"A Nice Orange Sunset\"";

		ArrayList<TextElement> retVals = new ArrayList<TextElement>();
		TextElement e1;

		e1 = new TextElement(TextElement.Type.TEXT_, text_above_pic);
		retVals.add(e1);

		// Bitmap bmp1 = BitmapFactory.decodeResource(this.getResources(),
		// R.raw.pic1);
		// ByteArrayOutputStream stream = new ByteArrayOutputStream();
		// bmp1.compress(Bitmap.CompressFormat.PNG, 100, stream);
		// byte[] byteArray = stream.toByteArray();

		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/tmm");
		dir.mkdirs();

		File file = new File(dir, "pic1.jpg");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn = getBaseContext().getResources().openRawResource(
				R.raw.pic1);
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

		TextElement e2 = new TextElement(TextElement.Type.IMAGE, caption1,
				file.getAbsolutePath());
		retVals.add(e2);

		TextElement e3;
		e3 = new TextElement(TextElement.Type.TEXT_, text_below_pic);
		retVals.add(e3);

		// Bitmap bmp2 = BitmapFactory.decodeResource(this.getResources(),
		// R.raw.pic2);
		// ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
		// bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
		// byte[] byteArray2 = stream2.toByteArray();

		File file2 = new File(dir, "pic2.jpg");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn2 = getBaseContext().getResources().openRawResource(
				R.raw.pic2);
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

		TextElement e4 = new TextElement(TextElement.Type.IMAGE, caption2,
				file2.getAbsolutePath());
		retVals.add(e4);

		return retVals;
	}

	private ArrayList<TextElement> getSampleArr2() {

		String text_above_pic = "In modern systems today, there is not really a good way to differentiate between blah and blahblah. In this paper, we present such and such novel solution. In inital testing, our solution provides several benefits while only incurring a 98% performance overhead.";
		String caption1 = "The experimental data.";
		String caption2 = "The performance results.";
		String caption3 = "The device used to make measurements.";

		ArrayList<TextElement> retVals = new ArrayList<TextElement>();
		TextElement e1;

		e1 = new TextElement(TextElement.Type.TEXT_, text_above_pic);
		retVals.add(e1);
		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/tmm");
		// Bitmap bmp1 = BitmapFactory.decodeResource(this.getResources(),
		// R.raw.graph_1);
		// ByteArrayOutputStream stream = new ByteArrayOutputStream();
		// bmp1.compress(Bitmap.CompressFormat.PNG, 100, stream);
		// byte[] byteArray = stream.toByteArray();

		File file2 = new File(dir, "graph_1.jpg");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn2 = getBaseContext().getResources().openRawResource(
				R.raw.graph_1);
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

		TextElement e2 = new TextElement(TextElement.Type.IMAGE, caption1,
				file2.getAbsolutePath());
		retVals.add(e2);

		// Bitmap bmp2 = BitmapFactory.decodeResource(this.getResources(),
		// R.raw.chart2);
		// ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
		// bmp2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
		// byte[] byteArray2 = stream2.toByteArray();

		File file3 = new File(dir, "chart2.png");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn3 = getBaseContext().getResources().openRawResource(
				R.raw.chart2);
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

		TextElement e4 = new TextElement(TextElement.Type.IMAGE, caption2,
				file3.getAbsolutePath());
		retVals.add(e4);

		// Bitmap bmp3 = BitmapFactory.decodeResource(this.getResources(),
		// R.raw.figure_3);
		// ByteArrayOutputStream stream3 = new ByteArrayOutputStream();
		// bmp3.compress(Bitmap.CompressFormat.PNG, 100, stream3);
		// byte[] byteArray3 = stream3.toByteArray();

		File file4 = new File(dir, "figure_3.png");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn4 = getBaseContext().getResources().openRawResource(
				R.raw.figure_3);
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

		TextElement e5 = new TextElement(TextElement.Type.IMAGE, caption3,
				file4.getAbsolutePath());
		retVals.add(e5);

		return retVals;
	}

	private void loadPosterCards() {

		Server source1 = new Server(POSTER_CARD_SERVER, "null",
				System.currentTimeMillis(), System.currentTimeMillis());

		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/tmm");

		// declare the basic cards that we will be using
		TextCard authors = new TextCard(10, "About the Authors", source1);         //DONE

		VideoCard glassIntro = new VideoCard(20, "Watch a Glass Video", source1); // source
		// vid:
		// v1uyQZNg2vE
		// //DONE
		TextCard useCases = new TextCard(200, "Possible Use Cases", source1);      //DONE

		TextCard sysFocus = new TextCard(50, "Focus: System Diagram", source1); //DONE

		VideoCard release = new VideoCard(60, "Watch the Release Story", source1); // source: OLn0cSZfl6c //DONE

		TextCard future = new TextCard(70, "What the Future Holds", source1); //DONE

		VideoCard myGlass = new VideoCard(80, "MyGlass App Explained", source1); // source:
		// vrwFwl3ZVRU
		// //DONE
		TextCard myGlasstxt = new TextCard(85, "The MyGlass Helper App", source1); //DONE
		VideoCard hwOverview = new VideoCard(90, "Glass's Hardware Explained", source1); // source: Ee5JzKbOAaw //DONE
		TextCard lims = new TextCard(100, "Glass' Unique Challenges", source1); //DONE
		TextCard nosql = new TextCard(110, "Focus: NoSQL Databases", source1); //TODO

		// set up the videocards
		// video: a look through glass
		File file0 = new File(dir, "glassintrobkgrnd.jpg");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.glassintrobkgrnd);
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

		glassIntro.setYTtag("v1uyQZNg2vE");
		glassIntro.setScreenshot(file0.getPath());

		// video: news story
		file0 = new File(dir, "newsstorybkgrnd.jpg");

		// manually write the audio file to the external to emulate it being
		// downloaded
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.newsstorybkgrnd);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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

		release.setYTtag("OLn0cSZfl6c");
		release.setScreenshot(file0.getPath());

		// video: myglass app explained
		file0 = new File(dir, "myglassbkgrnd.jpg");

		// manually write the audio file to the external to emulate it being
		// downloaded
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.myglassbkgrnd);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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

		myGlass.setYTtag("vrwFwl3ZVRU");
		myGlass.setScreenshot(file0.getPath());

		// video: glass Hardware
		file0 = new File(dir, "glasshwbkgrnd.jpg");

		// manually write the audio file to the external to emulate it being
		// downloaded
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.glasshwbkgrnd);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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

		hwOverview.setYTtag("Ee5JzKbOAaw");
		hwOverview.setScreenshot(file0.getPath());

		// set up text cards

		// About the authors
		ArrayList<TextElement> aboutAuthor_contents = getAuthorInfo();
		authors.setContents(aboutAuthor_contents);
		authors.setLine1("Alex Meijer");
		authors.setLine2("Dan Prudente");
		authors.setLine3("Tap to learn more");

		// use cases
		ArrayList<TextElement> useCases_contents = getUseCases();
		useCases.setContents(useCases_contents);
		useCases.setLine1("-Learning about posters");
		useCases.setLine2("-Learning about the world");
		useCases.setLine3("-Learning how to do things");

		//system focus diagram
		ArrayList<TextElement> sysFoucsd_contents = generateSysFocusContent();
		sysFocus.setContents(sysFoucsd_contents);
		sysFocus.setLine2("An in-depth focus on the central figure in this poster");
		sysFocus.setLine1("");
		sysFocus.setLine3("");
		//what the future holds
		ArrayList<TextElement> future_contents = getFutureContent();
		future.setContents(future_contents);
		future.setLine1("-Two way information flow");
		future.setLine2("-New ways to determine context");
		future.setLine3("-Complex analytics");

		//text card for the myglass
		file0 = new File(dir, "myglassic.png");
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.myglassic);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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

		ArrayList<TextElement> myGlass_contents = getMyGlassContent();
		myGlasstxt.setContents(myGlass_contents);
		myGlasstxt.setIconPath(file0.getPath());
		
		myGlasstxt.setLine1("Install the app on your phone");
		myGlasstxt.setLine2("Then use this guide");
		myGlasstxt.setLine3("");

		//limitations
		ArrayList<TextElement> limitations_contents = getLimitationsContent();
		lims.setContents(limitations_contents);

		file0 = new File(dir, "noglassic.jpg");
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.noglassic);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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

		lims.setIconPath(file0.getPath());
		lims.setLine1("-Privacy");
		lims.setLine3("-Battery life");
		lims.setLine2("");

		//focus on nosqls
		ArrayList<TextElement> nosql_contents = getnosql_contents();
		nosql.setContents(nosql_contents);

		file0 = new File(dir, "nosqlic.png");
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.nosqlic);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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

		nosql.setIconPath(file0.getPath());
		nosql.setLine2("Information about the NoSQL databases that form \nthe core of \nTellMeMore");
		nosql.setLine1("");
		nosql.setLine3("");

		// add all the poster card to the class variable to be uploaded
		cardz = new ArrayList<TMMCard>();
		cardz.add(authors);
		cardz.add(glassIntro);
		cardz.add(useCases);
		cardz.add(sysFocus);
		cardz.add(release);
		cardz.add(future);
		cardz.add(myGlass);
		cardz.add(myGlasstxt);
		cardz.add(hwOverview);
		cardz.add(lims);
		cardz.add(nosql);
		Log.i(TAG, "size of cardz array: " + cardz.size());

	}

	private ArrayList<TextElement> getnosql_contents(){
		ArrayList<TextElement> toReturn = new ArrayList<TextElement>();
		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/tmm");
		
		TextElement t1 = new TextElement(Type.TEXT_, "The following has been adapted from the introductory section on NoSQl databases from Apache's website. The origial copy of this information can be found at http://docs.couchdb.org/en/latest/intro/why.html. ");
		
		TextElement t2 = new TextElement(Type.TEXT_, "TellMeMore uses two NoSQL databases working together to serve content. The master database is an instance of Apache CouchDB, and the smaller, mobile client databases are running the Couchbase Lit NoSQL database.");
		
		//couchdb pic
		File file0 = new File(dir, "couchdb.jpg");

		InputStream fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.couchdb);
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
		
		TextElement t3 = new TextElement(Type.IMAGE, "", file0.getPath());
		
		//couchbase lite pic
		
		file0 = new File(dir, "cblite.jpg");
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.cblite);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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
		
		TextElement t4 = new TextElement(Type.IMAGE, "", file0.getPath());

		TextElement t5 = new TextElement(Type.TEXT_, "NoSQL databases are a great fit for common applications like this because they embrace the natural idea of evolving, self-contained documents as the very core of its data model (Apache).");
		TextElement t6 = new TextElement(Type.TEXT_, "This meshes well with the goal of TellMeMore, which uses many self-contained documents called cards.");
		
		TextElement t7 = new TextElement(Type.TEXT_, "Apache's Example: An Invoice\n\"An invoice contains all the pertinent information about a single transaction: the seller, the buyer, the date, and a list of the items or services sold. As shown below, there’s no abstract reference on this piece of paper that points to some other piece of paper with the seller’s name and address. Accountants appreciate the simplicity of having everything in one place. And given the choice, programmers appreciate that, too.\"");
		
		file0 = new File(dir, "selfcontained.png");
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.selfcontained);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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
		
		TextElement t8 = new TextElement(Type.IMAGE, "Self-contained documents", file0.getPath());
		
		TextElement t9 = new TextElement(Type.TEXT_, "While a traditional relational database requires you to model your data up front, the NoSQL database's schema-free design provides a powerful way to aggregate your data after the fact, just as is done with real-world documents (Apache).");
		
		TextElement t10 = new TextElement(Type.TEXT_, "This allows many different card types to be created later on, not just the three types of cards used currently in TellMeMore. ");
		
		TextElement t11 = new TextElement(Type.TEXT_, "Finally, NoSQL databases are very flexible, and can be used for many different types of storage depending on the needs of the system at the time. This will allow TellMeMore to expand in the future to cover all types of data, whether it be traditinal tabulated data or analytics graphs. ");
		
		file0 = new File(dir, "nosqlcapa.png");
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.nosqlcapa);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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
		
		TextElement t12 = new TextElement(Type.IMAGE, "The capabilities of NoSQL", file0.getPath());
		
		//t1 last?
		toReturn.add(t1);
		toReturn.add(t2);
		toReturn.add(t3);
		toReturn.add(t4);
		toReturn.add(t5);
		toReturn.add(t6);
		toReturn.add(t7);
		toReturn.add(t8);
		toReturn.add(t9);
		toReturn.add(t10);
		toReturn.add(t11);
		toReturn.add(t12);
		return toReturn;
	}
	
	private ArrayList<TextElement> getLimitationsContent(){
		ArrayList<TextElement> toReturn = new ArrayList<TextElement>();
		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/tmm");
		TextElement t1 = new TextElement(Type.TEXT_, "The wearable technology available in Google Glass is still in development. It does have a number of limitations which will need to be addressed before the technology can become ubuquitous.");
		
		TextElement t2 = new TextElement(Type.TEXT_, "The first issue is the high cost of google glass. Currently, one set of Google Glass costs abut $1,500. This is due to the high engineering cost of the device. Google hopes to reduce this by the time the product is released.");
		
		TextElement t3 = new TextElement(Type.TEXT_, "Another issue is the privacy of people around the glass user. Surrounding individuals cannot tell if the device is recording or not; this creates fears of government or corporate surveillance. The issues surrounding the privacy of glass has yet to be resolved. ");
		
		File file0 = new File(dir, "googleglassbigbrother.png");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.googleglassbrother);
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
		
		TextElement t3_1 = new TextElement(Type.IMAGE, "", file0.getPath());
		TextElement t4 = new TextElement(Type.TEXT_, "Finally, the small size of Google glass places serious constraints on the battery life of the product - in practice, the battery only lasts for around 30 minutes.");
		
		toReturn.add(t1);
		toReturn.add(t2);
		toReturn.add(t3);
		toReturn.add(t3_1);
		toReturn.add(t4);
		
		return toReturn; 
	}
	
	private ArrayList<TextElement> getMyGlassContent(){
		ArrayList<TextElement> toReturn = new ArrayList<TextElement>();
		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/tmm");
		File file0 = new File(dir, "myglassheader.jpg");
		InputStream fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.myglassheader);
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

		TextElement t1 = new TextElement(Type.IMAGE, "About the MyGlass App", file0.getPath());
		toReturn.add(t1);

		TextElement t2 = new TextElement(Type.TEXT_, "Since Google Glass itself has very limited ways to receive input, Google has released an app called MyGlass that unlocks the full potential of the platform. ");
		toReturn.add(t2);

		TextElement t3 = new TextElement(Type.TEXT_, "\n The MyGlass app is available for installation via the Google Play and Apple App Stores. Once installed, set it up and connect to the glass device.");
		toReturn.add(t3);

		TextElement t4 = new TextElement(Type.TEXT_, "Once the app is installed, it offers a number of useful features to get the most out of your glass.\n");
		toReturn.add(t4);


		file0 = new File(dir, "glasstxt1.png");
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.glasstxt1);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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


		TextElement t5 = new TextElement(Type.IMAGE, "You can view your contacts stored on the Glass device", file0.getPath());
		toReturn.add(t5);


		file0 = new File(dir, "glasstxt2.png");
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.glasstxt2);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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

		TextElement t6 = new TextElement(Type.IMAGE, "You can install new glassware on the device using the app", file0.getPath());
		toReturn.add(t6);


		file0 = new File(dir, "glasstxt3.png");
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.glasstxt3);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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

		TextElement t7 = new TextElement(Type.IMAGE, "You can view the status of the glass device using the MyGlass App", file0.getPath());
		toReturn.add(t7);

		return toReturn; 
	}

	private ArrayList<TextElement> getFutureContent(){
		ArrayList<TextElement> toReturn = new ArrayList<TextElement>();
		File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmm");

		TextElement t1 = new TextElement(Type.TEXT_, "There are a number of improvments planned for the near future of TellMeMore: \n");

		TextElement t2 = new TextElement(Type.TEXT_, "The NoSQL databases used by the system can support 2 way synchronization. This allows for analytical information to be uploaded to the master database about how each user uses their glass.");

		TextElement t3 = new TextElement(Type.TEXT_, "Such analytics can include: \n -Time spent on each card \n-Number of views per card");

		TextElement t3_2 = new TextElement(Type.TEXT_, "MapReduce applications can be added to the master database making aggregate analyitics generation easy and fast.");

		File file0 = new File(dir, "mapreduce.jpg");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.mapreduce);
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

		TextElement t3_3 = new TextElement(Type.IMAGE, "How MapReduce works", file0.getPath());

		TextElement t4 = new TextElement(Type.TEXT_, "TellMeMore will also use other methods to determine context in the future, such as RFID tags, WiFi localization, or Geo-Fencing");

		TextElement t5 = new TextElement(Type.TEXT_, "TellmeMore cards will be able to be created in many different ways. The most popular will undoubtedly be web-based card creation tools, but mobile and desktop apps can also be created to push new content. For example, an Android app can be used to make a video using the built-in phone camera, and the resulting video will automatically be added to the database using the mobile app.");

		toReturn.add(t1);
		toReturn.add(t2);
		toReturn.add(t3);
		toReturn.add(t3_2);
		toReturn.add(t3_3);
		toReturn.add(t4);
		toReturn.add(t5);

		return toReturn;
	}

	private ArrayList<TextElement> getUseCases() {
		ArrayList<TextElement> toReturn = new ArrayList<TextElement>();

		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/tmm");
		File file0 = new File(dir, "markedposter.jpg");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn0 = getBaseContext().getResources().openRawResource(R.raw.markedposter);
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

		TextElement t1 = new TextElement(Type.TEXT_, "We envision a number of possible uses from TellMeMore: \n");
		TextElement t2 = new TextElement(Type.TEXT_, "First of all, it can be used to deliver more information for things like this poster. TellMeMore allows sound and video to be added to posters, just by addign a QR code. ");
		TextElement t3 = new TextElement(Type.IMAGE, "TellMeMore QR code", file0.getPath());
		toReturn.add(t1);
		toReturn.add(t2);
		toReturn.add(t3);

		file0 = new File(dir, "geofence.jpg");
		TextElement t4 = new TextElement(Type.TEXT_, "TellMeMore can be used in the context of the real world, to tell you more about the place you are in. It can accomplish this using the concept of GeoFencing, using the GPS location to pick out relevant cards.");
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.geofence);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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

		TextElement t5 = new TextElement(Type.IMAGE, "A Geofence", file0.getPath());
		toReturn.add(t4);
		toReturn.add(t5);

		//talk about opportunities created by synchronization

		TextElement t6 = new TextElement(Type.TEXT_, "The databases used by TellMeMore can be set to synchronize with each other constantly. This capability brings up a whole range of other possible applications: Interactive cards, cards controlled by a central server, or cards that change their content based on the individual user.  ");
		toReturn.add(t6);

		//talk about image recognition and step-by-step guides

		TextElement t7 = new TextElement(Type.TEXT_, "TellMeMore can be used with any method of determining context. If integrated with computer vision technology, for example, it can detect an osilliscope in the user's view and even ascertain the model and state (ON, OFF, and etcetera). Using this context information, TellMeMore can provide cards prepared by an instructor or the manufacturer containing text and videos on how to use the osilliscope properly.");
		toReturn.add(t7);

		file0 = new File(dir, "oscope.jpg");
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.oscope);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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

		TextElement t8 = new TextElement(Type.IMAGE, "Computer Vision In Use", file0.getPath());
		toReturn.add(t8);

		TextElement t9 = new TextElement(Type.TEXT_, "There are, of course, mmany more applications and extensions in this space, we have provided only a sample here of the most fasible in the near term.");
		toReturn.add(t9);

		return toReturn;
	}

	private ArrayList<TextElement> getAuthorInfo() {
		ArrayList<TextElement> toReturn = new ArrayList<TextElement>();
		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/tmm");
		File file0 = new File(dir, "buic.png");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.buic);
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

		TextElement t1 = new TextElement(Type.IMAGE, "", file0.getPath());
		toReturn.add(t1);

		TextElement t2 = new TextElement(
				Type.TEXT_,
				"Alexander Meijer is a Computer Engineering major from the town of Groton, Massachusetts. Alex enjoys tinkering with the UNIX Operating System in all of its various forms and flavors, and can be relied upon to have the latest version running on at least one computer.\n");

		toReturn.add(t2);

		TextElement t3 = new TextElement(Type.TEXT_, "Daniel Prudente is a senior at Bucknell University. He is a computer engineering major and has a minor in mathematics. After graduation, he plans on working for IBM as a software developer in Littleton, MA.");
		toReturn.add(t3);
		return toReturn;

	}

	private ArrayList<TextElement> generateSysFocusContent(){
		ArrayList<TextElement> toReturn = new ArrayList<TextElement>();

		TextElement t1 = new TextElement(Type.TEXT_, "The TellMeMore System uses a QR code with a plaintext string encoded in it to determine what the user is looking at. In this example, the string \"project_1\" has been encoded into the QR code. The system does not scan a URL from the QR code, which allows for improved security. ");
		//qr code + string here 
		File dir = new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/tmm");
		File file0 = new File(dir, "qrdiagram.jpg");

		// manually write the audio file to the external to emulate it being
		// downloaded
		InputStream fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.qrdiagram);
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

		TextElement t1_1 = new TextElement(Type.IMAGE, "", file0.getPath());
		toReturn.add(t1);
		toReturn.add(t1_1);

		TextElement t2 = new TextElement(Type.TEXT_, "The QR Scanner and Reader system uses the Glass' camera and the Zbar library to read the QR code and parse the contents. This library then passes the string contained in the QR code to the remainder of the system. ");
		//qr code scanner block, maybe with approaching and leaving arrows
		file0 = new File(dir, "qrscannerd.jpg");
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.qrscannerd);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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

		TextElement t2_1 = new TextElement(Type.IMAGE, "", file0.getPath());

		toReturn.add(t2);
		toReturn.add(t2_1);

		TextElement t3 = new TextElement(Type.TEXT_, "Using the name of the database obtained from the QR scanner, the NoSQL database on the Glass establishes a connection with the master NoSQL database over HTTP. It makes a request for all the cards for porject 1, and receives them back in JSON from the master. ");
		//middle rectange part here showing data moving in multiple directions
		file0 = new File(dir, "midpart.jpg");
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.midpart);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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

		TextElement t3_1 = new TextElement(Type.IMAGE, "", file0.getPath());

		toReturn.add(t3);
		toReturn.add(t3_1);

		TextElement t4 = new TextElement(Type.TEXT_, "The card adapters and the assocated players and viewers for each type of card then acces the local database directly. This allows for fast access of card content once the local database has been synchronized. Also, the local database functions as a cache for recent card data, so in the event that network connectivity is lost, the cards stored locally can still be accessed.");
		//bottom part under client DB goes here
		file0 = new File(dir, "cardadapter.jpg");
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.cardadapter);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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

		TextElement t4_1 = new TextElement(Type.IMAGE, "", file0.getPath());

		toReturn.add(t4);
		toReturn.add(t4_1);

		TextElement t5 = new TextElement(Type.TEXT_, "New cards can be created from a wide variety of platorms, and uploaded to the master database easily. Cards can also be easily updated in the same manner.");
		//bottom new card addition part goes here
		file0 = new File(dir, "newcards.jpg");
		fIn0 = getBaseContext().getResources().openRawResource(
				R.raw.newcards);
		buffer0 = null;
		try {
			int size0 = fIn0.available();
			buffer0 = new byte[size0];
			fIn0.read(buffer0);
			fIn0.close();
		} catch (IOException e) {
			Log.e(TAG, "IOException first part");

		}

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


		TextElement t5_1 = new TextElement(Type.IMAGE, "", file0.getPath());

		toReturn.add(t5);
		toReturn.add(t5_1);

		return toReturn;
	}
}
