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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
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
		startSync();
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


	private void startSync() {

		URL syncUrl;
		try {
			syncUrl = new URL(MASTER_SERVER_URL + ":" + port +"/"+ dbName);

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		Replication pullReplication = database.createPullReplication(syncUrl);
		pullReplication.setCreateTarget(true);
		pullReplication.setContinuous(false);

		Replication pushReplication = database.createPushReplication(syncUrl);
		pullReplication.setCreateTarget(true);
		pushReplication.setContinuous(false);

		pullReplication.start();
		pushReplication.start();

		pullReplication.addChangeListener(this);
		pushReplication.addChangeListener(this);

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
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
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

	public boolean deleteDB(boolean areYouSure){
		if(!areYouSure){
			Log.d(TAG, "User wasn't sure");
			return false;
		}

		HttpClient httpclient = new DefaultHttpClient();
		HttpDelete dbdeleter = new HttpDelete(LOCAL_DB_URL + ":" + port + "/" + dbName);

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

		startSync();
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
			startSync();
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

//	private class entireDBGetter extends AsyncTask<Void, Void, String> {
//
//		@Override
//		protected String doInBackground(Void... params) {
//			//then we must have a good UUID
//			// Create a new HttpClient and get Header
//			HttpClient httpclient = new DefaultHttpClient();
//			//
//			//HttpGet everythingGetter = new HttpGet(LOCAL_DB_URL + ":" + port + "/" + dbName + "/_all_docs?include_docs=true");
//			HttpGet everythingGetter = new HttpGet(LOCAL_DB_URL + ":" + port + "/" + dbName);
//			//execute the put and record the response
//			HttpResponse response = null;
//			try {
//				response = httpclient.execute(everythingGetter);
//			} catch (ClientProtocolException e) {
//
//				Log.e(TAG, "error getting the DB as JSON", e);
//			} catch (IOException e) {
//				Log.e(TAG, "IO error getting the DB as JSON", e);
//			}
//
//			Log.i(TAG, "server response to all database get: " + response);
//
//			//parse the reponse 
//			BufferedReader reader = null;
//			try {
//				reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			} catch (IllegalStateException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			String json = null;
//			try {
//				json = reader.readLine();
//				Log.d(TAG, "Raw json string: " + json);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//
//
//
//			return json;
//		}
//
//		@Override
//		protected void onPostExecute(String result) {
//			//do stuff
//
//		}
//
//
//
//
//	}

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
		startSync();

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
			startSync();
			return true;
		}else {
			Log.w(TAG, "CREATION FAILURE -" + reason);
			return false; 
		}
	}

	//return ALL cards from all servers, sorted by priority
	public synchronized ArrayList<TMMCard> getAllCards(){
		//TODO 
		Log.d(TAG, "get all cards");
		ArrayList<TMMCard> cardlist = new ArrayList<TMMCard>();


		//sort
		Collections.sort(cardlist);
		return cardlist;
	}

	public ArrayList<TMMCard> findCardsbyServer(String server){


		ArrayList<TMMCard> matches = new ArrayList<TMMCard>();


		//sort cards by priority
		Collections.sort(matches);

		return matches;
	}

}