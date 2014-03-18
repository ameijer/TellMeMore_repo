package com.google.android.glass.TMM;

import android.app.Application;
import android.util.Log;

public class TellMeMoreApplication extends Application{
	//our global DB, abstracted through a manager
	public DBManager db;
	public static final String TAG = "TMM" +", " + TellMeMoreApplication.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();
		db = new DBManager(this.getApplicationContext());
		//open DB
		if(!db.open()){
			Log.e(TAG, "DB open failed");
			System.exit(1);
		}
		Log.d(TAG, "Application-level oncreate called");

		//we should think about initializing our TMMservice here
	}
}
