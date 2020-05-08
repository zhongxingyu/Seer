 package com.legit2.Demigods.Database;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.HashMap;
 
 import com.legit2.Demigods.Utilities.DDataUtil;
 import com.legit2.Demigods.Utilities.DObjUtil;
 import com.legit2.Demigods.Utilities.DUtil;
 
 /*
  * HASHMAP OF PLAYER'S NAMES
  * CONTAINS EACH PLAYER'S SAVED INFORMATION
  * BE VERY CAREFUL NOT TO SAVE THINGS THAT CAN'T BE WRITTEN
  */
 public class DFlatFile
 {
 	static String path = "plugins/Demigods/";
 	
 	/*
 	 * save : Saves itself, but must be loaded elsewhere (main plugin).
 	 */
 	public static void save()
 	{
 		try
 		{
 			(new File(path + "Data/")).mkdirs();
 			(new File(path + "Players/")).mkdirs();
 			(new File(path + "Characters/")).mkdirs();
 			for(String data : DDataUtil.getAllPluginData().keySet()) 
 			{
 				if(data.startsWith("temp_")) continue;
 				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path + "/Data/" + data + ".demi"));
 				oos.writeObject(DDataUtil.getAllPluginData().get(data));
 				oos.flush();
 				oos.close();
 			}
 			for(String data : DDataUtil.getAllPlayers().keySet()) 
 			{
 				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path + "/Players/" + data + ".demi"));
 				oos.writeObject(DDataUtil.getAllPlayers().get(data));
 				oos.flush();
 				oos.close();
 			}
 			for(Integer data : DDataUtil.getAllChars().keySet()) 
 			{
 				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path + "/Characters/" + data + ".demi"));
 				oos.writeObject(DDataUtil.getAllChars().get(data));
 				oos.flush();
 				oos.close();
 			}
 			
 			DUtil.info("Success! All hash map data saved to FlatFile!");
 		}
 		catch(Exception e)
 		{
 			DUtil.severe("Something went wrong while saving.");
 			e.printStackTrace();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public static void load()
 	{
 		// Data
 		File f1 = new File(path + "Data/");
		if(!f1.exists())
 		{
 			DUtil.info("Creating a new data save.");
 			f1.mkdirs();
 		}
 		File[] list1 = f1.listFiles();
 		if(list1 != null)
 		{
			for(File element : list1)
 			{
 				String load = element.getName();
				if(load.endsWith(".demi"))
 				{
 					load = load.substring(0, load.length() - 5);
 					
 					try
 					{
 						ObjectInputStream ois = new ObjectInputStream(new FileInputStream(element));
 						Object result = ois.readObject();
 						DDataUtil.getAllPluginData().put(load, (HashMap<String, Object>) result);
 						ois.close();
 					}
					catch(Exception error)
 					{
 						DUtil.severe("Could not load data " + load);
 						error.printStackTrace();
 						DUtil.severe("End stack trace for " + load);
 					}
 				}
 			}
 		}
 		
 		// Players
 		File f2 = new File(path + "Players/");
		if(!f2.exists())
 		{
 			DUtil.info("Creating a new player save.");
 			f2.mkdirs();
 		}
 		File[] list2 = f2.listFiles();
 		if(list2 != null)
 		{
			for(File element : list2)
 			{
 				String load = element.getName();
				if(load.endsWith(".demi"))
 				{
 					load = load.substring(0, load.length() - 5);
 					
 					try
 					{
 						ObjectInputStream ois = new ObjectInputStream(new FileInputStream(element));
 						Object result = ois.readObject();
 						DDataUtil.getAllPlayers().put(load, (HashMap<String, Object>) result);
 						ois.close();
 					}
					catch(Exception error)
 					{
 						DUtil.severe("Could not load player " + load);
 						error.printStackTrace();
 						DUtil.severe("End stack trace for " + load);
 					}
 				}
 			}
 		}
 		
 		// Characters
 		File f3 = new File(path + "Characters/");
		if(!f2.exists())
 		{
 			DUtil.info("Creating a new character save.");
 			f3.mkdirs();
 		}
 		File[] list3 = f3.listFiles();
 		if(list3 != null)
 		{
			for(File element : list3)
 			{
 				String load = element.getName();
				if(load.endsWith(".demi"))
 				{
 					load = load.substring(0, load.length() - 5);
 					
 					Integer intLoad = DObjUtil.toInteger(load);
 					
 					try
 					{
 						ObjectInputStream ois = new ObjectInputStream(new FileInputStream(element));
 						Object result = ois.readObject();
 						DDataUtil.getAllChars().put(intLoad, (HashMap<String, Object>) result);
 						ois.close();
 					}
					catch(Exception error)
 					{
 						DUtil.severe("Could not load character " + load);
 						error.printStackTrace();
 						DUtil.severe("End stack trace for " + load);
 					}
 				}
 			}
 		}
 	}
 }
