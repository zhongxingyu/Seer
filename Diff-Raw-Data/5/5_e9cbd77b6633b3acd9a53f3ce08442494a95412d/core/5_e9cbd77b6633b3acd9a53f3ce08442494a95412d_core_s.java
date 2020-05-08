 package com.cs304_p3;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.SQLException;
 import java.text.ParseException;
 
 import com.cs304.UIs.Login;
 import com.cs304.tables.Customer;
 import com.cs304.tables.HasSong;
 import com.cs304.tables.Item;
 import com.cs304.tables.LeadSinger;
 import com.cs304.tables.Purchase;
 
 public class core {
 	// reads cmd line
 	private BufferedReader BR = new BufferedReader(new InputStreamReader(
 			System.in));
 
 	private static Connection connect;
 
 	private static Item item;
 	private static HasSong hassong;
 	private static LeadSinger leadsinger;
 	private static Purchase purchase;
 	Login Login;
 
 	private static Customer customer;
 
 	public core() throws InterruptedException {
 
 		Login = new Login();
 
 		while (true) {
 			Thread.sleep(9000);
 			if (Login.checkConnection()) {
 				break;
 			}
 		}
 		if (Login.checkConnection() == true) {
 			connect = Login.getConnecton();
 			try {
 
 				item = new Item();
 				hassong = new HasSong();
 				leadsinger = new LeadSinger();
 				purchase = new Purchase();
 
 				//
 				customer = new Customer();
 
 				//
 
 				// Initialize point
 				MenuScreen();
 			} catch (ParseException perror) {
 				// TODO Auto-generated catch block
 				perror.printStackTrace();
 			}
 		}
 	}
 
 	private void MenuScreen() throws ParseException {
 		int mode;
 		boolean end = false;
 
 		try {
 			connect.setAutoCommit(false);
 
 			while (!end) {
 				System.out.print("\n\n TESTING PURPOSES \n\n");
 				System.out.print("1. Make every Table \n");
 				System.out.print("2. Drop every Table \n");
 				System.out.print("3. Insert all items \n");
 				System.out.print("4. Show every Table \n");
 				System.out.print("5. Delete every Table \n");
 				System.out.print("6. Quit \n");
 				mode = Integer.parseInt(BR.readLine());
 				System.out.println();
 
 				switch (mode) {
 				case 1:
 					AllTables();
 					break;
 				case 2:
 					DAllTables();
 					break;
 				case 3:
 					/*
 					 * item.insertItem(connect, 1, "testtitle", "cd", "rock",
 					 * "TestRecords", "2001", "25.25", 52);
 					 * hassong.insertHasSong(connect, 1, "testtitleSong");
 					 * leadsinger.insertLeadSinger(connect, 1, "testsinger");
 					 */
 					// showPurchase.insertPurcahse(connect, )
 					insertAllItems();
 					insertSongsSingers();
 
 					break;
 				case 4:
 					item.showItem(connect);
 					hassong.showHasSong(connect);
 					leadsinger.showLeadSinger(connect);
 					purchase.showPurchase(connect);
 					customer.showCustomer(connect);
 					break;
 				case 5:
 					item.deleteItem(connect, 1);
 					hassong.deleteHasSong(connect, 1);
 					leadsinger.deleteLeadSinger(connect, 1);
 					break;
 				case 6:
 					end = true;
 					break;
 
 				}
 			}
 			connect.close();
 			BR.close();
 			System.out.println("\n Done testing \n\n");
 			System.exit(0);
 		} catch (IOException error) {
 			System.out.println("IOExcept \n");
 
 		} catch (SQLException error) {
 			System.out.println("SQLException \n");
 
 		}
 	}
 
 	public Connection getConnect() {
 		return connect;
 	}
 
 	public static void main(String args[]) throws InterruptedException {
 		new core();
 		// ClerkGUI clerk = new ClerkGUI(connect);
 		// Menu.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 	}
 
 	private void AllTables() { /* create table function */
 
 		try {
 			item.createItem(connect);
 			hassong.createHasSong(connect);
 			leadsinger.createLeadSinger(connect);
 			customer.createCustomer(connect);
 			purchase.createPurchase(connect);
 
 		} catch (SQLException e) {
 			System.out.println("Creating Tables failed");
 		}
 	}
 
 	private void DAllTables() {
 		try {
 			item.dropItem(connect);
 			hassong.dropHasSong(connect);
 			leadsinger.dropLeadSinger(connect);
 			customer.dropCustomer(connect);
 			purchase.dropPurchase(connect);
 
 		} catch (SQLException error) {
 			System.out.println("drop table failed");
 
 		}
 	}
 
 	private void insertAllItems() {
 		try {
 			item.insertItem(connect, 1, "RockCity", "cd", "rock",
 					"TestRecords", "2001", "25.25", 52);
 
 			item.insertItem(connect, 2, "Armageddon", "dvd", "country",
 					"TestRecords2", "1992", "30.00", 10);
 
 			item.insertItem(connect, 3, "Signs", "dvd", "classical",
 					"NewRocords", "1992", "33.00", 11);
 
 			item.insertItem(connect, 4, "Encore", "cd", "rap", "DreRecords",
 					"1995", "13.00", 12);
 
 			item.insertItem(connect, 5, "Mr Deeds", "dvd", "instrumental",
 					"Fox", "1997", "25.00", 13);
 
 			item.insertItem(connect, 6, "Independance day", "dvd", "rock",
 					"WB", "1992", "30.00", 14);
 
 			item.insertItem(connect, 7, "Random", "cd", "pop", "Paramount",
 					"2000", "18.00", 15);
 
 			item.insertItem(connect, 8, "King Kong", "dvd", "rap", "Universal",
 					"1999", "25.00", 16);
 
 			item.insertItem(connect, 9, "Best of", "cd", "instrumental",
 					"DrDre", "2007", "18.00", 118);
 
 			item.insertItem(connect, 10, "Exorcist", "cd", "classical", "MGM",
 					"1999", "25.00", 30);
 
 		} catch (Exception e) {
 			System.out.println("Inserting items failed\n");
 		}
 	}
 
 	private void insertSongsSingers() {
 		try {
 			hassong.insertHasSong(connect, 1, "RockCity");
 			hassong.insertHasSong(connect, 1, "DetroitDock");
 			hassong.insertHasSong(connect, 1, "RockYourself");
 			leadsinger.insertLeadSinger(connect, 1, "Kiss");
 
 			hassong.insertHasSong(connect, 2, "RockOn");
 			leadsinger.insertLeadSinger(connect, 2, "RollingStones");
 
 			hassong.insertHasSong(connect, 3, "HoundDog");
 			leadsinger.insertLeadSinger(connect, 3, "Elvis");
 
 			hassong.insertHasSong(connect, 4, "LoseYourself");
 			leadsinger.insertLeadSinger(connect, 4, "Eminem");
 
 			hassong.insertHasSong(connect, 5, "Popular");
 			leadsinger.insertLeadSinger(connect, 5, "LilWayne");
 
 			hassong.insertHasSong(connect, 6, "HolyGrail");
 			hassong.insertHasSong(connect, 6, "TomFord");
 
 			leadsinger.insertLeadSinger(connect, 6, "JayZ");
 			leadsinger.insertLeadSinger(connect, 6, "J.T");
 
 			hassong.insertHasSong(connect, 7, "E.T");
 			leadsinger.insertLeadSinger(connect, 7, "KatyPerry");
 			leadsinger.insertLeadSinger(connect, 7, "KanyeWest");
 
 			hassong.insertHasSong(connect, 8, "InDaClub");
 			leadsinger.insertLeadSinger(connect, 8, "50Cent");
 
 			hassong.insertHasSong(connect, 9, "WakeMeUp");
 			leadsinger.insertLeadSinger(connect, 9, "Avicii");
 
 			hassong.insertHasSong(connect, 10, "MetalSong");
 			leadsinger.insertLeadSinger(connect, 10, "JustinBieber");
 
			customer.insertCustomer(connect, 44, "goof", "Qais", "133113",
 					"13313");
 
 			Date d1 = new Date(10, 8, 2013);
 			Date d2 = new Date(2008, 8, 15);
 			Date d3 = new Date(2008, 8, 25);
			purchase.insertPurchase(connect, 44, "32423", d1, d2, d3);
 			// purchase.insertPurchase(connect, CID, Cardnum, expireDate,
 			// expectedDate, deliveredD)
 		} catch (Exception E) {
 		}
 
 	}
 
 }
