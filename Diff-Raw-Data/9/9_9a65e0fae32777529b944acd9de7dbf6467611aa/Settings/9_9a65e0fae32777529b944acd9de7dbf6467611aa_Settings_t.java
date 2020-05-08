 package com.jamesmonger.XPStrength.util;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.jamesmonger.XPStrength.XPStrength;
 
 public class Settings
 {
 	public static int lowestLevel = Integer.MAX_VALUE;
 
 	public static Map<Integer, Integer> plugin_bonuses_start = new HashMap<Integer, Integer>();
 	public static Map<Integer, Integer> plugin_bonuses_max = new HashMap<Integer, Integer>();
 
	public static void loadSettings(XPStrength xps)
 	{
 		try
 		{
 			XPStrength.xpDrain = true;
 			XPStrength.levelCap = -1;
 			File file = new File(xps.getDataFolder(), "config.txt");
 			if (!file.exists())
 			{
 				FileOutputStream fos = new FileOutputStream(file);
 				fos.flush();
 				fos.close();
 				BufferedWriter bw = new BufferedWriter(new FileWriter(file));
 				bw.write("xp_drain : true");
 				bw.newLine();
 				
 				bw.write("level_cap : -1");
 				bw.newLine();
 				
 				bw.write("30 : 2 : 39");
 				bw.newLine();
 				plugin_bonuses_start.put(30, 2);
 				plugin_bonuses_max.put(39, 39);
 				lowestLevel = 30;
 				
 				bw.write("40 : 3 : 49");
 				bw.newLine();
 				plugin_bonuses_start.put(40, 2);
 				plugin_bonuses_max.put(40, 49);
 				
 				bw.write("50 : 4 : -1");
 				bw.newLine();
 				plugin_bonuses_start.put(50, 2);
 				plugin_bonuses_max.put(50, -1);
 				
 				bw.flush();
 				bw.close();
 			}
 			else
 			{
 				BufferedReader br = new BufferedReader(new FileReader(file));
 				String l;
 				while ((l = br.readLine()) != null)
 				{
 					String[] tokens = l.split(" : ");
 					if (tokens[0].equals("xp_drain"))
 					{
 						XPStrength.xpDrain = Boolean.parseBoolean(tokens[1]);
 					}
 					else if(tokens[0].equals("level_cap"))
 					{
 						XPStrength.levelCap = Integer.parseInt(tokens[1]);
 					}
 					else
 					{
 						if (NumberUtils.isNumeric(tokens[0])
 								&& NumberUtils.isNumeric(tokens[1])
 								&& NumberUtils.isNumeric(tokens[2]))
 						{
 							if (lowestLevel > Integer.parseInt(tokens[0]))
 							{
 								lowestLevel = Integer.parseInt(tokens[0]);
 							}
 							plugin_bonuses_start.put(
 									Integer.parseInt(tokens[0]),
 									Integer.parseInt(tokens[1]));
 							plugin_bonuses_max.put(Integer.parseInt(tokens[0]),
 									Integer.parseInt(tokens[2]));
 						}
 					}
 				}
 				br.close();
 			}
 		}
 		catch (IOException ioe)
 		{
 			ioe.printStackTrace();
 		}
 	}
 
 	public static String getNameForDamage(int damage)
 	{
 		switch (damage)
 		{
 			case 1:
 				return "0.5";
 			case 2:
 				return "1";
 			case 3:
 				return "1.5";
 			case 4:
 				return "2";
 			case 5:
 				return "2.5";
 			case 6:
 				return "3";
 			case 7:
 				return "3.5";
 			case 8:
 				return "4";
 			case 9:
 				return "4.5";
 			case 10:
 				return "5";
 			case 11:
 				return "5.5";
 			case 12:
 				return "6";
 			case 13:
 				return "6.5";
 			case 14:
 				return "7";
 			case 15:
 				return "7.5";
 			case 16:
 				return "8";
 			case 17:
 				return "8.5";
 			case 18:
 				return "9";
 			case 19:
 				return "9.5";
 			case 20:
 				return "10";
 			default:
 				return "0";
 		}
 	}
 }
