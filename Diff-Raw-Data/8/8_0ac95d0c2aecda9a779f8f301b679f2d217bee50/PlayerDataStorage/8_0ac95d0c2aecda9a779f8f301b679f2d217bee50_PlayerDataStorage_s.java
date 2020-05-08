 package coolawesomeme.basics_plugin;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Scanner;
 
 import org.bukkit.OfflinePlayer;
 import org.bukkit.entity.Player;
 
 public class PlayerDataStorage {
 	
 	private static String dataFolderPath;
 	
 	public static void setDataFolder(String path){
 		dataFolderPath = path;
 	}
 	
 	public static void makePlayerDataFile(String playerName){
 		File f = new File(dataFolderPath + "/" + playerName + ".dat");
 		if(!f.exists()){
 			try {
 				f.createNewFile();
 				FileWriter lol = new FileWriter(f);
 				lol.write("");
 				lol.flush();
 				lol.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/*public static int getPlayerAFactor(Player player){
 		String content = "";
 		try {
 			content = new Scanner(new File(dataFolderPath + "/" + player.getName() + ".dat")).useDelimiter("\\Z").next();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		String[] lulz = content.split("\\n");
 		return Integer.parseInt(lulz[0]);
 	}
 	
 	public static int getPlayerAFactor(OfflinePlayer player){
 		String content = "";
 		try {
 			content = new Scanner(new File(dataFolderPath + "/" + player.getName() + ".dat")).useDelimiter("\\Z").next();
 		} catch (FileNotFoundException e) {
 			makePlayerDataFile(player.getName());
 			try {
 				content = new Scanner(new File(dataFolderPath + "/" + player.getName() + ".dat")).useDelimiter("\\Z").next();
 			} catch (FileNotFoundException e1) {
 				e1.printStackTrace();
 			}
 		}
 		String[] lulz = content.split("\\n");
 		return Integer.parseInt(lulz[0]);
 	}*/
 	
 	public static int getPlayerServerHelpCount(Player player){
 		String content = "";
 		try {
			content = new Scanner(new File(dataFolderPath + "/" + player.getName() + ".dat")).useDelimiter("\\Z").next();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		String[] lulz = content.split("\\n");
 		return Integer.parseInt(lulz[0]);
 	}
 	
 	public static int getPlayerServerHelpCount(OfflinePlayer player){
 		String content = "";
 		try {
			content = new Scanner(new File(dataFolderPath + "/" + player.getName() + ".dat")).useDelimiter("\\Z").next();
 		} catch (FileNotFoundException e) {
 			makePlayerDataFile(player.getName());
 			try {
				content = new Scanner(new File(dataFolderPath + "/" + player.getName() + ".dat")).useDelimiter("\\Z").next();
 			} catch (FileNotFoundException e1) {
 				e1.printStackTrace();
 			}
 		}
 		String[] lulz = content.split("\\n");
 		return Integer.parseInt(lulz[0]);
 	}
 	
 	/*public static void setPlayerAFactor(Player player, int afactor){
 		File f = new File(dataFolderPath + "/" + player.getName() + ".dat");
 		try {
 			int playerHelpCount = getPlayerServerHelpCount(player);
 			FileWriter lol = new FileWriter(f);
 			lol.write(afactor + "\n");
 			lol.write(playerHelpCount + "");
 			lol.flush();
 			lol.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void setPlayerAFactor(OfflinePlayer player, int afactor){
 		File f = new File(dataFolderPath + "/" + player.getName() + ".dat");
 		try {
 			int playerHelpCount = getPlayerServerHelpCount(player);
 			FileWriter lol = new FileWriter(f);
 			lol.write(afactor + "\n");
 			lol.write(playerHelpCount + "");
 			lol.flush();
 			lol.close();
 		} catch (IOException e) {
 			makePlayerDataFile(player.getName());
 			try {
 				int playerHelpCount = getPlayerServerHelpCount(player);
 				FileWriter lol = new FileWriter(f);
 				lol.write(afactor + "\n");
 				lol.write(playerHelpCount + "");
 				lol.flush();
 				lol.close();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		}
 	}*/
 	
 	public static void setPlayerServerHelpCount(Player player, int playerHelpCount){
 		File f = new File(dataFolderPath + "/" + player.getName() + ".dat");
 		try {
 			FileWriter lol = new FileWriter(f);
 			lol.write(playerHelpCount + "");
 			lol.flush();
 			lol.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void setPlayerServerHelpCount(OfflinePlayer player, int playerHelpCount){
 		File f = new File(dataFolderPath + "/" + player.getName() + ".dat");
 		try {
 			FileWriter lol = new FileWriter(f);
 			lol.write(playerHelpCount + "");
 			lol.flush();
 			lol.close();
 		} catch (IOException e) {
 			makePlayerDataFile(player.getName());
 			try {
 				FileWriter lol = new FileWriter(f);
 				lol.write(playerHelpCount + "");
 				lol.flush();
 				lol.close();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		}
 	}
 }
