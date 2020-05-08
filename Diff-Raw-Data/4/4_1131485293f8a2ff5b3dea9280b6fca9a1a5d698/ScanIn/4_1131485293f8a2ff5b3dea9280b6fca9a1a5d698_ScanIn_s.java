//package com.example.droidbox;
 
 /*
  * This class works when the package com.example.droidbox is commented but it does not work when it is uncommented.
  * To get the app itself (which has nothing to do with this class) I need to uncomment the line. Not sure
  * what I need to fix for this. Maybe it shouldn't be in this folder???
  * -jmfurlott 09/12
  * 
  */
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 
 
 public class ScanIn {
 	public int currentSyncCode;
 	public String artist, album, song;
 	
 	public ScanIn() {
 		//if there is no sync code (on initialization) then just set to 0 so method will now to update it
 		currentSyncCode = 0;
 	}
 	
 	public ScanIn(int currentSyncCode) {
 		currentSyncCode = this.currentSyncCode; //hopefully can pass in the currentSynccode
 	}
 	
 	public void read() {  //TODO: return type of ArrayList<Song>
 		try { 
 			File file = new File("update.txt");
 			Scanner scanner = new Scanner(file);
 			
 			//check to see whether or not we should sync
 			int newSyncCode = scanner.nextInt();
 			
 			if(currentSyncCode == newSyncCode) {
 				//do not sync
 				//do something else here
 				System.out.println("Updated");
 			}
 			else {
 				while(scanner.hasNextLine()) {
 					if(scanner.hasNextLine()) {
 						artist = scanner.nextLine();
 					}
 					if(scanner.hasNextLine()) {
 						song = scanner.nextLine();
 					}
 					if(scanner.hasNextLine()) {
 					album = scanner.nextLine();
 					}
 					System.out.println(artist + song + album); //only to check to see if this is working
 					
 					//TODO: create song objects and save the properties into Songs and then put them into an ArrayList<Song>
 					
 				}
 				scanner.close();
 				
 			}
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void main(String[] args) {
 		ScanIn scan = new ScanIn();
 		scan.read();
 	}
 	
 }
