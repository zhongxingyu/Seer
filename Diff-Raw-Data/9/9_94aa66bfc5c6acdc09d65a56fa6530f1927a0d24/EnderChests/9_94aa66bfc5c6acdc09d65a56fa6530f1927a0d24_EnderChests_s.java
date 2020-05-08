 package com.newliberty.enderchests;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class EnderChests extends JavaPlugin {
 	public static EnderChests plugin;
 	public HashMap<String, Integer> playerChestCount = new HashMap<String, Integer>();
 	
 	public void onEnable(){
 		this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
 		plugin = this;
 		
 		loadPlayers();
 		
 		PluginDescriptionFile pdfFile = getDescription();
 		this.getLogger().info(pdfFile.getName() + ", Version " + pdfFile.getVersion() + ", Has Been Enabled!");
 	}
 	
 	public void onDisable(){
 		dumpPlayers();
 		
 		PluginDescriptionFile pdfFile = getDescription();
 		this.getLogger().info(pdfFile.getName() + ", Version "
 				+ pdfFile.getVersion() + ", Has Been Disabled!");
 	}
 	
 	/**
 	 * Crashes Server Removed!
 	 *//*
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		//TODO: Create command "/ecp list <player>" with permission enderchests.admin
 		if (commandLabel.equalsIgnoreCase("ecp")){
 			if (args != null && args[0].length() == 1 && args[0].equalsIgnoreCase("list") && sender.hasPermission("nlenderchest.admin")){
 				
 				return true;
 			}
 			return true;
 		}
 		return false;
 	
 	}*/
 	
 	/**
 	 * @author OstlerDev
 	 * 
 	 * This method saves the enderchest to a yml file called x,y,z.yml
 	 * it should be called when a player exits an ender chest.
 	 * @throws IOException 
 	 */
 	public void saveEnderChest(Location loc, Inventory inventory, Player p){
 		File packFile = getPackFile(loc);
 		FileConfiguration packConfig = YamlConfiguration.loadConfiguration(packFile);
 
         packConfig.set("inventory", inventory.getContents());
        if (packConfig.get("owner") == null) packConfig.set("owner", p.getDisplayName());
         try {
             packConfig.save(packFile);
         } catch (IOException e) {
             this.getLogger().info("Failed to save inventory...");
         }
     }
 
 	/**
 	 * @author OstlerDev
 	 * 
 	 * @This method loads the enderchest from a yml file called x,y,z.yml
 	 * it should be called when a player opens an ender chest.
 	 * 
 	 */
 	public Inventory loadEnderChest(Location loc, Inventory inventory, Player p){
 		Inventory inv = inventory;
 		File packFile = getPackFile(loc);
 		if (packFile.exists()){
         FileConfiguration packConfig = YamlConfiguration.loadConfiguration(packFile);
         List<?> list = packConfig.getList("inventory");
         if (list != null) {
             for (int i = 0; i < Math.min(list.size(), inv.getSize()); i += 1) {
                 inv.setItem(i, (ItemStack)list.get(i));
             }
         }
     }
 		return inv;
 	}
 
 	public static File getPackFile(Location loc) {
         return new File(plugin.getDataFolder(), loc.toString() + ".yml"); // TODO: formatting, 0000
     }
 	
 	public boolean getOwner(Location loc, Player p){
 		File packFile = getPackFile(loc);
 		FileConfiguration packConfig = YamlConfiguration.loadConfiguration(packFile);
 		String s = packConfig.getString("owner");
 		if (s != null && s.equalsIgnoreCase(p.getDisplayName())){
 			return true;
 		}
 		return false;
 	}
 	
 	public String getOwnerName(Location loc, Player p){
 		File packFile = getPackFile(loc);
 		FileConfiguration packConfig = YamlConfiguration.loadConfiguration(packFile);
 		String s = packConfig.getString("owner");
 		return s;
 	}
 	
 	public boolean dumpPlayers() {
 		final File file = new File("plugins/EnderChestProtect/playerChestCount.dump");
 		if (!file.exists()){
 			try {
 				file.createNewFile();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return false;
 			}
 		}
 		BufferedWriter bw;
 		try {
 			bw = new BufferedWriter(new FileWriter(file));
 			for (final String p : playerChestCount.keySet()) {
 				bw.write(p + "," + playerChestCount.get(p));
 				bw.newLine();
 			}
 			bw.flush();
 			bw.close();
 			return true;
 		} catch (final IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public boolean loadPlayers() {
 		final File file = new File("plugins/EnderChestProtect/playerChestCount.dump");
 		if (!file.exists()){
 			try {
 				file.createNewFile();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return false;
 			}
 		}
 		try {
 			final BufferedReader br = new BufferedReader(new InputStreamReader(
 					new FileInputStream(file)));
 			String l;
 			while ((l = br.readLine()) != null) {
 				final String[] args = l.split("[,]", 2);
 				if (args.length != 2)
 					continue;
 				final String p = args[0].replaceAll(" ", "");
 				final String b = args[1].replaceAll(" ", "");
 				plugin.playerChestCount.put(p, Integer.parseInt(b));
 			}
 			br.close();
 			file.delete();
 			return true;
 		} catch (final IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 }
