//package com.google.android.glass.TMM;
//
///*
// * File: DBTester.java
// * Author: Alexander Meijer
// * Date: 17 Mar, 2014
// * Class: ELEC 429 Independent Study
// * Version 1.0
// */
//import java.io.IOException;
//import java.util.ArrayList;
//
//import android.content.Context;
//import android.test.AndroidTestCase;
//import android.util.Log;
//import junit.framework.TestCase;
//
//public class DBTester extends AndroidTestCase {
//	DBManager dbman;
//	
//	
//	public void setUp() throws Exception{
//		super.setUp();
//		Context context = this.getContext();
//		dbman = new DBManager(context);
//		dbman.open();
//		Server source1 = new Server("server 1", "?=apiinfo", 0, 1);
//		Server source2 = new Server("server 2", "?=apiinfo", 2, 3);
//		
//		TextCard text1 = new TextCard(1, 100, "The title for test card 1", source1);
//		TextCard text2 = new TextCard(2, 10, "The title for test card 2", source1);
//		TextCard text3 = new TextCard(3, 1, "The title for test card 3", source1);
//
//		AudioCard audio1 = new AudioCard(1, 100, "audio card 1 title", "Path/to/file1", source1);
//		AudioCard audio2 = new AudioCard(2, 99, "audio card 2 title", "path/to/file2", source1);
//		AudioCard audio3 = new AudioCard(3, 98, "audio card 3 title", "path/to/file3", source1);
//
//		VideoCard vid1 = new VideoCard(1, 100, "video card 1 title", "video1tag", source2);
//		VideoCard vid2 = new VideoCard(2, 99, "video card 2 title", "video2tag", source2);
//		VideoCard vid3 = new VideoCard(3, 98, "video card 3 title", "video3tag", source2);
//
//		
//
//		TMMCard t1 = dbman.addCard(text1);
//		TMMCard t2 = dbman.addCard(text2);
//		TMMCard t3 = dbman.addCard(text3);
//
//		TMMCard a1 = dbman.addCard(audio1);
//		TMMCard a2 = dbman.addCard(audio2);
//		TMMCard a3 = dbman.addCard(audio3);
//
//		TMMCard v1 = dbman.addCard(vid1);
//		TMMCard v2 = dbman.addCard(vid2);
//		TMMCard v3 = dbman.addCard(vid3);
//		
//		
//		Server s1 = dbman.addServer(source1);
//		Server s2 = dbman.addServer(source2);
//		
//	}
//	
//	public void tearDown(){
//		boolean result = dbman.deleteDB(this.getContext());
//		assertEquals(result, true);
//	}
//
//	public void testOpen() {
//		if(!dbman.open())
//			fail("opening db failed");
//	}
//
//	public void testAddCards() throws IOException {
//		
//		Server source1 = new Server("server 1", "?=apiinfo", 0, 1);
//		Server source2 = new Server("server 2", "?=apiinfo", 2, 3);
//		
//		TextCard text1 = new TextCard(1, 100, "The title for text card 1", source1);
//		TextCard text2 = new TextCard(2, 10, "The title for text card 2", source1);
//		TextCard text3 = new TextCard(3, 1, "The title for text card 3", source1);
//
//		AudioCard audio1 = new AudioCard(1, 100, "audio card 1 title", "Path/to/file1", source1);
//		AudioCard audio2 = new AudioCard(2, 99, "audio card 2 title", "path/to/file2", source1);
//		AudioCard audio3 = new AudioCard(3, 98, "audio card 3 title", "path/to/file3", source1);
//
//		VideoCard vid1 = new VideoCard(1, 100, "video card 1 title", "video1tag", source2);
//		VideoCard vid2 = new VideoCard(2, 99, "video card 2 title", "video2tag", source2);
//		VideoCard vid3 = new VideoCard(3, 98, "video card 3 title", "video3tag", source2);
//
//		
//
//		boolean t1 = dbman.addCard(text1);
//		boolean t2 = dbman.addCard(text2);
//		boolean t3 = dbman.addCard(text3);
//
//		boolean a1 = dbman.addCard(audio1);
//		boolean a2 = dbman.addCard(audio2);
//		boolean a3 = dbman.addCard(audio3);
//
//		boolean v1 = dbman.addCard(vid1);
//		boolean v2 = dbman.addCard(vid2);
//		boolean v3 = dbman.addCard(vid3);
//
////		if(!(t1 instanceof TextCard) && t1.getPriority() != text1.getPriority() && t1.getTitle() != text1.getTitle())
////			fail("text card 1 added incorrectly");
////		if(!(t2 instanceof TextCard) && t2.getPriority() != text2.getPriority() && t2.getTitle() != text2.getTitle())
////			fail("text card 2 added incorrectly");
////		if(!(t3 instanceof TextCard) && t3.getPriority() != text3.getPriority() && t3.getTitle() != text3.getTitle())
////			fail("text card 3 added incorrectly");
////
////		if(!(a1 instanceof AudioCard) && a1.getPriority() != audio1.getPriority() && a1.getTitle() != audio1.getTitle())
////			fail("audio card 1 added incorrectly");
////		if(!(a2 instanceof AudioCard) && a2.getPriority() != audio2.getPriority() && a2.getTitle() != audio2.getTitle())
////			fail("audio card 2 added incorrectly");
////		if(!(a3 instanceof AudioCard) && a3.getPriority() != audio3.getPriority() && a3.getTitle() != audio3.getTitle())
////			fail("audio card 3 added incorrectly");
////
////		if(!(v1 instanceof VideoCard) && v1.getPriority() != vid1.getPriority() && v1.getTitle() != vid1.getTitle())
////			fail("video card 1 added incorrectly");
////		if(!(v2 instanceof VideoCard) && v2.getPriority() != vid2.getPriority() && v2.getTitle() != vid2.getTitle())
////			fail("video card 2 added incorrectly");
////		if(!(v3 instanceof VideoCard) && v3.getPriority() != vid3.getPriority() && v3.getTitle() != vid3.getTitle())
////			fail("video card 3 added incorrectly");
//
//	}
//	
//	public void testAddServers() throws IOException {
//		Server source1 = new Server("server 1", "?=apiinfo", 0, 1);
//		Server source2 = new Server("server 2", "?=apiinfo", 2, 3);
//		
//		Server s1 = dbman.addServer(source1);
//		Server s2 = dbman.addServer(source2);
//		
//		if(!s1.getName().equalsIgnoreCase(source1.getName())){
//			fail("server source 1 not added correctly");
//		}
//		
//		if(!s2.getName().equalsIgnoreCase(source2.getName())){
//			fail("server source 1 not added correctly");
//		}
//		
//	}
//	 
//	public void testFindCardById() {
//		Server source2 = new Server("server 2", "?=apiinfo", 2, 3);
//		
//		VideoCard vid3 = new VideoCard(3, 98, "video card 3 title", "video3tag", source2);
//		
//		ArrayList<TMMCard> allCards = dbman.getAllCards();
//		
//		//quick check that this is functioning properly
//		assertEquals(allCards.size(), 9);
//		
//		for(int i = 0; i < allCards.size(); i ++){
//			Log.d("MANUAL", "position: " + i + " contains card: " + allCards.get(i));
//		}
//		//vid3 should be the 6th card
//		TMMCard retCard = dbman.findCardById(allCards.get(3).getId());
//		
//		assertTrue(retCard instanceof VideoCard);
//		assertEquals(retCard.getPriority() ,vid3.getPriority());
//		assertTrue(retCard.getTitle().equalsIgnoreCase(vid3.getTitle()));
//	
//	}
//
//	public void testGetAllServers() {
//		ArrayList<Server> serverz = dbman.getAllServers();
//		
//		if(serverz.size() != 2){
//			fail("incorrect number of servers retrieved");
//		}
//	}
//	
//	public void testfindServerbyname() {
//		
//		Server source2 = new Server("server 2", "?=apiinfo", 2, 3);
//		Server retServ = dbman.findServerByName(source2.getName());
//		
//		if(!retServ.getName().equalsIgnoreCase(source2.getName())){
//			fail("find server by name not working");
//		}
//		
//	}
//
//	public void testDeleteServer() throws IOException {
//		
//		ArrayList<Server> servs = dbman.getAllServers();
//		dbman.deleteServer(servs.get(0));
//		
//		Server foundAfterDel = dbman.findServerByName(servs.get(0).getName());
//		
//		if(foundAfterDel != null){
//			fail("server targetserver was not deleted properly"); 
//		}
//		
//		dbman.addServer(servs.get(0));
//		
//	}
//
//	public void testDeleteCard() throws IOException {
//		ArrayList<TMMCard> cards = dbman.getAllCards();
//		
//		dbman.deleteCard(cards.get(3));
//		
//		TMMCard foundAfterDel = dbman.findCardById(cards.get(3).getId());
//		
//		if(foundAfterDel != null){
//			fail("TMMCard targetcard was not deleted properly");
//		}
//		dbman.addCard(cards.get(3));
//	}
//	
//	public void testInvalidDelete() {
//		Server source2 = new Server("server 2", "?=apiinfo", 2, 3);
//		TMMCard badCard = new TextCard(1, 1000, "a card that doesn't exist in the DB", source2);
//		badCard.setId(Integer.MAX_VALUE);
//		
//		TMMCard removedCard = dbman.deleteCard(badCard);
//		
//		if(removedCard != null){
//			fail("invalid card deletion not behaving as expected");
//		}
//		
//	}
//
//	public void testfindCardsbyServer() {
//		Server source2 = new Server("server 2", "?=apiinfo", 2, 3);
//		ArrayList<TMMCard> cardz = dbman.findCardsbyServer(source2.getName());
//		
//		//we expect the three video cards only to have targetserver as their source
//		assertEquals(3, cardz.size());
//		
//	}
//}
