package com.google.android.glass.TMM;

/*
 * File: DBManager.java
 * Author: Alexander Meijer
 * Date: Sept 5, 2013
 * Class: ELEC 602 Mobile Computing
 * Version 1.0
 * 
 * MODIFIED FOR ELEC429 SP14 A MEIJER
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;

import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class DBManager {
	private SQLiteDatabase database;
	private DBHelper dbhelper;
	public static final String TAG = "TMM: DBMANAGER";
	private Cursor cardListCursor;
	//private Cursor userListCursor_onlineOnly;
	private Cursor serverCursor;

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

	public void deleteDB(Context context){
		close();
		ContextWrapper wrapper = new ContextWrapper(context);
		boolean result = wrapper.deleteDatabase(DBHelper.DATABASE_NAME);
		Log.d(TAG, "DB deleted, delete returned " + result);
	}

	private Server cursorToServer(Cursor cursor){
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
	}



	private TMMCard cursorToCard(Cursor cursor){
		//convert blob to TMMcard

		byte[] blob = cursor.getBlob(cursor.getColumnIndexOrThrow(DBHelper.COLUMN_CARD_DATA));
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
	}

	//set preferOld to be true to prioritize the data already in the DB over the new card data passed through
	//MUST NOT CHANGE CURSOR POSITION
	private boolean updateCard(Cursor pointingToOld, TMMCard toUpdate, boolean preferOld){

		TMMCard dbCard = cursorToCard(pointingToOld);

		if(dbCard == null){
			return false;
		} else {

		}

		//we will use the cursor we have to perform updates directly 




		return true;
	}





	//returns the message as it exists in the DB
	public synchronized TMMCard addCard(TMMCard toAdd){

		//check if card already exists. If it does, then this becomes an update operation
		if(toAdd.getId() > 0){
			Cursor checkCursor = database.query(DBHelper.CARD_TABLE_NAME, null, DBHelper.CARD_ID + " = " + toAdd.getId(), null, null, null, null);

			if(checkCursor != null && checkCursor.moveToFirst()){
				//if the ID and the type are the same, then we count that as them being similar enough to update 
				if(checkCursor.getString(checkCursor.getColumnIndex(DBHelper.COLUMN_CARD_TYPE)).equalsIgnoreCase(DBHelper.TEXT) && toAdd instanceof TextCard){

					this.updateCard(checkCursor.getBlob(checkCursor.getColumnIndex(DBHelper.COLUMN_CARD_DATA)), toAdd);
				} else if(checkCursor.getString(checkCursor.getColumnIndex(DBHelper.COLUMN_CARD_TYPE)).equalsIgnoreCase(DBHelper.AUDIO) && toAdd instanceof AudioCard){

				} else if(checkCursor.getString(checkCursor.getColumnIndex(DBHelper.COLUMN_CARD_TYPE)).equalsIgnoreCase(DBHelper.VIDEO) && toAdd instanceof VideoCard){

				} else {
					//then the card stored in the DB with the same ID is a different type
					//we will delete the old card and replace it with the new
					database.delete(DBHelper.CARD_TABLE_NAME, DBHelper.CARD_ID + " = " + toAdd.getId(), null);
					Log.w(TAG, "Card type mismatch found. Deleting old card." );

				}

			} else {
				//there is nothing in the table with this ID
				Log.d(TAG, "no card with this ID found. adding to DB");
			}
		}

		//check for EXACT duplicate
		//ALL fields must be identical
		Cursor cursorcheck = database.query(
				DBHelper.MESSAGE_TABLE_NAME,
				null,
				DBHelper.COLUMN_MESSAGE_TEXT + " = ? " + " AND " + DBHelper.COLUMN_MESSAGE_TIME_RECEIVED + " = ?" ,
				new String[] {message.getMessageText(), Long.toString(message.getTimeReceived())},
				null,
				null,
				null);
		cursorcheck.close();
		ContentValues values = new ContentValues();

		//load the values with the message data
		//message contents
		values.put(DBHelper.COLUMN_MESSAGE_TEXT, message.getMessageText());
		//time sent, cast to string
		values.put(DBHelper.COLUMN_MESSAGE_TIME_RECEIVED, Long.toString(message.getTimeReceived()));
		//username of sender, stored as string
		values.put(DBHelper.COLUMN_MESSAGE_FROM, message.getMessageFrom().getName());
		//username of receiver, stored as string
		values.put(DBHelper.COLUMN_MESSAGE_TO, message.getMessageTo().getName());
		//put whether message is read or not
		if(message.isRead()){
			values.put(DBHelper.COLUMN_MESSAGE_ISREAD, 1);
		} else {
			values.put(DBHelper.COLUMN_MESSAGE_ISREAD, 0);
		}


		//insert the new message data
		long insert_id = database.insert(DBHelper.MESSAGE_TABLE_NAME, null, values);

		//query the message in the table and return it to make sure its there correctly
		Cursor cursor = database.query(DBHelper.MESSAGE_TABLE_NAME,
				null, DBHelper.MESSAGE_ID + " = " + insert_id, null,
				null, null, null);
		cursor.moveToFirst();
		Message dbMessage = cursorToMessage(cursor);
		cursor.close();

		return dbMessage;

	}

	public synchronized User addServer(User user){
		//find the user by name. if they already exist, then update the existing entry
		User existing = findUserByName(user.getName());
		ContentValues values = new ContentValues();

		if(existing == null){
			//there is no existing user

			//load the values with user data
			//username
			values.put(DBHelper.COLUMN_USER, user.getName());
			//last seen, cast to string
			values.put(DBHelper.COLUMN_LAST_SEEN, Long.toString(user.getLast_seen()));
			//first seen, cast to string
			values.put(DBHelper.COLUMN_FIRST_SEEN, Long.toString(user.getFirst_seen()));
			//whether user is online, as int
			if(user.is_online()){
				values.put(DBHelper.COLUMN_IS_ONLINE, 1);
			}else{
				values.put(DBHelper.COLUMN_IS_ONLINE, 0);
			}
			//store the IP of this user for TCP
			values.put(DBHelper.COLUMN_IP, user.getIp());

			//insert the new user data
			long insert_id = database.insert(DBHelper.USER_TABLE_NAME, null, values);

			//query the user in the table and return it to make sure its there correctly
			Cursor cursor = database.query(DBHelper.USER_TABLE_NAME,
					null, DBHelper.USER_ID + " = " + insert_id, null,
					null, null, null);
			cursor.moveToFirst();
			User result = cursorToUser(cursor);
			cursor.close();

			return result;

		} else {
			//user exists, must be modded

			//delete existing user in the table
			deleteUser(existing);

			//update online status
			if (user.is_online()){
				values.put(DBHelper.COLUMN_IS_ONLINE, 1);
			} else {
				values.put(DBHelper.COLUMN_IS_ONLINE, 0);
			}

			//update last seen
			values.put(DBHelper.COLUMN_LAST_SEEN, Long.toString(user.getLast_seen()));

			//update first seen... the existing user will have the earlier seen time
			values.put(DBHelper.COLUMN_FIRST_SEEN, Long.toString(existing.getFirst_seen()));
			//store the IP of this user for TCP
			values.put(DBHelper.COLUMN_IP, user.getIp());
			//name doesnt need to be updated
			values.put(DBHelper.COLUMN_USER, user.getName());


			//insert as above
			//insert the new user data
			long insert_id = database.insert(DBHelper.USER_TABLE_NAME, null, values);

			//query the user in the table and return it to make sure its there correctly
			Cursor cursor = database.query(DBHelper.USER_TABLE_NAME,
					null, DBHelper.USER_ID + " = " + insert_id, null,
					null, null, null);
			cursor.moveToFirst();
			User result = cursorToUser(cursor);
			cursor.close();

			return result;
		}

	}

	//should strip whitespaces before/after for accurate search
	public User findUserByName(String name){
		//query user table
		Log.d("DBMANAGER", "querying db for name = " + name);
		Cursor userCursor = database.query(DBHelper.USER_TABLE_NAME, null, DBHelper.COLUMN_USER + "=?", new String[]{name}, null, null, null);
		//should only be one user, the first one...
		userCursor.moveToFirst();
		User target = cursorToUser(userCursor);
		userCursor.close();

		return target;
	}


	public synchronized Message deleteServer(Message message){
		//make sure we dont delete the first row by accident
		if(message.getId() < 1)
			return null;

		database.delete(DBHelper.SERVER_TABLE_NAME, DBHelper.SERVER_ID + " = " + message.getId(), null);
		return message;
	}

	//null if no message
	public Message findMessageById(long id){
		Log.d("DBMANAGER", "querying db for message by id = " + id);

		//sql query
		Cursor messageCursor = database.query(DBHelper.MESSAGE_TABLE_NAME, null, DBHelper.MESSAGE_ID + " = " + id, null, null, null, null);
		if(messageCursor.getCount() < 1){
			return null;
		} else {
			Message result = cursorToMessage(messageCursor);
			return result;
		}

	}
	//returns null if user cant be found or error
	public synchronized TMMCard deleteCard(TMMCard toDelete){
		TMMCard existing = findUserByName(user.getName());

		if(existing != null){
			if(database.delete(DBHelper.USER_TABLE_NAME, DBHelper.COLUMN_USER + " = '" + existing.getName() + "'", null) < 0) {
				return null; //error
			}
		}
		return existing;

	}

	public synchronized void cleanDB(){
		Thread clean_db = new Thread() {
			public void run(){
				ArrayList<User> userlist = new ArrayList<User>();
				Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.USER_TABLE_NAME, null);

				if(cursor.moveToFirst()){
					userlist.add(cursorToUser(cursor));
					Log.d(TAG, "added user: " + cursorToUser(cursor).getName() + " to user list from DB");
					Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
					Log.d(TAG, "Moving Cursor Pos...");
					while(cursor.moveToNext()){
						userlist.add(cursorToUser(cursor));
						Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
						Log.d(TAG, "added user: " + cursorToUser(cursor).getName() + " to user list from DB");
					}

				}
				for(int i = 0; i < userlist.size(); i++){
					User userToCompare = userlist.get(i);
					Cursor userCursor = database.query(DBHelper.USER_TABLE_NAME, null, DBHelper.COLUMN_IP + "=?", new String[]{userToCompare.getIp()}, null, null, null);
					//should only be one user, the first one...
					if(userCursor.getCount() > 1){
						//then there are multiple users with the ip
						Log.d("clean", "Duplicates found. Num: " + userCursor.getCount());
						userCursor.moveToFirst();
						User target = cursorToUser(userCursor);
						if(target.getLast_seen() > userToCompare.getLast_seen()){
							database.delete(DBHelper.USER_TABLE_NAME, DBHelper.COLUMN_USER + " = '" + userToCompare.getName() + "'", null);
							Log.d("clean", "Removed user: " + userToCompare.getName() + " was same Ip as " + target.getName());
							userToCompare = target;
						}
						while(userCursor.moveToNext()){
							target = cursorToUser(userCursor);
							if(target.getLast_seen() > userToCompare.getLast_seen()){
								database.delete(DBHelper.USER_TABLE_NAME, DBHelper.COLUMN_USER + " = '" + userToCompare.getName() + "'", null);
								Log.d("clean", "Removed user: " + userToCompare.getName() + " was same Ip as " + target.getName());
								userToCompare = target;
							}
						}
					}
				}




			}

		};
		clean_db.start();
	}

	//return ALL users, both online and not online, sorted by time last seen
	public synchronized ArrayList<User> getAllUsers(){
		//TODO - TEST
		Log.d(TAG, "get all users");
		ArrayList<User> userlist = new ArrayList<User>();
		Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.USER_TABLE_NAME, null);

		if(cursor.moveToFirst()){
			userlist.add(cursorToUser(cursor));
			Log.d(TAG, "added user: " + cursorToUser(cursor).getName() + " to user list from DB");
			Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
			Log.d(TAG, "Moving Cursor Pos...");
			while(cursor.moveToNext()){
				userlist.add(cursorToUser(cursor));
				Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
				Log.d(TAG, "added user: " + cursorToUser(cursor).getName() + " to user list from DB");
			}

		}else {
			return userlist;
		}
		cursor.close();

		//sort
		Collections.sort(userlist);
		return userlist;
	}

	//get ALL messages in the DB, sorted by receipt/sent time
	public synchronized ArrayList<Message> getAllMessages(){
		//TODO - TEST
		Log.d(TAG, "get all messages");
		ArrayList<Message> messagelist = new ArrayList<Message>();
		Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.MESSAGE_TABLE_NAME, null);

		if(cursor.moveToFirst()){
			messagelist.add(cursorToMessage(cursor));
			Log.d(TAG, "added message: " + cursorToMessage(cursor).getMessageText()+ " to message list from DB");

			while(cursor.moveToNext()){
				messagelist.add(cursorToMessage(cursor));
				Log.d(TAG, "added message: " + cursorToMessage(cursor).getMessageText()+ " to message list from DB");
			}
		}else {
			return null;
		}
		cursor.close();


		//sort for convenience
		Collections.sort(messagelist);
		return messagelist;

	}
	public ArrayList<TMMCard> findCardsbyServer(String server){
		//query user table
		Log.d("findCardsbyServer", "querying db for server name = " + server);
		Cursor cardCursor = database.query(DBHelper.CARD_TABLE_NAME, null, DBHelper.COLUMN_SERVER + "=?", new String[]{server}, null, null, null);
		//should only be one user, the first one...
		Log.d("findCardsbyServer", "number of rows found with that server: " + cardCursor.getCount());
		cardCursor.moveToFirst();

		ArrayList<TMMCard> matches = new ArrayList<TMMCard>();

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

		cardCursor.close();
		return matches;
	}

	public ArrayList<Server> getAllServers(){
		//TODO - TEST
		Log.d(TAG, "get all servers");
		ArrayList<User> userlist = new ArrayList<User>();
		Cursor cursor = database.rawQuery("SELECT * FROM " + DBHelper.USER_TABLE_NAME + " WHERE "+DBHelper.COLUMN_IS_ONLINE+" =1", null);

		if(cursor.moveToFirst()){
			userlist.add(cursorToUser(cursor));
			Log.d(TAG, "added user: " + cursorToUser(cursor).getName() + " to user list from DB");
			Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
			Log.d(TAG, "Moving Cursor Pos...");
			while(cursor.moveToNext()){
				userlist.add(cursorToUser(cursor));
				Log.d(TAG, "Cursor at pos: " + cursor.getPosition());
				Log.d(TAG, "added user: " + cursorToUser(cursor).getName() + " to user list from DB");
			}

		}else {
			return null;
		}
		cursor.close();

		//sort
		Collections.sort(userlist);
		return userlist;
	}

}