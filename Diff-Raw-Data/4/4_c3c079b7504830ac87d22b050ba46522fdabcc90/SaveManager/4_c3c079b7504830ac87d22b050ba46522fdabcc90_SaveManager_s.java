 
 package se.chalmers.segway.game;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Writer;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import se.chalmers.segway.resources.ResourcesManager;
 import android.content.Context;
 
 /**
  * A class to save and load upgrades in between sessions.
  */
 public class SaveManager {
 	/**
 	 * Reads from a file which upgrades have been bought in previous
 	 * sessions and enables them.
 	 */
 	public static void loadUpgrades() {
 		File path=new File(ResourcesManager.getInstance().activity.getFilesDir(),"saves");
 		File file = new File(path, "upgrades");
 		if(file.exists()){
 			System.out.println("FOUND UPGRADE FILE BITCH");
 			try {
 				//TODO: Needs testing
 				System.out.println("IN TRY BITCH");
 				FileInputStream fis = new FileInputStream(file);
 				System.out.println("FIS BITCH");
 				ObjectInputStream ois = new ObjectInputStream(fis);
 				System.out.println("OIS BITCH");
 				Object obj = ois.readObject();
 				System.out.println("OBJ BITCH");
 				ois.close();
 				if (obj != null && obj instanceof HashMap<?, ?>){
 					System.out.println("BITCH INSTANCEOF IS TRUE UPGRADES");
 					for(Object i : ((HashMap<?,?>)obj).keySet()){
 						System.out.println("IN FOR LOOP BITCH");
 						for(Upgrades u : Upgrades.values()){
 							if(u.getName().equals((String)i)){
 								System.out.println("IT EQUALED BITCH");
 								u.setActive((Boolean) ((HashMap<?,?>)obj).get(i));
 							}
 						}
 					}
 				}
 			} catch (Exception e){
 				e.printStackTrace();
 			}
 		} else {
 			System.out.println("NO UPGRADE FILE BITCH");
 		}
 		
 	}
 	
 	/**
 	 * Writes which upgrades have been bought to a file.
 	 */
 	public static void saveUpgrades() {
 		HashMap<String,Boolean> saveMap = new HashMap<String, Boolean>();
 		for(Upgrades upg : Upgrades.values()){
 			saveMap.put(upg.getName(), upg.isActivated());
 		}
 		File path=new File(ResourcesManager.getInstance().activity.getFilesDir(),"saves");
 		System.out.println("BITCH IS DIR " +  path.isDirectory());
 		path.mkdir();
 		System.out.println("BITCH IS DIR " +  path.isDirectory());
 		FileOutputStream fos;
 		File file = new File(path, "upgrades");
 		try {
 			System.out.println("TRYING TO WRITE UPGRADE BITCH");
 			fos = new FileOutputStream(file);
 			
 			System.out.println("FOS CREATED BITCH");
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			System.out.println("OOS CREATED BITCH");
 			oos.writeObject(saveMap);
 			System.out.println("WROTE UPGRADE DATA BITCH");
 			oos.close();
 			System.out.println("CLOSED BITCH");
 			System.out.println("Exactly after it is " + file.exists() + " BITCH");
 		} catch (Exception e) {
 			System.out.println("EXCEPTION BITCH");
 			e.printStackTrace();
 			
 		}
 	}
 	/**
 	 * Saves the players data.
 	 * @param data the playerdata to be saved
 	 */
 	public static void savePlayerData(PlayerData data){
 		File path=new File(ResourcesManager.getInstance().activity.getFilesDir(),"saves");
 		System.out.println("BITCH IS DIR " +  path.isDirectory());
 		path.mkdir();
 		System.out.println("BITCH IS DIR " +  path.isDirectory());
 		FileOutputStream fos;
 		File file = new File(path, "player");
 		try {
 			System.out.println("TRYING TO WRITE PLAYER BITCH");
 			fos = new FileOutputStream(file);
 			
 			System.out.println("FOS CREATED BITCH");
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			System.out.println("OOS CREATED BITCH");
 			oos.writeObject(data);
 			System.out.println("WROTE DATA BITCH");
 			oos.close();
 			System.out.println("CLOSED BITCH");
 			System.out.println("Exactly after it is " + file.exists() + " BITCH");
 		} catch (Exception e) {
 			System.out.println("EXCEPTION BITCH");
 			e.printStackTrace();
 		}
 		//System.out.println(new File("player").exists() + " BITCH");
 	}
 	/**
 	 * Returns the players saved data
 	 * @return PlayerData from file, null if something went wrong, the file doesn't exist
 	 */
 	public static PlayerData loadPlayerData(){
 		File path=new File(ResourcesManager.getInstance().activity.getFilesDir(),"saves");
 		File file = new File(path, "player");
 		if(file.exists()){
 			System.out.println("FOUND PLAYER FILE BITCH");
 			try {
 				//TODO: Needs testing
 				FileInputStream fis = new FileInputStream(file);
 				System.out.println("FIS BITCH");
 				ObjectInputStream ois = new ObjectInputStream(fis);
 				System.out.println("OIS BITCH");
 				Object obj = ois.readObject();
 				System.out.println("OBJ BITCH");
 				ois.close();
 				
 				if (obj != null && obj instanceof PlayerData){
 					System.out.println("PLAYERCASH LOADED BITCH: " + ((PlayerData) obj).getMoney());
 					return (PlayerData) obj;
 				} else {
 					return null;
 				}
 			} catch (Exception e){
 				e.printStackTrace();
 				return null;
 			}
 		} else {
 			System.out.println("NO PLAYER FILE BITCH");
 			return null;
 		}
 	}
 	public static void saveSettings(Settings settings){
 		File path=new File(ResourcesManager.getInstance().activity.getFilesDir(),"saves");
 		System.out.println("BITCH IS DIR " +  path.isDirectory());
 		path.mkdir();
 		System.out.println("BITCH IS DIR " +  path.isDirectory());
 		FileOutputStream fos;
 		File file = new File(path, "settings");
 		try {
 			System.out.println("TRYING TO WRITE SETTINGS BITCH");
 			fos = new FileOutputStream(file);
 			System.out.println("FOS CREATED BITCH");
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			System.out.println("OOS CREATED BITCH");
 			oos.writeObject(settings);
 			System.out.println("WROTE SETTINGS BITCH");
 			oos.close();
 			System.out.println("CLOSED BITCH");
 			System.out.println("Exactly after it is " + file.exists() + " BITCH");
 		} catch (Exception e) {
 			System.out.println("EXCEPTION BITCH");
 			e.printStackTrace();
 		}
 		//System.out.println(new File("player").exists() + " BITCH");
 	}
 	public static Settings loadSettings(){
 		File path=new File(ResourcesManager.getInstance().activity.getFilesDir(),"saves");
 		File file = new File(path, "settings");
 		if(file.exists()){
 			System.out.println("FOUND SETTINGS FILE BITCH");
 			try {
 				//TODO: Needs testing
 				FileInputStream fis = new FileInputStream(file);
 				ObjectInputStream ois = new ObjectInputStream(fis);
 				Object obj = ois.readObject();
 				obj = ois.readObject();
 				ois.close();
 				if (obj != null && obj instanceof Settings){
 					return (Settings)obj;
 				} else {
 					return null;
 				}
 			} catch (Exception e){
 				e.printStackTrace();
 				return null;
 			}
 		} else {
			System.out.println("NO PLAYER FILE BITCH");
 			return null;
 		}
 	}
 		
 }
