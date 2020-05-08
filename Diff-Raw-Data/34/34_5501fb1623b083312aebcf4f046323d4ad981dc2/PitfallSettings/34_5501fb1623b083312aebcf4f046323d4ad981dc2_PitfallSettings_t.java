 package com.bukkit.jason.pitfall;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
 import org.bukkit.util.config.Configuration;
 
 public class PitfallSettings
 {
 
 	/**
 	 * Settings
 	 */
 
 	public static int pitItem = 82;
 	public static boolean redstonePitEnabled = false;
 	public static boolean redstoneTriggerCorner = false;
 	public static int redstonePitItem = 45;
 	public static String[] blackList = new String[0];
 
 	/**
 	 * Bukkit config class
 	 */
 	public static Configuration config = null;
	private static Pitfall plugin;
 	/**
 	 * Load and parse the YAML config file
 	 */
	public static void load(Pitfall p)
 	{
		plugin=p;
 
 		File dataDirectory = new File("plugins" + File.separator + "Pitfall" + File.separator);
 
 		dataDirectory.mkdirs();
 
 		File file = new File("plugins" + File.separator + "Pitfall", "config.yml");
 
 		if (file.exists())
 		{
 			config = new Configuration(file);
 			config.load();
 			String version = "";
 			version = config.getString("pitfall.version", version);
 			setSettings();
			if (version == "" || !(version.equals(plugin.pdfFile.getVersion())))
 			{
 				System.out.println("Version out of date, reconfigurating");
 				writeFile(file);
 			}
 		}
		else
 		{
 			writeFile(file);
			config = new Configuration(file);
			config.load();
 		}
 		config.load();
 		setSettings();
 	}
 
 	private static void writeFile(File file)
 	{
 		File temp = new File("plugins" + File.separator + "Pitfall", "temp.yml");
 		BufferedWriter bw;
 		try
 		{
 			Configuration tempConfig;
 			tempConfig = new Configuration(temp);
 			tempConfig.load();
			tempConfig.setProperty("pitfall.version", plugin.pdfFile.getVersion());
 			tempConfig.setProperty("pitfall.pitItem", pitItem);
 			tempConfig.setProperty("pitfall.block.blackList", "[]");
 			tempConfig.setProperty("pitfall.redstone.enabled", redstonePitEnabled);
 			tempConfig.setProperty("pitfall.redstone.pitItem", redstonePitItem);
 			tempConfig.setProperty("pitfall.redstone.triggerCorner", redstoneTriggerCorner);
 
 			tempConfig.save();
 
 			Scanner sc = new Scanner(temp);
 
 			bw = new BufferedWriter(new FileWriter(file));
 			bw.write("#pitfall.pitItem defines the block that acts as a pressure based trap. Defaults to " + pitItem);
 			bw.newLine();
 			bw.write("#pitfall.block.blackList defines surface blocks that do not trigger pressure trap. Format: [32,12,5,123]");
 			bw.newLine();
 			bw.write("#pitfall.redstone.enabled is used to enable redstone triggered traps. Defaults to " + redstonePitEnabled);
 			bw.newLine();
 			bw.write("#pitfall.redstone.pitItem defines the block that acts as the redstone trigged trap. Defaults to " + redstonePitItem);
 			bw.newLine();
 			bw.write("#pitfall.redstone.triggerCorner specifies whether trap blocks to the diagonal of the powered block should be destroyed. Defaults to " + redstoneTriggerCorner);
 			bw.newLine();
 			bw.newLine();
 			while (sc.hasNextLine())
 			{
 //				String l=sc.nextLine();
 //				System.out.println(l);
 				bw.write(sc.nextLine());
 				bw.newLine();
 			}
 			bw.close();
 			temp.delete();
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Sets the internal variables
 	 */
 	private static void setSettings()
 	{
 
 		pitItem = config.getInt("pitfall.pitItem", pitItem);
 		String blackListStr = "";
 		blackListStr = config.getString("pitfall.block.blackList", blackListStr);
 		redstonePitEnabled = config.getBoolean("pitfall.redstone.enabled", redstonePitEnabled);
 		redstoneTriggerCorner = config.getBoolean("pitfall.redstone.triggerCorner", redstoneTriggerCorner);
 		redstonePitItem = config.getInt("pitfall.redstone.pitItem", redstonePitItem);
 
 		if (blackListStr.startsWith("[") && blackListStr.endsWith("]"))
 		{
 			blackListStr = blackListStr.substring(1, blackListStr.length() - 1);
 			blackList = blackListStr.split(",");
 		}
 		else
 		{
 			System.out.println("WARNING: pitfall.block.blackList is malformed! Must be in following format: [blockId, blockId, blockId]");
 		}
 
 	}
 
 }
