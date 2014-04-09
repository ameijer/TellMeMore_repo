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
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Manager;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.replicator.Replication;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class DBManager implements Replication.ChangeListener{

	private TellMeMoreApplication app; 
	private String dbName;
	public static final String TAG = "TMM: DBMANAGER";
	public static final String MASTER_SERVER_URL = "http://134.82.132.99";
	public static final String LOCAL_DB_URL = "http://127.0.0.1";
	public static final int port = 5984;

	public DBManager(Context context){
		app = ((TellMeMoreApplication)context.getApplicationContext());
	}

	public String getName() {
		return this.dbName;
	}

	public boolean open(String dbName) throws IOException, CouchbaseLiteException{
		this.dbName = dbName;
		startCBLListener(port);
		startDatabase(app.getManager(), dbName);

		//sync up database here
		startSync();
		return true;
	}

	private int startCBLListener(int suggestedListenPort) throws IOException, CouchbaseLiteException {

		startDatabase(app.getManager(), dbName);

		LiteListener listener = new LiteListener(app.getManager(), suggestedListenPort);
		int port = listener.getListenPort();
		Thread thread = new Thread(listener);
		thread.start();

		return port;

	}

	protected void startDatabase(Manager manager, String databaseName) throws CouchbaseLiteException {
		app.setDatabase(manager.getDatabase(databaseName));
		app.getDatabase().open();
	}


	public void close(){
		app.getDatabase().close();
	}


	private void startSync() {

		URL syncUrl;
		try {
			syncUrl = new URL(MASTER_SERVER_URL + "/"+ dbName);

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		Replication pullReplication = app.getDatabase().createPullReplication(syncUrl);
		pullReplication.setCreateTarget(true);
		pullReplication.setContinuous(false);

		Replication pushReplication = app.getDatabase().createPushReplication(syncUrl);
		pullReplication.setCreateTarget(true);
		pushReplication.setContinuous(false);

		pullReplication.start();
		pushReplication.start();

		pullReplication.addChangeListener(this);
		pushReplication.addChangeListener(this);

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

	//	public Cursor generalQuery(String table_name){
	//		return database.rawQuery("select * from " + table_name,null);
	//	}

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

		return true;
	}

	//	private Server cursorToServer(Cursor cursor){
	//		if(cursor.getCount() > 0){
	//			//convert blob to server data
	//
	//			byte[] blob = cursor.getBlob(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_SERVER_DATA));
	//			//a straightforward de-serialization process
	//			ObjectInputStream in;
	//			try {
	//				in = new ObjectInputStream(new ByteArrayInputStream(blob));
	//			} catch (StreamCorruptedException e1) {
	//				e1.printStackTrace();
	//				return null;
	//			} catch (IOException e1) {
	//				e1.printStackTrace();
	//				return null;
	//			}
	//
	//			Server thisServer;
	//			try {
	//				//read in an object input stream into a new TMMCARD
	//				thisServer = (Server) in.readObject();
	//			} catch (OptionalDataException e) {
	//				e.printStackTrace();
	//				return null;
	//			} catch (ClassNotFoundException e) {
	//				e.printStackTrace();
	//				return null;
	//			} catch (IOException e) {
	//				e.printStackTrace();
	//				return null;
	//			}
	//
	//			if (thisServer != null){
	//				thisServer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.SERVER_ID)));
	//				thisServer.setName(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_SERVER_NAME)));
	//			} else {
	//				Log.e(TAG, "NULL CARD RETURNED");
	//			}
	//			return thisServer;
	//		} else {
	//			Log.w(TAG, "Attempted to convert an empty cursor to a server object");
	//			return null;
	//		}
	//
	//	}



	//	private TMMCard cursorToCard(Cursor cursor){
	//		//convert blob to TMMcard
	//
	//		//if cursor has nothing in it, then we return a null for the created card 
	//		if(cursor.getCount() > 0){
	//			int blobIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CARD_DATA); 
	//			Log.d(TAG, "column index of blob: " + blobIndex);
	//
	//			byte[] blob = cursor.getBlob(blobIndex);
	//			//a straightforward de-serialization process
	//			ObjectInputStream in;
	//			try {
	//				in = new ObjectInputStream(new ByteArrayInputStream(blob));
	//
	//			} catch (StreamCorruptedException e1) {
	//				e1.printStackTrace();
	//				return null;
	//			} catch (IOException e1) {
	//				e1.printStackTrace();
	//				return null;
	//			}
	//
	//			TMMCard dbcard;
	//			try {
	//				//read in an object input stream into a new TMMCARD
	//				dbcard = (TMMCard) in.readObject();
	//				in.close();
	//			} catch (OptionalDataException e) {
	//				e.printStackTrace();
	//				return null;
	//			} catch (ClassNotFoundException e) {
	//				e.printStackTrace();
	//				return null;
	//			} catch (IOException e) {
	//				e.printStackTrace();
	//				return null;
	//			}
	//			if (dbcard != null){
	//				dbcard.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.CARD_ID)));
	//			} else {
	//				Log.e(TAG, "NULL CARD RETURNED");
	//			}
	//			return dbcard;
	//		} else {
	//			Log.w(TAG, "tried to create a card from an empty cursor. returning null card");
	//			return null;
	//		}
	//	}


	//TODO
	public TMMCard findCardById(long id){
		Cursor cursor = database.query(DBHelper.CARD_TABLE_NAME, null, DBHelper.CARD_ID + " = " + id, null, null, null, null);
		cursor.moveToFirst();
		return cursorToCard(cursor);
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
	public synchronized boolean addCard(TMMCard toAdd) throws IOException{

		//check to make sure card doesn't already exist in DB
				if(this.getJSONRepresentation(toAdd) != null){
					//maybe we could update the card or something
					//I'm going to punt on this, leave it as a TODO
					throw new Exception("The card already exists in the DB, please use the update method instead");

				}

				//if we made it here, safe to assume that the card doesn't exist in the DB
				//get a UUID to use from couch
				String uuid = getUUID(LOCAL_DB_URL, port);
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

	public synchronized Server addServer(Server toAdd) throws IOException{

		//check if server ID is already in DB and wipe it if it is
		if(toAdd.getId() > 0){
			Cursor checkCursor = database.query(DBHelper.SERVER_TABLE_NAME, null, DBHelper.SERVER_ID + " = " + toAdd.getId(), null, null, null, null);

			if(checkCursor != null && checkCursor.moveToFirst()){
				//we will delete the old server and replace it with the new
				database.delete(DBHelper.SERVER_TABLE_NAME, DBHelper.SERVER_ID + " = " + toAdd.getId(), null);
				Log.e(TAG, "Old server found. Deleting old server." );

			} else {
				//there is nothing in the table with this ID
				Log.d(TAG, "no server with this ID found. adding to DB");
			}
		}

		//check if server with same name exists. we will delete the existing server to enforce the uniqueness of server names 

		Server existing = findServerByName(toAdd.getName());
		if(existing != null) {
			deleteServer(existing);
		}


		ContentValues values = new ContentValues();

		//load the values with server data
		//server name
		values.put(DBHelper.COLUMN_SERVER_NAME, toAdd.getName());

		//server data 
		ByteArrayOutputStream sout = new ByteArrayOutputStream();
		ObjectOutputStream sobjOut = new ObjectOutputStream(sout);
		sobjOut.writeObject(toAdd);
		sobjOut.close();
		values.put(DBHelper.COLUMN_SERVER_DATA, sout.toByteArray());
		sout.close();

		//insert the new server into the DB
		long insert_id = database.insert(DBHelper.SERVER_TABLE_NAME, null, values);

		//query the server in the table and return it to make sure its there correctly
		Cursor cursor = database.query(DBHelper.SERVER_TABLE_NAME,
				null, DBHelper.SERVER_ID + " = " + insert_id, null,
				null, null, null);
		cursor.moveToFirst();
		Server result = cursorToServer(cursor);
		cursor.close();

		return result;



	}

	//should strip whitespaces before/after for accurate search
	public Server findServerByName(String name){
		//query user table
		Log.d("TMM: DBMANAGER", "querying db for server name = " + name);
		Cursor serverCursor = database.query(DBHelper.SERVER_TABLE_NAME, null, DBHelper.COLUMN_SERVER_NAME + "=?", new String[]{name}, null, null, null);
		//should only be one user, the first one...
		serverCursor.moveToFirst();
		Server target = cursorToServer(serverCursor);
		serverCursor.close();

		return target;
	}


	public synchronized Server deleteServer(Server toDel){
		//make sure we don't delete the first row by accident
		if(toDel.getId() < 1)
			return null;

		database.delete(DBHelper.SERVER_TABLE_NAME, DBHelper.SERVER_ID + " = " + toDel.getId(), null);
		return toDel;
	}

	public boolean deleteCardsByServer(String serverName){
		ArrayList<TMMCard> toDel = findCardsbyServer(serverName);

		for(TMMCard temp : toDel){
			deleteCard(temp);
		}
		return true;
	}

	public synchronized TMMCard deleteCard(TMMCard toDelete){
		TMMCard existing = findCardById(toDelete.getId());

		if(existing != null){
			if(database.delete(DBHelper.CARD_TABLE_NAME, DBHelper.CARD_ID + " = '" + existing.getId() + "'", null) < 0) {
				return null; //error
			}
		}
		return existing;

	}


	//not really sure what to do for our cleaning method in this context. Leaving my old code here for reference
	//	public synchronized void cleanDB(){
	//		Thread clean_db = new Thread() {
	//			public void run(){
	//				ArrayList<User> userlist = new ArrayList<User>();
	//				Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.USER_TABLE_NAME, null);
	//
	//				if(cursor.moveToFirst()){
	//					userlist.add(cursorToUser(cursor));
	//					Log.d(TAG, "added user: " + cursorToUser(cursor).getName() + " to user list from DB");
	//					Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
	//					Log.d(TAG, "Moving Cursor Pos...");
	//					while(cursor.moveToNext()){
	//						userlist.add(cursorToUser(cursor));
	//						Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
	//						Log.d(TAG, "added user: " + cursorToUser(cursor).getName() + " to user list from DB");
	//					}
	//
	//				}
	//				for(int i = 0; i < userlist.size(); i++){
	//					User userToCompare = userlist.get(i);
	//					Cursor userCursor = database.query(DBHelper.USER_TABLE_NAME, null, DBHelper.COLUMN_IP + "=?", new String[]{userToCompare.getIp()}, null, null, null);
	//					//should only be one user, the first one...
	//					if(userCursor.getCount() > 1){
	//						//then there are multiple users with the ip
	//						Log.d("clean", "Duplicates found. Num: " + userCursor.getCount());
	//						userCursor.moveToFirst();
	//						User target = cursorToUser(userCursor);
	//						if(target.getLast_seen() > userToCompare.getLast_seen()){
	//							database.delete(DBHelper.USER_TABLE_NAME, DBHelper.COLUMN_USER + " = '" + userToCompare.getName() + "'", null);
	//							Log.d("clean", "Removed user: " + userToCompare.getName() + " was same Ip as " + target.getName());
	//							userToCompare = target;
	//						}
	//						while(userCursor.moveToNext()){
	//							target = cursorToUser(userCursor);
	//							if(target.getLast_seen() > userToCompare.getLast_seen()){
	//								database.delete(DBHelper.USER_TABLE_NAME, DBHelper.COLUMN_USER + " = '" + userToCompare.getName() + "'", null);
	//								Log.d("clean", "Removed user: " + userToCompare.getName() + " was same Ip as " + target.getName());
	//								userToCompare = target;
	//							}
	//						}
	//					}
	//				}
	//
	//
	//
	//
	//			}
	//
	//		};
	//		clean_db.start();
	//	}

	//return ALL cards from all servers, sorted by priority
	public synchronized ArrayList<TMMCard> getAllCards(){
		//TODO - TEST
		Log.d(TAG, "get all cards");
		ArrayList<TMMCard> cardlist = new ArrayList<TMMCard>();
		Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.CARD_TABLE_NAME, null);

		if(cursor.moveToFirst()){
			TMMCard temp = cursorToCard(cursor);
			cardlist.add(temp);
			Log.d(TAG, "added card: "  + temp + " to user list from DB");
			Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
			Log.d(TAG, "Moving Cursor Pos...");
			while(cursor.moveToNext()){
				cardlist.add(cursorToCard(cursor));
				Log.d(TAG, "added card: "  + temp + " to user list from DB");
				Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
			}

		}else {
			return cardlist;
		}
		cursor.close();

		//sort
		Collections.sort(cardlist);
		return cardlist;
	}

	public ArrayList<TMMCard> findCardsbyServer(String server){
		//query user table
		Log.d("findCardsbyServer", "querying db for server name = " + server);
		Cursor cardCursor = database.query(DBHelper.CARD_TABLE_NAME, null, DBHelper.COLUMN_SERVER + "=?", new String[]{server}, null, null, null);
		//should only be one user, the first one...
		Log.d("findCardsbyServer", "number of rows found with that server: " + cardCursor.getCount());
		ArrayList<TMMCard> matches = new ArrayList<TMMCard>();
		if(cardCursor.getCount() > 0){
			//cardCursor.moveToFirst();

			Log.d("findCardsbyServer", "number of rows found with that server before movetofirst: " + cardCursor.getCount());
			if(cardCursor.moveToFirst()){

				//obtain the blob from the card, convert the card from that, and add it to the list
				TMMCard temp = cursorToCard(cardCursor);
				matches.add(temp);
				Log.d(TAG, "added card: " + temp + " to card list from DB");
				Log.d(TAG, "Cursor at pos: " + cardCursor.getPosition());
				Log.d(TAG, "Moving Cursor Pos...");
				while(cardCursor.moveToNext()){
					temp = cursorToCard(cardCursor);
					matches.add(temp);
					Log.d(TAG, "Cursor at pos: " + cardCursor.getPosition());
					Log.d(TAG, "added card: " + temp + " to card list from DB");
				}
			}
		}	
		cardCursor.close();

		//sort cards by priority
		Collections.sort(matches);

		return matches;
	}

	public ArrayList<Server> getAllServers(){
		//TODO - TEST
		Log.d(TAG, "get all servers");
		ArrayList<Server> serverlist = new ArrayList<Server>();
		Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.SERVER_TABLE_NAME, null);

		if(cursor.moveToFirst()){
			serverlist.add(cursorToServer(cursor));
			Log.d(TAG, "added server: " + cursorToServer(cursor).getName() + " to user list from DB");
			Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
			Log.d(TAG, "Moving Cursor Pos...");
			while(cursor.moveToNext()){
				serverlist.add(cursorToServer(cursor));
				Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
				Log.d(TAG, "added server: " + cursorToServer(cursor).getName() + " to user list from DB");
			}

		}else {
			return null;
		}
		cursor.close();

		return serverlist;
	}

}