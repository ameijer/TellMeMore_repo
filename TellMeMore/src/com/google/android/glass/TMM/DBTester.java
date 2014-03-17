package com.google.android.glass.TMM;

/*
 * File: DBTester.java
 * Author: Alexander Meijer
 * Date: 17 Mar, 2014
 * Class: ELEC 429 Independent Study
 * Version 1.0
 */
import java.io.IOException;

import android.content.Context;
import android.test.AndroidTestCase;
import junit.framework.TestCase;

public class DBTester extends AndroidTestCase {
	DBManager dbman;
	public void setUp(){
		Context context = this.getContext();
		dbman = new DBManager(context);
		dbman.open();
	}

	public void testOpen() {
		if(!dbman.open())
			fail("opening db failed");
	}

	public void testAddCards() throws IOException {
		TextCard text1 = new TextCard(1, 100, "The title for test card 1");
		TextCard text2 = new TextCard(2, 10, "The title for test card 2");
		TextCard text3 = new TextCard(3, 1, "The title for test card 3");
		
		AudioCard audio1 = new AudioCard(1, 100, "audio card 1 title", "Path/to/file1");
		AudioCard audio2 = new AudioCard(2, 99, "audio card 2 title", "path/to/file2");
		AudioCard audio3 = new AudioCard(3, 98, "audio card 3 title", "path/to/file3");
		
		VideoCard vid1 = new VideoCard(1, 100, "video card 1 title", "video1tag");
		VideoCard vid2 = new VideoCard(2, 99, "video card 2 title", "video2tag");
		VideoCard vid3 = new VideoCard(3, 98, "video card 3 title", "video3tag");
		
		Server source1 = new Server("server 1", "?=apiinfo", 0, 1);
		Server source2 = new Server("server 2", "?=apiinfo", 2, 3);
		
		TMMCard t1 = dbman.addCard(text1, source1);
		TMMCard t2 = dbman.addCard(text2, source1);
		TMMCard t3 = dbman.addCard(text3, source1);
		
		TMMCard a1 = dbman.addCard(audio1, source1);
		TMMCard a2 = dbman.addCard(audio2, source1);
		TMMCard a3 = dbman.addCard(audio3, source1);
		
		TMMCard v1 = dbman.addCard(vid1, source2);
		TMMCard v2 = dbman.addCard(vid2, source2);
		TMMCard v3 = dbman.addCard(vid3, source2);
		
		if(!(t1 instanceof TextCard) && t1.getPriority() != text1.getPriority() && t1.getTitle() != text1.getTitle())
			fail("text card 1 added incorrectly");
		if(!(t2 instanceof TextCard) && t2.getPriority() != text2.getPriority() && t2.getTitle() != text2.getTitle())
			fail("text card 2 added incorrectly");
		if(!(t3 instanceof TextCard) && t3.getPriority() != text3.getPriority() && t3.getTitle() != text3.getTitle())
			fail("text card 3 added incorrectly");
		
		if(!(a1 instanceof AudioCard) && a1.getPriority() != audio1.getPriority() && a1.getTitle() != audio1.getTitle())
			fail("audio card 1 added incorrectly");
		if(!(a2 instanceof AudioCard) && a2.getPriority() != audio2.getPriority() && a2.getTitle() != audio2.getTitle())
			fail("audio card 2 added incorrectly");
		if(!(a3 instanceof AudioCard) && a3.getPriority() != audio3.getPriority() && a3.getTitle() != audio3.getTitle())
			fail("audio card 3 added incorrectly");
		
		if(!(v1 instanceof VideoCard) && v1.getPriority() != vid1.getPriority() && v1.getTitle() != vid1.getTitle())
			fail("video card 1 added incorrectly");
		if(!(v2 instanceof VideoCard) && v2.getPriority() != vid2.getPriority() && v2.getTitle() != vid2.getTitle())
			fail("video card 2 added incorrectly");
		if(!(v3 instanceof VideoCard) && v3.getPriority() != vid3.getPriority() && v3.getTitle() != vid3.getTitle())
			fail("video card 3 added incorrectly");
		
	}

	public void testAddServers() {
		User sender = new User("first user", 1, 2, false, "4");
		User receiver = new User("first receiver", 3, 4, true, "5");
		User dbsender = dbman.addUser(sender);
		User dbreceiver = dbman.addUser(receiver);
		if(!dbsender.getName().equals("first user")){
			fail("user not added correctly");
		}
		if(!dbreceiver.getName().equals("first receiver")){
			fail("user not added correctly");
		}
		if(dbreceiver.getFirst_seen() != 4){
			fail("first seen not added correctly");
		}
		if(dbsender.getFirst_seen() != 2){
			fail("first seen not added correctly");
		}
		if(dbreceiver.getLast_seen() != 3){
			fail("last seen not added correctly");
		}
		if(dbsender.getLast_seen() != 1){
			fail("last seen not added correctly");
		}
		if(dbsender.is_online() != false){
			fail("is online not added correctly");
		}
		if(dbreceiver.is_online() != true){
			fail("is online not added correctly");
		}
	}

	public void testDeleteMessage() {
		fail("Not yet implemented");
	}

	public void testDeleteUser() {
		fail("Not yet implemented");
	}

	public void testCleanDB() {
		fail("Not yet implemented");
	}

	public void testGetAllUsers() {
		fail("Not yet implemented");
	}

	public void testGetAllMessages() {
		fail("Not yet implemented");
	}

	public void testGetAllMessagesTo_FromUser() {
		fail("Not yet implemented");
	}
}
