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
//import android.database.Cursor;
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
	public static final String COLUMN_CARD_TYPE = "card_type";
	
	//some constants we can use for the current three types of cards
	public static final String AUDIO = "AUDIO";
	public static final String VIDEO = "VIDEO";
	public static final String TEXT = "TEXT";
	
	
	//server table
	public static final String SERVER_TABLE_NAME = "servers";
	public static final String SERVER_ID = "_id";
	
	//columns in server table 
	public static final String COLUMN_SERVER_NAME = "server_name";
	public static final String COLUMN_SERVER_DATA = "server_data";

	//SQL statement to create cards table
	private String CREATE_CARD_TABLE = "create table "
            + CARD_TABLE_NAME + "("
            + CARD_ID + " INTEGER primary key autoincrement, "
            + COLUMN_CARD_TYPE + " TEXT not null,"
            + COLUMN_CARD_DATA + " BLOB not null, "
            + COLUMN_MODIFIED + " INTEGER not null, "
            + COLUMN_SERVER + " TEXT not null );";

	// SQL statement to create server table
		private String CREATE_SERVER_TABLE = "create table "
	            + SERVER_TABLE_NAME + "("
	            + SERVER_ID + " INTEGER primary key autoincrement, "
	            + COLUMN_SERVER_NAME + " TEXT not null, "
	            + COLUMN_SERVER_DATA + " BLOB not null );";
		
		
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.d(TAG, "DBhelper Construtor called");
	}

	//make tables here
	//TODO-Threading? This could take some time...
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		//use the sql from above
		db.execSQL(CREATE_CARD_TABLE);
		db.execSQL(CREATE_SERVER_TABLE);
		
		Log.i(TAG, "Creating database...");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DBHelper.class.getName(),
		        "Upgrading database from version " + oldVersion + " to "
		            + newVersion + ", deleting old data");
		    db.execSQL("DROP TABLE IF EXISTS " + SERVER_TABLE_NAME);
		    db.execSQL("DROP TABLE IF EXISTS " + CARD_TABLE_NAME);
		    onCreate(db);

	}

}
