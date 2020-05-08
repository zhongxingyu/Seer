 package com.bricou.mineteryarpg;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.util.HashMap;
 
 /**
  * Classe de chargement et d'initialisation du paramtrage du plugin
  * @author Bricou & Dr.Jack
  * 
  */
 public class PluginConfigFile
 {
 	
 	/**
 	 * 
 	 * @param file
 	 */
 	public HashMap<String,String> initializeProperties(File configFile)
 	{
 		try
 		{
 			configFile.createNewFile();
 			BufferedWriter out = new BufferedWriter(new FileWriter(configFile));
 			out.write("#Fichier de paramtrage autognr");
 			out.newLine();
 			out.write("#Principe :  paramtre:valeur");
 		    out.newLine();
 		    out.write("LevelMax:100");
             out.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param file
 	 */
 	public HashMap<String,String> loadProperties(File configFile)
 	{
		HashMap<String,String> configMap = new HashMap<String,String>();
 		try 
 		{
 			// Lecture en boucle sur le fichier de paramtrage du plugin
 			BufferedReader reader = new BufferedReader(new FileReader(configFile));
 			String line = null;
 			while ((line = reader.readLine()) != null)
 			{
 				// Analyse de chaque enregistrement et stockage dans la hashmap
 				if (!line.contains("#") && (!line.equalsIgnoreCase("")))
 				{
 					String[] lines = line.split(":");
 					configMap.put(lines[0], lines[1]);
 				}
 			}
 			reader.close();
 		}
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 		}
		return configMap;
 	}
 	
 }
