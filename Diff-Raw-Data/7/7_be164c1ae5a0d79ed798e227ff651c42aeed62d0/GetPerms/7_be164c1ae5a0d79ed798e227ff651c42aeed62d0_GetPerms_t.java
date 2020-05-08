 package com.github.GetPerms.GetPerms;
 
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.lang.StringBuilder;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class GetPerms extends JavaPlugin {
 
 	public String gpversion;
 	WriteToFile WTF;
 	ConfigHandler ConfHandler;
 	PluginManager pm = Bukkit.getServer().getPluginManager();
 	public Plugin[] pluginlist;
 	private File file1 = new File("pnodes.txt");
 	private File file2 = new File("pnodesfull.txt");
	private File rm = new File("plugins/GetPerms/ReadMe.txt");
	private File cl = new File("plugins/GetPerms/Changelog.txt");
 	private File uf = new File("update");
	private File updt = new File("update/GetPerms.jar");
 	public PrintWriter pw1;
 	public PrintWriter pw2;
 	public static PluginDescriptionFile pdf;
 	public static Configuration cfg;
 
 	@Override
 	public void onEnable() { 
 		WTF = new WriteToFile(this);
 		cfg = this.getConfig();
 		if (cfg.getBoolean("firstRun")) {
 			cfg.set("firstRun", false);
 			try {
 				dlFile("https://raw.github.com/GetPerms/GetPerms/master/Changelog.txt", cl);
 				dlFile("https://raw.github.com/GetPerms/GetPerms/master/ReadMe.txt", rm);
 			} catch (MalformedURLException e) {
 				cfg.set("firstRun", true);
 				PST(e);
 				getLogger().warning("Error downloading readme and changelog!");
 			} catch (IOException e) {
 				cfg.set("firstRun", true);
 				PST(e);
 				getLogger().warning("Error downloading readme and changelog!");
 			}
 		}
 		pdf = this.getDescription();
 		gpversion = pdf.getVersion();
 		gpCreateCfg();
 		//gpCreateMisc();
 		try {
 			pw1 = new PrintWriter(new FileWriter(file1));
 			pw2 = new PrintWriter(new FileWriter(file2));
 		} catch (IOException e) {
 			PST(e);
 		}
 		pluginlist = pm.getPlugins();
 		getLogger().info(new StringBuilder().append("GetPerms ").append(gpversion).append(" enabled!").toString());
 		getLogger().info("GetPerms is the work of Smiley43210, with the help of");
 		getLogger().info("Tahkeh, wwsean08, desmin88, and many others. Thanks!");
 		if (cfg.getBoolean("autoUpdate", true)) {
 			getLogger().info("Checking for updates...");
 			gpCheckForUpdates();
 		}
 		if (cfg.getBoolean("autoGen", true)) {
 			getLogger().info("Retrieved plugin list!");
 			getLogger().info("Retrieving permission nodes...");
 			for (Plugin p : pluginlist) {
 				try {
 					WTF.Write(p);
 				} catch (IOException e) {
 					PST(e);
 					getLogger().warning("Error retrieving plugin list!");
 				}
 				if (!WTF.plist.isEmpty()) {
 					pw2.println("");
 				}
 			}
 			pw1.close();
 			pw2.close();
 			getLogger().info("Compiled permission nodes into 'pnodes.txt' and");
 			getLogger().info("'pnodesfull.txt' in the server root folder.");
 			this.saveConfig();
 			if (cfg.getBoolean("disableOnFinish", true))
 				getServer().getPluginManager().disablePlugin(this);
 		}
 	}
 	@Override
 	public void onDisable() { 
 		ConfHandler = new ConfigHandler(this);
 		ConfHandler.change();
 		getLogger().info(new StringBuilder().append("GetPerms ").append(gpversion).append(" unloaded").toString());
 	}
 
 	private final void gpCheckForUpdates() {
 		String check = "https://raw.github.com/GetPerms/GetPerms/master/ver";
 		try {
 			URL client = new URL(check);
 			BufferedReader buf = new BufferedReader(new InputStreamReader(client.openStream()));
 			String line = buf.readLine();
 			if(gpnewer(gpversion, line))
 				getLogger().info("Newest GetPerms version" + line + " is available for download, you can get it at https://raw.github.com/GetPerms/GetPerms/master/GetPerms.jar or http://dev.bukkit.org/server-mods/getperms/files");
 				if (cfg.getBoolean("autoDownload", true)) {
 					if (!uf.exists()) {
 						uf.mkdir();
 					}
 					dlFile("https://raw.github.com/GetPerms/GetPerms/master/GetPerms.jar", updt);
 					getLogger().info("Newest version of GetPerms is located in");
 					getLogger().info("'server_root_dir/update/GetPerms.jar'.");
 				}
 			else
 				getLogger().info("You have the latest version!");
 		} catch (MalformedURLException e) {
 			PST(e);
 			getLogger().warning("Unable to check for updates.");
 		} catch (IOException e) {
 			PST(e);
 			getLogger().warning("Unable to check for updates.");
 		}
 	}
 
 	public final void PST(IOException e) {
 		if (cfg.getBoolean("debugMode", true)) { 
 			e.printStackTrace();
 		}
 	}
 	
 	public final void PST(MalformedURLException e) {
 		if (cfg.getBoolean("debugMode", true)) { 
 			e.printStackTrace();
 		}
 	}
 
 	private final boolean gpnewer(String current, String check) {
 		boolean result = false;
 		String[] currentVersion = current.split("\\.");
 		String[] checkVersion = check.split("\\.");
 		int i = Integer.parseInt(currentVersion[0]);
 		int j = Integer.parseInt(checkVersion[0]);
 		if(i>j)
 			result = false;
 		else if(i==j){
 			i = Integer.parseInt(currentVersion[1]);
 			j = Integer.parseInt(checkVersion[1]);
 			if(i>j)
 				result = false;
 			else if(i == j){
 				i = Integer.parseInt(currentVersion[2]);
 				j = Integer.parseInt(checkVersion[2]);
 				if(i >= j)
 					result = false;
 				else
 					result = true;
 			}else
 				result = true;
 		}else
 			result = true;
 		return result;
 	}
 
 	private final void gpCreateCfg() {
 		cfg = this.getConfig();
 		cfg.options().copyDefaults(true);
 		this.saveConfig();
 		//Getting ready for option changes
 	}
 
 	public static void dlFile(String url, File file) throws MalformedURLException, IOException {		 
 		BufferedInputStream in = new BufferedInputStream(new 
 		java.net.URL(url).openStream());
 		FileOutputStream fos = new FileOutputStream(file);
 		BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
 		byte[] data = new byte[1024];
 		int x=0;
 		while((x=in.read(data,0,1024))>=0)
 		{
 			bout.write(data,0,x);
 		}
 		bout.close();
 		in.close();
 	}
 
 	@SuppressWarnings("unused")
 	private final void gpCreateMisc() {
 		if (!new File(getDataFolder(), "plugins.yml").exists()) {
 			try {
 			getDataFolder().mkdir();
 			new File(getDataFolder(), "plugins.yml").createNewFile();
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.out.println("[GetPerms] Error occurred while creating plugins.yml (plugin list)!");
 				getServer().getPluginManager().disablePlugin(this);
 				return;
 			}
 		}
 	}
 
 }
