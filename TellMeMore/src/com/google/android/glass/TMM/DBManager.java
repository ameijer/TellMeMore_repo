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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class DBManager {
	private SQLiteDatabase database;
	private DBHelper dbhelper;
	public static final String TAG = "TMM: DBMANAGER";

	public DBManager(Context context){
		dbhelper = new DBHelper(context);
	}

	public boolean open(){
		try {
			database = dbhelper.getWritableDatabase();
		} catch (SQLiteException e){
			return false;
		}
		return true;
	}

	public void close(){
		dbhelper.close();
	}

	public Cursor generalQuery(String table_name){
		return database.rawQuery("select * from " + table_name,null);
	}

	public boolean deleteDB(Context context){
		close();
		ContextWrapper wrapper = new ContextWrapper(context);
		boolean result = wrapper.deleteDatabase(DBHelper.DATABASE_NAME);
		Log.d(TAG, "DB deleted, delete returned " + result);
		return result;
	}

	private Server cursorToServer(Cursor cursor){
		if(cursor.getCount() > 0){
			//convert blob to server data

			byte[] blob = cursor.getBlob(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_SERVER_DATA));
			//a straightforward de-serialization process
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(new ByteArrayInputStream(blob));
			} catch (StreamCorruptedException e1) {
				e1.printStackTrace();
				return null;
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}

			Server thisServer;
			try {
				//read in an object input stream into a new TMMCARD
				thisServer = (Server) in.readObject();
			} catch (OptionalDataException e) {
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}

			if (thisServer != null){
				thisServer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.SERVER_ID)));
				thisServer.setName(cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_SERVER_NAME)));
			} else {
				Log.e(TAG, "NULL CARD RETURNED");
			}
			return thisServer;
		} else {
			Log.w(TAG, "Attempted to convert an empty cursor to a server object");
			return null;
		}

	}



	private TMMCard cursorToCard(Cursor cursor){
		//convert blob to TMMcard

		//if cursor has nothing in it, then we return a null for the created card 
		if(cursor.getCount() > 0){
			int blobIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CARD_DATA); 
			Log.d(TAG, "column index of blob: " + blobIndex);

			byte[] blob = cursor.getBlob(blobIndex);
			//a straightforward de-serialization process
			ObjectInputStream in;
			try {
				in = new ObjectInputStream(new ByteArrayInputStream(blob));

			} catch (StreamCorruptedException e1) {
				e1.printStackTrace();
				return null;
			} catch (IOException e1) {
				e1.printStackTrace();
				return null;
			}

			TMMCard dbcard;
			try {
				//read in an object input stream into a new TMMCARD
				dbcard = (TMMCard) in.readObject();
				in.close();
			} catch (OptionalDataException e) {
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			if (dbcard != null){
				dbcard.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.CARD_ID)));
			} else {
				Log.e(TAG, "NULL CARD RETURNED");
			}
			return dbcard;
		} else {
			Log.w(TAG, "tried to create a card from an empty cursor. returning null card");
			return null;
		}
	}



	public TMMCard findCardById(long id){
		Cursor cursor = database.query(DBHelper.CARD_TABLE_NAME, null, DBHelper.CARD_ID + " = " + id, null, null, null, null);
		cursor.moveToFirst();
		return cursorToCard(cursor);
	}


	//returns the message as it exists in the DB
	public synchronized TMMCard addCard(TMMCard toAdd) throws IOException{

		//check if card already exists. 
		if(toAdd.getId() > 0){
			Cursor checkCursor = database.query(DBHelper.CARD_TABLE_NAME, null, DBHelper.CARD_ID + " = " + toAdd.getId(), null, null, null, null);

			if(checkCursor != null && checkCursor.moveToFirst()){
				//we will delete the old card and replace it with the new
				database.delete(DBHelper.CARD_TABLE_NAME, DBHelper.CARD_ID + " = " + toAdd.getId(), null);
				Log.e(TAG, "Old card found. Deleting old card." );

			} else {
				//there is nothing in the table with this ID
				Log.d(TAG, "no card with this ID found. adding to DB");
			}
		}

		ContentValues values = new ContentValues();

		//load the values with the cards information
		//card data 
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream(out);
		objOut.writeObject(toAdd);
		objOut.close();
		values.put(DBHelper.COLUMN_CARD_DATA, out.toByteArray());
		out.close();
		//time received, as millis
		values.put(DBHelper.COLUMN_MODIFIED, System.currentTimeMillis());
		//server data
		values.put(DBHelper.COLUMN_SERVER, toAdd.getSource().getName());
		//type of card, as string
		if(toAdd instanceof AudioCard){
			values.put(DBHelper.COLUMN_CARD_TYPE, DBHelper.AUDIO);
		} else if (toAdd instanceof TextCard){
			values.put(DBHelper.COLUMN_CARD_TYPE, DBHelper.TEXT);
		} else {
			//must be a video card
			values.put(DBHelper.COLUMN_CARD_TYPE, DBHelper.VIDEO);
		}



		//insert the new card data
		long insert_id = database.insert(DBHelper.CARD_TABLE_NAME, null, values);

		Log.d(TAG, "Card inserted with id: " + insert_id);
		//query the message in the table and return it to make sure its there correctly
		Cursor cursor = database.query(DBHelper.CARD_TABLE_NAME,
				null, DBHelper.CARD_ID + " = " + insert_id, null,
				null, null, null);
		cursor.moveToFirst();
		TMMCard dbCard = cursorToCard(cursor);
		cursor.close();

		return dbCard;

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