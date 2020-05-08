 package com.lala.wordrank;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import com.lala.wordrank.Listeners.PlayerListen;
 import com.lala.wordrank.misc.PermHandle;
 import com.lala.wordrank.misc.Perms;
 import com.lala.wordrank.misc.RedeemType;
 import com.lala.wordrank.sql.MySQL;
 import com.lala.wordrank.sql.Query;
 import com.lala.wordrank.sql.SQLType;
 import com.lala.wordrank.sql.SQLWord;
 import com.lala.wordrank.sql.SQLite;
 
 import de.bananaco.permissions.Permissions;
 import de.bananaco.permissions.worlds.WorldPermissionsManager;
 
 public class WordRank extends JavaPlugin {
 	
 	public WorldPermissionsManager bperm;
 	public PermissionManager pex;
 	
 	public SQLite sqlite;
 	public MySQL mysql;
 	
 	public boolean bpermEnabled = false;
 	public boolean pexEnabled = false;
 	public boolean update = false;
 	
 	public Perms perms = Perms.Unknown;
 	public RedeemType redeemtype = RedeemType.Unknown;
 	public SQLType sqtype = SQLType.Unknown;
 	
 	private Config config;
 	private PluginDescriptionFile desc;
 	private PluginManager pm;
 	
 	public void onEnable(){
 		this.config = new Config(this);
 		this.desc = this.getDescription();
 		this.pm = this.getServer().getPluginManager();
 		
 		pm.registerEvent(Type.PLAYER_CHAT, new PlayerListen(this), Priority.Normal, this);
 		pm.registerEvent(Type.PLAYER_JOIN, new PlayerListen(this), Priority.Normal, this);
 		
 		if (config.getCheckForUpdates()) if (checkForUpdate()) send("New update for WordRank!");
 		if (!setupSQL()) return;
 		if (!setupPermissions()) return;
 		if (!checkRedeemType()) return;
 		
 		send("is now enabled, version: "+desc.getVersion());
 	}
 	
 	public void onDisable(){
 		if (sqtype.equals(SQLType.SQLite))
 			sqlite.close();
 		if (sqtype.equals(SQLType.MySQL))
 			mysql.close();
 		send("is now disabled!");
 	}
 	
 	private boolean setupSQL(){
 		send("Setting up SQL support");
 		FileConfiguration cfg = this.getConfig();
 		sqtype = config.getSQLType();
 		
 		if (sqtype.equals(SQLType.SQLite)){
 			this.sqlite = new SQLite(this, "wordrank_words", this.getDataFolder().getAbsolutePath());
 			send("Checking tables..");
 			if (!sqlite.tableExists("wordrank")){
 				send("Table 'wordrank' not found. Creating..");
 				sqlite.createTable(Query.CREATE_TABLE_IF_NOT_EXISTS);
 				send("Table 'wordrank' created!");
 				send("SQLite will be used.");
 				return true;
 			}else{
 				send("Table 'wordrank' found.");
 				send("SQLite will be used.");
 				return true;
 			}
 		}
 		
 		if (sqtype.equals(SQLType.MySQL)){
 			this.mysql = new MySQL(this, 
 					cfg.getString("sql-config.hostname"),
 					cfg.getString("sql-config.portnumber"),
 					cfg.getString("sql-config.database"),
 					cfg.getString("sql-config.username"),
 					cfg.getString("sql-config.password"));
 			send("Checking tables..");
 			if (!mysql.tableExists("wordrank")){
 				send("Table 'wordrank' not found. Creating..");
 				mysql.createTable(Query.CREATE_TABLE_IF_NOT_EXISTS);
 				send("Table 'wordrank' created!");
 				send("MySQL will be used.");
 				return true;
 			}else{
 				send("Table 'wordrank' found.");
 				send("MySQL will be used.");
 				return true;
 			}
 		}
 		
 		if (sqtype.equals(SQLType.Unknown)){
 			sendErr("Config is set to the unknown SQLType '"+config.sqlType()+"' WordRank will now disable.");
 			this.setEnabled(false);
 			return false;
 		}
 		send("There appears to be a deep problem here..");
 		return false;
 	}
 	
 	private boolean setupPermissions() {
 		send("Checking permission plugins");
 		if (getServer().getPluginManager().isPluginEnabled("bPermissions")){
 	    	bperm = Permissions.getWorldPermissionsManager();
 	    	bpermEnabled = true;
 	    	send("bPermissions is detected");
 		}else{
 	    	send("bPermissions is not detected");
 	    	bpermEnabled = false;
 		}
 	    
 	    if (getServer().getPluginManager().isPluginEnabled("PermissionsEx")){
 	    	pex = PermissionsEx.getPermissionManager();
 	    	pexEnabled = true;
 	    	send("PEX is detected");
 	    }else{
 	    	send("PEX is not detected");
 	    	pexEnabled = false;
 	    }
 	    
 	    perms = config.getPerms();
 	    
 	    if (bpermEnabled && !pexEnabled && !perms.equals(Perms.bPermissions)){
 	    	send("bPermissions is enabled, and PEX is not detected, however the config has PEX selected. Now changing selection to bPermissions.");
 	    	this.getConfig().set("perm-plugin", "bPermissions");
 	    	this.saveConfig();
 	    }
 	    
 	    if (!bpermEnabled && pexEnabled && !perms.equals(Perms.PEX)){
 	    	send("PEX is enabled, and bPermissions is not detected, however the config has bPermissions selected. Now changing selection to PEX.");
 	    	this.getConfig().set("perm-plugin", "PEX");
 	    	this.saveConfig();
 	    }
 	    
 	    perms = config.getPerms();
 	    
 	    if (!bpermEnabled && !pexEnabled){
 	    	sendErr("No compatible permission plugins found. WordRank will now disable.");
 	    	this.setEnabled(false);
 	    	return false;
 	    }
 	    
 	    if (perms.equals(Perms.bPermissions) && !bpermEnabled){
 	    	sendErr("Config is set to bPermissions, however bPermissions is not detected. WordRank will now disable.");
 	    	this.setEnabled(false);
 	    	return false;
 	    }
 	    
 	    if (perms.equals(Perms.PEX) && !pexEnabled){
 	    	sendErr("Config is set to PEX, however PEX is not detected. WordRank will now disable.");
 	    	this.setEnabled(false);
 	    	return false;
 	    }
 	    
 	    if (perms.equals(Perms.GroupManager)){
 	    	sendErr("Config is set to GroupManager, however GroupManager is not supported yet. WordRank will now disable.");
 	    	this.setEnabled(false);
 	    	return false;
 	    }
 	    
 	    if (perms.equals(Perms.Unknown)){
 	    	sendErr("Config is set to the unknown permission plugin '"+config.permPlugin()+"' WordRank will now disable.");
 	    	this.setEnabled(false);
 	    	return false;
 	    }
 	    if (config.getPerms().equals(Perms.bPermissions)) send("bPermissions will be used.");
 	    if (config.getPerms().equals(Perms.PEX)) send("PEX will be used.");
 	    return true;
 	}
 	
 	private boolean checkRedeemType(){
 		redeemtype = config.getRedeemType();
 		
 		if (redeemtype.equals(RedeemType.Unknown)){
 			sendErr("Config is set to the unknown redeem type '"+config.redeemType()+"' WordRank will now disable.");
 			this.setEnabled(false);
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean checkForUpdate(){
 		String ver = null;
 		try {
 			URL url = new URL("http://www.craftmod.net/jar/WordRank/version.txt");
 			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
 			ver = br.readLine();
 			br.close();
 		} catch (MalformedURLException e){
 			sendErr("MalformedURLException while checking for update at: "+getClass().getPackage().getName());
 			e.printStackTrace();
 		} catch (IOException e){
 			sendErr("IOException while checking for update at: "+getClass().getPackage().getName());
 			e.printStackTrace();
 		}
 		if (ver.equals(null))
 			return false;
		else if (ver.equals(desc.getVersion()) || Double.parseDouble(ver) <= Double.parseDouble(desc.getVersion()))
 			return false;
 		else
 			this.update = true;
 			return true;			
 	}
 	
 	public void send(String message){
 		System.out.println("[WordRank] "+message);
 	}
 	
 	public void send(String message, int times){
 		for (int i = times; i > 0; i--){
 			send(message);
 		}
 	}
 	
 	public void sendErr(String message){
 		System.err.println("[WordRank] [Error] "+message);
 	}
 	
 	public void sendErr(String message, int times){
 		for (int i = times; i > 0; i--){
 			sendErr(message);
 		}
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
 		if (args.length == 0){
 			if (redeemtype.equals(RedeemType.Chat)){
 				sender.sendMessage(ChatColor.DARK_RED+"|--WordRank Commands--|");
 				sender.sendMessage(ChatColor.GOLD+"/wordrank add [word] [group]");
 				sender.sendMessage(ChatColor.GOLD+"/wordrank remove [word]");
 				sender.sendMessage(ChatColor.GOLD+"/wordrank words");
 				sender.sendMessage(ChatColor.YELLOW+"To redeem a group, simply type in-chat the word!");
 				return true;
 			}
 			if (redeemtype.equals(RedeemType.Command)){
 				sender.sendMessage(ChatColor.DARK_RED+"|--WordRank Commands--|");
 				sender.sendMessage(ChatColor.GOLD+"/wordrank add [word] [group]");
 				sender.sendMessage(ChatColor.GOLD+"/wordrank remove [word]");
 				sender.sendMessage(ChatColor.GOLD+"/wordrank redeem [word]");
 				sender.sendMessage(ChatColor.GOLD+"/wordrank words");
 				sender.sendMessage(ChatColor.YELLOW+"To redeem a group, use /wordrank redeem [word]");
 				return true;
 			}
 		}
 		if (args[0].equalsIgnoreCase("redeem")){
 			if (redeemtype.equals(RedeemType.Chat)){
 				sender.sendMessage(ChatColor.RED+"Redeem Type 'Chat' is used, please type words without this command.");
 				sender.sendMessage(ChatColor.RED+"If you wish to use this command, please change 'redeem-type' in the config to 'Command'");
 				return true;
 			}
 			if (!(sender instanceof Player)){
 				sender.sendMessage(ChatColor.DARK_RED+"You must be a player to redeem a word!");
 			}else{
 				Player player = (Player) sender;
 				if (player.hasPermission("WordRank.say") || player.hasPermission("WordRank."+args[1])){
 					if (args.length == 2){
 						Word w = new Word(args[1], "unknown");
 						SQLWord sw = new SQLWord(this, w);
 						ArrayList<String> wordlist = sw.getWords();
 						if (wordlist.contains(args[1])){
 							Config config = new Config(this);
 							PermHandle ph = new PermHandle(this, config.getPerms(), player);
 							String groupname = sw.getWordGroup();
 							
 							/*if (ph.getPlayerGroups().contains(groupname)){
 								player.sendMessage(ChatColor.RED+"You are already in the group '"+groupname+"' that this word assigns.");
 								return true;
 							}else{*/
 								w.setGroup(groupname);
 								ph.setGroup(groupname);
 								
 								player.sendMessage(ChatColor.GREEN+"Congrats! You have been promoted to the group "+ChatColor.YELLOW+w.getGroup()+ChatColor.GREEN+"!");
 								send(player.getName()+" has been promoted to "+w.getGroup()+" by WordRank.");
 								return true;
 							//}
 						}else{
							player.sendMessage(ChatColor.RED+"That word doesn't exist!");
 							return true;
 						}
 					}else{
 						player.sendMessage(ChatColor.RED+"Syntax Error, invalid argument length");
 						player.sendMessage(ChatColor.AQUA+"/wordrank redeem [word]");
 						return true;
 					}
 				}else{
 					player.sendMessage(ChatColor.RED+"You do not have permission to use this command.");
 					return true;
 				}
 			}
 		}
 		if (args[0].equalsIgnoreCase("add")){
 			if (sender.hasPermission("WordRank.add")){
 				if (args.length == 3){
 					Word w = new Word(args[1], args[2]);
 					SQLWord sw = new SQLWord(this, w);
 					sender.sendMessage(sw.addToDB());
 					return true;
 				}else{
 					sender.sendMessage(ChatColor.RED+"Syntax Error, invalid argument length");
 					sender.sendMessage(ChatColor.AQUA+"/wordrank add [word] [groupname]");
 					return true;
 				}
 			}else{
 				sender.sendMessage(ChatColor.DARK_RED+"You do not have permission to use this command.");
 				return true;
 			}
 		}
 		else if (args[0].equalsIgnoreCase("remove")){
 			if (sender.hasPermission("WordRank.remove")){
 				if (args.length == 2){
 					Word w = new Word(args[1], "unknown");
 					SQLWord sw = new SQLWord(this, w);
 					sender.sendMessage(sw.removeFromDB());
 					return true;
 				}else{
 					sender.sendMessage(ChatColor.RED+"Syntax Error, invalid argument length");
 					sender.sendMessage(ChatColor.AQUA+"/wordrank remove [word]");
 					return true;
 				}
 			}else{
 				sender.sendMessage(ChatColor.RED+"You do not have permission to use this command.");
 				return true;
 			}
 		}
 		else if (args[0].equalsIgnoreCase("words")){
 			if (sender.hasPermission("WordRank.wordlist")){
 				if (args.length == 1){
 					ArrayList<String> words = new SQLWord(this, new Word(null, null)).getWords();
 					if (words.isEmpty() || words.isEmpty() || words.equals(null)){
 						sender.sendMessage(ChatColor.RED+"No words found.");
 						return true;
 					}
 					StringBuilder sb = new StringBuilder();
 					sb.append(ChatColor.DARK_PURPLE+"Word List: "+ChatColor.GOLD);
 					for (int i = 0; i < words.size(); i++){
 						sb.append(words.get(i));
 						if (!(Integer.valueOf(i+1).equals(words.size()))){
 							sb.append(", ");
 						}
 					}
 					sender.sendMessage(sb.toString());
 					return true;
 					
 				}else{
 					sender.sendMessage(ChatColor.DARK_RED+"Syntax Error, invalid argument length");
 					sender.sendMessage(ChatColor.AQUA+"/wordrank words");
 					return true;
 				}
 			}else{
 				sender.sendMessage(ChatColor.RED+"You do not have permission to use this command.");
 				return true;
 			}
 		}
 		else if (args[0].equalsIgnoreCase("update")){
 			if (sender.hasPermission("WordRank.update")){
				if (!update){
 					sender.sendMessage(ChatColor.RED+"No update available.");
 					return true;
 				}
 				sender.sendMessage(ChatColor.GOLD+"Updating WordRank.. (this may take a few moments)");
 				try {
 					BufferedInputStream in = new BufferedInputStream(new URL("http://www.craftmod.net/jar/WordRank/latest/WordRank.jar").openStream());
 					FileOutputStream fos = new FileOutputStream(new File("plugins/WordRank.jar"));
 					
 					byte d[] = new byte[1024];
 					int count;
 					while ((count = in.read(d, 0, 1024)) != -1)
 						fos.write(d, 0, count);
 					in.close();
 					fos.close();
 				} catch (MalformedURLException e){
 					sender.sendMessage(ChatColor.DARK_RED+"Error while downloading file. Please check the console for more info.");
 					e.printStackTrace();
 				} catch (IOException e){
 					sender.sendMessage(ChatColor.DARK_RED+"Error while downloading file. Please check the console for more info.");
 					e.printStackTrace();
 				}
 				sender.sendMessage(ChatColor.GREEN+"The new version of WordRank will be ready to use next server reload or restart.");
 				return true;
 			}else{
 				sender.sendMessage(ChatColor.RED+"You do not have permission to use this command.");
 				return true;
 			}
 		}
 		return true;
 	}
 }
