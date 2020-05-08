 package net.hexid.hexbot.bot;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 import net.hexid.Utils;
 
 public class Bots {
 	private static TreeMap<String, HashMap<String, String>> bots = new TreeMap<>();
 
 	/**
 	 * Add a new bot to the list of usable bots
 	 * @param id
 	 * @param name
 	 * @param cliClassPath
 	 * @param guiClassPath
 	 * @param fileName
 	 */
 	public static void addBot(String id, String name, String cli, String gui, String file) {
 		HashMap<String, String> bot = new HashMap<>();
 		bot.put("botName", name);
 		bot.put("cliClassPath", cli);
 		bot.put("guiClassPath", gui);
 		bot.put("fileName", file);
 		bots.put(id.toLowerCase(), bot);
 	}
 
 	/**
 	 * Remove all bots whose file does not exist
 	 */
 	public static void removeInvalidBots() {
 		Iterator<Entry<String, HashMap<String, String>>> iter = bots.entrySet().iterator();
 		while(iter.hasNext()) {
 			if(!(new File(getBotFile(iter.next().getKey()))).exists())
 				iter.remove();
 		}
 	}
 
 	/**
	 * @return TreeMap of botNames linked with HashMaps of botData
 	 */
 	public static TreeMap<String, HashMap<String, String>> getBots() {
 		return bots;
 	}
 
 	public static String getBotEnvPath(String oldEnvPath) {
 		String dir = Utils.getPWD().getPath();
 		String sysSpecific = System.getProperty("os.name").toLowerCase();
 		String casperBin = "bin";
 
 		if(sysSpecific.contains("win")) {
 			sysSpecific = "windows";
 			casperBin = "batchbin";
 		} else if(sysSpecific.contains("mac")) {
 			sysSpecific = "macosx";
 		} else {
 			sysSpecific += ("-" + System.getProperty("os.arch"));
 		}
 
 		String phantom = Utils.joinFile(dir, "libs", "phantomjs");
 		String phantomBin = Utils.joinFile(phantom, "bin");
 		String phantomOS = Utils.joinFile(phantomBin, sysSpecific);
 		String casper = Utils.joinFile(dir, "libs", "casperjs", casperBin);
 		return Utils.join(File.pathSeparator, oldEnvPath, phantomOS, phantomBin, phantom, casper);
 	}
 
 	/**
 	 * @return Array of all bot IDs (keys to map)
 	 */
 	public static String[] botIDs() {
 		return bots.keySet().toArray(new String[0]);
 	}
 
 	/**
 	 * @return String representation of the array of bot IDs
 	 */
 	public static String botIDsString() {
 		return java.util.Arrays.toString(botIDs());
 	}
 
 	/**
 	 * @param botID
 	 * @return HashMap with bot's data
 	 */
 	public static HashMap<String, String> getBot(String botID) {
 		return bots.get(botID.toLowerCase()); // get the data associated with a bot
 	}
 
 	/**
 	 * @param botID
 	 * @return true if a key exists for the bot
 	 */
 	public static boolean hasBot(String botID) {
 		return bots.containsKey(botID.toLowerCase());
 	}
 
 	/**
 	 * Get the specified bot's value at a given key
	 * @param botName
 	 * @param key Key to query for in bot's HashMap
 	 * @return botData; null if nonexistant
 	 */
 	public static String getFromBot(String botID, String key) {
 		return (hasBot(botID)) ? getBot(botID).get(key) : null;
 	}
 	/**
	 * @param botName
 	 * @param key
 	 * @param value
 	 * @return True if the bot exists
 	 */
 	public static boolean setInBot(String botID, String key, String value) {
 		if(hasBot(botID)) {
 			getBot(botID).put(key, value);
 			return true;
 		}
 		return false;
 	}
 
 	public static String getBotName(String botID) {
 		return getFromBot(botID, "botName");
 	}
 	public static String getBotCliClassPath(String botID) {
 		return getFromBot(botID, "cliClassPath");
 	}
 	public static String getBotGuiClassPath(String botID) {
 		return getFromBot(botID, "guiClassPath");
 	}
 	public static String getBotFileName(String botID) {
 		return getFromBot(botID, "fileName");
 	}
 
 	public static boolean setBotName(String botID, String newBotName) {
 		return setInBot(botID, "botName", newBotName);
 	}
 	public static boolean setBotCliClassPath(String botID, String newCliClassPath) {
 		return setInBot(botID, "cliClassPath", newCliClassPath);
 	}
 	public static boolean setBotGuiClassPath(String botID, String newGuiClassPath) {
 		return setInBot(botID, "guiClassPath", newGuiClassPath);
 	}
 	public static boolean setBotFileName(String botID, String newBotFileName) {
 		return setInBot(botID, "fileName", newBotFileName);
 	}
 
 	public static String getBotFile(String botID) {
 		return Utils.joinFile(Utils.getPWD().getPath(), "bots", getBotFileName(botID));
 	}
 	public static String getBotFile(Bot bot) {
 		return getBotFile(bot.getBotID());
 	}
 }
