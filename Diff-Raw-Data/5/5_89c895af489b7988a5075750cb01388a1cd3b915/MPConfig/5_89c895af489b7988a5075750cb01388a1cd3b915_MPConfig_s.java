 package me.sinnoh.MasterPromote;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.configuration.file.FileConfiguration;
 
 
 
 public class MPConfig 
 {
 	public static MasterPromote plugin = MasterPromote.instance;
 	
 	
 	public static void createdefaults()
 	{
 		try
 		{
 			if(!plugin.configFile.exists())
 			{
 				plugin.configFile.getParentFile().mkdirs();
 			}
 			if(!plugin.messagesFile.exists())
 			{
 				plugin.messagesFile.getParentFile().mkdirs();
 			}
 			if(!plugin.tokenFile.exists())
 			{
 				plugin.tokenFile.getParentFile().mkdirs();
 			}
			updateconfig();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static void loadYamls() 
 	{
 	    try 
 	    {
 	        plugin.config.load(plugin.configFile);
 	        plugin.messages.load(plugin.messagesFile);
 	        plugin.token.load(plugin.tokenFile);
 	    }catch (Exception e) 
 	    {
 	        e.printStackTrace();
 	    }
 	}
 	public static void saveYamls() 
 	{
 	    try 
 	    {
 	        plugin.config.save(plugin.configFile);
 	        plugin.messages.save(plugin.messagesFile);
 	        plugin.token.save(plugin.tokenFile);
 	    } catch (IOException e)
 	    {
 	        e.printStackTrace();
 	    }
 	}
 	
 	private static void updateconfig()
 	{
 		addDefault(plugin.config, "Apply.Enabled", true);
 		addDefault(plugin.config, "Apply.Password", "test");
 		addDefault(plugin.config, "Apply.Defaultgroup", "default");
 		addDefault(plugin.config, "Apply.Group", "Member");
 		addDefault(plugin.config, "Apply.Freeze", false);
 		addDefault(plugin.config, "Apply.Mute", false);
 		addDefault(plugin.config, "Apply.KickWrongPW", false);
 		addDefault(plugin.config, "Apply.BlockPWinChat", false);
 		addDefault(plugin.config, "Time.Enabled", false);
 		addDefault(plugin.config, "Time.Group", false);
 		addDefault(plugin.config, "Time.Time", 10);
 		addDefault(plugin.config, "Time.CountOffline", false);
 		List<String> ranks = new ArrayList<String>();
 		ranks.add("Admin,5");
 		addDefault(plugin.config, "Ranks", ranks);
 		addDefault(plugin.config, "PromoteSyntax", "none");
 		
 		addDefault(plugin.messages, "NoPermissions", "&cYou do not have permissions to do this!");
 		addDefault(plugin.messages, "CreatedSign", "&a[MasterPromote]Successfull created a promotion sign!");
 		addDefault(plugin.messages, "UsedSign", "&a[MasterPromote]Sucsessfull promoted to <group>!");
 		addDefault(plugin.messages, "UsedPW", "&a[MasterPromote]You have been succsesfully promoted to <group>!");
 		addDefault(plugin.messages, "WrongPW", "&cWrong PW!");
 		addDefault(plugin.messages, "Reload", "&a[MasterPromote] reloaded!");
 		addDefault(plugin.messages, "TokenUse", "&aYou have been succsesfully promoted to <group>!");
 		addDefault(plugin.messages, "CreateToken", "&a[MasterPromote]Created token <token> for <group>!");
 		addDefault(plugin.messages, "Join", "&5<player>, &aplease write /apply [Password] to get Permissions to build!");
 		addDefault(plugin.messages, "Mute", "&cYou are not allowed to chat!");
 		addDefault(plugin.messages, "FunctionDisabled", "&cThis function has been disabled by the server administrator!");
 		addDefault(plugin.messages, "BuyRank", "&5Do you really want to buy <group> for <price>?");
 		addDefault(plugin.messages, "CantBuyRank", "&cYou can not buy this rank!");
 		addDefault(plugin.messages, "NoMoney", "&cYou do not have enought money to buy this rank!");
 		addDefault(plugin.messages, "BoughtRank", "&aBought rank <group>!");
 		addDefault(plugin.messages, "Confirm", "&5Type /mpconfirm to continue");
 		addDefault(plugin.messages, "PromotedAfterTime", "&aYou have been promoted to <group>!");
 		saveYamls();
 	}
 	
 	private static void addDefault(FileConfiguration f, String path, Object v)
 	{
 		if(f.getString(path) == null)
 		{
 			f.set(path, v);
 		}
 	}
 }
