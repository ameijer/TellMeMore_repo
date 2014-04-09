package com.google.android.glass.TMM;

import java.io.File;
import java.io.IOException;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class TellMeMoreApplication extends Application{
	//our global DB, abstracted through a manager
	
	public DBManager db;
	
	public static final String TAG = "TMM" +", " + TellMeMoreApplication.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();
		//db = new DBManager(this.getApplicationContext());
		//setCardsRetreived(false);
		//open DB
		Log.d(TAG, "Application-level oncreate called");
		try {
			db = new DBManager(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//check state of external storage, as detailed in http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
		boolean iswriteable = true;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			Log.i(TAG, "Application onCreate reports storage ready for R+W");
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			Log.w(TAG, "Application onCreate reports storage ready for R only");
			iswriteable = false;
			Toast.makeText(getApplicationContext(), "Warning - readable only storage. No new audio may be downloaded",
					Toast.LENGTH_LONG).show();
		} else {
			// Something else is wrong. It may be one of many other states, but all we need
			//  to know is we can neither read nor write
			Log.e(TAG, "Application onCreate reports storage NON R/W-able");
			iswriteable = false;
			Toast.makeText(getApplicationContext(), "Warning - storage is inaccesible. No audio content available",
					Toast.LENGTH_LONG).show();
		}

		
		//mkdir for the data if it isn't already there
		if(iswriteable){
			File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/tmm");
			boolean exists = dir.exists();
			if (!exists){
				dir.mkdirs();
				}
		}


		//we should think about initializing our TMMservice here
	}




	
	
}
