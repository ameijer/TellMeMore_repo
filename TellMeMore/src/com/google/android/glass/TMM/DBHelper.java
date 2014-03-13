/*
 * File: DBHelper.java
 * Author: Alexander Meijer
 * Date: Sept 5, 2013
 * Class: ELEC 602 Mobile Computing
 * Version 1.0
 * 
 * 
 * ADAPTED SP14 A.MEIJER for ELEC 429
 */


package com.google.android.glass.TMM;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//for the user database
public class DBHelper extends SQLiteOpenHelper{

	public static final String TAG = "DBhelper";

	//name
	public static final String DATABASE_NAME = "directchatDB.db";
	//db version, al upgrades must be monotonic 
	private static final int DATABASE_VERSION = 1;


	//Cards table
	public static final String CARD_TABLE_NAME = "cards";
	public static final String CARD_ID = "_id";
	
	//columns in cards table
	public static final String COLUMN_CARD_DATA = "card_data";
	public static final String COLUMN_MODIFIED = "date_modified";
	public static final String COLUMN_SERVER = "card_server_info";
	
	//server table
	public static final String MESSAGE_TABLE_NAME = "servers";
	public static final String MESSAGE_ID = "_id";
	
	//columns in server table 
	public static final String COLUMN_IP = "ip";
	public static final String COLUMN_API_INFO = "api_tags";
	public static final String COLUMN_FIRST_USED = "date_first_used";
	public static final String COLUMN_LAST_USED = "date_last_used";

	//yucky SQL statement to create users table
	private String CREATE_CARD_TABLE = "create table "
            + CARD_TABLE_NAME + "("
            + CARD_ID + " integer primary key autoincrement, "
            + COLUMN_CARD_DATA + " BLOB not null, "
            + COLUMN_MODIFIED + " INTEGER not null, "
            + COLUMN_SERVER + " text not null );";

	//yucky SQL statement to create message table
		private String CREATE_MESSAGE_TABLE = "create table "
	            + MESSAGE_TABLE_NAME + "("
	            + MESSAGE_ID + " integer primary key autoincrement, "
	            + COLUMN_MESSAGE_TEXT + " text not null, "
	            + COLUMN_MESSAGE_TIME_RECEIVED + " text not null, "
	            + COLUMN_MESSAGE_FROM + " text not null, "
	            + COLUMN_MESSAGE_TO + " text not null, "
	            + COLUMN_MESSAGE_ISREAD + " INTEGER );";
		
		
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d(TAG, "DBhelper Construtor called");
	}

	//make tables here
	//TODO-Threading? This could take some time...
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		//use the sql from above
		db.execSQL(CREATE_MESSAGE_TABLE);
		db.execSQL(CREATE_USER_TABLE);
		
		Log.i(TAG, "Creating database...");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DBHelper.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", deleting old data");
		    db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
		    db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE_NAME);
		    onCreate(db);

	}

}
