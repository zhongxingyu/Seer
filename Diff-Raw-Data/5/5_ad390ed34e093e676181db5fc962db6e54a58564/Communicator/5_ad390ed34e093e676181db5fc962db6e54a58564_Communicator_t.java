 package me.werner291.navigator;
 
 import java.io.File;
 import java.util.HashMap;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 public class Communicator {
 	
 	static HashMap<Instruction.InstructionType, String> instructionTemplates = new HashMap<Instruction.InstructionType, String>();
 	static HashMap<String, String> messages = new HashMap<String, String>();
 	static HashMap<Cardinal, String> cardinalStrings = new HashMap<Cardinal, String>();
 
 	public static void sendNavigationInstruction(Player player, Instruction inst) {
 		String message1 = instructionTemplates.get(inst.instructionType);
 		if (message1 == null) {
 			System.out.println("[Navigator] WARNING! Instruction template for "+inst.instructionType+" is missing from language file!");
 			player.sendMessage("[Navigator] WARNING! Instruction template for "+inst.instructionType+" is missing from language file!");
 			return;
 		}
 		message1 = message1.replaceAll("CARDINALDIR", cardinalStrings.get(inst.cardinalDir));
 		String[] lines = message1.split("/nl");
 		
 		for (int i=0;i<lines.length;i++) player.sendMessage("[Navigator] "+lines[i]);
 	}
 
 	public static void sendCompass(Player player, Cardinal cardinal) {
 		player.sendMessage("[Navigator] "+cardinalStrings.get(cardinal));
 	}
 
 	public static void Initialise(String language) {
 		if (language == null) language = "English";
 		else System.out.println("[Navigator] Selected "+language+" as language.");
 		
 		File languageFile = new File("plugins/Navigator/"+language+".yml");
 		
 		if (!languageFile.exists()) System.out.println("Not found: "+languageFile.getPath());
 		
 		FileConfiguration languageConfig = YamlConfiguration.loadConfiguration(languageFile);
 		 
 		cardinalStrings.put(Cardinal.NORTH, languageConfig.getString("North"));
 		cardinalStrings.put(Cardinal.EAST, languageConfig.getString("East"));
 		cardinalStrings.put(Cardinal.WEST, languageConfig.getString("West"));
 		cardinalStrings.put(Cardinal.SOUTH, languageConfig.getString("South"));
 		cardinalStrings.put(Cardinal.NORTH_WEST, languageConfig.getString("North_West"));
 		cardinalStrings.put(Cardinal.NORTH_EAST, languageConfig.getString("North_East"));
 		cardinalStrings.put(Cardinal.SOUTH_WEST, languageConfig.getString("South_West"));
 		cardinalStrings.put(Cardinal.SOUTH_EAST, languageConfig.getString("South_East"));
 		
 		instructionTemplates.put(Instruction.InstructionType.START, languageConfig.getString("Start"));
 		instructionTemplates.put(Instruction.InstructionType.END, languageConfig.getString("End"));
 		instructionTemplates.put(Instruction.InstructionType.GO_STRAIGHT, languageConfig.getString("Go_Straight"));
 		instructionTemplates.put(Instruction.InstructionType.TURN_LEFT, languageConfig.getString("Turn_Left"));
 		instructionTemplates.put(Instruction.InstructionType.TURN_RIGHT, languageConfig.getString("Turn_Right"));
 		instructionTemplates.put(Instruction.InstructionType.TURN_AROUND, languageConfig.getString("Turn_Around"));
 		
 		messages.put("OffrouteA",languageConfig.getString("OffrouteA"));
 		messages.put("OffrouteB",languageConfig.getString("OffrouteB"));
 		messages.put("NoRoute",languageConfig.getString("NoRoute"));
 		messages.put("NoMapOfWorld",languageConfig.getString("NoMapOfWorld"));
 		messages.put("NavStart",languageConfig.getString("NavStart"));
 		messages.put("TooFarFromRoad",languageConfig.getString("TooFarFromRoad"));
 		messages.put("InGameOnlyCommand",languageConfig.getString("InGameOnlyCommand"));
 		messages.put("RouteCalc",languageConfig.getString("RouteCalc"));
 		messages.put("DestCoordsFarFromRoad",languageConfig.getString("DestCoordsFarFromRoad"));
		
		messages.put("DestinationExists",languageConfig.getString("DestinationExists"));
 	}
 
 	public static void sendOffRouteWarningA(Player player) {
 		String message1 = messages.get("OffrouteA");
 		
 		if (message1 == null) {
 			System.out.println("[Navigator] WARNING! Off route warning A missing from language file.");
 			return;
 		}
 		
 		String[] lines = message1.split("/nl");
 		for (int i=0;i<lines.length;i++) player.sendMessage("[Navigator] "+lines[i]);
 	}
 	
 	public static void sendOffRouteWarningB(Player player) {
 		String message1 = messages.get("OffrouteB");
 		
 		if (message1 == null) {
 			System.out.println("[Navigator] WARNING! Off route warning A missing from language file.");
 			return;
 		}
 		
 		String[] lines = message1.split("/nl");
 		for (int i=0;i<lines.length;i++) player.sendMessage("[Navigator] "+lines[i]);
 	}
 
 	public static void tellNoRoute(Player player, String destname) {
 		String message1 = messages.get("NoRoute");
 		
 		if (message1 == null) {
 			System.out.println("[Navigator] WARNING! NoRoute missing from language file.");
 			return;
 		}
 		message1 = message1.replaceAll("DESTNAME",destname );
 		String[] lines = message1.split("/nl");
 		for (int i=0;i<lines.length;i++) player.sendMessage("[Navigator] "+lines[i]);
 	}
 
 	public static void tellNoMap(Player player, String worldname) {
 		String message1 = messages.get("NoMapOfWorld");
 		
 		if (message1 == null) {
 			System.out.println("[Navigator] WARNING! NoMapOfWorld warning missing from language file.");
 			return;
 		}
 		message1 = message1.replaceAll("WORLDNAME", worldname);
 		String[] lines = message1.split("/nl");
 		for (int i=0;i<lines.length;i++) player.sendMessage("[Navigator] "+lines[i]);
 	}
 
 	public static void tellMessage(CommandSender recipient, String messageName) {
 		
 		String message1 = messages.get(messageName);
 		
 		if (message1 == null) {
 			System.out.println("[Navigator] WARNING! "+messageName+" missing from language file.");
 			return;
 		}
 		String[] lines = message1.split("/nl");
 		for (int i=0;i<lines.length;i++) recipient.sendMessage("[Navigator] "+lines[i]);
 	}
 
 	public static void tellNavStart(Player player, String destname) {
 		String message1 = messages.get("NavStart");
 		
 		if (message1 == null) {
 			System.out.println("[Navigator] WARNING! NavStart missing from language file.");
 			return;
 		}
 		message1 = message1.replaceAll("DESTNAME",destname );
 		String[] lines = message1.split("/nl");
 		for (int i=0;i<lines.length;i++) player.sendMessage("[Navigator] "+lines[i]);
 	}
 
 	public static void tellRouteCalc(Player player, String destname) {
 		String message1 = messages.get("RouteCalc");
 		
 		if (message1 == null) {
 			System.out.println("[Navigator] WARNING! RouteCalc missing from language file.");
 			return;
 		}
 		message1 = message1.replaceAll("DESTNAME",destname );
 		String[] lines = message1.split("/nl");
 		for (int i=0;i<lines.length;i++) player.sendMessage("[Navigator] "+lines[i]);
 	}
 	
 	public static void tellDestCoordsFarFromRoad(Player player, int distance){
 		String message1 = messages.get("DestCoordsFarFromRoad");
 		
 		if (message1 == null) {
 			System.out.println("[Navigator] WARNING! DestCoordsFarFromRoad missing from language file.");
 			return;
 		}
 		
 		message1 = message1.replaceAll("DISTANCE",Integer.toString(distance));
 		String[] lines = message1.split("/nl");
 		player.sendMessage("Far!");
 		for (int i=0;i<lines.length;i++) player.sendMessage("[Navigator] "+lines[i]);
 	}
 
 	public static void tellDestionationExists(Player player, String destname) {
 		String message1 = messages.get("DestinationExists");
 		
 		if (message1 == null) {
			System.out.println("[Navigator] WARNING! DestinationExists missing from language file.");
 			return;
 		}
 		
 		message1 = message1.replaceAll("DESTNAME",destname );
 		String[] lines = message1.split("/nl");
 		
 		for (int i=0;i<lines.length;i++) player.sendMessage("[Navigator] "+lines[i]);
 	}
 	
 	
 }
